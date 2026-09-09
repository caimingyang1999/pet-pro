<template>
  <view class="home-page">
    <!-- ========== 毛玻璃吸顶导航栏 ========== -->
    <view class="glass-nav" :style="{ paddingTop: navBarHeight + 'px' }">
      <view class="nav-inner">
        <view class="brand" @click="goTop">
          <view class="brand-logo">🐾</view>
          <text class="brand-name">宠迹</text>
        </view>
        <view class="search-capsule" @click="focusSearch">
          <Icon name="search" :size="16" color="#FF8C42" />
          <text class="search-placeholder">搜索宠物动态...</text>
        </view>
      </view>
    </view>

    <!-- ========== 顶部渐变区：轮播图 ========== -->
    <view class="top-hero" :style="{ paddingTop: contentPad + 'px' }">
      <!-- 欢迎标语 -->
      <view class="hero-slogan">
        <text class="slogan-text">和毛孩子的每一天都值得记录 🐶</text>
      </view>

      <!-- 圆角卡片式轮播图 + 胶囊指示器 -->
      <view class="banner-wrap" v-if="bannerList.length">
        <swiper
          class="banner-swiper"
          :autoplay="true"
          :interval="4000"
          :duration="500"
          :circular="true"
          easing-function="easeInOutCubic"
          @change="onSwiperChange"
        >
          <swiper-item
            v-for="(banner, idx) in bannerList"
            :key="banner.id"
            @click="onBannerClick(banner)"
          >
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
        <!-- 指示器：圆点 + 当前项拉长为胶囊 -->
        <view class="banner-dots" v-if="bannerList.length > 1">
          <view
            v-for="(banner, idx) in bannerList"
            :key="'dot-' + banner.id"
            class="banner-dot"
            :class="{ active: idx === currentBanner }"
          />
        </view>
      </view>

      <!-- 无轮播数据时的占位卡片 -->
      <view class="banner-placeholder" v-else>
        <text class="placeholder-emoji">🐾</text>
        <text class="placeholder-text">更多精彩动态即将上线~</text>
      </view>
    </view>

    <!-- ========== 内容区 ========== -->
    <view class="content-area">
      <!-- 推荐 / 关注 / 最新 分段切换 -->
      <view class="feed-tabs">
        <view
          class="feed-tab"
          :class="{ active: currentTab === tab.key }"
          v-for="tab in tabList"
          :key="tab.key"
          @click="switchTab(tab.key)"
        >
          <text class="tab-text">{{ tab.label }}</text>
        </view>
      </view>

      <!-- 首次加载：爪印旋转加载 -->
      <view class="loading-wrap" v-if="loading && !postList.length">
        <view class="loading-paw">
          <text>🐾</text>
        </view>
        <text class="loading-text">正在为你加载新鲜动态...</text>
      </view>

      <!-- 动态卡片列表 -->
      <view class="feed-list" v-else>
        <PostCard
          v-for="post in postList"
          :key="post.id"
          :post="post"
          @like="handleLike"
          @comment="handleComment"
          @preview="handlePreview"
          @delete="handleDelete"
          @follow="handleFollow"
        />
      </view>

      <!-- 加载更多 -->
      <view v-if="postList.length" class="load-more">
        <view v-if="loadStatus === 'loading'" class="load-more-state">
          <text class="load-paw-sm">🐾</text>
          <text class="load-text">努力加载中...</text>
        </view>
        <text v-else-if="loadStatus === 'nomore'" class="load-text nomore-text">
          ─ 已经到底啦，没有更多动态 ─
        </text>
        <view v-else-if="loadStatus === 'loadmore'" class="load-more-btn" @click="loadMore">
          <text class="load-more-text">加载更多</text>
        </view>
      </view>

      <!-- 空状态：引导发布 -->
      <view v-if="!loading && !postList.length" class="empty-wrap">
        <view class="empty-art">🐕‍🦺</view>
        <text class="empty-title">还没有动态哦~</text>
        <text class="empty-desc">快来发布第一条动态，记录毛孩子的高光时刻吧！</text>
        <view class="empty-btn pet-press" @click="goPublish">
          <text class="empty-btn-text">📝 发布第一条动态</text>
        </view>
      </view>
    </view>

    <!-- ========== 发布动态悬浮按钮 ========== -->
    <view class="fab-btn pet-press" @click="goPublish">
      <Icon name="plus" :size="28" color="#fff" />
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

/** 导航栏与状态栏相关尺寸（px） */
const navBarHeight = ref(44);
const contentPad = computed(() => navBarHeight.value + uni.upx2px(96) + 4);

// #ifdef MP-WEIXIN
try {
  const menuRect = uni.getMenuButtonBoundingClientRect();
  navBarHeight.value = menuRect.bottom + 8;
} catch (e) {}
// #endif

