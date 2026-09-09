<template>
  <view class="empty-state">
    <view class="empty-art">
      <text class="empty-emoji">{{ emoji }}</text>
    </view>
    <text class="empty-title">{{ text }}</text>
    <text class="empty-sub" v-if="subText">{{ subText }}</text>
    <view
      v-if="showButton"
      class="empty-btn pet-press"
      @click="handleClick"
    >
      <text class="empty-btn-text">{{ buttonText }}</text>
    </view>
    <view class="empty-slot" v-if="$slots.default || $slots.action">
      <slot name="action" />
      <slot />
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue';

/** 不同 empty mode 对应的可爱插画（emoji 占位） */
const MODE_EMOJI = {
  data: '🐾',
  order: '📦',
  address: '📍',
  coupon: '🎟️',
  message: '💬',
  search: '🔍',
  list: '📋',
  points: '🪙',
  post: '📝',
  cart: '🛒',
};

const props = defineProps({
  text: {
    type: String,
    default: '暂无数据',
  },
  /** 辅助说明文案 */
  subText: {
    type: String,
    default: '',
  },
  mode: {
    type: String,
    default: 'data',
  },
  showButton: {
    type: Boolean,
    default: false,
  },
  buttonText: {
    type: String,
    default: '去逛逛',
  },
});

const emit = defineEmits(['click']);

const emoji = computed(() => MODE_EMOJI[props.mode] || '🐾');

const handleClick = () => {
  emit('click');
};
</script>

<style lang="scss" scoped>
.empty-state {
  @include pet-flex-center;
  flex-direction: column;
  padding: 100rpx 56rpx;

  .empty-art {
    width: 176rpx;
    height: 176rpx;
    border-radius: 50%;
    background: $gradient-card;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: $shadow-md;
    animation: pet-float 3.2s ease-in-out infinite;

    .empty-emoji {
      font-size: 84rpx;
    }
  }

  .empty-title {
    margin-top: 36rpx;
    font-size: $font-lg;
    color: $text-primary;
    font-weight: $font-weight-bold;
    text-align: center;
  }

  .empty-sub {
    margin-top: 12rpx;
    font-size: $font-sm;
    color: $text-hint;
    text-align: center;
    line-height: 1.6;
  }

  .empty-btn {
    margin-top: 40rpx;
    padding: 18rpx 60rpx;
    border-radius: $radius-round;
    background: $gradient-primary;
    box-shadow: $shadow-primary;

    .empty-btn-text {
      font-size: $font-md;
      color: $text-white;
      font-weight: $font-weight-medium;
    }
  }

  .empty-slot {
    margin-top: 32rpx;
    width: 100%;
    @include pet-flex-center;
    flex-direction: column;
  }
}
</style>
