import { features } from '@/config/features.js';

/**
 * 商城「演示模式」统一文案
 *
 * 背景：本小程序为个人主体项目，商城仅用于演示积分兑换流程，
 * 兑换不会产生真实交易，也不会发货。为兼顾合规与用户体验，
 * 所有涉及"发货 / 物流"的承诺文案在此集中替换成中性说明，
 * 界面各处提示也统一从这里取，避免同一件事写好几遍、改漏一处。
 *
 * 关闭方式：.env 配 VITE_SHOP_DEMO_MODE=false，或后端下发 shopDemoMode=false。
 */

/** 商城演示模式是否开启 */
export const isShopDemo = () => features.shopDemoMode !== false;

export const SHOP_DEMO = {
  /** 商城首页 / 订单列表顶部的一句话提示 */
  banner: '功能演示：兑换仅记录积分消耗，不产生真实交易，也不会发货',

  /** 商品详情页提示 */
  detail: '功能演示：本商品为演示数据，兑换后不会真实发货，仅用于演示积分流转',

  /** 兑换前的二次确认 */
  exchange: {
    title: '演示模式提示',
    content:
      '本商城是个人作品的功能演示，兑换不会产生真实交易、也不会发货，仅用于演示积分消耗流程。\n是否继续体验？',
    confirmText: '继续体验',
    cancelText: '再看看',
  },

  /** 兑换成功后的弹窗 */
  success: {
    title: '兑换成功（演示）',
    content: '这是一条演示订单，不会真实发货。可在「我的 → 兑换订单」中查看订单记录。',
  },

  /** 兑换须知（演示模式替换原来的发货承诺） */
  tips: [
    '· 本商城为个人作品的功能演示，不涉及真实交易',
    '· 兑换后仅生成演示订单，不会发货、不产生物流信息',
    '· 消耗的积分可在「我的 → 积分明细」中查看',
  ],

  /** 订单状态文案（演示模式：不用"发货"这类承诺词） */
  statusText: {
    '0': '待处理',
    '1': '已处理',
    '2': '已完成',
    '3': '已取消',
  },

  /** 订单状态补充文案（演示模式） */
  statusTip: {
    '0': '演示订单已生成，等待处理',
    '1': '演示订单已处理（不会真实发货）',
    '2': '演示流程已完成，感谢体验',
    '3': '订单已取消',
  },
};

/** 演示模式下的订单状态文案，未开启演示模式时回退到原文案 */
export const shopStatusText = (status, fallback) =>
  isShopDemo() ? SHOP_DEMO.statusText[String(status)] || fallback : fallback;

/** 演示模式下的订单状态补充文案 */
export const shopStatusTip = (status, fallback) =>
  isShopDemo() ? SHOP_DEMO.statusTip[String(status)] || fallback : fallback;
