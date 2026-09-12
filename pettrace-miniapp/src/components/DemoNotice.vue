<template>
  <!--
    演示模式提示条
    theme:
      primary —— 用在品牌橙渐变底上（商城首页顶部），半透明白底 + 白字
      plain   —— 用在浅色页面卡片上，浅橙底 + 橙字
  -->
  <view v-if="text" class="demo-notice" :class="theme">
    <Icon name="info" :size="14" :color="theme === 'primary' ? '#fff' : '#C97A1E'" />
    <text class="notice-text">{{ text }}</text>
  </view>
</template>

<script setup>
import Icon from '@/components/Icon.vue';

defineProps({
  /** 提示文案，为空则不展示 */
  text: { type: String, default: '' },
  /** 配色主题：primary（深底）/ plain（浅底） */
  theme: { type: String, default: 'plain' },
});
</script>

<style lang="scss" scoped>
.demo-notice {
  display: flex;
  align-items: flex-start;
  gap: 10rpx;
  padding: 16rpx 22rpx;
  border-radius: $radius-md;

  .notice-text {
    flex: 1;
    font-size: $font-xs;
    line-height: 1.6;
  }

  /* 品牌橙渐变底：半透明白底 + 白字 */
  &.primary {
    background: rgba(255, 255, 255, 0.22);
    border: 1rpx solid rgba(255, 255, 255, 0.28);

    .notice-text {
      color: #fff;
    }
  }

  /* 浅色页面：浅橙底 + 橙字 */
  &.plain {
    background: #FFF7E6;
    border: 1rpx solid rgba(232, 163, 61, 0.28);

    .notice-text {
      color: #B76A22;
      font-weight: $font-weight-medium;
    }
  }
}
</style>
