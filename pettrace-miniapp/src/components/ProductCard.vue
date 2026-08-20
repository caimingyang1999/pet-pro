<template>
  <view class="product-card" @click="handleClick">
    <!-- 左侧图片 -->
    <view class="image-wrapper">
      <!-- 图片正常加载 -->
      <image
        v-if="imageSrc"
        class="product-image"
        :src="imageSrc"
        mode="aspectFill"
        lazy-load
        @error="onImageError"
      />
      <!-- 无图 / 加载失败 兜底 -->
      <view v-else class="image-placeholder">
        <text class="placeholder-icon">📦</text>
      </view>

      <!-- 售罄蒙层 -->
      <view v-if="product.stock === 0" class="sold-out-mask">
        <text class="sold-out-text">售罄</text>
      </view>

      <!-- 热兑标签 -->
      <view v-if="product.stock > 0 && product.totalExchange >= 100" class="hot-tag">
        <text class="hot-tag-text">热门</text>
      </view>
    </view>

    <!-- 右侧信息 -->
    <view class="product-info">
      <text v-if="product.categoryName" class="category-tag">{{ product.categoryName }}</text>
      <text class="name">{{ product.productName }}</text>
      <text v-if="product.description" class="desc">{{ plainDesc }}</text>
      <view class="bottom-row">
        <view class="price">
          <text class="points">{{ product.pointsPrice }}</text>
          <text class="unit">积分</text>
        </view>
        <text v-if="product.totalExchange" class="exchange-count">{{ formatExchange(product.totalExchange) }}人兑换</text>
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

// 图片加载失败时切换到 false 显示占位
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
  if (!images) {
    console.log('[ProductCard] productImages 为空, productId:', props.product.id);
    return '';
  }
  try {
    const parsed = typeof images === 'string' ? JSON.parse(images) : images;
    // 可能是数组，也可能是单张图片字符串
    if (Array.isArray(parsed) && parsed.length > 0) return parsed[0];
    if (typeof parsed === 'string') return parsed;
    console.log('[ProductCard] productImages 解析后无有效图片, productId:', props.product.id);
    return '';
  } catch {
    // JSON.parse 失败说明是普通字符串，直接返回
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
  // 已经是完整 URL 就直接用，否则用 fullImageUrl 补全
  if (rawFirstImage.value.startsWith('http://') || rawFirstImage.value.startsWith('https://')) {
    return rawFirstImage.value;
  }
  return fullImageUrl(rawFirstImage.value);
});

/** 图片加载失败回调 */
const onImageError = (e) => {
  // eslint-disable-next-line no-console
  console.warn('[ProductCard] 图片加载失败:', imageSrc.value, e?.detail);
  imageError.value = true;
};

/** 纯文本描述（去 HTML） */
const plainDesc = computed(() => {
  const desc = props.product.description;
  if (!desc) return '';
  return String(desc).replace(/<[^>]+>/g, '').trim();
});

const formatExchange = (num) => {
  if (num >= 10000) return (num / 10000).toFixed(1) + '万';
  if (num >= 1000) return (num / 1000).toFixed(1) + 'k';
  return String(num);
};

const handleClick = () => emit('click', props.product.id);
</script>

<style lang="scss" scoped>
.product-card {
  display: flex;
  background-color: $pet-bg-white;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 16rpx rgba(0, 0, 0, 0.05);
  transition: transform 0.2s;

  &:active {
    transform: scale(0.98);
  }

  .image-wrapper {
    position: relative;
    flex-shrink: 0;
    width: 240rpx;
    height: 240rpx;
    overflow: hidden;
    background-color: #F5F5F5;

    .product-image {
      width: 100%;
      height: 100%;
    }

    // 无图 / 加载失败占位
    .image-placeholder {
      width: 100%;
      height: 100%;
      @include pet-flex-center;
      background-color: #F0F0F0;

      .placeholder-icon {
        font-size: 64rpx;
      }
    }

    .sold-out-mask {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background-color: rgba(0, 0, 0, 0.4);
      @include pet-flex-center;

      .sold-out-text {
        color: #fff;
        font-size: 28rpx;
        font-weight: 600;
        letter-spacing: 4rpx;
      }
    }

    .hot-tag {
      position: absolute;
      top: 16rpx;
      left: 0;
      background: linear-gradient(135deg, #FF6B6B, #FF8C42);
      padding: 6rpx 20rpx 6rpx 16rpx;
      border-radius: 0 20rpx 20rpx 0;

      .hot-tag-text {
        color: #fff;
        font-size: 20rpx;
        font-weight: 600;
      }
    }
  }

  .product-info {
    flex: 1;
    padding: 20rpx 24rpx;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    overflow: hidden;

    .category-tag {
      display: inline-block;
      align-self: flex-start;
      font-size: 20rpx;
      color: $pet-primary;
      background-color: $pet-primary-light;
      padding: 4rpx 16rpx;
      border-radius: 20rpx;
      margin-bottom: 10rpx;
    }

    .name {
      font-size: 30rpx;
      color: $pet-text-main;
      font-weight: 600;
      line-height: 1.4;
      @include pet-multi-ellipsis(2);
    }

    .desc {
      font-size: 24rpx;
      color: $pet-text-secondary;
      line-height: 1.5;
      margin-top: 8rpx;
      @include pet-multi-ellipsis(1);
    }

    .bottom-row {
      display: flex;
      align-items: flex-end;
      justify-content: space-between;
      margin-top: auto;

      .price {
        display: flex;
        align-items: baseline;

        .points {
          font-size: 36rpx;
          color: $pet-primary;
          font-weight: 800;
          line-height: 1;
        }

        .unit {
          font-size: 22rpx;
          color: $pet-text-secondary;
          margin-left: 6rpx;
        }
      }

      .exchange-count {
        font-size: 22rpx;
        color: $pet-text-placeholder;
        white-space: nowrap;
      }
    }
  }
}
</style>
