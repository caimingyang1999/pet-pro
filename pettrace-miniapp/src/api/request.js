import { useUserStore } from '@/store/user.js';
import { SERVER_BASE, BASE_URL } from '@/config/server.js';
import { recoverSession, setSessionHooks } from '@/utils/session.js';

// 地址常量统一在 config/server.js 计算，这里原样转出，保持既有引用路径可用
export { SERVER_BASE, BASE_URL };

// 会话恢复的登录态同步：由 utils/session.js 在静默重登成功/失败时回调，
// 这里负责把结果同步到 Pinia 与本地存储（store 必须在调用时再取，不能提到模块顶层）
setSessionHooks({
  onRecovered: ({ token, userId }) => {
    try {
      const userStore = useUserStore();
      userStore.setToken(token);
      if (userId) userStore.setUserId(userId);
      // 登录态恢复后刷新用户资料（积分等可能已变化），失败不影响主流程
      userStore.fetchUserInfo().catch(() => {});
    } catch (e) {
      // store 未初始化时兜底，直接写本地存储
      uni.setStorageSync('token', token);
    }
  },
  onExpired: () => {
    try {
      const userStore = useUserStore();
      userStore.logout();
    } catch (e) {
      // store 未初始化时兜底，直接清除本地存储
      uni.removeStorageSync('token');
      uni.removeStorageSync('userInfo');
    }
  },
});

const request = (options) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token') || '';
    // 本次请求发起时是否处于登录态，用于区分"登录过期"与"游客访问受限接口"
    const wasLoggedIn = !!token;

    const headers = {
      'Content-Type': 'application/json',
      ...options.header,
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    /**
     * 401 的两种语义分流处理
     *
     * - 游客态（本次请求发起时就无 token）：静默失败，由调用方按需提示，
     *   不做任何跳转，避免"一打开小程序就被踢到登录页"，违反微信审核对浏览体验的要求；
     * - 已登录态（token 失效）：先尝试静默恢复登录态，成功后自动重放本次请求，
     *   用户全程无感；恢复不了才由 session 层弹窗引导，且不强制清空页面栈。
     */
    const handleUnauthorized = (payload) => {
      if (!wasLoggedIn) {
        reject(payload);
        return;
      }

      recoverSession().then((restored) => {
        // 每个请求最多重放一次，避免新 token 同样失效时陷入死循环
        if (restored && !options._retried) {
          request({ ...options, _retried: true }).then(resolve).catch(reject);
        } else {
          reject(payload);
        }
      });
    };

    uni.request({
      url: `${BASE_URL}${options.url}`,
      method: options.method || 'GET',
      data: options.data || {},
      header: headers,
      timeout: 30000,
      success: (res) => {
        console.log(`[request] ${options.method || 'GET'} ${options.url} → statusCode:`, res.statusCode);
        if (res.statusCode >= 200 && res.statusCode < 300) {
          const data = res.data;
          if (data.code === 200 || data.code === 0) {
            resolve(data);
          } else if (data.code === 401) {
            handleUnauthorized(data);
          } else {
            if (!options.silent) {
              uni.showToast({ title: data.msg || '请求失败', icon: 'none' });
            }
            reject(data);
          }
        } else {
          // 非 2xx HTTP 状态码，构造安全的错误对象（避免循环引用）
          const errData = (res.data && typeof res.data === 'object') ? res.data : { msg: String(res.data || '') };
          const err = {
            statusCode: res.statusCode,
            code: errData.code,
            msg: errData.msg || `HTTP ${res.statusCode}`,
          };
          if (res.statusCode === 401) {
            handleUnauthorized(err);
          } else if (!options.silent) {
            uni.showToast({ title: err.msg, icon: 'none' });
          }
          reject(err);
        }
      },
      fail: (err) => {
        console.error(`[request] ${options.method || 'GET'} ${options.url} → 网络错误:`, err.errMsg);
        if (!options.silent) {
          uni.showToast({ title: '网络请求失败，请检查网络', icon: 'none' });
        }
        reject({ code: -1, msg: err.errMsg || '网络请求失败' });
      },
    });
  });
};

export const get = (url, params = {}, options = {}) => {
  return request({ url, method: 'GET', data: params, ...options });
};

export const post = (url, data = {}, options = {}) => {
  return request({ url, method: 'POST', data, ...options });
};

export const put = (url, data = {}) => {
  return request({ url, method: 'PUT', data });
};

export const del = (url, data = {}) => {
  return request({ url, method: 'DELETE', data });
};

export default request;

/**
 * 从 RuoYi 上传接口响应中提取图片相对路径
 * 兼容字段（按优先级）：fileName > url > imgUrl > data.fileName / data.url / data.imgUrl
 * 并做安全兜底：
 *   - 若后端返回了"服务器磁盘绝对路径"（/home/.../uploadPath/... 或 D:\\...），自动归一化为 /profile/...
 *   - 前后空格清除
 *   - blob:/data: 等前端临时 URL 原样返回
 *   - http(s) 绝对 URL 原样返回（前端使用 fullImageUrl 时会重新拼域名）
 */
export function pickUploadedPath(data) {
  if (!data) return '';
  const candidates = [
    data.fileName,
    data.url,
    data.imgUrl,
    data.data?.fileName,
    data.data?.url,
    data.data?.imgUrl,
  ];
  let raw = '';
  for (const v of candidates) {
    if (typeof v === 'string' && v.trim()) {
      raw = v.trim();
      break;
    }
  }
  if (!raw) return '';
  if (raw.startsWith('data:') || raw.startsWith('blob:')) return raw;
  if (raw.startsWith('http://') || raw.startsWith('https://')) return raw;
  if (raw.startsWith('/profile')) return raw;

  // 兜底：后端返回磁盘绝对路径（例如 /home/ruoyi/uploadPath/upload/2026/...
  // 或 Windows D:\ruoyi\uploadPath\upload\2026\...）时，
  // 找到 upload / avatar / import / download 子目录那段，归一化为 /profile/xxx
  const norm = raw.replace(/\\/g, '/');
  const segs = ['/upload/', '/avatar/', '/import/', '/download/'];
  for (const seg of segs) {
    const idx = norm.indexOf(seg);
    if (idx !== -1) {
      return '/profile' + norm.slice(idx);
    }
  }
  // 最后再兜底：如果没有分段但却是 /home/... 这种本地路径，无法解析则返回空（让前端提示失败）
  if (norm.startsWith('/') || /^[A-Za-z]:/.test(norm)) {
    console.warn('[upload] 返回了无法解析的本地路径:', raw);
    return '';
  }
  // 否则视为相对路径，确保以 / 开头
  return norm.startsWith('/') ? norm : '/' + norm;
}
