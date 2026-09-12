<template>
  <view class="follows-page">
    <!-- 顶部 tab -->
    <view class="top-tabs">
      <view
        class="top-tab"
        :class="{ active: currentTab === 'follow' }"
        @click="switchTab('follow')"
      >
        <text class="tab-label">关注</text>
        <text class="tab-count">{{ followCount }}</text>
      </view>
      <view
        class="top-tab"
        :class="{ active: currentTab === 'follower' }"
        @click="switchTab('follower')"
      >
        <text class="tab-label">粉丝</text>
        <text class="tab-count">{{ followerCount }}</text>
      </view>
    </view>

    <!-- 列表 -->
    <scroll-view
      scroll-y
      class="list-scroll"
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="loadMore"
    >
      <view class="user-list">
        <view v-for="item in list" :key="item.id" class="user-item">
          <image
            class="avatar"
            :src="fullImageUrl(currentTab === 'follow' ? item.followeeAvatar : item.followerAvatar)"
            mode="aspectFill"
            @click="goUserHome(currentTab === 'follow' ? item.followeeId : item.followerId)"
          />
          <view class="user-info" @click="goUserHome(currentTab === 'follow' ? item.followeeId : item.followerId)">
            <text class="nickname">{{ currentTab === 'follow' ? item.followeeNickName : item.followerNickName }}</text>
          </view>
          <!-- 操作按钮 -->
          <view v-if="currentTab === 'follow'" class="action-btn unfollow" @click="handleToggle(item)">
            <text>已关注</text>
          </view>
          <view v-else class="action-btn" :class="{ active: item.isFollowed }" @click="handleToggle(item)">
            <text>{{ item.isFollowed ? '已关注' : '+ 关注' }}</text>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-if="!loading && !list.length" class="empty-wrap">
        <Icon class="empty-icon" name="pet" :size="58" color="#FFC8A2" />
        <text class="empty-text">{{ currentTab === 'follow' ? '还没有关注任何人' : '还没有粉丝' }}</text>
      </view>

      <view v-if="list.length" class="load-more">
        <text v-if="loadStatus === 'loading'" class="load-text">加载中...</text>
        <text v-else-if="loadStatus === 'nomore'" class="load-text">- 没有更多了 -</text>
        <text v-else class="load-text" @click="loadMore">加载更多</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import Icon from '@/components/Icon.vue';
