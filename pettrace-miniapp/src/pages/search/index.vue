<template>
  <view class="search-page">
    <!-- 顶部搜索栏（适配状态栏） -->
    <view class="search-header" :style="{ paddingTop: navBarHeight + 'px' }">
      <view class="search-bar">
        <view class="search-input-wrap">
          <Icon name="search" :size="16" color="#999" />
          <input
            class="search-input"
            type="text"
            v-model="keyword"
            placeholder="搜索动态内容、用户昵称"
            placeholder-class="search-placeholder"
            confirm-type="search"
            @confirm="onSearch"
            focus
          />
          <view v-if="keyword" class="clear-btn" @click="clearKeyword">
            <Icon name="close" :size="14" color="#999" />
          </view>
        </view>
        <text class="cancel-btn" @click="onCancel">取消</text>
      </view>
    </view>

    <!-- 搜索结果 -->
    <view class="result-wrap" v-if="hasSearched">
      <!-- 动态 tab -->
      <view class="result-tabs">
        <view
          class="result-tab"
          :class="{ active: resultTab === 'post' }"
          @click="resultTab = 'post'"
        >
          <text>动态</text>
        </view>
        <view
          class="result-tab"
          :class="{ active: resultTab === 'user' }"
          @click="resultTab = 'user'"
        >
          <text>用户</text>
        </view>
      </view>

      <!-- 动态结果 -->
      <view v-if="resultTab === 'post'">
        <PostCard
          v-for="post in postList"
          :key="post.id"
          :post="post"
          @like="handleLike"
          @comment="handleComment"
          @preview="handlePreview"
          @delete="handleDelete"
        />
        <view v-if="!postList.length" class="empty-wrap">
          <text class="empty-icon">🔍</text>
          <text class="empty-text">没有找到相关动态</text>
        </view>
      </view>

      <!-- 用户结果 -->
      <view v-else class="user-list">
        <view v-for="user in userList" :key="user.userId" class="user-item" @click="goUserHome(user.userId)">
          <image class="user-avatar" :src="fullImageUrl(user.avatar)" mode="aspectFill" />
          <view class="user-meta">
            <text class="user-nickname">{{ user.nickName }}</text>
            <text class="user-id">ID: {{ user.userId }}</text>
          </view>
        </view>
        <view v-if="!userList.length" class="empty-wrap">
          <text class="empty-icon">🙋</text>
          <text class="empty-text">没有找到相关用户</text>
        </view>
      </view>
    </view>

    <!-- 热门占位（未搜索时） -->
    <view v-else class="tips-wrap">
      <text class="tips-text">输入关键词搜索动态或用户</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import PostCard from '@/components/PostCard.vue';
import Icon from '@/components/Icon.vue';
import { getPostList, searchUsers, likePost, deletePost } from '@/api/post.js';
import { showToast, fullImageUrl } from '@/utils/index.js';

const navBarHeight = ref(44);
// #ifdef MP-WEIXIN
try {
  const menuRect = uni.getMenuButtonBoundingClientRect();
  navBarHeight.value = menuRect.bottom + 8;
} catch (e) {}
// #endif

const keyword = ref('');
const hasSearched = ref(false);
const resultTab = ref('post');
const postList = ref([]);
const userList = ref([]);

const clearKeyword = () => {
  keyword.value = '';
  postList.value = [];
  userList.value = [];
  hasSearched.value = false;
};

const onCancel = () => {
  uni.navigateBack();
};

const onSearch = async () => {
  const kw = keyword.value.trim();
  if (!kw) {
    showToast('请输入搜索内容');
    return;
  }
  hasSearched.value = true;
  // 并行搜索动态和用户
  try {
    const [postRes, userRes] = await Promise.allSettled([
      getPostList({ pageNum: 1, pageSize: 20, keyword: kw }),
      searchUsers({ keyword: kw, limit: 20 }),
    ]);
    if (postRes.status === 'fulfilled') {
      postList.value = postRes.value.rows || [];
    }
    if (userRes.status === 'fulfilled') {
      userList.value = userRes.value.data || [];
    }
  } catch (e) {
    console.error('搜索失败', e);
  }
};

