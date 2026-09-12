<template>
  <view class="detail-page">
    <!-- 加载骨架 -->
    <template v-if="loading">
      <view class="skeleton-swiper" />
      <view class="skeleton-info">
        <view class="skeleton-line sk-name" />
        <view class="skeleton-line sk-tags" />
        <view class="skeleton-line sk-price" />
        <view class="skeleton-line sk-desc-short" />
        <view class="skeleton-line sk-desc-long" />
      </view>
    </template>

    <!-- 正常内容 -->
    <template v-else-if="product">
      <!-- 图片轮播 -->
      <view class="detail-hero" v-if="imageList.length > 0">
        <swiper
          class="image-swiper"
          :autoplay="imageList.length > 1"
          :interval="3800"
          :duration="400"
          :circular="true"
          @change="onSwiperChange"
        >
          <swiper-item v-for="(img, idx) in imageList" :key="idx">
            <image
              class="swiper-image"
              :src="img"
              mode="aspectFill"
              @error="onSwiperError(idx)"
              @click="previewImage(idx)"
            />
          </swiper-item>
        </swiper>
        <!-- 自定义指示器：当前项拉长胶囊 -->
        <view class="hero-dots" v-if="imageList.length > 1">
          <view
            v-for="(img, idx) in imageList"
            :key="'dot-' + idx"
            class="hero-dot"
            :class="{ active: idx === currentSwiper }"
          />
        </view>
      </view>
      <!-- 单张兜底 -->
      <view v-else class="detail-hero single-image-wrap">
        <image
          v-if="displayImage"
          class="single-image"
          :src="displayImage"
          mode="aspectFill"
          @error="onCoverError"
        />
        <view v-else class="image-placeholder">
          <Icon class="placeholder-icon" name="order" :size="48" color="#FFC8A2" />
        </view>
      </view>

      <!-- 商品信息卡片 -->
      <view class="info-card">
        <!-- 演示模式提示：兑换不会真实发货 -->
        <DemoNotice v-if="isShopDemo()" class="detail-demo-tip" :text="SHOP_DEMO.detail" />

        <!-- 名称 + 状态 -->
        <view class="name-row">
          <text class="product-name">{{ product.productName }}</text>
          <text v-if="product.status === '0'" class="status-badge off">已下架</text>
          <text v-else-if="product.stock === 0" class="status-badge sold-out">已售罄</text>
        </view>

        <!-- 价格 -->
        <view class="price-section">
          <view class="price-main">
            <Icon class="price-coin" name="wallet" :size="14" color="#FF8C42" />
            <text class="points-num">{{ product.pointsPrice }}</text>
            <text class="points-unit">积分</text>
            <view class="price-paw">🐾</view>
          </view>
          <!-- 兑换统计 -->
          <view class="exchange-stats">
            <text class="stat-item">
              库存 <text class="stat-value">{{ product.stock }}</text> 件
            </text>
            <text class="stat-divider">|</text>
            <text class="stat-item">
              已兑 <text class="stat-value">{{ product.totalExchange }}</text> 次
            </text>
          </view>
        </view>

        <!-- 简要描述（纯文本） -->
        <text v-if="product.description" class="product-desc">{{ plainDesc }}</text>
      </view>

      <!-- 商品详情（HTML 富文本） -->
      <view v-if="product.description" class="detail-card">
        <view class="section-header">
          <view class="header-bar" />
          <text class="section-title">商品详情</text>
        </view>
        <view class="detail-content">
          <rich-text :nodes="product.description" class="detail-rich" />
        </view>
      </view>

      <!-- 兑换须知 -->
      <view class="tips-card">
        <view class="section-header">
          <view class="header-bar" />
          <text class="section-title">兑换须知</text>
        </view>
        <view class="tips-list">
          <!-- 演示模式下替换掉"1-3 个工作日发货"这类承诺文案 -->
          <text v-for="tip in exchangeTips" :key="tip" class="tip-item">{{ tip }}</text>
        </view>
      </view>

      <!-- 底部安全区占位 -->
      <view class="bottom-placeholder" />
    </template>

    <!-- 错误状态 -->
    <EmptyState
      v-else
      text="商品不存在或已下架"
      showButton
      buttonText="返回商城"
      @click="goBack"
    />

    <!-- 底部操作栏 -->
    <view v-if="product" class="bottom-bar">
      <view class="bar-price">
        <text class="bar-points">{{ product.pointsPrice }}</text>
        <text class="bar-unit">积分</text>
      </view>
      <view
        class="exchange-btn"
        :class="{ disabled: !canExchange }"
        @click="handleExchange"
      >
        <text class="btn-text">{{ exchangeBtnText }}</text>
      </view>
    </view>

    <!-- 收货地址选择弹层 -->
    <view v-if="showPicker" class="picker-mask" @click="closePicker">
      <view class="picker-panel" @click.stop>
        <view class="panel-header">
          <text class="panel-title">选择收货地址</text>
          <Icon name="close" color="#999" size="18" @click="closePicker" />
        </view>

        <scroll-view scroll-y class="addr-scroll" :show-scrollbar="false">
          <view
            v-for="addr in addressList"
            :key="addr.id"
            class="addr-item"
            :class="{ selected: String(selectedAddressId) === String(addr.id) }"
            @click="selectedAddressId = addr.id"
          >
            <view class="addr-info">
              <view class="addr-top">
                <text class="addr-name">{{ addr.receiverName }}</text>
                <text class="addr-phone">{{ maskPhone(addr.receiverPhone) }}</text>
                <view class="tag-default" v-if="String(addr.isDefault) === '1'">
                  <text>默认</text>
                </view>
              </view>
              <text class="addr-detail">{{ fullAddress(addr) }}</text>
            </view>
            <view class="radio-box" :class="{ checked: String(selectedAddressId) === String(addr.id) }">
              <view class="radio-dot" v-if="String(selectedAddressId) === String(addr.id)" />
            </view>
          </view>

          <view v-if="!addressList.length" class="addr-empty">
            <text>还没有收货地址，请先添加</text>
          </view>
        </scroll-view>

        <view class="panel-footer">
          <view class="manage-btn" @click="goAddressManage">管理地址</view>
          <view class="confirm-btn" @click="confirmExchange">确认兑换</view>
        </view>
        <!-- 演示模式：兑换前再次说明，避免用户误以为会真实发货 -->
        <text v-if="isShopDemo()" class="panel-demo-tip">{{ SHOP_DEMO.banner }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import Icon from '@/components/Icon.vue';
import { ref, computed, onMounted } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import EmptyState from '@/components/EmptyState.vue';
import DemoNotice from '@/components/DemoNotice.vue';
import { getProductDetail, createOrder } from '@/api/shop.js';
import { getAddressList } from '@/api/user.js';
import { showToast, showLoading, hideLoading, fullImageUrl } from '@/utils/index.js';
import { requireLogin } from '@/utils/auth.js';
import { isShopDemo, SHOP_DEMO } from '@/config/shopDemo.js';

const product = ref(null);
const productId = ref('');
const loading = ref(true);
const currentSwiper = ref(0);

// ---- 收货地址 ----
const addressList = ref([]);
const showPicker = ref(false);
const selectedAddressId = ref('');
const pendingExchange = ref(false);

// ---- 图片列表 ----
const imageList = computed(() => {
  const images = product.value?.productImages;
  if (!images) return [];
  try {
    const parsed = typeof images === 'string' ? JSON.parse(images) : images;
    // 可能是 JSON 数组，也可能是单张路径字符串
    if (Array.isArray(parsed)) return parsed.map(img => fullImageUrl(img));
    if (typeof parsed === 'string') return [fullImageUrl(parsed)];
    return [];
  } catch {
    // JSON.parse 失败说明是普通字符串，直接拼接
    if (typeof images === 'string') return [fullImageUrl(images)];
    return [];
  }
});

// 用于单张兜底
const displayImage = computed(() => {
  const list = imageList.value;
  return list.length > 0 ? list[0] : '';
});

// 图片加载失败
const coverFailed = ref(false);
const onCoverError = (e) => {
  console.warn('[Detail] 封面图加载失败:', displayImage.value, e?.detail);
  coverFailed.value = true;
};
const onSwiperError = (idx) => {
  console.warn('[Detail] 轮播图[' + idx + ']加载失败:', imageList.value[idx]);
};

// 纯文本描述（去除 HTML 标签）
const plainDesc = computed(() => {
  const desc = product.value?.description;
  if (!desc) return '';
  return String(desc).replace(/<[^>]+>/g, '').trim();
});

// ---- 是否可兑换 ----
const canExchange = computed(() => {
  return product.value?.status === '1' && product.value?.stock > 0;
});

// ---- 兑换须知：演示模式下换成中性说明，避免出现"发货/物流"承诺 ----
const exchangeTips = computed(() =>
  isShopDemo()
    ? SHOP_DEMO.tips
    : [
        '· 兑换成功后，管理员将在 1-3 个工作日内发货',
        '· 可在「我的 → 兑换订单」中查看物流信息',
        '· 积分一经扣除不予退还，请确认后再兑换',
      ]
);

const exchangeBtnText = computed(() => {
  if (product.value?.status === '0') return '已下架';
  if (product.value?.stock === 0) return '已售罄';
  return '立即兑换';
});

// ---- 获取商品详情 ----
const fetchDetail = async (id) => {
  loading.value = true;
  try {
    const res = await getProductDetail(id);
    // 详情接口返回 { code, msg, data: {...} }
    product.value = res.data;
  } catch {
    product.value = null;
  } finally {
    loading.value = false;
  }
};

// ---- 预览图片 ----
const previewImage = (idx) => {
  uni.previewImage({
    current: idx,
    urls: imageList.value,
  });
};

const onSwiperChange = (e) => {
  currentSwiper.value = e.detail.current;
};

// ---- 收货地址工具 ----
const fullAddress = (addr) => {
  return [addr.province, addr.city, addr.district, addr.detailAddress]
    .filter(Boolean)
    .join('');
};

const maskPhone = (phone) => {
  if (!phone) return '';
  const str = String(phone);
  return str.length === 11 ? `${str.slice(0, 3)}****${str.slice(7)}` : str;
};

// ---- 兑换操作 ----
const handleExchange = async () => {
  if (!canExchange.value) return;
  // 游客可浏览商品详情，兑换需登录（由用户自行选择是否登录）
  if (!(await requireLogin('兑换商品'))) return;

  showLoading('获取地址中...');
  try {
    const res = await getAddressList();
    const list = res.data || [];
    addressList.value = list;
    // 默认选中默认地址
    const defaultAddr = list.find((a) => String(a.isDefault) === '1');
    selectedAddressId.value = defaultAddr?.id ?? (list[0]?.id || '');

    if (!list.length) {
      // 无地址：引导去添加，返回后自动继续兑换流程
      uni.showModal({
        title: '提示',
        content: '兑换商品需要收货地址，请先添加一个地址',
        confirmText: '去添加',
        cancelText: '取消',
        success: (modalRes) => {
          if (modalRes.confirm) {
            pendingExchange.value = true;
            uni.navigateTo({ url: '/pages/mine/address-edit' });
          }
        },
      });
      return;
    }

    showPicker.value = true;
  } catch (err) {
    console.error('[兑换] 获取地址失败:', err?.msg);
  } finally {
    hideLoading();
  }
};

const closePicker = () => {
  showPicker.value = false;
};

const goAddressManage = () => {
  uni.navigateTo({ url: '/pages/mine/address' });
};

/**
 * 演示模式二次确认：明确告知不会真实发货，由用户自行决定是否继续
 * @returns {Promise<boolean>} 是否继续兑换
 */
const confirmDemoNotice = () => {
  return new Promise((resolve) => {
    uni.showModal({
      title: SHOP_DEMO.exchange.title,
      content: SHOP_DEMO.exchange.content,
      confirmText: SHOP_DEMO.exchange.confirmText,
      cancelText: SHOP_DEMO.exchange.cancelText,
      confirmColor: '#FF8C42',
      success: (res) => resolve(!!res.confirm),
      fail: () => resolve(false),
    });
  });
};

const confirmExchange = async () => {
  if (!selectedAddressId.value) {
    showToast('请选择收货地址');
    return;
  }
  // 演示模式：下单前再确认一次，避免用户误以为会收到实物
  if (isShopDemo() && !(await confirmDemoNotice())) return;

  closePicker();
  showLoading('兑换中...');
  try {
    await createOrder({
      productId: productId.value,
      quantity: 1,
      addressId: selectedAddressId.value,
    });
    // 刷新商品数据（库存、兑换数）
    fetchDetail(productId.value);
    const success = isShopDemo()
      ? SHOP_DEMO.success
      : { title: '兑换成功', content: '可在「我的 → 兑换订单」中查看订单及物流信息' };
    uni.showModal({
      title: success.title,
      content: success.content,
      confirmText: '查看订单',
      cancelText: '继续逛逛',
      success: (modalRes) => {
        if (modalRes.confirm) {
          uni.navigateTo({ url: '/pages/mine/orders' });
        }
      },
    });
  } catch (err) {
    showToast(err?.msg || '兑换失败，请稍后重试');
  } finally {
    hideLoading();
  }
};

// 从地址添加页返回后，继续之前中断的兑换流程
onShow(() => {
  if (!pendingExchange.value) return;
  pendingExchange.value = false;
  setTimeout(() => {
    handleExchange();
  }, 300);
});

// ---- 返回 ----
const goBack = () => {
  uni.switchTab({
    url: '/pages/shop/index',
  });
};

onMounted(() => {
  const pages = getCurrentPages();
  const current = pages[pages.length - 1];
  const { id } = current.options || current.$route?.query || {};
  if (id) {
    productId.value = id;
    fetchDetail(id);
  } else {
    loading.value = false;
  }
});
</script>

<style lang="scss" scoped>
.detail-page {
  min-height: 100vh;
  background-color: $pet-bg;
  padding-bottom: 140rpx;
}

// ===== 图片轮播 =====
.detail-hero {
  position: relative;
  background-color: #FFF;

  .hero-dots {
    position: absolute;
    left: 0;
    right: 0;
    bottom: 24rpx;
    display: flex;
    justify-content: center;
    gap: 10rpx;
    pointer-events: none;

    .hero-dot {
      width: 12rpx;
      height: 12rpx;
      border-radius: $radius-round;
      background: rgba(255, 255, 255, 0.55);
      transition: all 0.3s ease;

      &.active {
        width: 44rpx;
        background: #fff;
      }
    }
  }
}

.image-swiper {
  width: 100%;
  height: 560rpx;
  background-color: #F8F8F8;

  .swiper-image {
    width: 100%;
    height: 100%;
  }
}

.single-image-wrap {
  width: 100%;
  height: 560rpx;
  background-color: #F8F8F8;
}

.single-image {
  width: 100%;
  height: 560rpx;
  background-color: #F8F8F8;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  @include pet-flex-center;
  background-color: $bg-input;

  .placeholder-icon {
    font-size: 80rpx;
  }
}

// ===== 商品信息卡片 =====
.info-card {
  background-color: $pet-bg-white;
  margin: 20rpx 24rpx;
  border-radius: $radius-lg;
  padding: 32rpx;
  box-shadow: $shadow-card;

  /* 演示模式提示条：与下方名称行留出间距 */
  .detail-demo-tip {
    margin-bottom: 24rpx;
  }

  .name-row {
    display: flex;
    align-items: center;
    margin-bottom: 20rpx;

    .product-name {
      font-size: $font-xl;
      color: $pet-text-main;
      font-weight: $font-weight-bold;
      flex: 1;
      line-height: 1.45;
    }

    .status-badge {
      font-size: 22rpx;
      padding: 6rpx 18rpx;
      border-radius: $radius-round;
      font-weight: 500;
    }

    .off {
      color: $pet-text-secondary;
      background-color: #F0F0F0;
    }

    .sold-out {
      color: #fff;
      background-color: $pet-text-placeholder;
    }
  }

  .price-section {
    margin-bottom: 20rpx;
    padding: 24rpx;
    border-radius: $radius-md;
    background: linear-gradient(135deg, #FFF6EC 0%, #FFEFDD 100%);

    .price-main {
      display: flex;
      align-items: baseline;
      position: relative;

      .points-num {
        font-size: 58rpx;
        color: $pet-primary;
        font-weight: $font-weight-bold;
        line-height: 1;
        margin-left: 4rpx;
      }

      .points-unit {
        font-size: $font-sm;
        color: $primary-dark;
        margin-left: 10rpx;
      }

      .price-coin {
        font-size: 36rpx;
        align-self: center;
      }

      .price-paw {
        position: absolute;
        right: 0;
        top: -6rpx;
        font-size: 52rpx;
        opacity: 0.22;
        transform: rotate(-14deg);
      }
    }

    .exchange-stats {
      display: flex;
      align-items: center;

      .stat-item {
        font-size: $font-sm;
        color: $text-secondary;
      }

      .stat-value {
        color: $primary-dark;
        font-weight: 500;
      }

      .stat-divider {
        margin: 0 16rpx;
        color: $pet-border;
      }
    }
  }

  .product-desc {
    display: block;
    font-size: $font-sm;
    color: $text-secondary;
    line-height: 1.7;
    padding-top: 20rpx;
    border-top: 1rpx solid $pet-border-lighter;
    @include pet-multi-ellipsis(3);
  }
}

// ===== 详情 & 须知卡片共用 =====
.detail-card,
.tips-card {
  background-color: $pet-bg-white;
  margin: 0 24rpx 24rpx;
  border-radius: $radius-lg;
  padding: 30rpx 32rpx;
  box-shadow: $shadow-card;
}

// ===== 区块标题 =====
.section-header {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;

  .header-bar {
    width: 6rpx;
    height: 32rpx;
    border-radius: 3rpx;
    background: linear-gradient(180deg, #FF8C42, #FFA940);
    margin-right: 16rpx;
  }

  .section-title {
    font-size: 30rpx;
    color: $pet-text-main;
    font-weight: 600;
  }
}

// ===== 详情内容 =====
.detail-content {
  .detail-rich {
    font-size: 28rpx;
    color: $pet-text-regular;
    line-height: 1.8;
    width: 100%;
    overflow-x: hidden;
  }
}

// ===== 兑换须知 =====
.tips-list {
  .tip-item {
    display: block;
    font-size: 26rpx;
    color: $pet-text-secondary;
    line-height: 2;
  }
}

// ===== 底部操作栏 =====
.bottom-placeholder {
  height: 40rpx;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18rpx 30rpx;
  padding-bottom: calc(18rpx + env(safe-area-inset-bottom));
  background-color: $pet-bg-white;
  box-shadow: 0 -2rpx 16rpx rgba(0, 0, 0, 0.06);
  z-index: 100;

  .bar-price {
    display: flex;
    align-items: baseline;

    .bar-points {
      font-size: 44rpx;
      color: $pet-primary;
      font-weight: 800;
    }

    .bar-unit {
      font-size: 24rpx;
      color: $pet-text-secondary;
      margin-left: 8rpx;
    }
  }

  .exchange-btn {
    background: linear-gradient(135deg, #FF8C42, #FFA940);
    padding: 16rpx 56rpx;
    border-radius: 44rpx;
    box-shadow: 0 6rpx 20rpx rgba(255, 140, 66, 0.35);

    .btn-text {
      font-size: 30rpx;
      color: #fff;
      font-weight: 600;
    }

    &:active {
      transform: scale(0.96);
      opacity: 0.9;
    }

    &.disabled {
      background: #E0E0E0;
      box-shadow: none;

      .btn-text {
        color: #999;
      }

      &:active {
        transform: none;
        opacity: 1;
      }
    }
  }
}

// ===== 载入骨架 =====
.skeleton-swiper {
  width: 100%;
  height: 560rpx;
  background: linear-gradient(90deg, #F0F0F0 25%, #E8E8E8 50%, #F0F0F0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-info {
  background-color: $pet-bg-white;
  margin: 20rpx;
  border-radius: 20rpx;
  padding: 28rpx;

  .skeleton-line {
    height: 32rpx;
    border-radius: 8rpx;
    background-color: #F0F0F0;
    margin-bottom: 20rpx;

    &.sk-name {
      width: 70%;
      height: 40rpx;
    }

    &.sk-tags {
      width: 50%;
      height: 26rpx;
    }

    &.sk-price {
      width: 40%;
      height: 48rpx;
    }

    &.sk-desc-short {
      width: 80%;
    }

    &.sk-desc-long {
      width: 60%;
    }
  }
}

@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

// ===== 收货地址选择弹层 =====
.picker-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 999;
  display: flex;
  align-items: flex-end;
}

.picker-panel {
  width: 100%;
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
  padding: 28rpx 30rpx calc(28rpx + env(safe-area-inset-bottom));
  max-height: 70vh;
  display: flex;
  flex-direction: column;

  .panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: 24rpx;
    border-bottom: 1rpx solid $pet-border-lighter;

    .panel-title {
      font-size: 32rpx;
      color: $pet-text-main;
      font-weight: 700;
    }
  }

  .addr-scroll {
    flex: 1;
    min-height: 160rpx;
    max-height: 44vh;

    .addr-item {
      display: flex;
      align-items: center;
      padding: 24rpx 8rpx;
      border-bottom: 1rpx solid $pet-border-lighter;

      &.selected {
        .addr-name {
          color: $pet-primary;
        }
      }

      .addr-info {
        flex: 1;
        min-width: 0;
        margin-right: 16rpx;

        .addr-top {
          display: flex;
          align-items: center;
          margin-bottom: 10rpx;

          .addr-name {
            font-size: 30rpx;
            color: $pet-text-main;
            font-weight: 600;
            margin-right: 16rpx;
          }

          .addr-phone {
            font-size: 26rpx;
            color: $pet-text-secondary;
            margin-right: 12rpx;
          }

          .tag-default {
            padding: 2rpx 12rpx;
            border-radius: 8rpx;
            background: $pet-primary-light;

            text {
              font-size: 20rpx;
              color: $pet-primary;
              font-weight: 500;
            }
          }
        }

        .addr-detail {
          display: block;
          font-size: 26rpx;
          color: $pet-text-regular;
          line-height: 1.4;
          @include pet-multi-ellipsis(2);
        }
      }

      .radio-box {
        width: 36rpx;
        height: 36rpx;
        border-radius: 50%;
        border: 2rpx solid $pet-border;
        background: #fff;
        @include pet-flex-center;
        flex-shrink: 0;

        &.checked {
          border-color: $pet-primary;
          background: $pet-primary;
        }

        .radio-dot {
          width: 12rpx;
          height: 12rpx;
          border-radius: 50%;
          background: #fff;
        }
      }
    }

    .addr-empty {
      padding: 60rpx 0;
      text-align: center;

      text {
        font-size: 26rpx;
        color: $pet-text-placeholder;
      }
    }
  }

  .panel-footer {
    display: flex;
    gap: 20rpx;
    margin-top: 24rpx;

    .manage-btn {
      flex: 1;
      height: 84rpx;
      border-radius: 42rpx;
      border: 2rpx solid $pet-border;
      @include pet-flex-center;

      text {
        font-size: 28rpx;
        color: $pet-text-regular;
        font-weight: 500;
      }

      &:active {
        background: $pet-bg;
      }
    }

    .confirm-btn {
      flex: 1.6;
      height: 84rpx;
      border-radius: 42rpx;
      background: linear-gradient(135deg, #FF8C42, #FFA940);
      @include pet-flex-center;
      box-shadow: 0 6rpx 20rpx rgba(255, 140, 66, 0.35);

      text {
        font-size: 28rpx;
        color: #fff;
        font-weight: 600;
      }

      &:active {
        opacity: 0.9;
      }
    }

    /* 演示模式：地址弹层底部的补充说明 */
    .panel-demo-tip {
      display: block;
      margin-top: 18rpx;
      font-size: 22rpx;
      color: $text-hint;
      line-height: 1.6;
      text-align: center;
    }
  }
}
</style>
