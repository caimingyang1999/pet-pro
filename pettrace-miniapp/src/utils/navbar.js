/**
 * 导航栏尺寸工具
 *
 * 三端（商城 / 爱宠 / 我的）标题栏统一使用本文件计算出的尺寸，
 * 保证标题与微信右上角胶囊按钮水平居中对齐，避免各页面各算各的导致高低不一。
 *
 * 布局约定：
 *   导航总高 = 状态栏 + 胶囊上间距 + 胶囊高度 + 胶囊下间距
 *   其中「胶囊上间距 = 胶囊下间距 = capsule.top - statusBarHeight」，保证标题居中
 */

let cached = null;

/**
 * 获取导航栏尺寸（同一机型只计算一次并缓存）
 * @returns {{ statusBarHeight: number, gap: number, capsuleHeight: number, navBarHeight: number }}
 */
export function getNavBarMetrics() {
  if (cached) return cached;

  let statusBarHeight = 20;
  let capsuleHeight = 32;
  let gap = 4;

  // #ifdef MP-WEIXIN
  try {
    const sysInfo = uni.getSystemInfoSync();
    statusBarHeight = sysInfo.statusBarHeight || 20;
    const rect = uni.getMenuButtonBoundingClientRect();
    if (rect && rect.height) {
      capsuleHeight = rect.height;
      // 胶囊顶部与状态栏底部的距离，作为上下对称留白
      gap = Math.max(rect.top - statusBarHeight, 4);
    }
  } catch (e) {
    // 取不到时沿用默认值
  }
  // #endif

  cached = {
    statusBarHeight,
    gap,
    capsuleHeight,
    navBarHeight: statusBarHeight + gap * 2 + capsuleHeight,
  };
  return cached;
}

/** 仅取导航总高（页面需要给吸顶栏留占位时使用） */
export const getNavBarHeight = () => getNavBarMetrics().navBarHeight;

export default { getNavBarMetrics, getNavBarHeight };
