/**
 * 登录过期统一恢复（会话续命）
 *
 * 背景：本项目的后端支持「按 openid 复用同一账号重新签发 token」
 * （见 WxUserServiceImpl.wxLogin：selectUserByOpenId 命中老用户即复用），
 * 且 TokenService.verifyToken 在剩余有效期不足 20 分钟时会自动续期，
 * 因此 token 过期只可能发生在「离线超过 30 分钟没有发出过任何请求」的场景，
 * 而这种场景几乎都可以用微信静默登录把登录态无感接回来。
 *
 * 处理策略（三层递进）：
 * 1. 静默重登：uni.login 取 code → /wx/login 换新 token，成功后由调用方重放原请求，用户完全无感；
 * 2. 静默失败才提示：弹窗征询用户意愿，请求暂时挂起等待；
 *    - 「重新登录」→ navigateTo 登录页（不清空页面栈），登录成功后回跳原页面并继续原操作；
 *    - 「稍后再说」→ 释放挂起的请求，降级为游客态继续浏览（微信审核要求不强迫登录）；
 * 3. 兜底：页面栈已满导致 navigateTo 失败时退化为 redirectTo；
 *    用户长时间不处理时定时释放挂起请求，避免请求永久悬挂。
 *
 * 注意：游客（本地从未存过 token）触发的 401 不进入本流程，由调用方静默处理。
 */
import { BASE_URL } from '@/config/server.js';
import { LOGIN_PAGE } from '@/utils/auth.js';

/** 静默重登连续失败次数上限，达到后不再徒劳重试，直接转人工登录 */
const MAX_SILENT_FAILURES = 2;

/** 用户选择「稍后再说」后的静默期，期内不再重试静默登录、不再弹窗 */
const MODAL_SUPPRESS_MS = 60 * 1000;

/** 挂起请求的最长等待时间（用户迟迟不完成登录时的安全网） */
const PENDING_TIMEOUT_MS = 120 * 1000;

/** 外部钩子，由 api/request.js 注入，用于同步 Pinia 登录态 */
let hooks = { onRecovered: null, onExpired: null };

/** 由 api/request.js 注入登录态同步回调 */
export const setSessionHooks = (next = {}) => {
  hooks = { ...hooks, ...next };
};

/** 单飞：同一时刻只允许存在一次会话恢复流程，并发 401 共享同一结果 */
let recovering = null;

/** 静默重登连续失败计数 */
let silentFailures = 0;

/** 静默期截止时间戳 */
let suppressUntil = 0;

/** 因等待用户登录而挂起的请求 */
const pendingRequests = [];

/** 挂起请求的超时计时器 */
let pendingTimer = null;

/** 弹窗是否已展示，避免并发 401 重复弹窗 */
let modalShowing = false;

const readUserId = () => {
  const val = uni.getStorageSync('userId');
  return val ? String(val) : '';
};

/**
 * 直接发起登录请求（不经过统一请求封装，避免 401 时递归进入会话恢复）
 * @param {string} code wx.login 返回的临时凭证
 * @returns {Promise<Object>} 登录结果（含 token、userId 等）
 */
const requestWxLogin = (code) =>
  new Promise((resolve, reject) => {
    uni.request({
      url: `${BASE_URL}/wx/login`,
      method: 'POST',
      data: { code },
      header: { 'Content-Type': 'application/json' },
      timeout: 15000,
      success: (res) => {
        const data = res.data || {};
        if (
          res.statusCode >= 200 &&
          res.statusCode < 300 &&
          (data.code === 200 || data.code === 0)
        ) {
          resolve(data.data || data);
        } else {
          reject({ code: data.code, msg: data.msg || `HTTP ${res.statusCode}` });
        }
      },
      fail: (err) => reject({ code: -1, msg: err.errMsg || '网络请求失败' }),
    });
  });

/** 获取微信登录临时凭证 */
const getWxCode = () =>
  new Promise((resolve, reject) => {
    uni.login({
      provider: 'weixin',
      success: (res) =>
        res.code ? resolve(res.code) : reject({ code: -1, msg: '未获取到微信登录凭证' }),
      fail: (err) => reject({ code: -1, msg: err.errMsg || '微信登录失败' }),
    });
  });

/**
 * 静默重登：用微信凭证换取新 token
 *
 * 账号一致性校验非常关键：后端在 openid 查不到用户时会直接创建新账号并发放注册积分，
 * 因此若本地已有 userId 且与新账号不一致，必须中止，否则会把用户切成另一个空账号。
 */