const postList = ref([]);
const loading = ref(false);
const loadStatus = ref('loadmore');
const pageParams = ref({ pageNum: 1, pageSize: 10 });

const currentTab = ref('recommend');
const tabList = ref([
  { key: 'recommend', label: '推荐' },
  { key: 'follow', label: '关注' },
  { key: 'latest', label: '最新' },
]);

const bannerList = ref([]);
const currentBanner = ref(0);

/** 回到页面顶部 */
const goTop = () => {
  uni.pageScrollTo({ scrollTop: 0, duration: 300 });
};

/** 获取启用的轮播图列表（已按 sortOrder 升序、ID 降序排列） */
const fetchBannerList = async () => {
  try {
    const res = await getBannerList();
    bannerList.value = res.data || [];
  } catch (err) {
    console.error('获取轮播图失败:', err);
  }
};

const onSwiperChange = (e) => {
  currentBanner.value = e.detail.current;
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
  uni.navigateTo({ url: '/pages/search/index' });
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
    // 传递 tab 参数给后端：recommend 推荐 / follow 关注 / latest 最新
    const res = await getPostList({ ...pageParams.value, tab: currentTab.value });
    const list = res.rows || [];
    if (isRefresh) {
      postList.value = list;
    } else {
      postList.value = [...postList.value, ...list];
    }
    loadStatus.value = list.length < pageParams.value.pageSize ? 'nomore' : 'loadmore';
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

const handleFollow = ({ userId, followed }) => {
  postList.value.forEach((p) => {
    if (p.userId === userId) p.isFollowed = followed;
  });
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
    title: '删除提示',
    content: '确定删除这条动态吗？删除后不可恢复哦~',
    confirmColor: '#FF6B6B',
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
  } finally {
    uni.stopPullDownRefresh();
  }
});

onReachBottom(() => {
  loadMore();
});
</script>

<style lang="scss" scoped>
.home-page {
  min-height: 100vh;
  background-color: $bg-page;
  padding-bottom: calc(env(safe-area-inset-bottom) + 160rpx);
}

/* ========== 毛玻璃吸顶导航 ========== */
.glass-nav {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 200;
  background-color: rgba(255, 255, 255, 0.68);
  backdrop-filter: blur(24rpx);
  -webkit-backdrop-filter: blur(24rpx);
  box-shadow: 0 2rpx 12rpx rgba(52, 59, 76, 0.04);

  .nav-inner {
    display: flex;
    align-items: center;
    gap: 20rpx;
    height: 96rpx;
    padding: 0 24rpx;
  }

  .brand {
    display: flex;
    align-items: center;
    gap: 10rpx;
    flex-shrink: 0;

    .brand-logo {
      width: 60rpx;
      height: 60rpx;
      border-radius: 20rpx;
      background: $gradient-primary;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 32rpx;
      box-shadow: $shadow-primary;
    }

    .brand-name {
      font-size: $font-xl;
      font-weight: $font-weight-bold;
      color: $text-primary;
      letter-spacing: 1rpx;
    }
  }

  .search-capsule {
    flex: 1;
    display: flex;
    align-items: center;
    gap: 12rpx;
    height: 72rpx;
    background-color: rgba(255, 255, 255, 0.82);
    border: 1rpx solid rgba(255, 140, 66, 0.12);
    border-radius: $radius-round;
    padding: 0 28rpx;

    .search-placeholder {
      font-size: $font-sm;
      color: $text-hint;
    }
  }
}

/* ========== 顶部渐变区 ========== */
.top-hero {
  background: $gradient-hero;
  padding-bottom: 8rpx;
}

.hero-slogan {
  padding: 8rpx 40rpx 20rpx;

  .slogan-text {
    font-size: $font-sm;
    color: $primary-dark;
    opacity: 0.72;
  }
}

/* ===== 轮播区 ===== */
.banner-wrap {
  position: relative;
  margin: 0 24rpx;
  border-radius: $radius-lg;

  .banner-swiper {
    height: 300rpx;
    border-radius: $radius-lg;
  }

  .banner-card {
    position: relative;
    width: 100%;
    height: 100%;
    border-radius: $radius-lg;
    overflow: hidden;
    box-shadow: $shadow-md;

    .banner-img {
      width: 100%;
      height: 100%;
    }

    .banner-mask {
      position: absolute;
      inset: 0;
      background: linear-gradient(180deg, rgba(0, 0, 0, 0) 32%, rgba(0, 0, 0, 0.62) 100%);
    }

    .banner-info {
      position: absolute;
      left: 32rpx;
      right: 32rpx;
      bottom: 30rpx;

      .banner-title {
        display: block;
        font-size: $font-xl;
        color: $text-white;
        font-weight: $font-weight-bold;
        line-height: 1.35;
        text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.2);
      }

      .banner-tags {
        display: flex;
        gap: 12rpx;
        margin-top: 10rpx;
      }

      .banner-tag {
        font-size: $font-xs;
        color: $text-white;
        background-color: rgba(255, 140, 66, 0.88);
        padding: 4rpx 18rpx;
        border-radius: $radius-round;
      }
    }
  }

  /* 自定义指示器：圆点 + 当前胶囊拉长 */
  .banner-dots {
    position: absolute;
    left: 0;
    right: 0;
    bottom: 16rpx;
    display: flex;
    justify-content: center;
    gap: 10rpx;
    pointer-events: none;

    .banner-dot {
      width: 12rpx;
      height: 12rpx;
      border-radius: $radius-round;
      background-color: rgba(255, 255, 255, 0.62);
      transition: all 0.3s ease;

      &.active {
        width: 40rpx;
        background-color: #ffffff;
      }
    }
  }
}

