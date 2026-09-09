<template>
  <view class="shop-page">
    <!-- ========== 顶部渐变区 ========== -->
    <view class="top-area">
      <!-- 装饰爪印 -->
      <text class="deco-paw paw-1">🐾</text>
      <text class="deco-paw paw-2">🐾</text>
      <text class="deco-paw paw-3">🦴</text>

      <!-- 自定义导航 -->
      <view class="custom-nav" :style="{ paddingTop: navBarHeight + 'px' }">
        <view class="nav-content">
          <view class="nav-title-wrap">
            <text class="nav-title">积分商城</text>
          </view>
          <view class="nav-right">
            <view class="nav-btn pet-press" @click="handleSearch">
              <Icon name="search" :size="20" color="#fff" />
            </view>
          </view>
        </view>
      </view>

      <!-- 积分余额卡片 -->
      <view class="points-card">
        <view class="points-left">
          <text class="points-label">我的积分</text>
          <view class="points-value-row">
            <text class="points-value">{{ userPoints }}</text>
            <view class="coin-paw" @click="goPointsDetail">
              <text class="coin-emoji">🪙</text>
              <text class="coin-text">明细</text>
            </view>
          </view>
          <text class="points-sub">每 500 积分即可开启一次心动兑换 🎁</text>
        </view>
        <view class="points-right" @click="goPointsDetail">
          <view class="paw-badge">
            <text class="paw-badge-emoji">🐾</text>
          </view>
        </view>
        <text class="points-watermark">🐾</text>
      </view>
    </view>

    <!-- ========== 分类图标导航（横向滚动 + 渐隐边缘） ========== -->
    <view class="category-wrap">
      <scroll-view
        scroll-x
        class="category-scroll pet-scroll-x"
        :show-scrollbar="false"
        :scroll-into-view="scrollIntoId"
        scroll-with-animation
      >
        <view
          class="category-item"
          :id="'cat-' + cat.id"
          v-for="(cat, idx) in categories"
          :key="cat.id"
          @click="changeCategory(cat)"
        >
          <view class="cat-icon" :class="{ active: currentCategory === cat.id }">
            <text class="cat-emoji">{{ categoryIcons[cat.id] || DEFAULT_ICONS[idx % DEFAULT_ICONS.length] }}</text>
          </view>
          <text class="cat-name" :class="{ active: currentCategory === cat.id }">{{ cat.categoryName }}</text>
        </view>
      </scroll-view>
      <view class="edge-fade edge-left" />
      <view class="edge-fade edge-right" />
    </view>

    <!-- 搜索关键词结果条 -->
    <view class="search-summary" v-if="keyword && !searchOpen">
      <text class="summary-label">“{{ keyword }}”</text>
      <text class="summary-text">的相关商品</text>
      <view class="summary-clear pet-press" @click="clearSearch">
        <text>清除</text>
      </view>
    </view>

    <!-- ========== 商品列表区 ========== -->
    <scroll-view
      class="product-scroll"
      scroll-y
      :refresher-enabled="true"
      :refresher-triggered="isRefreshing"
      @refresherrefresh="onPullRefresh"
      @scrolltolower="loadMore"
      :show-scrollbar="false"
    >
      <!-- 首次加载骨架屏 -->
      <LoadingState
        v-if="loading && productList.length === 0"
        mode="skeleton"
        type="product"
        :count="6"
      />

      <!-- 商品列表（双列网格） -->
      <view v-else class="product-grid">
        <ProductCard
          v-for="product in productList"
          :key="product.id"
          :product="product"
          @click="goDetail"
        />
      </view>

      <!-- 加载更多 -->
      <view v-if="productList.length > 0" class="load-more">
        <text v-if="loadStatus === 'loading'" class="load-text">加载中...</text>
        <text v-else-if="loadStatus === 'nomore'" class="load-text nomore">─ 已经到底啦 ─</text>
      </view>

      <!-- 空状态 -->
      <EmptyState
        v-if="!loading && productList.length === 0"
        :text="keyword ? '没有找到相关商品' : '商城补货中，敬请期待~'"
        :showButton="!!keyword"
        buttonText="清除搜索"
        @click="clearSearch"
      />

      <view class="bottom-safe" />
    </scroll-view>

    <!-- ========== 搜索弹层 ========== -->
    <view v-if="searchOpen" class="search-overlay" @click="cancelSearch">
      <view class="search-panel" :style="{ paddingTop: navBarHeight + 'px' }" @click.stop>
        <view class="search-bar">
          <view class="search-input-wrap">
            <Icon name="search" :size="17" color="#FF8C42" />
            <input
              v-model="searchDraft"
              class="search-input"
              focus
              placeholder="搜索商品名称，如：猫粮"
              placeholder-class="search-placeholder"
              confirm-type="search"
              @confirm="submitSearch"
            />
            <view v-if="searchDraft" class="draft-clear" @click.stop="searchDraft = ''">
              <Icon name="close" :size="14" color="#B0B2BE" />
            </view>
          </view>
          <view class="search-btn pet-press" @click="submitSearch">
            <text>搜索</text>
          </view>
          <view class="search-cancel pet-press" @click="cancelSearch">
            <text>取消</text>
          </view>
        </view>

        <!-- 热门搜索 -->
        <view class="hot-search">
          <view class="hot-title">
            <text class="hot-emoji">🔥</text>
            <text class="hot-text">大家都在搜</text>
          </view>
          <view class="hot-tags">
            <view
              class="hot-tag pet-press"
              v-for="(word, idx) in hotWords"
              :key="word"
              :class="'tone-' + (idx % 4)"
              @click="useHotWord(word)"
            >
              <text>{{ word }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import ProductCard from '@/components/ProductCard.vue';
import EmptyState from '@/components/EmptyState.vue';
import LoadingState from '@/components/LoadingState.vue';
import Icon from '@/components/Icon.vue';
import { getCategories, getProductList } from '@/api/shop.js';
import { useUserStore } from '@/store/user.js';

const userStore = useUserStore();
const userPoints = computed(() => userStore.userInfo?.points || 0);

/** 分类图标兜底序列（emoji） */
const DEFAULT_ICONS = ['🐾', '🍖', '🍗', '🧸', '🛁', '👕', '🏠', '🎾', '🦴', '💊', '🧶'];

const navBarHeight = ref(44);
// #ifdef MP-WEIXIN
try {
  const menuRect = uni.getMenuButtonBoundingClientRect();
  navBarHeight.value = menuRect.bottom + 4;
} catch (e) {}
// #endif

const keyword = ref('');
const searchOpen = ref(false);
const searchDraft = ref('');
const hotWords = ['猫粮', '狗粮', '零食', '玩具', '猫砂', '牵引绳'];
const categories = ref([{ id: '', categoryName: '全部' }]);
const currentCategory = ref('');
const scrollIntoId = ref('');
const productList = ref([]);
const loading = ref(false);
const isRefreshing = ref(false);
const loadStatus = ref('loadmore');
const pageParams = ref({ pageNum: 1, pageSize: 10 });

/** 已按分类名/位置分配好的 emoji */
const categoryIcons = ref({});

const assignCategoryIcons = (cats) => {
  const map = {};
  cats.forEach((cat, idx) => {
    // 常见品类关键词 → emoji
    const kw = cat.categoryName || '';
    const icon =
      (kw.includes('粮') || kw.includes('主食') || kw.includes('零食')) ? '🍖' :
      (kw.includes('猫') || kw.includes('狗狗') || kw.includes('狗')) ? '🐶' :
      (kw.includes('玩具')) ? '🧸' :
      (kw.includes('洗') || kw.includes('清洁')) ? '🛁' :
      (kw.includes('服饰') || kw.includes('衣')) ? '👕' :
      (kw.includes('窝') || kw.includes('居') || kw.includes('笼')) ? '🏠' :
      (kw.includes('保健') || kw.includes('药品') || kw.includes('驱虫')) ? '💊' :
      DEFAULT_ICONS[idx % DEFAULT_ICONS.length];
    map[cat.id] = icon;
  });
  categoryIcons.value = map;
};

const fetchCategories = async () => {
  try {
    const res = await getCategories();
    const tree = res.data || [];
    const flat = [{ id: '', categoryName: '全部' }];
    tree.forEach((parent) => {
      flat.push({ id: parent.id, categoryName: parent.categoryName });
      if (parent.children && parent.children.length) {
        parent.children.forEach((child) => {
          flat.push({ id: child.id, categoryName: child.categoryName });
        });
      }
    });
    categories.value = flat;
    assignCategoryIcons(flat);
  } catch {}
};

const fetchProducts = async (isRefresh = false) => {
  if (isRefresh) pageParams.value.pageNum = 1;
  loading.value = true;
  loadStatus.value = 'loading';

  try {
    const params = {
      pageNum: pageParams.value.pageNum,
      pageSize: pageParams.value.pageSize,
    };
    if (currentCategory.value) params.categoryId = currentCategory.value;
    if (keyword.value) params.keyword = keyword.value;

    const res = await getProductList(params);
    const rows = res.rows || [];
    if (isRefresh) {
      productList.value = rows;
    } else {
      productList.value = [...productList.value, ...rows];
    }
    loadStatus.value = rows.length < pageParams.value.pageSize ? 'nomore' : 'loadmore';
  } catch {
    loadStatus.value = 'loadmore';
  } finally {
    loading.value = false;
    isRefreshing.value = false;
  }
};

const changeCategory = (cat) => {
  if (currentCategory.value === cat.id) return;
  currentCategory.value = cat.id;
  scrollIntoId.value = `cat-${cat.id}`;
  fetchProducts(true);
};

/** 打开搜索框，回填当前关键词 */
const handleSearch = () => {
  searchDraft.value = keyword.value;
  searchOpen.value = true;
};

/** 执行搜索：回车 / 点击搜索按钮 */
const submitSearch = () => {
  keyword.value = searchDraft.value.trim();
  searchOpen.value = false;
  uni.hideKeyboard();
  fetchProducts(true);
};

/** 取消：关闭搜索层，保留当前列表结果 */
const cancelSearch = () => {
  searchOpen.value = false;
  uni.hideKeyboard();
};

/** 使用热门关键词直接搜索 */
const useHotWord = (word) => {
  searchDraft.value = word;
  submitSearch();
};

/** 清除搜索条件并回到全部商品 */
const clearSearch = () => {
  keyword.value = '';
  searchDraft.value = '';
  fetchProducts(true);
};
const onPullRefresh = () => { isRefreshing.value = true; fetchProducts(true); };
const loadMore = () => {
  if (loadStatus.value === 'nomore' || loadStatus.value === 'loading') return;
  pageParams.value.pageNum++;
  fetchProducts(false);
};
const goDetail = (id) => uni.navigateTo({ url: `/pages/shop/detail?id=${id}` });
const goPointsDetail = () => {
  uni.navigateTo({ url: '/pages/mine/points' });
};

onMounted(() => {
  fetchCategories();
  fetchProducts(true);
});
</script>

<style lang="scss" scoped>
.shop-page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: $bg-page;
}

