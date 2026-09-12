<template>
  <uni-icons
    class="app-icon"
    :type="iconType"
    :size="size"
    :color="color || 'inherit'"
  />
</template>

<script setup>
import { computed } from 'vue';
import UniIcons from '@dcloudio/uni-ui/lib/uni-icons/uni-icons.vue';

/**
 * 全局统一图标组件
 *
 * 底层使用 uni-app 官方图标组件 uni-icons（@dcloudio/uni-ui），
 * 字体文件随包内置，不依赖任何远程 CDN，小程序端与 H5 端表现一致。
 *
 * 对外保持原有 `name / size / color` 三个属性的调用方式，
 * 语义化名称（如 chevron_right、heart_fill）通过 ICON_MAP 映射到 uni-icons 的 type，
 * 也支持直接传入 uni-icons 原生 type（如 'home-filled'）透传使用。
 */
const props = defineProps({
  /** 图标名称（本组件语义化名称，或 uni-icons 原生 type） */
  name: { type: String, required: true },
  /** 图标尺寸：数字按 px 处理，也可传 '32rpx' 这类带单位字符串 */
  size: { type: [String, Number], default: 24 },
  /** 图标颜色，不传则继承父级文字颜色 */
  color: { type: String, default: '' },
});

/**
 * 语义化名称 → uni-icons type 映射表
 * 左值为项目内沿用的图标名，右值为 uni-icons 官方图标名
 */
const ICON_MAP = {
  /* ---------- 导航 / 通用 ---------- */
  back: 'arrow-left',
  arrow_left: 'arrow-left',
  arrow_right: 'arrow-right',
  chevron_left: 'left',
  chevron_right: 'right',
  chevron_down: 'down',
  chevron_up: 'up',
  home: 'home',
  home_fill: 'home-filled',
  home_outline: 'home',
  shop: 'shop',
  shop_fill: 'shop-filled',
  shop_circle: 'shop-filled',
  user: 'person',
  user_fill: 'person-filled',
  user_circle: 'person-filled',
  search: 'search',
  plus: 'plus-filled',
  plus_empty: 'plus',
  minus: 'minus-filled',
  close: 'closeempty',
  close_circle: 'clear',
  more: 'more-filled',
  refresh: 'refreshempty',
  reload: 'reload',
  undo: 'undo',
  redo: 'redo',
  settings: 'gear-filled',
  tune: 'tune',
  list: 'list',
  bars: 'bars',
  scan: 'scan',
  navigate: 'navigate-filled',
  paperclip: 'paperclip',

  /* ---------- 互动 / 社交 ---------- */
  heart: 'heart',
  heart_fill: 'heart-filled',
  like: 'hand-up',
  unlike: 'hand-down',
  message: 'chatbubble',
  comment: 'chatbubble-filled',
  chat: 'chatboxes',
  chat_fill: 'chatboxes-filled',
  share: 'paperplane',
  send: 'paperplane',
  forward: 'redo',
  star: 'star',
  star_fill: 'star-filled',
  bell: 'notification-filled',
  notification: 'notification',
  contact: 'contact-filled',
  personadd: 'personadd',

  /* ---------- 内容 / 编辑 ---------- */
  file_text: 'list',
  edit: 'compose',
  trash: 'trash',
  camera: 'camera-filled',
  image: 'image',
  images: 'images',
  eye: 'eye',
  eye_off: 'eye-slash',
  locked: 'locked-filled',
  mail: 'email-filled',
  phone: 'phone-filled',
  location: 'location-filled',
  address: 'location-filled',
  map: 'map-filled',
  calendar: 'calendar-filled',
  cake: 'calendar-filled',
  flag: 'flag-filled',
  info: 'info-filled',
  help: 'help-filled',
  headphone: 'headphones',
  logout: 'undo',
  check: 'checkmarkempty',
  checkbox: 'checkbox-filled',
  circle: 'circle',
  circle_fill: 'circle-filled',
  vip: 'vip-filled',
  wallet: 'wallet-filled',
  weixin: 'weixin',
  font: 'font',

  /* ---------- 业务 ---------- */
  gift: 'gift-filled',
  fire: 'fire-filled',
  medal: 'medal-filled',
  order: 'cart-filled',
  cart: 'cart-filled',
  shopping: 'cart-filled',

  /* ---------- 宠物主题（uni-icons 无宠物图标，取语义最接近的填充图标） ---------- */
  pet: 'heart-filled',
  pet_fill: 'heart-filled',
  paw: 'heart-filled',
  paw_fill: 'heart-filled',
  paw_circle: 'heart-filled',
  dog_bone: 'star-filled',
  vaccine: 'medal-filled',
  weight: 'tune',
};

/** 最终传给 uni-icons 的 type：命中映射表则取映射值，否则按原生 type 透传 */
const iconType = computed(() => ICON_MAP[props.name] || props.name);
</script>

<style lang="scss" scoped>
.app-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
  vertical-align: middle;
}
</style>
