<template>
  <view class="user-home-page">
    <!-- 用户信息卡 -->
    <view class="user-card">
      <image class="user-avatar" :src="avatarUrl" mode="aspectFill" />
      <text class="nickname">{{ userInfo.nickName || '用户' }}</text>
      <text class="user-id">ID: {{ userId }}</text>

      <view class="stats-row">
        <view class="stat-item" @click="goFollowList('follow')">
          <text class="stat-value">{{ followCount }}</text>
          <text class="stat-label">关注</text>
        </view>
        <view class="stat-divider" />
        <view class="stat-item" @click="goFollowList('follower')">
          <text class="stat-value">{{ followerCount }}</text>
          <text class="stat-label">粉丝</text>
        </view>
        <view class="stat-divider" />
        <view class="stat-item">
          <text class="stat-value">{{ postCount }}</text>
          <text class="stat-label">动态</text>
        </view>
      </view>

      <!-- 操作按钮 -->
      <view class="action-row">
        <view
          v-if="!isOwner"
          class="action-btn"
          :class="{ followed: isFollowed }"
          @click="handleToggleFollow"
        >
          <text>{{ isFollowed ? '已关注' : '+ 关注' }}</text>
        </view>
      </view>
    </view>

    <!-- 动态列表 -->
    <view class="feed-list">
      <PostCard
        v-for="post in postList"
        :key="post.id"
        :post="post"
        @like="handleLike"
        @comment="handleComment"
        @preview="handlePreview"
      />
    </view>

    <view v-if="!loading && !postList.length" class="empty-wrap">
      <Icon class="empty-icon" name="pet" :size="58" color="#FFC8A2" />
      <text class="empty-text">该用户还没有发布动态</text>
    </view>
  </view>
</template>

<script setup>
import Icon from '@/components/Icon.vue';
import { ref, computed, onMounted } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import PostCard from '@/components/PostCard.vue';
import { getPostList, likePost } from '@/api/post.js';
import { getFollowList, getFollowCount, toggleFollow, checkFollowed } from '@/api/follow.js';
import { fullImageUrl, showToast } from '@/utils/index.js';
import { requireLogin } from '@/utils/auth.js';
import { useUserStore } from '@/store/user.js';

const userStore = useUserStore();
const userId = ref(null);
const userInfo = ref({});
const avatarUrl = computed(() => fullImageUrl(userInfo.value.avatar));
const isOwner = computed(() => userStore.userInfo?.userId === userId.value);
const isFollowed = ref(false);

const followCount = ref(0);
const followerCount = ref(0);
const postCount = ref(0);
const postList = ref([]);
const loading = ref(false);

onLoad((options) => {
  userId.value = options.userId;
});

onShow(async () => {
  await loadUserInfo();
  await fetchFollowStatus();
  await fetchFollowCount();
  await fetchPosts();
  uni.setNavigationBarTitle({ title: userInfo.value.nickName || '用户主页' });
});

const loadUserInfo = async () => {
  try {
    const res = await getPostList({ pageNum: 1, pageSize: 1, userId: userId.value });
    const first = res.rows?.[0];
    if (first) {
      userInfo.value = {
        nickName: first.userName,
        avatar: first.userAvatar,
      };
    }
    postCount.value = res.total || 0;
  } catch (e) {}
};

const fetchFollowStatus = async () => {
  if (isOwner.value) { isFollowed.value = false; return; }
  try {
    const res = await checkFollowed(userId.value);
    isFollowed.value = !!res.data?.followed;
  } catch (e) {}
};

const fetchFollowCount = async () => {
  try {
    const res = await getFollowCount({ userId: userId.value });
    followCount.value = res.data?.followCount || 0;
    followerCount.value = res.data?.followerCount || 0;
  } catch (e) {}
};

const fetchPosts = async () => {
  loading.value = true;
  try {
    const res = await getPostList({ pageNum: 1, pageSize: 50, userId: userId.value });
    postList.value = res.rows || [];
  } catch (e) {
    showToast(e?.msg || '获取动态失败');
  } finally {
    loading.value = false;
  }
};

const handleToggleFollow = async () => {
  // 关注需登录，由用户自行选择是否登录
  if (!(await requireLogin('关注 TA'))) return;
  try {
    const res = await toggleFollow(userId.value);
    isFollowed.value = res.data?.followed ?? !isFollowed.value;
    if (isFollowed.value) {
      followerCount.value++;
      showToast('关注成功', 'success');
    } else {
      followerCount.value = Math.max(0, followerCount.value - 1);
      showToast('已取关', 'success');
    }
  } catch (err) { showToast(err?.msg || '操作失败'); }
};

const goFollowList = (type) => {
  uni.navigateTo({ url: `/pages/mine/follows?type=${type}&userId=${userId.value}` });
};

const handleLike = async (id) => {
  // 点赞需登录，由用户自行选择是否登录
  if (!(await requireLogin('点赞'))) return;
  try {
    const res = await likePost(id);
    const post = postList.value.find((p) => p.id === id);
    if (post) {
      const nowLiked = res.data?.liked !== undefined ? res.data.liked : !post.isLike;
      post.isLike = nowLiked;
      post.likeCount = (post.likeCount || 0) + (nowLiked ? 1 : -1);
      if (post.likeCount < 0) post.likeCount = 0;
    }
  } catch (err) { showToast(err?.msg || '操作失败'); }
};

const handleComment = (id) => uni.navigateTo({ url: `/pages/index/detail?id=${id}` });
const handlePreview = ({ images, current }) => {
  if (!images?.length) return;
  uni.previewImage({ urls: images, current: images[current] || images[0] });
};
</script>

<style lang="scss" scoped>
.user-home-page {
  min-height: 100vh;
  background-color: #F8F9FC;
}

.user-card {
  background: linear-gradient(180deg, #FFE8D6 0%, #fff 60%);
  padding: 40rpx 32rpx 32rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.user-avatar {
  width: 140rpx;
  height: 140rpx;
  border-radius: 50%;
  background-color: #F0F0F0;
  border: 4rpx solid #fff;
  box-shadow: 0 8rpx 24rpx rgba(255, 126, 61, 0.2);
}

.nickname {
  font-size: 38rpx;
  font-weight: 700;
  color: #333;
  margin-top: 20rpx;
}

.user-id {
  font-size: 24rpx;
  color: #999;
  margin-top: 8rpx;
}

.stats-row {
  display: flex;
  align-items: center;
  background-color: #fff;
  border-radius: 20rpx;
  padding: 20rpx 40rpx;
  margin-top: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(255, 126, 61, 0.1);

  .stat-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 0 24rpx;

    .stat-value {
      font-size: 34rpx;
      font-weight: 700;
      color: #FF8C42;
    }

    .stat-label {
      font-size: 24rpx;
      color: #999;
      margin-top: 4rpx;
    }
  }

  .stat-divider {
    width: 1rpx;
    height: 40rpx;
    background-color: #FFE8D6;
  }
}

.action-row {
  margin-top: 24rpx;

  .action-btn {
    padding: 16rpx 60rpx;
    border-radius: 40rpx;
    background: linear-gradient(135deg, #FF8C42 0%, #FF6B35 100%);
    color: #fff;
    font-size: 28rpx;
    font-weight: 600;

    &.followed {
      background: #F5F5F5;
      color: #999;
    }

    &:active {
      opacity: 0.85;
    }
  }
}

.feed-list {
  margin-top: 20rpx;
}

.empty-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 200rpx;

  .empty-icon { font-size: 120rpx; margin-bottom: 20rpx; }
  .empty-text { font-size: 28rpx; color: #B0B0B0; }
}
</style>
