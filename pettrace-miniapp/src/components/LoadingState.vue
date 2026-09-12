<template>
  <!-- 旋转加载模式 -->
  <view v-if="mode === 'spinner'" class="loading-spinner">
    <view class="spinner" />
    <text class="spinner-text">{{ text }}</text>
  </view>

  <!-- 骨架屏模式 -->
  <view
    v-else
    class="skeleton-list"
    :class="{ 'skeleton-grid': mode === 'skeleton' && type === 'product' }"
  >
    <view
      v-for="i in count"
      :key="i"
      class="skeleton-card"
      :class="`skeleton-${type}`"
    >
      <!-- 列表类型骨架 -->
      <template v-if="type === 'list'">
        <view class="post-header">
          <view class="skeleton-avatar" />
          <view class="post-info">
            <view class="skeleton-line w-name" />
            <view class="skeleton-line w-time" />
          </view>
        </view>
        <view class="skeleton-line w-content" />
        <view class="skeleton-line w-content short" />
        <view class="skeleton-images">
          <view class="skeleton-img" />
          <view class="skeleton-img" />
          <view class="skeleton-img" />
        </view>
      </template>

      <!-- 商品类型骨架（双列竖版卡片） -->
      <template v-else-if="type === 'product'">
        <view class="skeleton-img product-img" />
        <view class="product-info">
          <view class="skeleton-line w-name" />
          <view class="skeleton-line w-name short" />
          <view class="skeleton-line w-price" />
        </view>
      </template>

      <!-- 宠物类型骨架（纵向卡片） -->
      <template v-else-if="type === 'pet'">
        <view class="pet-header">
          <view class="skeleton-img pet-avatar" />
          <view class="pet-info">
            <view class="skeleton-line w-name" />
            <view class="skeleton-line w-time" />
            <view class="skeleton-line w-content" />
          </view>
        </view>
      </template>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  // 加载模式：spinner（旋转图标） | skeleton（骨架屏）
  mode: {
    type: String,
    default: 'spinner',
  },
  // 骨架屏类型：list（列表） | product（商品） | pet（宠物）
  type: {
    type: String,
    default: 'post',
  },
  // 骨架屏数量
  count: {
    type: Number,
    default: 3,
  },
  // spinner 模式下的提示文字
  text: {
    type: String,
    default: '加载中...',
  },
});
</script>

<style lang="scss" scoped>
/* ============ spinner 模式 ============ */
.loading-spinner {
  @include pet-flex-center;
  flex-direction: column;
  padding: 80rpx 40rpx;

  .spinner {
    width: 60rpx;
    height: 60rpx;
    border: 4rpx solid $pet-border-lighter;
    border-top-color: $pet-primary;
    border-radius: 50%;
    animation: loading-spin 0.8s linear infinite;
    margin-bottom: 20rpx;
  }

  .spinner-text {
    font-size: 26rpx;
    color: $pet-text-secondary;
  }
}

@keyframes loading-spin {
  to {
    transform: rotate(360deg);
  }
}

/* ============ skeleton 骨架屏 ============ */
.skeleton-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24rpx;
}

.skeleton-card {
  background-color: $pet-bg-white;
  border-radius: $radius-md;
  padding: 0;
  overflow: hidden;
  box-shadow: $shadow-sm;
}

/* 通用骨架线条 */
.skeleton-line {
  border-radius: 8rpx;
  background: linear-gradient(90deg, #F0F0F0 25%, #E8E8E8 50%, #F0F0F0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-img {
  background: linear-gradient(90deg, #F0F0F0 25%, #E8E8E8 50%, #F0F0F0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 8rpx;
}

@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

/* ============ 列表类型骨架 ============ */
.skeleton-post {
  .post-header {
    display: flex;
    align-items: center;
    margin-bottom: 20rpx;
  }

  .skeleton-avatar {
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
    margin-right: 16rpx;
    flex-shrink: 0;
  }

  .post-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 10rpx;
  }

  .w-name {
    width: 30%;
    height: 28rpx;
  }

  .w-time {
    width: 20%;
    height: 22rpx;
  }

  .w-content {
    width: 90%;
    height: 28rpx;
    margin-bottom: 12rpx;

    &.short {
      width: 60%;
    }
  }

  .skeleton-images {
    display: flex;
    gap: 12rpx;
    margin-top: 16rpx;

    .skeleton-img {
      flex: 1;
      height: 200rpx;
    }
  }
}

/* ============ 商品类型骨架（双列） ============ */
.skeleton-product {
  padding: 0;

  .product-img {
    width: 100%;
    height: 326rpx;
    border-radius: 0;
  }

  .product-info {
    padding: 20rpx 20rpx 28rpx;
    display: flex;
    flex-direction: column;
    gap: 16rpx;
  }

  .w-name {
    width: 92%;
    height: 28rpx;

    &.short {
      width: 62%;
    }
  }

  .w-price {
    width: 150rpx;
    height: 32rpx;
    margin-top: 6rpx;
  }
}

/* ============ 宠物类型骨架 ============ */
.skeleton-pet {
  .pet-header {
    display: flex;
    align-items: center;
  }

  .pet-avatar {
    width: 120rpx;
    height: 120rpx;
    border-radius: 50%;
    margin-right: 24rpx;
    flex-shrink: 0;
  }

  .pet-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 12rpx;
  }

  .w-name {
    width: 35%;
    height: 32rpx;
  }

  .w-time {
    width: 25%;
    height: 24rpx;
  }

  .w-content {
    width: 70%;
    height: 24rpx;
  }
}
</style>
