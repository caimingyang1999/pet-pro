<template>
  <view class="my-posts-page">
    <view class="post-list">
      <PostCard
        v-for="post in postList"
        :key="post.id"
        :post="post"
        @like="handleLike"
        @comment="handleComment"
        @preview="handlePreview"
        @delete="handleDelete"
      />
    </view>

    <view class="load-more">
      <text v-if="loadStatus === 'loading'" class="load-text">加载中...</text>
      <text v-else-if="loadStatus === 'nomore' && postList.length > 0" class="load-text">- 没有更多了 -</text>
      <text v-else-if="loadStatus === 'loadmore' && postList.length > 0" class="load-text load-more-btn" @click="loadMore">加载更多</text>
    </view>

    <view v-if="!loading && !postList.length" class="empty-wrap">
      <text class="empty-icon">📝</text>
      <text class="empty-text">还没有发布过动态</text>
      <view class="go-publish-btn" @click="goPublish">去发布</view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app';
import PostCard from '@/components/PostCard.vue';
import { getMyPosts, likePost, deletePost } from '@/api/post.js';
import { showToast, showLoading, hideLoading, fullImageUrl } from '@/utils/index.js';

const postList = ref([]);
const loading = ref(false);
const loadStatus = ref('loadmore');
const pageParams = ref({ pageNum: 1, pageSize: 10 });
let isFirstShow = true;

const fetchPosts = async (isRefresh = false) => {
  if (isRefresh) pageParams.value.pageNum = 1;
  loading.value = true;
  loadStatus.value = 'loading';
  try {
    const res = await getMyPosts({ ...pageParams.value });
    const list = res.rows || [];
    if (isRefresh) {
      postList.value = list;
    } else {
      postList.value = [...postList.value, ...list];
    }
    loadStatus.value = list.length < pageParams.value.pageSize ? 'nomore' : 'loadmore';
  } catch (err) {
    console.error('[我的动态] 加载失败:', err?.code || err?.msg || err);
    loadStatus.value = 'loadmore';
  } finally {
    loading.value = false;
  }
};

const loadMore = () => {
  if (loadStatus.value === 'nomore') return;
  pageParams.value.pageNum++;
  fetchPosts();
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
    console.error('[我的动态] 点赞失败:', err?.code || err?.msg || err);
    showToast(err?.msg || '操作失败');
  }
};

const handleComment = (id) => {
  uni.navigateTo({
    url: `/pages/index/detail?id=${id}`,
  });
};

const handlePreview = ({ images, current }) => {
  if (!images || !images.length) return;
  uni.previewImage({
    urls: images,
    current: images[current] || images[0],
  });
};

const handleDelete = (id) => {
  uni.showModal({
    title: '提示',
    content: '确定删除这条动态吗？',
    confirmColor: '#FF4D4F',
    success: async (res) => {
      if (res.confirm) {
        try {
          await deletePost(id);
          postList.value = postList.value.filter((p) => p.id !== id);
          showToast('已删除', 'success');
        } catch (err) {
          console.error('[我的动态] 删除失败:', err?.code || err?.msg || err);
          showToast(err?.msg || '删除失败');
        }
      }
    },
  });
};

const goPublish = () => {
  uni.navigateTo({ url: '/pages/index/publish' });
};

onMounted(() => {
  fetchPosts(true);
});

onShow(() => {
  // 首次进入由 onMounted 加载，后续 onShow 时才刷新
  if (!isFirstShow) {
    fetchPosts(true);
  }
  isFirstShow = false;
});

// 下拉刷新
onPullDownRefresh(async () => {
  try {
    await fetchPosts(true);
  } finally {
    uni.stopPullDownRefresh();
  }
});
</script>

<style lang="scss" scoped>
.my-posts-page {
  min-height: 100vh;
  background-color: #EDEDED;
}

.post-list {
  background-color: #fff;
  margin-top: 16rpx;
}

.load-more {
  text-align: center;
  padding: 30rpx 0 50rpx;

  .load-text {
    font-size: 26rpx;
    color: #B0B0B0;
  }

  .load-more-btn {
    color: #576B95;

    &:active { opacity: 0.6; }
  }
}

.empty-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 200rpx;

  .empty-icon {
    font-size: 80rpx;
    margin-bottom: 20rpx;
  }

  .empty-text {
    font-size: 28rpx;
    color: #B0B0B0;
    margin-bottom: 32rpx;
  }

  .go-publish-btn {
    padding: 16rpx 48rpx;
    background: #FF8C42;
    color: #fff;
    border-radius: 40rpx;
    font-size: 28rpx;

    &:active { opacity: 0.8; }
  }
}
</style>
