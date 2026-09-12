<template>
  <!--
    统一标题栏（tabBar 页面通用：商城 / 爱宠 / 我的）
    - 高度按「状态栏 + 胶囊上下留白 + 胶囊高度」计算，标题与微信胶囊水平居中对齐
    - 默认透明，背景由页面自带的渐变头部提供；需要独立底色时传 solid
    - 右侧操作（如搜索）通过 #right 插槽传入，标题始终居中
  -->
  <view class="nav-bar" :class="{ 'is-fixed': fixed, 'is-solid': solid }" :style="barStyle">
    <view class="nav-bar-inner" :style="{ height: capsuleHeight + 'px' }">
      <view class="nav-side">
        <slot name="left" />
      </view>

      <text class="nav-title">{{ title }}</text>

      <view class="nav-side nav-side-right">
        <slot name="right" />
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue';
import { getNavBarMetrics } from '@/utils/navbar.js';

const props = defineProps({
  /** 标题文案 */
  title: { type: String, default: '' },
  /** 是否吸顶固定（内容会穿过标题栏时开启） */
  fixed: { type: Boolean, default: false },
  /** 是否显示品牌渐变底（页面未自带渐变头部，或滚动后需要独立底色时开启） */
  solid: { type: Boolean, default: false },
});

const { statusBarHeight, gap, capsuleHeight } = getNavBarMetrics();

/** 上下留白对称，保证标题视觉居中 */
const barStyle = computed(() => ({
  paddingTop: `${statusBarHeight + gap}px`,
  paddingBottom: `${gap}px`,
}));
</script>

<style lang="scss" scoped>
.nav-bar {
  position: relative;
  z-index: 999;
  width: 100%;
  background: transparent;

  /* 吸顶：脱离文档流，由使用方用 navBarHeight 预留占位高度 */
  &.is-fixed {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
  }

  /* 独立渐变底：仅在需要时显示，避免与页面头部渐变叠加出分割线 */
  &.is-solid {
    background: $gradient-primary;
    box-shadow: 0 4rpx 16rpx rgba(216, 75, 16, 0.12);
  }

  .nav-bar-inner {
    position: relative;
    display: flex;
    align-items: center;
    padding: 0 24rpx;
  }

  .nav-side {
    position: relative;
    z-index: 1;
    display: flex;
    align-items: center;
    min-width: 0;

    &.nav-side-right {
      margin-left: auto;
    }
  }

  .nav-title {
    position: absolute;
    left: 0;
    right: 0;
    text-align: center;
    font-size: $font-lg;
    font-weight: $font-weight-bold;
    color: $text-white;
    letter-spacing: 2rpx;
    text-shadow: 0 2rpx 8rpx rgba(185, 61, 9, 0.18);
    pointer-events: none;
  }
}
</style>
