import { SERVER_BASE } from '@/api/request.js';

/**
 * 格式化时间（微信朋友圈风格）
 * @param {string|number|Date} time
 * @returns {string}
 */
export function formatTime(time) {
  if (!time) return '';
  const date = parseServerTime(time);
  if (!date) return time;

  const now = Date.now();
  const diff = now - date.getTime();
  const minute = 60 * 1000;
  const hour = 60 * minute;
  const day = 24 * hour;

  if (diff < minute) {
    return '刚刚';
  } else if (diff < hour) {
    return Math.floor(diff / minute) + '分钟前';
  } else if (diff < day) {
    return Math.floor(diff / hour) + '小时前';
  } else if (diff < 2 * day) {
    return '昨天 ' + pad(date.getHours()) + ':' + pad(date.getMinutes());
  } else if (diff < 3 * day) {
    return '前天 ' + pad(date.getHours()) + ':' + pad(date.getMinutes());
  } else {
    const currentYear = new Date().getFullYear();
    const y = date.getFullYear();
    const m = pad(date.getMonth() + 1);
    const d = pad(date.getDate());
    const h = pad(date.getHours());
    const min = pad(date.getMinutes());
    if (y === currentYear) {
      return m + '月' + d + '日 ' + h + ':' + min;
    }
    return y + '年' + m + '月' + d + '日';
  }
}

/**
 * 格式化完整日期时间（YYYY-MM-DD HH:mm）
 * @param {string|number|Date} time
 * @returns {string}
 */
export function formatDateTime(time) {
  if (!time) return '';
  const date = parseServerTime(time);
  if (!date) return time;
  const y = date.getFullYear();
  const m = pad(date.getMonth() + 1);
  const d = pad(date.getDate());
  const h = pad(date.getHours());
  const min = pad(date.getMinutes());
  return `${y}-${m}-${d} ${h}:${min}`;
}

function pad(n) {
  return String(n).padStart(2, '0');
}

/**
 * 解析后端时间字符串。
 * 后端统一返回东八区时间（yyyy-MM-dd HH:mm:ss），这里显式带上 +08:00，
 * 避免不同小程序运行环境把无时区字符串解析成 UTC/本地时间导致显示偏差。
 */
function parseServerTime(time) {
  let date = null;
  if (typeof time === 'string') {
    const m = time.match(/^(\d{4})-(\d{2})-(\d{2})[ T](\d{2}):(\d{2})(?::(\d{2}))?$/);
    if (m) {
      const sec = m[6] || '00';
      date = new Date(`${m[1]}-${m[2]}-${m[3]}T${m[4]}:${m[5]}:${sec}+08:00`);
    } else {
      date = new Date(time);
    }
  } else {
    date = new Date(time);
  }
  if (!date || isNaN(date.getTime())) {
    return null;
  }
  return date;
}

/**
 * 补全图片完整URL（Ruoyi 返回的相对路径 → 绝对路径）
 * @param {string} url 可能是 /profile/upload/xxx.jpg 或 /upload/xxx.jpg 或 /avatar/xxx.jpg
 * @returns {string} 完整URL
 */
export function fullImageUrl(url) {
  if (!url) return '';
  const base = SERVER_BASE.endsWith('/') ? SERVER_BASE.slice(0, -1) : SERVER_BASE;

  // 如果是完整 URL，提取 /profile 路径部分，用当前 SERVER_BASE 重新拼接
  // 这样可以修复数据库中存储的 localhost / 旧 IP 的图片 URL
  if (url.startsWith('http://') || url.startsWith('https://')) {
    const profileIdx = url.indexOf('/profile');
    if (profileIdx !== -1) {
      return base + url.substring(profileIdx);
    }
    // 非本服务的外部图片，直接返回
    return url;
  }

  // 相对路径处理：确保以 / 开头
  let path = url.startsWith('/') ? url : '/' + url;
  // RuoYi 的资源映射只处理 /profile/**
  if (!path.startsWith('/profile')) {
    path = '/profile' + path;
  }
  return base + path;
}

/**
 * 防抖函数
 * @param {Function} fn
 * @param {number} delay
 * @returns {Function}
 */
export function debounce(fn, delay = 500) {
  let timer = null;
  return function (...args) {
    if (timer) clearTimeout(timer);
    timer = setTimeout(() => {
      fn.apply(this, args);
    }, delay);
  };
}

/**
 * 节流函数
 * @param {Function} fn
 * @param {number} interval
 * @returns {Function}
 */
export function throttle(fn, interval = 500) {
  let lastTime = 0;
  return function (...args) {
    const now = Date.now();
    if (now - lastTime >= interval) {
      lastTime = now;
      fn.apply(this, args);
    }
  };
}

/**
 * 检查是否登录（同步）
 *
 * @deprecated 建议优先使用 `@/utils/auth.js` 的 `requireLogin()`：
 * 它会在未登录时以弹窗征询用户意愿，由用户自行决定是否登录，
 * 而不会把用户强制推向登录页（微信审核明确要求浏览不受阻）。
 *
 * @returns {boolean}
 */
export function checkLogin() {
  const token = uni.getStorageSync('token');
  if (!token) {
    showToast('登录后可继续操作');
    return false;
  }
  return true;
}

/**
 * 显示提示信息
 * @param {string} title
 * @param {string} icon
 */
export function showToast(title, icon = 'none') {
  uni.showToast({
    title,
    icon,
    duration: 2000,
  });
}

/**
 * 显示加载中
 * @param {string} title
 */
export function showLoading(title = '加载中...') {
  uni.showLoading({
    title,
    mask: true,
  });
}

/**
 * 隐藏加载中
 */
export function hideLoading() {
  uni.hideLoading();
}

/**
 * 确认对话框
 * @param {string} content
 * @returns {Promise<boolean>}
 */
export function showConfirm(content) {
  return new Promise((resolve) => {
    uni.showModal({
      title: '提示',
      content,
      success: (res) => {
        resolve(res.confirm);
      },
    });
  });
}
