<template>
  <view class="home-page">
    <!-- ========== 毛玻璃吸顶导航栏 ========== -->
    <view class="glass-nav" :style="{ paddingTop: navBarHeight + 'px' }">
      <view class="nav-inner">
        <view class="brand" @click="goTop">
          <view class="brand-logo">🐾</view>
          <text class="brand-name">宠迹</text>
        </view>
        <view class="search-capsule" @click="goSearch">
          <Icon name="search" :size="16" color="#FF8C42" />
          <text class="search-placeholder">搜索养宠知识...</text>
        </view>
      </view>
    </view>

    <!-- ========== 顶部渐变区：轮播图 ========== -->
    <view class="top-hero" :style="{ paddingTop: contentPad + 'px' }">
      <view class="hero-slogan">
        <text class="slogan-text">科学养宠，从记录每一天开始</text>
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
            v-for="banner in bannerList"
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
        <view class="banner-dots" v-if="bannerList.length > 1">
          <view
            v-for="(banner, idx) in bannerList"
            :key="'dot-' + banner.id"
            class="banner-dot"
            :class="{ active: idx === currentBanner }"
          />
        </view>
      </view>

      <view class="banner-placeholder" v-else>
        <text class="placeholder-emoji">🐾</text>
        <text class="placeholder-text">更多精彩内容即将上线~</text>
      </view>
    </view>

    <!-- ========== 内容区 ========== -->
    <view class="content-area">
      <!-- 快捷工具 -->
      <view class="tool-card">
        <view
          class="tool-item"
          v-for="tool in toolList"
          :key="tool.key"
          @click="handleToolClick(tool)"
        >
          <view class="tool-icon" :style="{ background: tool.bg }">
            <Icon :name="tool.icon" :size="22" :color="tool.color" />
          </view>
          <text class="tool-name">{{ tool.name }}</text>
        </view>
      </view>

      <!-- 养宠知识 -->
      <view class="section-head">
        <view class="section-title-wrap">
          <text class="section-title">养宠知识</text>
          <text class="section-sub">{{ articleSectionSub }}</text>
        </view>
        <view class="section-more pet-press" @click="goArticleList">
          <text class="more-text">查看更多</text>
          <Icon name="chevron_right" :size="14" color="#FF8C42" />
        </view>
      </view>

      <!-- 首次加载 -->
      <view class="loading-wrap" v-if="articleLoading && !articleList.length">
        <view class="loading-paw">
          <Icon name="file_text" :size="26" color="#FF8C42" />
        </view>
        <text class="loading-text">正在加载养宠知识...</text>
      </view>

      <!-- 文章卡片 -->
      <view class="article-list" v-else-if="articleList.length">
        <view
          class="article-card pet-press"
          v-for="item in articleList"
          :key="item.id"
          @click="goArticleDetail(item.id)"
        >
          <image
            v-if="item.coverImage"
            class="article-thumb"
            :src="fullImageUrl(item.coverImage)"
            mode="aspectFill"
          />
          <view v-else class="article-thumb article-thumb--empty">
            <Icon name="file_text" :size="26" color="#FFC8A2" />
          </view>

          <view class="article-info">
            <text class="article-title pet-line2">{{ item.title }}</text>
            <text class="article-summary pet-line2" v-if="item.summary">{{ item.summary }}</text>
            <view class="article-meta">
              <text v-if="item.category" class="meta-chip">{{ item.category }}</text>
              <text v-if="petLabel(item)" class="meta-chip meta-chip--pet">{{ petLabel(item) }}</text>
              <text class="meta-time">{{ formatDateTime(item.createTime) }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 加载更多 / 到底提示 -->
      <view class="list-foot" v-if="articleList.length">
        <view class="foot-loading" v-if="articleLoadingMore">
          <Icon name="reload" :size="15" color="#FF8C42" />
          <text class="foot-text">正在加载更多...</text>
        </view>
        <view class="foot-end-wrap" v-else-if="articleFinished">
          <text class="foot-text foot-end">— 到底啦 —</text>
          <view class="foot-more pet-press" @click="goArticleList">
            <text class="foot-more-text">查看更多养宠知识</text>
            <Icon name="chevron_right" :size="14" color="#FF8C42" />
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view class="empty-wrap" v-else>
        <view class="empty-art">
          <Icon name="file_text" :size="44" color="#FFB07A" />
        </view>
        <text class="empty-title">知识文章正在筹备中</text>
        <text class="empty-desc">我们会尽快为你准备实用的养宠内容，先去记录爱宠的档案吧~</text>
        <view class="empty-btn pet-press" @click="goPetList">
          <text class="empty-btn-text">去添加爱宠</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onShow, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app';
import Icon from '@/components/Icon.vue';
import { getArticleList } from '@/api/article.js';
import { getBannerList } from '@/api/banner.js';
import { showToast, formatDateTime, fullImageUrl } from '@/utils/index.js';
import { requireLogin } from '@/utils/auth.js';
import { useUserStore } from '@/store/user.js';
import { usePetStore } from '@/store/pet.js';
import { features } from '@/config/features.js';
import { isRecommendablePetType, petTypeLabel } from '@/config/petTypes.js';

const userStore = useUserStore();
const petStore = usePetStore();

/** 导航栏与状态栏相关尺寸（px） */
const navBarHeight = ref(44);
const contentPad = computed(() => navBarHeight.value + uni.upx2px(96) + 4);

// #ifdef MP-WEIXIN
try {
  const menuRect = uni.getMenuButtonBoundingClientRect();
  navBarHeight.value = menuRect.bottom + 8;
} catch (e) {}
// #endif

/* ==================== 轮播图 ==================== */
const bannerList = ref([]);
const currentBanner = ref(0);

/** 回到页面顶部 */
const goTop = () => {
  uni.pageScrollTo({ scrollTop: 0, duration: 300 });
};

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

/**
 * 点击轮播图：根据 jumpType 跳转
 * none-不跳转 article-知识文章 product-商品详情 url-外部链接 miniapp-小程序页面
 */
const onBannerClick = (banner) => {
  if (!banner) return;
  const { jumpType, jumpTarget } = banner;
  switch (jumpType) {
    case 'none':
    case undefined:
    case null:
      break;
    case 'article':
      if (jumpTarget) uni.navigateTo({ url: `/pages/article/detail?id=${jumpTarget}` });
      break;
    case 'product':
      uni.navigateTo({ url: `/pages/shop/detail?id=${jumpTarget}` });
      break;
    case 'miniapp':
      if (jumpTarget) uni.navigateTo({ url: jumpTarget });
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

/* ==================== 快捷工具 ==================== */
const toolList = computed(() => {
  const list = [
    { key: 'pets', name: '宠物档案', icon: 'pet', color: '#FF8C42', bg: '#FFF0E6', path: '/pages/pet/list', tab: true, login: true },
    { key: 'vaccine', name: '疫苗提醒', icon: 'vaccine', color: '#4CAF7D', bg: '#E8F7EB', path: '/pages/pet/health', login: true },
    { key: 'weight', name: '体重记录', icon: 'weight', color: '#3D9BE9', bg: '#E5F4FF', path: '/pages/pet/weight', login: true },
    { key: 'shop', name: '积分商城', icon: 'gift', color: '#E8A33D', bg: '#FFF7DE', path: '/pages/shop/index', tab: true },
  ];
  // 养宠助手受功能开关控制，提审期间关闭入口
  if (features.adviserEnabled !== false) {
    list.splice(3, 0, {
      key: 'chat', name: 'AI助手', icon: 'chat', color: '#8A7BE0', bg: '#F0EBFF',
      path: '/pages/chat/index', login: true,
    });
  }
  return list;
});

const handleToolClick = async (tool) => {
  if (tool.login && !(await requireLogin(`使用「${tool.name}」`))) return;
  if (tool.tab) {
    uni.switchTab({ url: tool.path });
    return;
  }
  uni.navigateTo({ url: tool.path });
};

/* ==================== 养宠知识（兴趣推荐 + 触底加载） ==================== */
/** 首页每次加载的条数 */
const ARTICLE_PAGE_SIZE = 5;

const articleList = ref([]);
const articleLoading = ref(false);      // 首次加载 / 下拉刷新

/** 适用宠物文案（文章按宠物种类分类，与主题分类是两个维度） */
const petLabel = (item) => petTypeLabel(item?.petType, '');
const articleLoadingMore = ref(false);  // 触底追加加载
const articleFinished = ref(false);     // 是否已无更多数据
const articlePageNum = ref(1);
const articlePetType = ref('');         // 本次推荐命中的宠物类型（cat/dog），空串表示不做个性化

/**
 * 解析用于兴趣推荐的宠物类型
 *
 * 取用户第一只猫或狗的类型；未登录、没有宠物、或宠物类型为"其他"时返回空串，
 * 由后端按平台默认排序返回（即不做个性化的随机推荐）。
 */
const resolveInterestPetType = async () => {
  if (!userStore.isLogin) return '';
  try {
    if (!petStore.petList.length) {
      await petStore.fetchPetList();
    }
  } catch (e) {
    return '';
  }
  const hit = (petStore.petList || []).find((p) => isRecommendablePetType(p.petType));
  return hit ? hit.petType : '';
};

/** 拉取第一页文章 */
const fetchArticleList = async () => {
  articleLoading.value = true;
  articleLoadingMore.value = false;
  articleFinished.value = false;
  articlePageNum.value = 1;
  try {
    articlePetType.value = await resolveInterestPetType();
    const res = await getArticleList({
      pageNum: 1,
      pageSize: ARTICLE_PAGE_SIZE,
      petType: articlePetType.value || undefined,
    });
    const rows = res.rows || [];
    articleList.value = rows;
    // 到底判定以接口返回的 total 为准：
    // RuoYi 的 PageHelper 默认开启"分页参数合理化"，pageNum 超过总页数时会回退到
    // 最后一页、再次返回同一批数据，因此只看"本页不足一页"永远触发不了到底。
    const total = Number(res.total);
    if (Number.isFinite(total) && total > 0) {
      articleFinished.value = articleList.value.length >= total;
    } else {
      articleFinished.value = rows.length < ARTICLE_PAGE_SIZE;
    }
  } catch (err) {
    console.error('获取养宠知识失败:', err);
    articleList.value = [];
    articleFinished.value = true;
  } finally {
    articleLoading.value = false;
  }
};

/** 触底加载下一页 */
const loadMoreArticles = async () => {
  if (articleLoading.value || articleLoadingMore.value || articleFinished.value) return;
  articleLoadingMore.value = true;
  try {
    const nextPage = articlePageNum.value + 1;
    const res = await getArticleList({
      pageNum: nextPage,
      pageSize: ARTICLE_PAGE_SIZE,
      petType: articlePetType.value || undefined,
    });
    const rows = res.rows || [];
    if (rows.length) {
      // 按 id 去重后再追加：后端分页合理化可能把最后一页重复返回，前端再兜一层
      const existing = new Set(articleList.value.map((item) => item.id));
      articleList.value = articleList.value.concat(rows.filter((item) => !existing.has(item.id)));
      articlePageNum.value = nextPage;
    }
    const total = Number(res.total);
    if (Number.isFinite(total) && total > 0) {
      articleFinished.value = articleList.value.length >= total;
    } else {
      articleFinished.value = rows.length < ARTICLE_PAGE_SIZE;
    }
  } catch (err) {
    console.error('加载更多养宠知识失败:', err);
    // 出错时停止继续加载，避免用户一直下拉空转
    articleFinished.value = true;
  } finally {
    articleLoadingMore.value = false;
  }
};

/** 区块副标题：命中兴趣时提示专属推荐 */
const articleSectionSub = computed(() => {
  if (articlePetType.value === 'cat') return '猫咪专属 · 只读浏览';
  if (articlePetType.value === 'dog') return '狗狗专属 · 只读浏览';
  return '平台精选 · 只读浏览';
});

/** 查看更多 → 养宠知识列表页 */
const goArticleList = () => uni.navigateTo({ url: '/pages/article/list' });
const goArticleDetail = (id) => uni.navigateTo({ url: `/pages/article/detail?id=${id}` });
/** 首页搜索框 → 搜索页（保留搜索历史） */
const goSearch = () => uni.navigateTo({ url: '/pages/search/index' });
const goPetList = () => uni.switchTab({ url: '/pages/pet/list' });

/* ==================== 生命周期 ==================== */
let articleLoadedOnce = false;
let lastLoginState = null;

onShow(async () => {
  if (userStore.isLogin && !userStore.userInfo) {
    try { await userStore.fetchUserInfo(); } catch (e) {}
  }
  fetchBannerList();
  // 首次进入、或登录状态发生变化（宠物数据可能随之变化）时重新拉取推荐，
  // 从文章详情返回首页则保留已加载的进度，不回到第一页
  if (!articleLoadedOnce || lastLoginState !== userStore.isLogin) {
    articleLoadedOnce = true;
    lastLoginState = userStore.isLogin;
    fetchArticleList();
  }
});

/** 触底加载更多养宠知识 */
onReachBottom(() => {
  loadMoreArticles();
});

onPullDownRefresh(async () => {
  try {
    await Promise.all([fetchBannerList(), fetchArticleList()]);
  } finally {
    uni.stopPullDownRefresh();
  }
});
</script>

<style lang="scss" scoped>
.home-page {
  min-height: 100vh;
  background-color: $bg-page;
  padding-bottom: calc(env(safe-area-inset-bottom) + 60rpx);
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
  padding: 24rpx 0 0;
}

/* ===== 快捷工具 ===== */
.tool-card {
  display: flex;
  align-items: flex-start;
  margin: 0 24rpx 28rpx;
  padding: 32rpx 12rpx 24rpx;
  border-radius: $radius-lg;
  background-color: #fff;
  box-shadow: $shadow-card;

  .tool-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 14rpx;

    &:active {
      opacity: 0.72;
    }
  }

  .tool-icon {
    width: 88rpx;
    height: 88rpx;
    border-radius: 28rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .tool-name {
    font-size: $font-xs;
    color: $text-secondary;
    font-weight: $font-weight-medium;
  }
}

/* ===== 区块标题 ===== */
.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 0 32rpx 20rpx;

  .section-title-wrap {
    display: flex;
    align-items: baseline;
    gap: 14rpx;

    .section-title {
      font-size: 36rpx;
      font-weight: $font-weight-bold;
      color: $text-primary;
    }

    .section-sub {
      font-size: $font-xs;
      color: $text-hint;
    }
  }

  .section-more {
    display: flex;
    align-items: center;
    gap: 4rpx;

    .more-text {
      font-size: $font-sm;
      color: $primary;
    }
  }
}

/* ===== 文章卡片 ===== */
.article-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  padding: 0 24rpx;
}

.article-card {
  display: flex;
  gap: 22rpx;
  padding: 24rpx;
  border-radius: $radius-lg;
  background-color: #fff;
  box-shadow: $shadow-card;
  animation: pet-fadeInUp 0.36s ease both;

  .article-thumb {
    width: 200rpx;
    height: 148rpx;
    border-radius: $radius-md;
    flex-shrink: 0;
    background-color: $bg-input;

    &--empty {
      display: flex;
      align-items: center;
      justify-content: center;
      background: $gradient-card;
    }
  }

  .article-info {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;

    .article-title {
      font-size: $font-md;
      font-weight: $font-weight-bold;
      color: $text-primary;
      line-height: 1.45;
    }

    .article-summary {
      margin-top: 10rpx;
      font-size: $font-xs;
      color: $text-secondary;
      line-height: 1.6;
    }

    .article-meta {
      display: flex;
      align-items: center;
      gap: 14rpx;
      margin-top: auto;
      padding-top: 14rpx;

      .meta-chip {
        font-size: $font-xs;
        color: $primary-dark;
        background: $primary-lighter;
        padding: 2rpx 14rpx;
        border-radius: $radius-round;

        /* 适用宠物：与主题分类（喂养/健康）区分开，用另一套配色 */
        &.meta-chip--pet {
          color: #4F9E62;
          background: #E8F6E9;
        }
      }

      .meta-time {
        font-size: $font-xs;
        color: $text-hint;
      }
    }
  }
}

/* ===== 加载更多 / 到底提示 ===== */
.list-foot {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 32rpx 24rpx 8rpx;

  .foot-loading {
    display: flex;
    align-items: center;
    gap: 10rpx;
    animation: pet-breath 1.6s ease-in-out infinite;
  }

  .foot-text {
    font-size: $font-xs;
    color: $text-hint;
  }

  .foot-end-wrap {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 18rpx;
  }

  .foot-end {
    color: $text-placeholder;
  }

  .foot-more {
    display: flex;
    align-items: center;
    gap: 4rpx;
    padding: 12rpx 32rpx;
    border-radius: $radius-round;
    background-color: #fff;
    box-shadow: $shadow-sm;

    .foot-more-text {
      font-size: $font-sm;
      color: $primary;
    }
  }
}

/* ===== 首次加载 ===== */
.loading-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 90rpx 0 70rpx;

  .loading-paw {
    width: 80rpx;
    height: 80rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    animation: pet-breath 1.6s ease-in-out infinite;
  }

  .loading-text {
    margin-top: 22rpx;
    font-size: $font-sm;
    color: $text-hint;
  }
}

/* ===== 空状态 ===== */
.empty-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 90rpx 72rpx 120rpx;

  .empty-art {
    width: 180rpx;
    height: 180rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    background: $gradient-card;
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
</style>