/* ========== 顶部渐变区 ========== */
.top-area {
  position: relative;
  overflow: hidden;
  background: $gradient-primary;
  padding-bottom: 46rpx;

  .deco-paw {
    position: absolute;
    opacity: 0.12;
    font-size: 96rpx;
  }

  .paw-1 { top: 110rpx; right: -18rpx; transform: rotate(18deg); }
  .paw-2 { top: 250rpx; left: -28rpx; transform: rotate(-22deg); font-size: 120rpx; }
  .paw-3 { bottom: 24rpx; right: 100rpx; font-size: 56rpx; transform: rotate(8deg); }
}

/* ===== 自定义导航 ===== */
.custom-nav {
  position: relative;
  z-index: 2;

  .nav-content {
    display: flex;
    align-items: center;
    padding: 8rpx 28rpx 20rpx;
    position: relative;
  }

  .nav-title-wrap {
    position: absolute;
    left: 0;
    right: 0;
    top: 8rpx;
    text-align: center;
  }

  .nav-title {
    font-size: $font-xl;
    font-weight: $font-weight-bold;
    color: $text-white;
    text-shadow: 0 2rpx 8rpx rgba(224, 86, 24, 0.25);
  }

  .nav-right {
    margin-left: auto;
    position: relative;
    z-index: 1;
  }

  .nav-btn {
    width: 64rpx;
    height: 64rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: rgba(255, 255, 255, 0.22);
    border: 1rpx solid rgba(255, 255, 255, 0.25);
    border-radius: 50%;
    backdrop-filter: blur(8rpx);
    -webkit-backdrop-filter: blur(8rpx);
  }
}

