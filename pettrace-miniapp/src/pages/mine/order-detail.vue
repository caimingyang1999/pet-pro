<template>
  <view class="detail-page">
    <LoadingState v-if="loading" mode="skeleton" type="post" :count="3" />

    <template v-else-if="order">
      <!-- 状态卡片 -->
      <view class="status-card">
        <view class="status-main">
          <u-tag :text="statusText(order.status)" size="large" :type="statusType(order.status)" />
          <text class="status-tip">{{ statusTip(order.status) }}</text>
        </view>
        <view class="order-no-row" @click="copyOrderNo">
          <text class="order-no">订单号: {{ order.orderNo }}</text>
          <u-icon name="file-text" color="#C0C4CC" size="18" />
        </view>
      </view>

      <!-- 收货信息 -->
      <view class="info-card" v-if="address">
        <view class="section-title">
          <view class="title-bar" />
          <text>收货信息</text>
        </view>
        <view class="addr-line">
          <view class="addr-top">
            <text class="addr-name">{{ address.receiverName }}</text>
            <text class="addr-phone">{{ maskPhone(address.receiverPhone) }}</text>
            <u-tag v-if="isDefaultAddress" text="默认" size="mini" type="primary" />
          </view>
          <text class="addr-detail">{{ fullAddress }}</text>
        </view>
      </view>

      <!-- 商品信息 -->
      <view class="info-card">
        <view class="section-title">
          <view class="title-bar" />
          <text>商品信息</text>
        </view>
        <view class="product-row">
          <image class="product-image" :src="fullImageUrl(order.productImage)" mode="aspectFill" />
          <view class="product-info">
            <text class="product-name">{{ order.productName }}</text>
            <view class="points-line">
              <text class="price">{{ order.pointsPrice }} 积分 × {{ order.quantity }}</text>
              <text class="total">共 {{ order.totalPoints }} 积分</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 订单信息 -->
      <view class="info-card">
        <view class="section-title">
          <view class="title-bar" />
          <text>订单信息</text>
        </view>
        <view class="info-row">
          <text class="info-label">兑换积分</text>
          <text class="info-value points">{{ order.totalPoints }} 积分</text>
        </view>
        <view class="info-row">
          <text class="info-label">下单时间</text>
          <text class="info-value">{{ formatDateTime(order.createTime) }}</text>
        </view>
        <view class="info-row" v-if="order.updateTime && order.updateTime !== order.createTime">
          <text class="info-label">更新时间</text>
          <text class="info-value">{{ formatDateTime(order.updateTime) }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">快递公司</text>
          <text class="info-value">{{ order.expressCompany || '—' }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">快递单号</text>
          <text class="info-value">{{ order.expressNo || '—' }}</text>
        </view>
      </view>

      <!-- 物流提示 -->
      <view class="tips-card" v-if="order.status === '0'">
        <text class="tips-text">· 商品将在 1-3 个工作日内发货，请耐心等待</text>
      </view>
    </template>

    <EmptyState
      v-else
      text="订单不存在或已删除"
      showButton
      buttonText="返回订单列表"
      @click="goBack"
    />
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import EmptyState from '@/components/EmptyState.vue';
import LoadingState from '@/components/LoadingState.vue';
import { getOrderDetail } from '@/api/shop.js';
import { getAddressList } from '@/api/user.js';
import { formatDateTime, fullImageUrl, showToast } from '@/utils/index.js';

const order = ref(null);
const address = ref(null);
const loading = ref(true);

const STATUS_TEXT = {
  '0': '待发货',
  '1': '已发货',
  '2': '已完成',
  '3': '已取消',
};

const STATUS_TYPE = {
  '0': 'warning',
  '1': 'primary',
  '2': 'success',
  '3': 'info',
};

const STATUS_TIP = {
  '0': '商家正在备货中，请耐心等待',
  '1': '商品已发出，请留意物流信息',
  '2': '订单已完成，感谢兑换',
  '3': '订单已取消',
};

const statusText = (s) => STATUS_TEXT[String(s)] || '未知';
const statusType = (s) => STATUS_TYPE[String(s)] || 'info';
const statusTip = (s) => STATUS_TIP[String(s)] || '';

const isDefaultAddress = computed(() => {
  return address.value && String(address.value.isDefault) === '1';
});

const fullAddress = computed(() => {
  if (!address.value) return '';
  return [address.value.province, address.value.city, address.value.district, address.value.detailAddress]
    .filter(Boolean)
    .join('');
});

const maskPhone = (phone) => {
  if (!phone) return '';
  const str = String(phone);
  return str.length === 11 ? `${str.slice(0, 3)}****${str.slice(7)}` : str;
};

onLoad(async (query) => {
  const orderId = query?.id;
  if (!orderId) {
    loading.value = false;
    return;
  }
  try {
    const [orderRes, addrRes] = await Promise.all([
      getOrderDetail(orderId),
      getAddressList().catch(() => ({ data: [] })),
    ]);
    order.value = orderRes.data;
    const addrList = addrRes.data || [];
    if (order.value?.addressId) {
      address.value =
        addrList.find((item) => String(item.id) === String(order.value.addressId)) || null;
    }
  } catch (err) {
    console.error('[订单详情] 加载失败:', err?.msg);
  } finally {
    loading.value = false;
  }
});

const copyOrderNo = () => {
  if (!order.value?.orderNo) return;
  uni.setClipboardData({
    data: order.value.orderNo,
    success: () => showToast('订单号已复制'),
  });
};

const goBack = () => {
  uni.navigateBack({
    fail: () => uni.reLaunch({ url: '/pages/mine/orders' }),
  });
};
</script>

<style lang="scss" scoped>
.detail-page {
  min-height: 100vh;
  padding: 20rpx;
  background-color: $pet-bg;
}

.status-card {
  @include pet-card;
  margin-bottom: 20rpx;
  background: linear-gradient(135deg, #FF8C42, #FFA940);

  .status-main {
    display: flex;
    align-items: center;
    margin-bottom: 20rpx;

    :deep(.u-tag) {
      background: rgba(255, 255, 255, 0.2);
      border-color: rgba(255, 255, 255, 0.5);

      .u-tag__text {
        color: #fff;
      }
    }

    .status-tip {
      font-size: 24rpx;
      color: rgba(255, 255, 255, 0.9);
      margin-left: 20rpx;
    }
  }

  .order-no-row {
    display: flex;
    align-items: center;
    padding-top: 20rpx;
    border-top: 1rpx solid rgba(255, 255, 255, 0.25);

    .order-no {
      flex: 1;
      font-size: 24rpx;
      color: rgba(255, 255, 255, 0.92);
    }
  }
}

.info-card {
  @include pet-card;
  margin-bottom: 20rpx;

  .section-title {
    display: flex;
    align-items: center;
    margin-bottom: 20rpx;

    .title-bar {
      width: 6rpx;
      height: 30rpx;
      border-radius: 3rpx;
      background: linear-gradient(180deg, #FF8C42, #FFA940);
      margin-right: 14rpx;
    }

    text {
      font-size: 30rpx;
      color: $pet-text-main;
      font-weight: 600;
    }
  }

  .addr-line {
    .addr-top {
      display: flex;
      align-items: center;
      margin-bottom: 12rpx;

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
    }

    .addr-detail {
      display: block;
      font-size: 26rpx;
      color: $pet-text-regular;
      line-height: 1.5;
    }
  }

  .product-row {
    display: flex;
    align-items: center;

    .product-image {
      width: 140rpx;
      height: 140rpx;
      border-radius: 12rpx;
      background: $pet-bg;
      margin-right: 20rpx;
      flex-shrink: 0;
    }

    .product-info {
      flex: 1;
      min-width: 0;

      .product-name {
        display: block;
        font-size: 28rpx;
        color: $pet-text-main;
        font-weight: 500;
        margin-bottom: 14rpx;
        @include pet-ellipsis;
      }

      .points-line {
        display: flex;
        align-items: baseline;

        .price {
          font-size: 24rpx;
          color: $pet-text-secondary;
        }

        .total {
          font-size: 30rpx;
          color: $pet-primary;
          font-weight: 700;
          margin-left: 16rpx;
        }
      }
    }
  }

  .info-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16rpx 0;
    border-bottom: 1rpx solid $pet-border-lighter;

    &:last-child {
      border-bottom: none;
    }

    .info-label {
      font-size: 26rpx;
      color: $pet-text-secondary;
    }

    .info-value {
      font-size: 26rpx;
      color: $pet-text-main;

      &.points {
        color: $pet-primary;
        font-weight: 700;
      }
    }
  }
}

.tips-card {
  @include pet-card;

  .tips-text {
    font-size: 24rpx;
    color: $pet-text-secondary;
    line-height: 1.6;
  }
}
</style>
