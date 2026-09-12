<template>
  <view class="article-list-page">
    <!-- ========== 吸顶导航：返回 + 搜索 + 分类 ========== -->
    <view class="page-nav" :style="{ paddingTop: navBarHeight + 'px' }">
      <view class="nav-inner">
        <view class="back-btn pet-press" @click="goBack">
          <Icon name="back" :size="20" color="#343B4C" />
        </view>
        <view class="search-capsule pet-press" @click="goSearch">
          <Icon name="search" :size="16" color="#FF8C42" />
          <text class="search-placeholder">搜索养宠知识</text>
        </view>
      </view>

      <!-- 分类筛选 -->
      <scroll-view class="cate-scroll" scroll-x :show-scrollbar="false">
        <view class="cate-row">
          <view
            class="cate-chip pet-press"
            v-for="item in categoryOptions"
            :key="item.value"
            :class="{ active: activeCategory === item.value }"
            @click="switchCategory(item.value)"
          >
            <text class="cate-text">{{ item.label }}</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <view class="content-area">
      <!-- 首屏加载 -->
      <view class="loading-wrap" v-if="loading && !articleList.length">
        <view class="loading-icon">
          <Icon name="file_text" :size="26" color="#FF8C42" />
        </view>
        <text class="loading-text">正在加载养宠知识...</text>
      </view>

      <!-- 文章列表 -->
      <view class="article-list" v-else-if="articleList.length">
        <view
          class="article-card pet-press"
          v-for="item in articleList"
          :key="item.id"
          @click="goDetail(item.id)"
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
        <view class="foot-loading" v-if="loadingMore">
          <Icon name="reload" :size="15" color="#FF8C42" />
          <text class="foot-text">正在加载更多...</text>
        </view>
        <text class="foot-text foot-end" v-else-if="finished">— 到底啦 —</text>
      </view>

      <!-- 空状态 -->
      <view class="empty-wrap" v-else>
        <view class="empty-art">
          <Icon name="file_text" :size="44" color="#FFB07A" />
        </view>
        <text class="empty-title">{{ activeCategory ? '该分类下暂无文章' : '知识文章正在筹备中' }}</text>
        <text class="empty-desc">换个分类看看，或稍后再来逛逛~</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad, onReachBottom, onPullDownRefresh } from '@dcloudio/uni-app';
import Icon from '@/components/Icon.vue';
import { getArticleList } from '@/api/article.js';
import { formatDateTime, fullImageUrl } from '@/utils/index.js';
import { useUserStore } from '@/store/user.js';
import { usePetStore } from '@/store/pet.js';
import { petTypeLabel } from '@/config/petTypes.js';

const userStore = useUserStore();
const petStore = usePetStore();

/** 适用宠物文案（文章按宠物种类分类，与主题分类是两个维度） */
const petLabel = (item) => petTypeLabel(item?.petType, '');

/** 每页条数 */
const PAGE_SIZE = 10;

/** 分类筛选项（与后台文章分类保持一致） */
const categoryOptions = [
  { label: '全部', value: '' },
  { label: '喂养', value: '喂养' },
  { label: '健康', value: '健康' },
  { label: '训练', value: '训练' },
  { label: '洗护', value: '洗护' },
];

/** 导航栏高度（px），避免内容被胶囊按钮遮挡 */
const navBarHeight = ref(44);
// #ifdef MP-WEIXIN
try {
  const menuRect = uni.getMenuButtonBoundingClientRect();
  navBarHeight.value = menuRect.bottom + 8;
} catch (e) {}
// #endif

const articleList = ref([]);
const loading = ref(false);      // 首屏 / 切换分类
const loadingMore = ref(false);  // 触底追加
const finished = ref(false);     // 已无更多
const pageNum = ref(1);
const activeCategory = ref('');
const petType = ref('');         // 兴趣推荐命中的宠物类型

/**
 * 解析兴趣推荐用的宠物类型
 * 未登录 / 没有猫狗 / 宠物类型为"其他"时返回空串，由后端按默认排序返回。
 */
const resolveInterestPetType = async () => {
  if (!userStore.isLogin) return '';
  try {
    if (!petStore.petList.length) await petStore.fetchPetList();
  } catch (e) {
    return '';
  }
  const hit = (petStore.petList || []).find((p) => p.petType === 'cat' || p.petType === 'dog');
  return hit ? hit.petType : '';
};

/** 拉取第一页 */
const fetchList = async () => {
  loading.value = true;
  loadingMore.value = false;
  finished.value = false;
  pageNum.value = 1;
  try {
    petType.value = await resolveInterestPetType();
    const res = await getArticleList({
      pageNum: 1,
      pageSize: PAGE_SIZE,
      category: activeCategory.value || undefined,
      petType: petType.value || undefined,
    });
    const rows = res.rows || [];
    articleList.value = rows;
    // 到底判定以接口返回的 total 为准：
    // PageHelper 默认开启"分页参数合理化"，pageNum 超界时会回退到最后一页重复返回，
    // 只看"本页不足一页"永远触发不了到底。
    const total = Number(res.total);
    if (Number.isFinite(total) && total > 0) {
      finished.value = articleList.value.length >= total;
    } else {
      finished.value = rows.length < PAGE_SIZE;
    }
  } catch (e) {
    console.error('[养宠知识] 获取列表失败:', e);
    articleList.value = [];
    finished.value = true;
  } finally {
    loading.value = false;
  }
};