/* ===== 积分余额卡片（玻璃拟态） ===== */
.points-card {
  position: relative;
  margin: 0 24rpx;
  padding: 32rpx 32rpx 30rpx;
  border-radius: $radius-lg;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.3) 0%, rgba(255, 255, 255, 0.12) 100%);
  border: 1rpx solid rgba(255, 255, 255, 0.32);
  box-shadow: 0 12rpx 32rpx rgba(216, 75, 16, 0.24);
  backdrop-filter: blur(16rpx);
  -webkit-backdrop-filter: blur(16rpx);
  overflow: hidden;

  .points-left {
    position: relative;
    z-index: 2;

    .points-label {
      font-size: $font-sm;
      color: rgba(255, 255, 255, 0.88);
    }

    .points-value-row {
      display: flex;
      align-items: center;
      margin: 14rpx 0;

      .points-value {
        font-size: 76rpx;
        font-weight: $font-weight-bold;
        color: #fff;
        line-height: 1;
        text-shadow: 0 4rpx 12rpx rgba(185, 61, 9, 0.22);
      }

      .coin-paw {
        display: flex;
        align-items: center;
        gap: 8rpx;
        margin-left: 24rpx;
        padding: 8rpx 22rpx;
        background: rgba(255, 255, 255, 0.24);
        border: 1rpx solid rgba(255, 255, 255, 0.3);
        border-radius: $radius-round;

        .coin-emoji {
          font-size: $font-md;
          animation: pet-bounce 2.6s ease infinite;
        }

        .coin-text {
          font-size: $font-xs;
          color: #fff;
        }
      }
    }

    .points-sub {
      font-size: $font-xs;
      color: rgba(255, 255, 255, 0.78);
    }
  }

  .points-right {
    position: absolute;
    right: 28rpx;
    top: 50%;
    transform: translateY(-50%);
    z-index: 2;

    .paw-badge {
      width: 120rpx;
      height: 120rpx;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.22);
      border: 2rpx dashed rgba(255, 255, 255, 0.55);
      display: flex;
      align-items: center;
      justify-content: center;
      animation: pet-float 3.4s ease-in-out infinite;

      .paw-badge-emoji {
        font-size: 64rpx;
        filter: drop-shadow(0 4rpx 8rpx rgba(181, 58, 8, 0.2));
      }
    }
  }

  .points-watermark {
    position: absolute;
    left: 240rpx;
    bottom: -40rpx;
    font-size: 200rpx;
    opacity: 0.08;
    transform: rotate(-14deg);
  }
}

