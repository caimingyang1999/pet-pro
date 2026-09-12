/**
 * 登录态与登录引导统一入口
 *
 * 设计原则（微信审核要求）：
 * 1. 未登录用户（游客）可以自由浏览首页养宠知识、商城商品、文章详情等公开内容，不被强制跳转登录页；
 * 2. 仅在用户主动触发需要身份的功能（宠物档案、健康记录、智能问答、兑换等）时，
 *    才以弹窗方式征询用户意愿，由用户自行选择是否登录；
 * 3. 绝不使用 reLaunch 强制清空页面栈把用户赶到登录页。
 */

/** 登录页路径（非 tabBar 页面，可 navigateTo） */
export const LOGIN_PAGE = '/pages/login/index';

/** 用户协议页路径 */
export const USER_AGREEMENT_PAGE = '/pages/common/agreement?type=user';

/** 隐私政策页路径 */
export const PRIVACY_PAGE = '/pages/common/agreement?type=privacy';

/** 当前是否已登录（本地存在有效 token） */
export const isLoggedIn = () => !!uni.getStorageSync('token');

/** 跳转到登录页（用户主动触发） */
export const goLogin = () => {
  const pages = getCurrentPages();
  const currentRoute = pages[pages.length - 1]?.route;
  if (currentRoute === 'pages/login/index') return;
  uni.navigateTo({ url: LOGIN_PAGE });
};

/** 查看《用户协议》 */
export const goUserAgreement = () => {
  uni.navigateTo({ url: USER_AGREEMENT_PAGE });
};

/** 查看《隐私政策》 */
export const goPrivacyPolicy = () => {
  uni.navigateTo({ url: PRIVACY_PAGE });
};

/**
 * 需要登录才能执行的操作统一入口
 *
 * 已登录 → 直接 resolve(true)，调用方继续执行业务逻辑；
 * 未登录 → 弹窗征询用户意愿（"去登录" / "再逛逛"），并 resolve(false)。
 *
 * @param {string} actionText 操作描述，用于文案，如"记录体重""添加爱宠"
 * @param {Object} [options] 可选配置
 * @param {string} [options.title] 弹窗标题
 * @param {string} [options.content] 弹窗正文（优先于 actionText）
 * @returns {Promise<boolean>} 是否已登录、可继续执行
 */
export function requireLogin(actionText = '使用该功能', options = {}) {
  if (isLoggedIn()) return Promise.resolve(true);

  return new Promise((resolve) => {
    uni.showModal({
      title: options.title || '需要登录',
      content: options.content || `登录后即可${actionText}，是否现在登录？`,
      confirmText: '去登录',
      cancelText: '再逛逛',
      confirmColor: '#FF8C42',
      cancelColor: '#8A8A8A',
      success: (res) => {
        if (res.confirm) goLogin();
        resolve(false);
      },
      fail: () => resolve(false),
    });
  });
}

export default {
  LOGIN_PAGE,
  USER_AGREEMENT_PAGE,
  PRIVACY_PAGE,
  isLoggedIn,
  goLogin,
  goUserAgreement,
  goPrivacyPolicy,
  requireLogin,
};
