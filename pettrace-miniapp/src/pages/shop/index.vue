<template>
  <view class="shop-page">
    <!-- 顶部紫色背景区 -->
    <view class="top-bg">
      <!-- 自定义导航栏 -->
      <view class="custom-nav" :style="{ paddingTop: statusBarHeight + 'px' }">
        <view class="nav-content">
          <view class="nav-title-wrap">
            <text class="nav-title">积分商城</text>
            <text class="nav-subtitle">好物兑换 · 积分当钱花</text>
          </view>
          <view class="nav-right">
            <view class="nav-btn" @click="handleSearch">
              <Icon name="search" :size="20" color="#fff" />
            </view>
          </view>
        </view>
      </view>

      <!-- 积分卡片 -->
      <view class="points-card">
        <view class="points-content">
          <view class="points-left">
            <text class="points-label">我的积分</text>
            <view class="points-value-row">
              <text class="points-value">{{ userPoints }}</text>
              <view class="points-action" @click="goPointsDetail">
                <text class="action-text">积分明细</text>
                <Icon name="arrow_right" :size="14" color="#fff" />
              </view>
            </view>
            <text class="points-sub">距下次兑换还差 {{ needPoints }} 分</text>
          </view>
          <view class="points-right">
            <view class="points-glow-icon">
              <Icon name="medal" :size="36" color="#FFD700" />
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 分类筛选 Tab -->
    <view class="filter-bar">
      <scroll-view
        scroll-x
        class="filter-scroll"
        :show-scrollbar="false"
        :scroll-into-view="scrollIntoId"
        scroll-with-animation
      >
        <view
          class="filter-item"
          :id="'filter-' + cat.id"
          :class="{ active: currentCategory === cat.id }"
          v-for="cat in categories"
          :key="cat.id"
          @click="changeCategory(cat)"
        >
          <text class="filter-text">{{ cat.categoryName }}</text>
        </view>
      </scroll-view>
    </view>

    <!-- 商品列表区 -->
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

      <!-- 商品列表（双列） -->
      <view v-else class="product-grid">
        <view
          class="product-grid-item"
          v-for="product in productList"
          :key="product.id"
          @click="goDetail(product.id)"
        >
          <view class="pgi-image-wrap">
            <image
              v-if="getProductImage(product)"
              class="pgi-image"
              :src="getProductImage(product)"
              mode="aspectFill"
            />
            <view v-else class="pgi-placeholder">
              <Icon name="gift" :size="40" color="#ddd" />
            </view>
            <view v-if="product.stock === 0" class="pgi-soldout">
              <text>售罄</text>
            </view>
          </view>
          <view class="pgi-info">
            <text class="pgi-name">{{ product.productName }}</text>
            <view class="pgi-bottom">
              <view class="pgi-price">
                <text class="pgi-points">{{ product.pointsPrice }}</text>
                <text class="pgi-unit">积分</text>
              </view>
              <view class="pgi-exchange">
                <text>{{ product.totalExchange || 0 }}人兑换</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 加载更多 -->
      <view v-if="productList.length > 0" class="load-more">
        <text v-if="loadStatus === 'loading'" class="load-text">加载中...</text>
        <text v-else-if="loadStatus === 'nomore'" class="load-text">- 已经到底了 -</text>
      </view>

      <!-- 空状态 -->
      <EmptyState
        v-if="!loading && productList.length === 0"
        :text="keyword ? '没有找到相关商品' : '暂无商品'"
        :showButton="!!keyword"
        buttonText="清除搜索"
        @click="clearSearch"
      />

      <view class="bottom-safe" />
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import ProductCard from '@/components/ProductCard.vue';
import EmptyState from '@/components/EmptyState.vue';
import LoadingState from '@/components/LoadingState.vue';
import Icon from '@/components/Icon.vue';
import { getCategories, getProductList } from '@/api/shop.js';
import { fullImageUrl } from '@/utils/index.js';
import { useUserStore } from '@/store/user.js';

const userStore = useUserStore();
const userPoints = computed(() => userStore.userInfo?.points || 0);
const needPoints = computed(() => Math.max(0, 500 - userPoints.value));

const statusBarHeight = ref(20);
// #ifdef MP-WEIXIN
try {
  const sysInfo = uni.getSystemInfoSync();
  statusBarHeight.value = sysInfo.statusBarHeight || 20;
} catch (e) {}
// #endif

const keyword = ref('');
const categories = ref([{ id: '', categoryName: '全部' }]);
const currentCategory = ref('');
const scrollIntoId = ref('');
const productList = ref([]);
const loading = ref(false);
const isRefreshing = ref(false);
const loadStatus = ref('loadmore');
const pageParams = ref({ pageNum: 1, pageSize: 10 });

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
  scrollIntoId.value = `filter-${cat.id}`;
  fetchProducts(true);
};

const handleSearch = () => fetchProducts(true);
const clearSearch = () => { keyword.value = ''; fetchProducts(true); };
const onPullRefresh = () => { isRefreshing.value = true; fetchProducts(true); };
const loadMore = () => {
  if (loadStatus.value === 'nomore' || loadStatus.value === 'loading') return;
  pageParams.value.pageNum++;
  fetchProducts(false);
};
const goDetail = (id) => uni.navigateTo({ url: `/pages/shop/detail?id=${id}` });

const goPointsDetail = () => {
  uni.navigateTo({ url: '/pages/mine/orders' });
};