/* ========== 分类图标导航 ========== */
.category-wrap {
  position: relative;
  margin-top: -28rpx;
  background: $bg-page;
  padding-top: 40rpx;
  border-radius: 36rpx 36rpx 0 0;

  .category-scroll {
    width: 100%;
    box-sizing: border-box;
    padding: 0 16rpx;
    white-space: nowrap;
  }

  .category-item {
    display: inline-flex;
    flex-direction: column;
    align-items: center;
    width: 132rpx;
    vertical-align: top;
    margin: 0 4rpx;
  }

  .cat-icon {
    width: 92rpx;
    height: 92rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    background: $bg-card;
    box-shadow: $shadow-sm;
    border: 2rpx solid rgba(255, 140, 66, 0.08);
    transition: all 0.3s ease;

    .cat-emoji {
      font-size: 44rpx;
      transition: transform 0.3s ease;
    }

    &.active {
      background: $gradient-primary;
      box-shadow: $shadow-primary;
      border-color: transparent;
      transform: translateY(-4rpx);

      .cat-emoji {
        transform: scale(1.08);
      }
    }
  }

  .cat-name {
    margin-top: 12rpx;
    font-size: $font-xs;
    color: $text-secondary;
    transition: color 0.3s ease;
    max-width: 120rpx;
    @include pet-ellipsis;

    &.active {
      color: $primary-dark;
      font-weight: $font-weight-bold;
    }
  }

  .edge-fade {
    position: absolute;
    top: 0;
    bottom: 0;
    width: 32rpx;
    pointer-events: none;

    &.edge-left {
      left: 0;
      background: linear-gradient(90deg, $bg-page, rgba(248, 249, 252, 0));
    }

    &.edge-right {
      right: 0;
      background: linear-gradient(270deg, $bg-page, rgba(248, 249, 252, 0));
    }
  }
}

