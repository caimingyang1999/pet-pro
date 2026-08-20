<template>
  <view class="home-page">
    <!-- 顶部区域：搜索框 + 轮播图 -->
    <view class="top-section" :style="{ paddingTop: statusBarHeight + 'px' }">
      <!-- 搜索框 -->
      <view class="search-bar">
        <view class="search-box" @click="focusSearch">
          <Icon name="search" :size="16" color="#999" />
          <text class="search-placeholder">搜索动态、用户、话题</text>
        </view>
      </view>

      <!-- 轮播图 -->
      <swiper
        class="swiper"
        :indicator-dots="true"
        indicator-color="rgba(255,255,255,0.4)"
        indicator-active-color="#FF8C42"
        :autoplay="true"
        :interval="4000"
        :duration="500"
        :circular="true"
        easing-function="easeInOutCubic"
      >
        <swiper-item v-for="banner in bannerList" :key="banner.id" @click="onBannerClick(banner)">
          <view class="banner-card">
            <image class="banner-img" :src="fullImageUrl(banner.imageUrl)" mode="aspectFill" />
            <view class="banner-mask" />
            <view class="banner-info">
              <text class="banner-title">{{ banner.title }}</text>
              <view class="banner-tags" v-if="banner.remark">
                <text class="banner-tag">{{ banner.remark }}</text>
              </view>
            </view>
          </view>
        </swiper-item>
      </swiper>
    </view>

    <!-- Tab 栏：推荐 / 关注 / 最新 -->
    <view class="tab-bar">
      <view
        class="tab-item"
        :class="{ active: currentTab === tab.key }"
        v-for="tab in tabList"
        :key="tab.key"
        @click="switchTab(tab.key)"
      >
        <text class="tab-text">{{ tab.label }}</text>
        <view class="tab-indicator" v-if="currentTab === tab.key" />
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
        @delete="handleDelete"
      />
    </view>

    <!-- 加载更多 -->
    <view v-if="postList.length" class="load-more">
      <text v-if="loadStatus === 'loading'" class="load-text">加载中...</text>
      <text v-else-if="loadStatus === 'nomore'" class="load-text">- 没有更多了 -</text>
      <text v-else-if="loadStatus === 'loadmore'" class="load-text load-more-btn" @click="loadMore">加载更多</text>
    </view>

    <!-- 空状态 -->
    <view v-if="!loading && !postList.length" class="empty-wrap">
      <text class="empty-icon">🐾</text>
      <text class="empty-text">还没有动态，快来发布第一条吧</text>
    </view>

    <!-- 发布动态悬浮按钮 -->
    <view class="fab-btn" @click="goPublish">
      <Icon name="plus" :size="24" color="#fff" />
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { onShow, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app';
import PostCard from '@/components/PostCard.vue';
import Icon from '@/components/Icon.vue';
import { getPostList, likePost, deletePost } from '@/api/post.js';
import { getBannerList } from '@/api/banner.js';
import { showToast, fullImageUrl } from '@/utils/index.js';
import { useUserStore } from '@/store/user.js';

const userStore = useUserStore();
const userInfo = computed(() => userStore.userInfo);

const statusBarHeight = ref(20);

// #ifdef MP-WEIXIN
try {
  const sysInfo = uni.getSystemInfoSync();
  statusBarHeight.value = sysInfo.statusBarHeight || 20;
} catch (e) {}
// #endif

const postList = ref([]);
const loading = ref(false);
const loadStatus = ref('loadmore');
const hasInitialized = ref(false);
const pageParams = ref({ pageNum: 1, pageSize: 10 });

const currentTab = ref('recommend');
const tabList = ref([
  { key: 'recommend', label: '推荐' },
  { key: 'follow', label: '关注' },
  { key: 'latest', label: '最新' },
]);

const bannerList = ref([]);

// 获取启用的轮播图列表（已按 sortOrder 升序、ID 降序排列）
const fetchBannerList = async () => {
  try {
    const res = await getBannerList();
    bannerList.value = res.data || [];
  } catch (err) {
    console.error('获取轮播图失败:', err);
  }
};

// 点击轮播图：根据 jumpType 跳转
// none-不跳转 post-动态详情 product-商品详情 url-外部链接 miniapp-小程序页面
const onBannerClick = (banner) => {
  if (!banner) return;
  const { jumpType, jumpTarget } = banner;
  switch (jumpType) {
    case 'none':
    case undefined:
    case null:
      break;
    case 'post':
      uni.navigateTo({ url: `/pages/index/detail?id=${jumpTarget}` });
      break;
    case 'product':
      uni.navigateTo({ url: `/pages/shop/detail?id=${jumpTarget}` });
      break;
    case 'miniapp':
      if (jumpTarget) {
        uni.navigateTo({ url: jumpTarget });
      }
      break;
    case 'url':
      if (jumpTarget) {
        uni.navigateTo({
          url: `/pages/common/webview?url=${encodeURIComponent(jumpTarget)}&title=${encodeURIComponent(banner.title || '详情')}`,
        });
      }
      break;
    default:
      break;
  }
};

const focusSearch = () => {
  showToast('搜索功能开发中');
};

const switchTab = (key) => {
  if (currentTab.value === key) return;
  currentTab.value = key;
  fetchPostList(true);
};

const fetchPostList = async (isRefresh = false) => {
  if (isRefresh) {
    pageParams.value.pageNum = 1;
  }
  loading.value = true;
  loadStatus.value = 'loading';
  try {
    const res = await getPostList(pageParams.value);
    const list = res.rows || [];
    if (isRefresh) {
      postList.value = list;
    } else {
      postList.value = [...postList.value, ...list];
    }
    loadStatus.value = list.length < pageParams.value.pageSize ? 'nomore' : 'loadmore';
    hasInitialized.value = true;
  } catch (err) {
    loadStatus.value = 'loadmore';
    console.error('获取动态失败:', err);
  } finally {
    loading.value = false;
  }
};

const loadMore = () => {
  if (loadStatus.value === 'nomore' || loadStatus.value === 'loading') return;
  pageParams.value.pageNum++;
  fetchPostList();
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

const handleComment = (id) => {
  uni.navigateTo({ url: `/pages/index/detail?id=${id}` });
};

const handlePreview = ({ images, current }) => {
  if (!images || !images.length) return;
  uni.previewImage({ urls: images, current: images[current] || images[0] });
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
          showToast(err?.msg || '删除失败');
        }
      }
    },
  });
};

