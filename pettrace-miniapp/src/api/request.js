import { useUserStore } from '@/store/user.js';

// 服务器根地址，用于非 /api/v1 前缀的接口（如文件上传 /system/user/profile/avatar、/common/upload 等）
// 通过根目录 .env 文件配置（VITE_DEV_SERVER_BASE / VITE_PROD_SERVER_BASE + VITE_API_PREFIX）
// 真机调试时把 VITE_DEV_SERVER_BASE 改为电脑的局域网 IP，例如 http://192.168.1.7:8080
// （localhost 在手机上指向手机自己，会导致 ERR_CONNECTION_REFUSED）
const {
  VITE_DEV_SERVER_BASE,
  VITE_PROD_SERVER_BASE,
  VITE_API_PREFIX,
  PROD,
} = import.meta.env;
export const SERVER_BASE = PROD
  ? (VITE_PROD_SERVER_BASE || 'http://192.168.1.7:8080')
  : (VITE_DEV_SERVER_BASE || 'http://192.168.1.7:8080');
// API 接口完整地址 = 服务器根地址 + 接口前缀
export const BASE_URL = `${SERVER_BASE}${VITE_API_PREFIX || '/api/v1'}`;

const LOGIN_PAGE_URL = '/pages/login/index';
let isRedirectingToLogin = false;

const redirectToLogin = () => {
  if (isRedirectingToLogin) return;

  const pages = getCurrentPages();
  const currentRoute = pages[pages.length - 1]?.route;
  if (
    currentRoute === 'pages/login/index' ||
    currentRoute === 'pages/login/register'
  ) {
    return;
  }

  isRedirectingToLogin = true;
  const resetRedirectState = () => {
    setTimeout(() => { isRedirectingToLogin = false; }, 300);
  };

  uni.reLaunch({
    url: LOGIN_PAGE_URL,
    complete: resetRedirectState,
  });
};

// 登录过期统一处理：清除用户信息（含 Pinia 状态与本地存储）并跳转登录页
const handleTokenExpired = () => {
  try {
    const userStore = useUserStore();
    userStore.logout();
  } catch (e) {
    // store 未初始化时兜底，直接清除本地存储
    uni.removeStorageSync('token');
    uni.removeStorageSync('userInfo');
  }
  uni.showToast({ title: '登录已过期，请重新登录', icon: 'none' });
  setTimeout(() => { redirectToLogin(); }, 1500);
};

const request = (options) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token') || '';

    const headers = {
      'Content-Type': 'application/json',
      ...options.header,
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

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
            handleTokenExpired();
            reject(data);
          } else {
            uni.showToast({ title: data.msg || '请求失败', icon: 'none' });
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
            handleTokenExpired();
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

export const get = (url, params = {}) => {
  return request({ url, method: 'GET', data: params });
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