/* ========== 商品滚动区 ========== */
.product-scroll {
  flex: 1;
  padding: 24rpx 24rpx 0;
  box-sizing: border-box;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24rpx;
}


/* ===== 加载更多 ===== */
.load-more {
  padding: 36rpx 0 20rpx;
  text-align: center;

  .load-text {
    font-size: $font-sm;
    color: $text-hint;
  }

  .nomore {
    color: $text-placeholder;
  }
}

.bottom-safe {
  height: calc(env(safe-area-inset-bottom) + 40rpx);
}

/* ========== 搜索结果条 ========== */
.search-summary {
  display: flex;
  align-items: center;
  margin: 24rpx 28rpx 0;

  .summary-label {
    font-size: $font-md;
    font-weight: $font-weight-bold;
    color: $primary-dark;
  }

  .summary-text {
    font-size: $font-sm;
    color: $text-hint;
    margin-left: 4rpx;
  }

  .summary-clear {
    margin-left: auto;
    padding: 6rpx 20rpx;
    border-radius: $radius-round;
    background: $primary-lighter;

    text {
      font-size: $font-xs;
      color: $primary;
      font-weight: $font-weight-medium;
    }
  }
}

/* ========== 搜索弹层 ========== */
.search-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 600;
  background: rgba(45, 45, 45, 0.28);
  animation: pet-fadeIn 0.25s ease both;
}

.search-panel {
  background: $gradient-primary;
  border-radius: 0 0 52rpx 52rpx;
  padding: 0 24rpx 44rpx;
  box-shadow: 0 12rpx 40rpx rgba(216, 75, 16, 0.2);

  .search-bar {
    display: flex;
    align-items: center;
    gap: 16rpx;

    .search-input-wrap {
      flex: 1;
      display: flex;
      align-items: center;
      height: 80rpx;
      background: #fff;
      border-radius: $radius-round;
      padding: 0 24rpx;
      gap: 12rpx;
      box-shadow: 0 6rpx 20rpx rgba(190, 70, 18, 0.16);
    }

    .search-input {
      flex: 1;
      height: 76rpx;
      font-size: $font-md;
      color: $text-primary;
    }

    .search-placeholder {
      color: $text-placeholder;
      font-size: $font-md;
    }

    .draft-clear {
      width: 44rpx;
      height: 44rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 50%;
      background: $bg-input;
      flex-shrink: 0;
    }

    .search-btn {
      flex-shrink: 0;
      padding: 16rpx 32rpx;
      border-radius: $radius-round;
      background: #fff;
      box-shadow: 0 6rpx 16rpx rgba(190, 70, 18, 0.18);

      text {
        font-size: $font-md;
        color: $primary-dark;
        font-weight: $font-weight-bold;
      }
    }

    .search-cancel {
      flex-shrink: 0;
      padding: 14rpx 8rpx;

      text {
        font-size: $font-md;
        color: rgba(255, 255, 255, 0.96);
      }
    }
  }

  .hot-search {
    margin-top: 36rpx;
    padding: 26rpx 28rpx;
    border-radius: $radius-lg;
    background: rgba(255, 255, 255, 0.16);
    border: 1rpx solid rgba(255, 255, 255, 0.22);
    backdrop-filter: blur(8rpx);
    -webkit-backdrop-filter: blur(8rpx);

    .hot-title {
      display: flex;
      align-items: center;

      .hot-emoji {
        font-size: $font-md;
        margin-right: 8rpx;
      }

      .hot-text {
        font-size: $font-sm;
        color: #fff;
        font-weight: $font-weight-bold;
      }
    }

    .hot-tags {
      display: flex;
      flex-wrap: wrap;
      gap: 16rpx;
      margin-top: 22rpx;

      .hot-tag {
        padding: 12rpx 28rpx;
        border-radius: $radius-round;
        box-shadow: 0 4rpx 12rpx rgba(190, 70, 18, 0.12);

        text {
          font-size: $font-sm;
          font-weight: $font-weight-medium;
        }

        &.tone-0 { background: #fff; text { color: $primary-dark; } }
        &.tone-1 { background: #E8F3FF; text { color: #3C78C2; } }
        &.tone-2 { background: #E8F7EB; text { color: #3E8F46; } }
        &.tone-3 { background: #FFEBF0; text { color: #C25A76; } }
      }
    }
  }
}
</style>
