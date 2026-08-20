<template>
  <text
    class="iconfont"
    :style="iconStyle"
  >{{ glyph }}</text>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  name: { type: String, required: true },
  size: { type: [String, Number], default: 24 },
  color: { type: String, default: '' },
});

// 阿里巴巴 iconfont 常用图标映射（使用 Unicode 码点）
// 当 iconfont 字体未加载时，退化为 emoji 显示
const ICON_MAP = {
  home: { code: '\ue8c6', fallback: '🏠' },
  home_fill: { code: '\ue8c4', fallback: '🏡' },
  shop: { code: '\ue8c9', fallback: '🛍' },
  shop_fill: { code: '\ue8c5', fallback: '🏪' },
  pet: { code: '\ue8ca', fallback: '🐾' },
  pet_fill: { code: '\ue8cb', fallback: '�' },
  user: { code: '\ue8c2', fallback: '👤' },
  user_fill: { code: '\ue8c3', fallback: '🧑' },
  search: { code: '\ue8cc', fallback: '🔍' },
  heart: { code: '\ue8d4', fallback: '🤍' },
  heart_fill: { code: '\ue8d3', fallback: '❤️' },
  like: { code: '\ue8d5', fallback: '👍' },
  message: { code: '\ue8d8', fallback: '💬' },
  chat: { code: '\ue8d8', fallback: '💬' },
  comment: { code: '\ue8d9', fallback: '💭' },
  plus: { code: '\ue8d2', fallback: '➕' },
  camera: { code: '\ue8d0', fallback: '�' },
  edit: { code: '\ue8e0', fallback: '✏️' },
  edit_pen: { code: '\ue8e0', fallback: '✏️' },
  trash: { code: '\ue8df', fallback: '🗑' },
  arrow_right: { code: '\ue8de', fallback: '➡️' },
  close: { code: '\ue8d7', fallback: '✕' },
  bell: { code: '\ue8d6', fallback: '🔔' },
  star: { code: '\ue8d1', fallback: '⭐' },
  star_fill: { code: '\ue8d0', fallback: '🌟' },
  file_text: { code: '\ue8e2', fallback: '📄' },
  shopping: { code: '\ue8e3', fallback: '🛒' },
  location: { code: '\ue8e1', fallback: '📍' },
  settings: { code: '\ue8e4', fallback: '⚙️' },
  info: { code: '\ue8e5', fallback: 'ℹ️' },
  headphone: { code: '\ue8e6', fallback: '🎧' },
  weixin: { code: '\ue8e7', fallback: '�' },
  share: { code: '\ue8e8', fallback: '📤' },
  eye: { code: '\ue8e9', fallback: '�' },
  mail: { code: '\ue8ea', fallback: '✉️' },
  phone: { code: '\ue8eb', fallback: '📱' },
  chevron_right: { code: '\ue8de', fallback: '›' },
  chevron_left: { code: '\ue8f9', fallback: '‹' },
  chevron_down: { code: '\ue8ed', fallback: '‹' },
  refresh: { code: '\ue8ee', fallback: '🔄' },
  gift: { code: '\ue8ef', fallback: '🎁' },
  medal: { code: '\ue8f0', fallback: '�' },
  fire: { code: '\ue8f1', fallback: '🔥' },
  paw: { code: '\ue8ca', fallback: '🐾' },
  paw_fill: { code: '\ue8cb', fallback: '🐶' },
  more: { code: '\ue8f2', fallback: '⋯' },
  logout: { code: '\ue8f3', fallback: '🚪' },
  order: { code: '\ue8f4', fallback: '📋' },
  address: { code: '\ue8e1', fallback: '📍' },
  dog_bone: { code: '\ue8f5', fallback: '🦴' },
  vaccine: { code: '\ue8f6', fallback: '💉' },
  weight: { code: '\ue8f7', fallback: '⚖️' },
  cake: { code: '\ue8f8', fallback: '🎂' },
  back: { code: '\ue8f9', fallback: '⬅️' },
  home_circle: { code: '\ue8fa', fallback: '🏠' },
  home_outline: { code: '\ue8fb', fallback: '�' },
  like_circle: { code: '\ue8fc', fallback: '�' },
  user_circle: { code: '\ue8fd', fallback: '�' },
  paw_circle: { code: '\ue8fe', fallback: '�' },
  shop_circle: { code: '\ue8ff', fallback: '🛍' },
};

// 检测 iconfont 是否已加载
const isIconFontLoaded = () => {
  if (typeof document === 'undefined') return false;
  const testEl = document.createElement('span');
  testEl.className = 'iconfont';
  testEl.style.visibility = 'hidden';
  testEl.style.position = 'absolute';
  testEl.textContent = ICON_MAP.home.code;
  document.body.appendChild(testEl);
  const loaded = testEl.offsetWidth > 0 && testEl.offsetWidth < 50;
  document.body.removeChild(testEl);
  return loaded;
};

const glyph = computed(() => {
  const icon = ICON_MAP[props.name];
  if (!icon) return '⭐';
  // 如果 iconfont 已加载则返回 Unicode，否则降级为 emoji
  if (typeof document !== 'undefined' && isIconFontLoaded()) {
    return icon.code;
  }
  return icon.fallback;
});

const iconStyle = computed(() => ({
  fontSize: typeof props.size === 'number' ? `${props.size}px` : props.size,
  color: props.color || 'inherit',
  lineHeight: 1,
  display: 'inline-flex',
  alignItems: 'center',
  justifyContent: 'center',
}));
</script>

<style lang="scss" scoped>
.iconfont {
  font-family: "iconfont", "Apple Color Emoji", "Segoe UI Emoji", "Noto Color Emoji", sans-serif;
  font-style: normal;
  line-height: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
</style>
