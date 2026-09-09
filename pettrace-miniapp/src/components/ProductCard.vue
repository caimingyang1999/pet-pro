<template>
  <view class="product-card pet-press" @click="handleClick">
    <!-- 商品图片 -->
    <view class="image-wrapper">
      <image
        v-if="imageSrc"
        class="product-image"
        :src="imageSrc"
        mode="aspectFill"
        lazy-load
        @error="onImageError"
      />
      <!-- 无图 / 加载失败兜底 -->
      <view v-else class="image-placeholder">
        <text class="placeholder-icon">🎁</text>
      </view>

      <!-- 热兑角标 -->
      <view v-if="product.stock > 0 && product.totalExchange >= 100" class="hot-tag">
        <text class="hot-tag-text">🔥 热兑</text>
      </view>

      <!-- 库存状态 -->
      <view v-if="product.stock === 0" class="stock-tag soldout">
        <text class="stock-text">已兑罄</text>
      </view>
      <view v-else-if="product.stock <= 20" class="stock-tag">
        <text class="stock-text">仅剩 {{ product.stock }} 件</text>
      </view>
    </view>

    <!-- 商品信息 -->
    <view class="product-info">
      <text class="name pet-line2">{{ product.productName }}</text>
      <view class="meta-row">
        <text class="exchange-count">{{ formatExchange(product.totalExchange) }}人兑换</text>
      </view>
      <view class="bottom-row">
        <view class="price">
          <text class="coin">🪙</text>
          <text class="points">{{ product.pointsPrice }}</text>
          <text class="unit">积分</text>
        </view>
        <view class="exchange-btn">
          <text class="btn-text">兑换</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { fullImageUrl } from '@/utils/index.js';

const props = defineProps({
  product: {
    type: Object,
    default: () => ({}),
  },
});

const emit = defineEmits(['click']);

// 图片加载失败时切换到占位图
const imageError = ref(false);

// 监听 product 变化重置错误态
watch(() => props.product?.id, () => {
  imageError.value = false;
});

/**
 * 解析商品首图
 */
const rawFirstImage = computed(() => {
  const images = props.product.productImages;
  if (!images) return '';
  try {
    const parsed = typeof images === 'string' ? JSON.parse(images) : images;
    if (Array.isArray(parsed) && parsed.length > 0) return parsed[0];
    if (typeof parsed === 'string') return parsed;
    return '';
  } catch {
    if (typeof images === 'string') return images;
    return '';
  }
});

/**
 * 最终图片 src —— 有图且未出错时才赋值
 */
const imageSrc = computed(() => {
  if (!rawFirstImage.value) return '';
  if (imageError.value) return '';
  if (rawFirstImage.value.startsWith('http://') || rawFirstImage.value.startsWith('https://')) {
    return rawFirstImage.value;
  }
  return fullImageUrl(rawFirstImage.value);
});

/** 图片加载失败回调 */
const onImageError = () => {
  imageError.value = true;
};

const formatExchange = (num) => {
  if (!num) return '0';
  if (num >= 10000) return (num / 10000).toFixed(1) + '万';
  if (num >= 1000) return (num / 1000).toFixed(1) + 'k';
  return String(num);
};

const handleClick = () => emit('click', props.product.id);
</script>

<style lang="scss" scoped>
.product-card {
  background-color: $pet-bg-white;
  border-radius: $radius-md;
  overflow: hidden;
  box-shadow: $shadow-card;
  animation: pet-slideIn 0.4s ease both;
  height: 100%;
}

.image-wrapper {
  position: relative;
  width: 100%;
  height: 326rpx;
  overflow: hidden;
  background-color: $bg-input;

  .product-image {
    width: 100%;
    height: 100%;
    transition: transform 0.45s ease;
  }

  .image-placeholder {
    width: 100%;
    height: 100%;
    @include pet-flex-center;
    background: $gradient-card;

    .placeholder-icon {
      font-size: 72rpx;
    }
  }

  .hot-tag {
    position: absolute;
    top: 0;
    left: 0;
    background: $gradient-primary;
    padding: 8rpx 18rpx;
    border-radius: 0 0 20rpx 0;

    .hot-tag-text {
      color: #fff;
      font-size: $font-xs;
      font-weight: $font-weight-medium;
    }
  }

  .stock-tag {
    position: absolute;
    top: 0;
    right: 0;
    padding: 8rpx 18rpx;
    background: rgba(255, 201, 77, 0.96);
    border-radius: 0 0 0 20rpx;

    .stock-text {
      font-size: $font-xs;
      color: #7A5200;
      font-weight: $font-weight-medium;
    }

    &.soldout {
      background: rgba(45, 45, 45, 0.72);

      .stock-text {
        color: #fff;
      }
    }
  }
}

.product-info {
  padding: 18rpx 18rpx 22rpx;

  .name {
    font-size: $font-md;
    color: $pet-text-main;
    font-weight: $font-weight-medium;
    line-height: 1.45;
    min-height: 80rpx;
  }

  .meta-row {
    margin-top: 8rpx;

    .exchange-count {
      font-size: $font-xs;
      color: $pet-text-secondary;
    }
  }

  .bottom-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 14rpx;
  }

  .price {
    display: flex;
    align-items: baseline;
    gap: 4rpx;
    min-width: 0;

    .coin {
      font-size: $font-md;
      align-self: center;
    }

    .points {
      font-size: $font-lg;
      color: $pet-primary;
      font-weight: $font-weight-bold;
      line-height: 1;
    }

    .unit {
      font-size: $font-xs;
      color: $pet-text-secondary;
    }
  }

  .exchange-btn {
    flex-shrink: 0;
    padding: 10rpx 24rpx;
    border-radius: $radius-round;
    background: $gradient-primary;
    box-shadow: 0 4rpx 12rpx rgba(255, 140, 66, 0.3);

    .btn-text {
      font-size: $font-xs;
      color: #fff;
      font-weight: $font-weight-medium;
    }
  }
}

.product-card:active {
  .product-image {
    transform: scale(1.05);
  }
}
</style>