/** 触底加载下一页 */
const loadMore = async () => {
  if (loading.value || loadingMore.value || finished.value) return;
  loadingMore.value = true;
  try {
    const nextPage = pageNum.value + 1;
    const res = await getArticleList({
      pageNum: nextPage,
      pageSize: PAGE_SIZE,
      category: activeCategory.value || undefined,
      petType: petType.value || undefined,
    });
    const rows = res.rows || [];
    if (rows.length) {
      // 按 id 去重后再追加：后端分页合理化可能把最后一页重复返回，前端再兜一层
      const existing = new Set(articleList.value.map((item) => item.id));
      articleList.value = articleList.value.concat(rows.filter((item) => !existing.has(item.id)));
      pageNum.value = nextPage;
    }
    const total = Number(res.total);
    if (Number.isFinite(total) && total > 0) {
      finished.value = articleList.value.length >= total;
    } else {
      finished.value = rows.length < PAGE_SIZE;
    }
  } catch (e) {
    console.error('[养宠知识] 加载更多失败:', e);
    // 出错时停止继续加载，避免用户一直下拉空转
    finished.value = true;
  } finally {
    loadingMore.value = false;
  }
};

/** 切换分类 */
const switchCategory = (value) => {
  if (activeCategory.value === value) return;
  activeCategory.value = value;
  uni.pageScrollTo({ scrollTop: 0, duration: 200 });
  fetchList();
};

const goBack = () => uni.navigateBack();
const goSearch = () => uni.navigateTo({ url: '/pages/search/index' });
const goDetail = (id) => uni.navigateTo({ url: `/pages/article/detail?id=${id}` });

onLoad(() => {
  fetchList();
});

onReachBottom(() => {
  loadMore();
});

onPullDownRefresh(async () => {
  try {
    await fetchList();
  } finally {
    uni.stopPullDownRefresh();
  }
});
</script>

<style lang="scss" scoped>
.article-list-page {
  min-height: 100vh;
  background-color: $bg-page;
  padding-bottom: calc(env(safe-area-inset-bottom) + 60rpx);
}

/* ===== 吸顶导航 ===== */
.page-nav {
  position: sticky;
  top: 0;
  z-index: 100;
  background-color: #fff;
  box-shadow: 0 2rpx 12rpx rgba(52, 59, 76, 0.05);

  .nav-inner {
    display: flex;
    align-items: center;
    gap: 16rpx;
    height: 96rpx;
    padding: 0 24rpx;
  }

  .back-btn {
    width: 56rpx;
    height: 56rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .search-capsule {
    flex: 1;
    display: flex;
    align-items: center;
    gap: 12rpx;
    height: 72rpx;
    background-color: $bg-page;
    border-radius: $radius-round;
    padding: 0 28rpx;

    .search-placeholder {
      font-size: $font-sm;
      color: $text-hint;
    }
  }

  .cate-scroll {
    white-space: nowrap;
    padding: 0 24rpx 20rpx;

    .cate-row {
      display: inline-flex;
      align-items: center;
      gap: 16rpx;
    }

    .cate-chip {
      padding: 12rpx 32rpx;
      border-radius: $radius-round;
      background-color: $bg-page;

      .cate-text {
        font-size: $font-sm;
        color: $text-secondary;
      }

      &.active {
        background: $gradient-primary;

        .cate-text {
          color: $text-white;
          font-weight: $font-weight-medium;
        }
      }
    }
  }
}

/* ===== 内容区 ===== */
.content-area {
  padding: 24rpx 0 0;
}

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

/* ===== 首屏加载 ===== */
.loading-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 140rpx 0;

  .loading-icon {
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

/* ===== 加载更多 / 到底提示 ===== */
.list-foot {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 36rpx 0 16rpx;

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

  .foot-end {
    color: $text-placeholder;
  }
}

/* ===== 空状态 ===== */
.empty-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 140rpx 72rpx;

  .empty-art {
    width: 176rpx;
    height: 176rpx;
    border-radius: 50%;
    background: $gradient-card;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: $shadow-md;
  }

  .empty-title {
    margin-top: 32rpx;
    font-size: $font-md;
    color: $text-primary;
    font-weight: $font-weight-bold;
  }

  .empty-desc {
    margin-top: 12rpx;
    font-size: $font-sm;
    color: $text-hint;
    text-align: center;
    line-height: 1.6;
  }
}
</style>