const handleLike = async (id) => {
  try {
    const res = await likePost(id);
    const post = postList.value.find((p) => p.id === id);
    if (post) {
      const nowLiked = res.liked !== undefined ? res.liked : !post.isLike;
      post.isLike = nowLiked;
      post.likeCount = (post.likeCount || 0) + (nowLiked ? 1 : -1);
      if (post.likeCount < 0) post.likeCount = 0;
    }
  } catch (err) {
    showToast(err?.msg || '操作失败');
  }
};

const handleComment = (id) => uni.navigateTo({ url: `/pages/index/detail?id=${id}` });
const handlePreview = ({ images, current }) => {
  uni.previewImage({ urls: images, current: images[current] || images[0] });
};
const handleDelete = async (id) => {
  uni.showModal({
    title: '提示', content: '确定删除这条动态吗？', confirmColor: '#FF4D4F',
    success: async (res) => {
      if (res.confirm) {
        try {
          await deletePost(id);
          postList.value = postList.value.filter((p) => p.id !== id);
          showToast('已删除', 'success');
        } catch (err) { showToast(err?.msg || '删除失败'); }
      }
    },
  });
};

const goUserHome = (userId) => {
  uni.navigateTo({ url: `/pages/user/home?userId=${userId}` });
};
</script>

<style lang="scss" scoped>
.search-page {
  min-height: 100vh;
  background-color: #F5F6FA;
}

.search-header {
  background-color: #fff;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
  position: sticky;
  top: 0;
  z-index: 100;

  .search-bar {
    display: flex;
    align-items: center;
    padding: 16rpx 24rpx;
    gap: 16rpx;
    height: 88rpx;
    box-sizing: border-box;
  }

  .search-input-wrap {
    flex: 1;
    display: flex;
    align-items: center;
    background-color: #F2F3F5;
    border-radius: 40rpx;
    padding: 12rpx 24rpx;
    gap: 12rpx;
  }

  .search-input {
    flex: 1;
    font-size: 28rpx;
    color: #1A1A1A;
  }

  .search-placeholder {
    color: #B0B0B0;
    font-size: 28rpx;
  }

  .clear-btn {
    width: 36rpx;
    height: 36rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .cancel-btn {
    font-size: 28rpx;
    color: #FF7E3D;
    flex-shrink: 0;
  }
}

.result-wrap {
  padding-top: 20rpx;
}

.result-tabs {
  display: flex;
  padding: 0 24rpx 16rpx;
  background-color: #fff;
  gap: 40rpx;

  .result-tab {
    font-size: 30rpx;
    color: #666;
    font-weight: 500;
    padding-bottom: 12rpx;
    position: relative;

    &.active {
      color: #1A1A1A;
      font-weight: 700;

      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 40rpx;
        height: 6rpx;
        background: linear-gradient(90deg, #FF8C42, #FFB07C);
        border-radius: 3rpx;
      }
    }
  }
}

.user-list {
  background-color: #fff;
  border-radius: 24rpx 24rpx 0 0;

  .user-item {
    display: flex;
    align-items: center;
    padding: 24rpx;
    border-bottom: 1rpx solid #F5F5F5;

    &:last-child {
      border-bottom: none;
    }

    .user-avatar {
      width: 88rpx;
      height: 88rpx;
      border-radius: 50%;
      background-color: #F0F0F0;
      margin-right: 20rpx;
      flex-shrink: 0;
    }

    .user-meta {
      flex: 1;
      min-width: 0;

      .user-nickname {
        display: block;
        font-size: 30rpx;
        color: #333;
        font-weight: 600;
      }

      .user-id {
        display: block;
        font-size: 24rpx;
        color: #999;
        margin-top: 6rpx;
      }
    }
  }
}

.empty-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 0;

  .empty-icon { font-size: 120rpx; margin-bottom: 20rpx; }
  .empty-text { font-size: 28rpx; color: #B0B0B0; }
}

.tips-wrap {
  display: flex;
  justify-content: center;
  padding-top: 200rpx;

  .tips-text {
    font-size: 28rpx;
    color: #B0B0B0;
  }
}
</style>