.banner-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20rpx;
  margin: 0 24rpx;
  height: 300rpx;
  border-radius: $radius-lg;
  background: $gradient-card;

  .placeholder-emoji {
    font-size: 56rpx;
  }

  .placeholder-text {
    font-size: $font-md;
    color: $primary-dark;
    opacity: 0.65;
  }
}

/* ========== 内容区 ========== */
.content-area {
  padding-top: 24rpx;
}

/* ===== 分段 Tab ===== */
.feed-tabs {
  display: flex;
  align-items: center;
  margin: 0 24rpx 24rpx;
  padding: 8rpx;
  border-radius: $radius-round;
  background-color: rgba(255, 255, 255, 0.92);
  box-shadow: $shadow-sm;

  .feed-tab {
    flex: 1;
    height: 72rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: $radius-round;
    transition: all 0.3s ease;

    .tab-text {
      font-size: $font-md;
      color: $text-secondary;
      font-weight: $font-weight-medium;
      transition: color 0.3s ease;
    }

    &.active {
      background: $gradient-primary;
      box-shadow: $shadow-primary;

      .tab-text {
        color: $text-white;
        font-weight: $font-weight-bold;
      }
    }
  }
}

/* ===== 动态列表 ===== */
.feed-list {
  display: flex;
  flex-direction: column;
}

/* ===== 首次加载 ===== */
.loading-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 160rpx 0 100rpx;

  .loading-paw {
    width: 96rpx;
    height: 96rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 56rpx;
    animation: pet-spin 1.2s linear infinite;
  }

  .loading-text {
    margin-top: 28rpx;
    font-size: $font-sm;
    color: $text-hint;
    animation: pet-breath 1.6s ease-in-out infinite;
  }
}

/* ===== 加载更多 ===== */
.load-more {
  display: flex;
  justify-content: center;
  padding: 20rpx 0 40rpx;

  .load-more-state {
    display: flex;
    align-items: center;
    gap: 12rpx;

    .load-paw-sm {
      font-size: $font-md;
      animation: pet-spin 1.2s linear infinite;
      display: inline-flex;
    }

    .load-text {
      font-size: $font-sm;
      color: $text-hint;
    }
  }

  .load-text.nomore-text {
    font-size: $font-sm;
    color: $text-hint;
  }

  .load-more-btn {
    padding: 14rpx 52rpx;
    border-radius: $radius-round;
    background: $primary-lighter;
    transition: all 0.3s ease;

    &:active {
      transform: scale(0.96);
      opacity: 0.8;
    }

    .load-more-text {
      font-size: $font-sm;
      color: $primary;
      font-weight: $font-weight-medium;
    }
  }
}

/* ===== 空状态 ===== */
.empty-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 72rpx 160rpx;

  .empty-art {
    width: 180rpx;
    height: 180rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    background: $gradient-card;
    font-size: 88rpx;
    box-shadow: $shadow-md;
    animation: pet-float 3s ease-in-out infinite;
  }

  .empty-title {
    margin-top: 36rpx;
    font-size: $font-lg;
    font-weight: $font-weight-bold;
    color: $text-primary;
  }

  .empty-desc {
    margin-top: 14rpx;
    font-size: $font-sm;
    color: $text-hint;
    text-align: center;
    line-height: 1.6;
  }

  .empty-btn {
    margin-top: 44rpx;
    padding: 20rpx 56rpx;
    border-radius: $radius-round;
    background: $gradient-primary;
    box-shadow: $shadow-primary;

    .empty-btn-text {
      font-size: $font-md;
      color: $text-white;
      font-weight: $font-weight-medium;
    }
  }
}

/* ===== 悬浮发布按钮 ===== */
.fab-btn {
  position: fixed;
  right: 40rpx;
  bottom: calc(env(safe-area-inset-bottom) + 72rpx);
  width: 108rpx;
  height: 108rpx;
  background: $gradient-primary;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: $shadow-primary;
  z-index: 300;
  animation: pet-pulse 2.4s ease-out infinite;

  &:active {
    transform: scale(0.9);
  }
}
</style>