const goPublish = () => {
  const token = uni.getStorageSync('token');
  if (!token) {
    showToast('请先登录');
    uni.switchTab({ url: '/pages/mine/index' });
    return;
  }
  uni.navigateTo({ url: '/pages/index/publish' });
};

onMounted(() => {
  fetchBannerList();
  fetchPostList(true);
});

onShow(async () => {
  if (userStore.isLogin && !userStore.userInfo) {
    try { await userStore.fetchUserInfo(); } catch (e) {}
  }
  fetchPostList(true);
});

onPullDownRefresh(async () => {
  try {
    await Promise.all([fetchBannerList(), fetchPostList(true)]);
  } finally { uni.stopPullDownRefresh(); }
});

onReachBottom(() => {
  loadMore();
});
</script>

<style lang="scss" scoped>
.home-page {
  min-height: 100vh;
  background-color: #F5F6FA;
  padding-bottom: calc(env(safe-area-inset-bottom) + 40rpx);
}

/* ===== 顶部区域 ===== */
.top-section {
  background-color: #fff;
  padding: 0 24rpx 24rpx;
}

.search-bar {
  padding: 16rpx 0;

  .search-box {
    display: flex;
    align-items: center;
    background-color: #F2F3F5;
    border-radius: 40rpx;
    padding: 16rpx 24rpx;
    gap: 12rpx;

    .search-placeholder {
      flex: 1;
      font-size: 26rpx;
      color: #999;
    }
  }
}

.swiper {
  height: 320rpx;
  border-radius: 24rpx;
  overflow: hidden;
}

.banner-card {
  position: relative;
  width: 100%;
  height: 100%;
  border-radius: 24rpx;
  overflow: hidden;

  .banner-img {
    width: 100%;
    height: 100%;
  }

  .banner-mask {
    position: absolute;
    inset: 0;
    background: linear-gradient(180deg, rgba(0,0,0,0) 40%, rgba(0,0,0,0.65) 100%);
  }

  .banner-info {
    position: absolute;
    left: 32rpx;
    bottom: 28rpx;
    right: 32rpx;

    .banner-title {
      font-size: 36rpx;
      color: #fff;
      font-weight: 700;
      line-height: 1.3;
    }

    .banner-tags {
      display: flex;
      gap: 12rpx;
      margin-top: 12rpx;
    }

    .banner-tag {
      font-size: 22rpx;
      color: #fff;
      background-color: rgba(255, 140, 66, 0.85);
      padding: 6rpx 18rpx;
      border-radius: 20rpx;
    }
  }
}

/* ===== Tab 栏 ===== */
.tab-bar {
  display: flex;
  align-items: center;
  padding: 20rpx 28rpx 0;
  background-color: #F5F6FA;

  .tab-item {
    position: relative;
    padding: 16rpx 28rpx;

    .tab-text {
      font-size: 30rpx;
      color: #666;
      font-weight: 500;
    }

    &.active .tab-text {
      color: #1A1A1A;
      font-weight: 700;
      font-size: 32rpx;
    }

    .tab-indicator {
      position: absolute;
      bottom: 0;
      left: 50%;
      transform: translateX(-50%);
      width: 36rpx;
      height: 6rpx;
      background: linear-gradient(90deg, #FF8C42, #FFB07C);
      border-radius: 3rpx;
    }
  }
}

/* ===== 动态列表 ===== */
.feed-list {
  margin-top: 20rpx;
  background-color: #fff;
  border-radius: 24rpx 24rpx 0 0;
  overflow: hidden;
}

/* ===== 加载更多 ===== */
.load-more {
  text-align: center;
  padding: 40rpx 0 60rpx;

  .load-text {
    font-size: 26rpx;
    color: #B0B0B0;
  }

  .load-more-btn {
    color: #FF8C42;
  }
}

/* ===== 空状态 ===== */
.empty-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 200rpx;

  .empty-icon {
    font-size: 120rpx;
    margin-bottom: 20rpx;
  }

  .empty-text {
    font-size: 28rpx;
    color: #B0B0B0;
  }
}

/* ===== 悬浮发布按钮 ===== */
.fab-btn {
  position: fixed;
  right: 40rpx;
  bottom: calc(env(safe-area-inset-bottom) + 40rpx);
  width: 100rpx;
  height: 100rpx;
  background: linear-gradient(135deg, #FF8C42 0%, #FF6B35 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 12rpx 30rpx rgba(255, 140, 66, 0.4);
  z-index: 999;

  &:active {
    transform: scale(0.95);
  }
}
</style>