const getProductImage = (product) => {
  if (!product.productImages) return '';
  try {
    const parsed = typeof product.productImages === 'string'
      ? JSON.parse(product.productImages)
      : product.productImages;
    if (Array.isArray(parsed) && parsed.length > 0) return fullImageUrl(parsed[0]);
    if (typeof parsed === 'string') return fullImageUrl(parsed);
  } catch {
    return fullImageUrl(product.productImages);
  }
  return '';
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
  background-color: #F5F6FA;
}

/* ===== 顶部背景区 ===== */
.top-bg {
  background: linear-gradient(135deg, #FF934F 0%, #FF7E3D 60%, #F4672A 100%);
  padding-bottom: 40rpx;
  overflow: hidden;
}

/* ===== 自定义导航栏 ===== */
.custom-nav {
  .nav-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16rpx 28rpx 24rpx;
  }

  .nav-title-wrap {
    display: flex;
    flex-direction: column;
  }

  .nav-title {
    font-size: 38rpx;
    font-weight: 700;
    color: #fff;
    line-height: 1.3;
  }

  .nav-subtitle {
    font-size: 22rpx;
    color: rgba(255, 255, 255, 0.8);
    margin-top: 4rpx;
  }

  .nav-right {
    display: flex;
    align-items: center;
  }

  .nav-btn {
    width: 64rpx;
    height: 64rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: rgba(255, 255, 255, 0.2);
    border-radius: 50%;

    &:active {
      background-color: rgba(255, 255, 255, 0.3);
    }
  }
}

/* ===== 积分卡片 ===== */
.points-card {
  position: relative;
  margin: 0 24rpx;
  padding: 32rpx 28rpx;
  border-radius: 24rpx;
  background: linear-gradient(135deg, rgba(255,255,255,0.25) 0%, rgba(255,255,255,0.1) 100%);
  box-shadow: 0 8rpx 28rpx rgba(0, 0, 0, 0.1);

  .points-content {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: space-between;
    z-index: 1;
  }

  .points-left {
    .points-label {
      font-size: 26rpx;
      color: rgba(255, 255, 255, 0.85);
    }

    .points-value-row {
      display: flex;
      align-items: baseline;
      margin: 8rpx 0;
    }

    .points-value {
      font-size: 56rpx;
      font-weight: 800;
      color: #fff;
      line-height: 1;
    }

    .points-action {
      display: flex;
      align-items: center;
      margin-left: 20rpx;
      background-color: rgba(255, 255, 255, 0.2);
      padding: 8rpx 20rpx;
      border-radius: 30rpx;

      .action-text {
        font-size: 22rpx;
        color: #fff;
        margin-right: 6rpx;
      }
    }

    .points-sub {
      font-size: 22rpx;
      color: rgba(255, 255, 255, 0.7);
    }
  }

  .points-right {
    .points-glow-icon {
      width: 100rpx;
      height: 100rpx;
      background: rgba(255, 255, 255, 0.15);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }
}



/* ===== 筛选栏 ===== */
.filter-bar {
  width: 100%;
  box-sizing: border-box;
  padding: 16rpx 0;
  background-color: #F5F6FA;
  overflow: hidden;

  .filter-scroll {
    width: 100%;
    white-space: nowrap;
    padding: 0 24rpx;
    box-sizing: border-box;
  }

  .filter-item {
    display: inline-block;
    vertical-align: top;
    padding: 12rpx 28rpx;
    border-radius: 32rpx;
    background-color: #fff;
    margin-right: 16rpx;
    transition: all 0.25s;

    .filter-text {
      font-size: 26rpx;
      color: #666;
      white-space: nowrap;
    }

    &.active {
      background: linear-gradient(135deg, #FF934F, #FF7E3D);

      .filter-text {
        color: #fff;
        font-weight: 600;
      }
    }
  }
}

/* ===== 商品滚动区 ===== */
.product-scroll {
  flex: 1;
  padding: 20rpx 24rpx 0;
  box-sizing: border-box;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
}

.product-grid-item {
  background-color: #fff;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

  .pgi-image-wrap {
    position: relative;
    width: 100%;
    height: 280rpx;
    background-color: #F5F5F5;

    .pgi-image {
      width: 100%;
      height: 100%;
    }

    .pgi-placeholder {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .pgi-soldout {
      position: absolute;
      inset: 0;
      background-color: rgba(0, 0, 0, 0.4);
      display: flex;
      align-items: center;
      justify-content: center;

      text {
        color: #fff;
        font-size: 28rpx;
        font-weight: 600;
      }
    }
  }

  .pgi-info {
    padding: 20rpx 20rpx 24rpx;

    .pgi-name {
      font-size: 28rpx;
      color: #333;
      font-weight: 500;
      line-height: 1.4;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      min-height: 78rpx;
    }

    .pgi-bottom {
      display: flex;
      align-items: flex-end;
      justify-content: space-between;
      margin-top: 16rpx;
    }

    .pgi-price {
      display: flex;
      align-items: baseline;

      .pgi-points {
        font-size: 32rpx;
        color: #FF7E3D;
        font-weight: 700;
      }

      .pgi-unit {
        font-size: 20rpx;
        color: #999;
        margin-left: 4rpx;
      }
    }

    .pgi-exchange {
      font-size: 20rpx;
      color: #999;
    }
  }
}

/* ===== 加载更多 ===== */
.load-more {
  padding: 40rpx 0;
  text-align: center;

  .load-text {
    font-size: 24rpx;
    color: #999;
  }
}

.bottom-safe {
  height: calc(env(safe-area-inset-bottom) + 40rpx);
}
</style>