async function silentRelogin() {
  const code = await getWxCode();
  const data = await requestWxLogin(code);
  const token = data && data.token;
  if (!token) {
    const err = new Error('服务端未返回登录凭证');
    err.code = 'NO_TOKEN';
    throw err;
  }

  const cachedUserId = readUserId();
  const incomingUserId = data.userId != null ? String(data.userId) : '';
  if (cachedUserId && incomingUserId && cachedUserId !== incomingUserId) {
    const err = new Error('静默重登命中不同账号');
    err.code = 'ACCOUNT_MISMATCH';
    throw err;
  }

  return { data, token, userId: incomingUserId };
}

/** 跳转登录页（不使用 reLaunch，保留页面栈，登录后可原路返回） */
function gotoLoginPage() {
  const pages = getCurrentPages();
  const route = pages[pages.length - 1]?.route;
  if (route === 'pages/login/index' || route === 'pages/login/register') return;

  uni.navigateTo({
    url: LOGIN_PAGE,
    fail: () => {
      // 页面栈已满（最多 10 层）等场景兜底，避免点击后毫无反应
      uni.redirectTo({ url: LOGIN_PAGE });
    },
  });
}

/** 启动挂起请求的超时释放 */
function startPendingTimeout() {
  clearPendingTimeout();
  pendingTimer = setTimeout(() => {
    releasePendingRequests(false);
  }, PENDING_TIMEOUT_MS);
}

function clearPendingTimeout() {
  if (pendingTimer) {
    clearTimeout(pendingTimer);
    pendingTimer = null;
  }
}

/**
 * 释放所有挂起的请求
 * @param {boolean} success 会话是否已恢复
 */
export function releasePendingRequests(success) {
  clearPendingTimeout();
  modalShowing = false;
  const list = pendingRequests.splice(0);
  list.forEach((holder) => holder.resolve(success));
  if (success) {
    // 登录成功，重置熔断计数与静默期
    silentFailures = 0;
    suppressUntil = 0;
  }
}

/** 当前是否存在等待登录后重放的请求（登录页据此决定登录后回跳还是回首页） */
export const hasPendingRequests = () => pendingRequests.length > 0;

/** 弹窗征询用户意愿，并挂起等待结果 */
function askUserToLogin() {
  if (Date.now() < suppressUntil) return Promise.resolve(false);

  let holder = null;
  const wait = new Promise((resolve) => {
    holder = { resolve };
  });
  pendingRequests.push(holder);

  if (!modalShowing) {
    modalShowing = true;
    uni.showModal({
      title: '登录状态已过期',
      content: '为了账号安全需要重新登录，登录后可以继续刚才的操作。',
      confirmText: '重新登录',
      cancelText: '稍后再说',
      confirmColor: '#FF8C42',
      cancelColor: '#8A8A8A',
      success: (res) => {
        modalShowing = false;
        if (res.confirm) {
          gotoLoginPage();
          startPendingTimeout();
        } else {
          suppressUntil = Date.now() + MODAL_SUPPRESS_MS;
          releasePendingRequests(false);
        }
      },
      fail: () => {
        modalShowing = false;
        releasePendingRequests(false);
      },
    });
  }

  return wait;
}

/**
 * 尝试恢复登录态
 *
 * @returns {Promise<boolean>} true 表示会话已恢复，调用方可重放原请求；
 *                             false 表示未恢复（用户选择稍后再说或恢复失败），调用方按游客态处理
 */
export function recoverSession() {
  if (recovering) return recovering;
  if (Date.now() < suppressUntil) return Promise.resolve(false);

  recovering = (async () => {
    if (silentFailures < MAX_SILENT_FAILURES) {
      try {
        const { token, userId, data } = await silentRelogin();
        silentFailures = 0;
        if (hooks.onRecovered) hooks.onRecovered({ token, userId, data });
        return true;
      } catch (err) {
        silentFailures += 1;
        if (err && err.code === 'ACCOUNT_MISMATCH') {
          console.warn('[session] 静默重登命中不同账号，已中止并转为人工登录');
        } else {
          console.warn('[session] 静默重登失败：', err && (err.msg || err.message));
        }
        // 清理本地失效登录态，避免页面仍以为处于登录状态
        if (hooks.onExpired) hooks.onExpired();
      }
    } else if (hooks.onExpired) {
      hooks.onExpired();
    }

    return askUserToLogin();
  })().finally(() => {
    recovering = null;
  });

  return recovering;
}

export default {
  setSessionHooks,
  recoverSession,
  releasePendingRequests,
  hasPendingRequests,
};
