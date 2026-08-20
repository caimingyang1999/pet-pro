<template>
  <view class="orders-page">
    <!-- 状态筛选 Tab -->
    <view class="status-tabs">
      <scroll-view scroll-x class="tabs-scroll" :show-scrollbar="false">
        <view class="tabs-list">
          <view
            v-for="tab in statusTabs"
            :key="tab.value"
            class="tab-item"
            :class="{ active: currentStatus === tab.value }"
            @click="switchStatus(tab.value)"
          >
            <text class="tab-text">{{ tab.label }}</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 订单列表 -->
    <view class="order-list">
      <view
        class="order-card"
        v-for="order in orderList"
        :key="order.id"
        @click="goDetail(order)"
      >
        <view class="order-header">
          <text class="order-no">订单号: {{ order.orderNo }}</text>
          <u-tag :text="statusText(order.status)" size="mini" :type="statusType(order.status)" />
        </view>
        <view class="order-body">
          <image class="product-image" :src="fullImageUrl(order.productImage)" mode="aspectFill" />
          <view class="product-info">
            <text class="name">{{ order.productName }}</text>
            <view class="points-line">
              <text class="price">{{ order.pointsPrice }} 积分 × {{ order.quantity }}</text>
              <text class="total">共 {{ order.totalPoints }} 积分</text>
            </view>
          </view>
          <u-icon name="arrow-right" color="#C0C4CC" size="14" />
        </view>
        <view class="order-footer">
          <text class="time">{{ formatDateTime(order.createTime) }}</text>
          <text class="detail-link">查看详情</text>
        </view>
      </view>
    </view>

    <u-loadmore
      v-if="orderList.length"
      :status="loadStatus"
      @loadmore="loadMore"
    />
    <EmptyState v-if="!loading && !orderList.length" :text="emptyText" />
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad, onShow, onReachBottom } from '@dcloudio/uni-app';
import EmptyState from '@/components/EmptyState.vue';
import { getOrderList } from '@/api/shop.js';
import { formatDateTime, fullImageUrl } from '@/utils/index.js';

const statusTabs = [
  { label: '全部', value: '' },
  { label: '待发货', value: '0' },
  { label: '已发货', value: '1' },
  { label: '已完成', value: '2' },
  { label: '已取消', value: '3' },
];

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

const currentStatus = ref('');
const orderList = ref([]);
const loading = ref(false);
const total = ref(0);
const loadStatus = ref('loadmore');
const pageParams = ref({ pageNum: 1, pageSize: 10 });

const emptyText = computed(() => {
  const tab = statusTabs.find((t) => t.value === currentStatus.value);
  return tab && tab.value ? `暂无${tab.label}订单` : '暂无订单';
});

onLoad((query) => {
  if (query?.status !== undefined && query.status !== '') {
    currentStatus.value = query.status;
  }
});

onShow(() => {
  fetchOrders(true);
});

onReachBottom(() => {
  loadMore();
});

const statusText = (status) => STATUS_TEXT[String(status)] || '未知';
const statusType = (status) => STATUS_TYPE[String(status)] || 'info';

const switchStatus = (value) => {
  if (currentStatus.value === value) return;
  currentStatus.value = value;
  fetchOrders(true);
};

const fetchOrders = async (isRefresh = false) => {
  if (isRefresh) {
    pageParams.value.pageNum = 1;
    orderList.value = [];
  }
  loading.value = true;
  loadStatus.value = 'loading';
  try {
    const res = await getOrderList({
      ...pageParams.value,
      ...(currentStatus.value !== '' ? { status: currentStatus.value } : {}),
    });
    const list = res.rows || [];
    total.value = res.total || 0;
    if (isRefresh) {
      orderList.value = list;
    } else {
      orderList.value = [...orderList.value, ...list];
    }
    const hasMore = orderList.value.length < total.value;
    loadStatus.value = hasMore ? 'loadmore' : 'nomore';
  } catch (err) {
    loadStatus.value = 'loadmore';
  } finally {
    loading.value = false;
  }
};

const loadMore = () => {
  if (loadStatus.value === 'nomore' || loadStatus.value === 'loading') return;
  pageParams.value.pageNum++;
  fetchOrders();
};

const goDetail = (order) => {
  uni.navigateTo({ url: `/pages/mine/order-detail?id=${order.id}` });
};
</script>

<style lang="scss" scoped>
.orders-page {
  min-height: 100vh;
  padding: 20rpx 20rpx 40rpx;
  background-color: $pet-bg;
}

.status-tabs {
  position: sticky;
  top: 0;
  z-index: 10;
  margin: -20rpx -20rpx 20rpx;
  padding: 16rpx 20rpx;
  background-color: #fff;
  border-bottom: 1rpx solid $pet-border-lighter;

  .tabs-scroll {
    white-space: nowrap;

    .tabs-list {
      display: inline-flex;
      padding: 4rpx;
      border-radius: 40rpx;
      background: $pet-bg;

      .tab-item {
        padding: 12rpx 32rpx;
        border-radius: 36rpx;

        &.active {
          background: linear-gradient(135deg, #FF8C42, #FFA940);
          box-shadow: 0 4rpx 12rpx rgba(255, 140, 66, 0.3);
        }

        .tab-text {
          font-size: 26rpx;
          color: $pet-text-secondary;

          .active & {
            color: #fff;
            font-weight: 600;
          }
        }
      }
    }
  }
}

.order-list {
  .order-card {
    @include pet-card;
    margin-bottom: 20rpx;

    .order-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16rpx;

      .order-no {
        font-size: 24rpx;
        color: $pet-text-secondary;
      }
    }

    .order-body {
      display: flex;
      align-items: center;
      padding: 20rpx 0;
      border-top: 1rpx solid $pet-border-lighter;
      border-bottom: 1rpx solid $pet-border-lighter;

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
        margin-right: 12rpx;

        .name {
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

    .order-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 16rpx;

      .time {
        font-size: 22rpx;
        color: $pet-text-secondary;
      }

      .detail-link {
        font-size: 24rpx;
        color: $pet-primary;
      }
    }
  }
}
</style>