import { ref, onMounted } from 'vue';
import { onShow, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app';
import { fullImageUrl } from '@/utils/index.js';
import { useUserStore } from '@/store/user.js';
import { getFollowList, getFollowerList, getFollowCount, toggleFollow } from '@/api/follow.js';
import { showToast } from '@/utils/index.js';

const userStore = useUserStore();
const targetUserId = ref(null); // 目标用户ID，null 表示当前登录用户
const currentTab = ref('follow');
const list = ref([]);
const loading = ref(false);
const loadStatus = ref('loadmore');
const refreshing = ref(false);
const pageParams = ref({ pageNum: 1, pageSize: 20 });
const followCount = ref(0);
const followerCount = ref(0);

const switchTab = (key) => {
  if (currentTab.value === key) return;
  currentTab.value = key;
  fetchList(true);
};

const fetchCount = async () => {
  try {
    const res = await getFollowCount({ userId: targetUserId.value || userStore.userInfo?.userId });
    followCount.value = res.data?.followCount || 0;
    followerCount.value = res.data?.followerCount || 0;
  } catch (e) {}
};

const fetchList = async (isRefresh = false) => {
  if (isRefresh) pageParams.value.pageNum = 1;
  loading.value = true;
  try {
    const params = { ...pageParams.value, userId: targetUserId.value || undefined };
    const fn = currentTab.value === 'follow' ? getFollowList : getFollowerList;
    const res = await fn(params);
    // 手动补齐 isFollowed 字段（follower 列表里需要显示是否已回关）
    const rows = (res.rows || []).map((item) => {
      // follower 列表的 followee 就是当前用户，默认不算已关注
      // 这里简化处理：关注列表里的人显示"已关注"，粉丝列表里默认显示未关注
      if (currentTab.value === 'follower') {
        return { ...item, isFollowed: false };
      }
      return item;
    });
    if (isRefresh) {
      list.value = rows;
    } else {
      list.value = [...list.value, ...rows];
    }
    loadStatus.value = rows.length < pageParams.value.pageSize ? 'nomore' : 'loadmore';
  } catch (e) {
    loadStatus.value = 'loadmore';
  } finally {
    loading.value = false;
    refreshing.value = false;
  }
};

const loadMore = () => {
  if (loadStatus.value === 'nomore' || loadStatus.value === 'loading') return;
  pageParams.value.pageNum++;
  fetchList();
};

const onRefresh = async () => {
  refreshing.value = true;
  await Promise.all([fetchCount(), fetchList(true)]);
};

const handleToggle = async (item) => {
  // 确定要关注/取关的 userId
  const targetUserId = currentTab.value === 'follow' ? item.followeeId : item.followerId;
  try {
    const res = await toggleFollow(targetUserId);
    if (currentTab.value === 'follow') {
      // 关注列表取关后从列表移除
      list.value = list.value.filter((it) => it.followeeId !== targetUserId);
      followCount.value = Math.max(0, followCount.value - 1);
      showToast('已取关', 'success');
    } else {
      item.isFollowed = res.data?.followed;
      followerCount.value = res.data?.followed ? followerCount.value : Math.max(0, followerCount.value - 1);
      if (res.data?.followed) showToast('关注成功', 'success');
      else showToast('已取关', 'success');
    }
  } catch (e) { showToast(e?.msg || '操作失败'); }
};

const goUserHome = (userId) => {
  if (userId) uni.navigateTo({ url: `/pages/user/home?userId=${userId}` });
};

onLoad((options) => {
  if (options?.userId) {
    targetUserId.value = Number(options.userId);
  }
  if (options?.tab || options?.type) {
    currentTab.value = options?.tab || options?.type;
  }
});

onShow(() => {
  fetchCount();
  fetchList(true);
  uni.setNavigationBarTitle({ title: currentTab.value === 'follow' ? '我的关注' : '我的粉丝' });
});
</script>

<style lang="scss" scoped>
.follows-page {
  min-height: 100vh;
  background-color: #F8F9FC;
  display: flex;
  flex-direction: column;
}

.top-tabs {
  display: flex;
  background-color: #fff;
  border-bottom: 1rpx solid #F0F0F0;

  .top-tab {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 24rpx 0;
    position: relative;

    .tab-label {
      font-size: 30rpx;
      color: #666;
      font-weight: 500;
    }

    .tab-count {
      font-size: 24rpx;
      color: #999;
      margin-top: 8rpx;
    }

    &.active {
      .tab-label { color: #1A1A1A; font-weight: 700; }
      .tab-count { color: #FF8C42; }

      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 60rpx;
        height: 6rpx;
        background: linear-gradient(90deg, #FF8C42, #FFB07C);
        border-radius: 3rpx;
      }
    }
  }
}

.list-scroll {
  flex: 1;
}

.user-list {
  background-color: #fff;
  border-radius: 24rpx 24rpx 0 0;
  margin-top: 20rpx;

  .user-item {
    display: flex;
    align-items: center;
    padding: 28rpx 24rpx;
    border-bottom: 1rpx solid #F5F5F5;

    &:last-child { border-bottom: none; }

    .avatar {
      width: 88rpx;
      height: 88rpx;
      border-radius: 50%;
      background-color: #F0F0F0;
      margin-right: 20rpx;
      flex-shrink: 0;
    }

    .user-info {
      flex: 1;
      min-width: 0;

      .nickname {
        font-size: 30rpx;
        color: #333;
        font-weight: 600;
      }
    }

    .action-btn {
      padding: 8rpx 28rpx;
      border-radius: 24rpx;
      font-size: 24rpx;
      color: #FF8C42;
      border: 2rpx solid #FF8C42;
      background-color: #fff;
      flex-shrink: 0;

      &.active, &.unfollow {
        color: #999;
        border-color: #E0E0E0;
        background-color: #F5F5F5;
      }
    }
  }
}

.empty-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 200rpx;

  .empty-icon { font-size: 120rpx; margin-bottom: 20rpx; }
  .empty-text { font-size: 28rpx; color: #B0B0B0; }
}

.load-more {
  text-align: center;
  padding: 40rpx 0;

  .load-text { font-size: 26rpx; color: #B0B0B0; }
}
</style>
