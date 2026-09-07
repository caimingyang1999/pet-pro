<template>
  <view class="points-page">
    <!-- 积分余额卡片 -->
    <view class="balance-card">
      <view class="balance-label">当前积分</view>
      <view class="balance-value">{{ pointsBalance }}</view>
      <view class="balance-tip">积分可在商城兑换商品</view>
    </view>

    <!-- 积分明细列表 -->
    <view class="log-list">
      <view
        class="log-item"
        v-for="log in logList"
        :key="log.id"
      >
        <view class="log-icon" :class="iconClass(log.changeType)">
          <text>{{ typeIcon(log.changeType) }}</text>
        </view>
        <view class="log-info">
          <text class="log-title">{{ typeText(log.changeType) }}</text>
          <text class="log-time">{{ formatDateTime(log.createTime) }}</text>
        </view>
        <view class="log-points" :class="{ 'minus': log.pointsChange < 0 }">
          {{ log.pointsChange > 0 ? '+' : '' }}{{ log.pointsChange }}
        </view>
      </view>
    </view>

    <u-loadmore
      v-if="logList.length"
      :status="loadStatus"
      @loadmore="loadMore"
    />
    <EmptyState v-if="!loading && !logList.length" text="暂无积分记录" />
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app';
import EmptyState from '@/components/EmptyState.vue';
import { getPointsLog, getUserInfo } from '@/api/user.js';
import { formatDateTime } from '@/utils/index.js';

const logList = ref([]);
const loading = ref(false);
const total = ref(0);
const loadStatus = ref('loadmore');
const pointsBalance = ref(0);
const pageParams = ref({ pageNum: 1, pageSize: 10 });

// 变动类型映射
const TYPE_TEXT = {
  sign_in: '每日签到',
  post: '发布动态',
  exchange: '兑换商品',
  admin: '管理员操作',
  register: '注册奖励',
  pet: '完善宠物信息',
};

const TYPE_ICON = {
  sign_in: '签',
  post: '发',
  exchange: '兑',
  admin: '管',
  register: '注',
  pet: '宠',
};

const typeText = (type) => TYPE_TEXT[type] || '积分变动';
const typeIcon = (type) => TYPE_ICON[type] || '积';

const iconClass = (type) => {
  if (type === 'exchange' || type === 'admin') return 'icon-minus';
  return 'icon-plus';
};

onLoad(() => {
  fetchBalance();
  fetchLogs(true);
});

onPullDownRefresh(() => {
  Promise.all([fetchBalance(), fetchLogs(true)]).finally(() => {
    uni.stopPullDownRefresh();
  });
});

onReachBottom(() => {
  loadMore();
});

const fetchBalance = async () => {
  try {
    const res = await getUserInfo();
    pointsBalance.value = res.points || 0;
  } catch (e) {
    console.error('[积分明细] 获取积分余额失败:', e?.msg);
  }
};

const fetchLogs = async (isRefresh = false) => {
  if (isRefresh) {
    pageParams.value.pageNum = 1;
    logList.value = [];
  }
  loading.value = true;
  loadStatus.value = 'loading';
  try {
    const res = await getPointsLog(pageParams.value);
    const list = res.rows || [];
    total.value = res.total || 0;
    if (isRefresh) {
      logList.value = list;
    } else {
      logList.value = logList.value.concat(list);
    }
    loadStatus.value = logList.value.length >= total.value ? 'nomore' : 'loadmore';
  } catch (e) {
    console.error('[积分明细] 获取记录失败:', e?.msg);
    loadStatus.value = 'loadmore';
  } finally {
    loading.value = false;
  }
};

const loadMore = () => {
  if (loadStatus.value !== 'loadmore' || loading.value) return;
  pageParams.value.pageNum++;
  fetchLogs(false);
};
</script>

<style lang="scss" scoped>
.points-page {
  min-height: 100vh;
  background-color: #F6F7FB;
  padding-bottom: 40rpx;
}

/* 积分余额卡片 */
.balance-card {
  margin: 24rpx 32rpx;
  padding: 48rpx 40rpx;
  border-radius: 24rpx;
  background: linear-gradient(135deg, #FF7E3D 0%, #FF5722 100%);
  color: #fff;
  box-shadow: 0 8rpx 24rpx rgba(255, 126, 61, 0.25);

  .balance-label {
    font-size: 26rpx;
    opacity: 0.9;
  }

  .balance-value {
    font-size: 64rpx;
    font-weight: 700;
    margin: 12rpx 0;
  }

  .balance-tip {
    font-size: 24rpx;
    opacity: 0.8;
  }
}

/* 明细列表 */
.log-list {
  margin: 0 32rpx;
  background: #fff;
  border-radius: 24rpx;
  overflow: hidden;
}

.log-item {
  display: flex;
  align-items: center;
  padding: 28rpx 32rpx;
  border-bottom: 1rpx solid #F2F2F2;

  &:last-child {
    border-bottom: none;
  }

  .log-icon {
    width: 72rpx;
    height: 72rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 24rpx;
    flex-shrink: 0;
    font-size: 28rpx;
    font-weight: 600;
    color: #fff;

    &.icon-plus {
      background: linear-gradient(135deg, #FF7E3D 0%, #FF5722 100%);
    }

    &.icon-minus {
      background: linear-gradient(135deg, #909399 0%, #606266 100%);
    }
  }

  .log-info {
    flex: 1;
    display: flex;
    flex-direction: column;

    .log-title {
      font-size: 28rpx;
      color: #303133;
      font-weight: 500;
    }

    .log-time {
      font-size: 22rpx;
      color: #909399;
      margin-top: 6rpx;
    }
  }

  .log-points {
    font-size: 32rpx;
    font-weight: 600;
    color: #FF7E3D;

    &.minus {
      color: #606266;
    }
  }
}
</style>
