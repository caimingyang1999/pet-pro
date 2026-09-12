<template>
  <view class="search-page">
    <!-- 自定义导航栏：搜索输入 -->
    <view class="search-nav" :style="{ paddingTop: navBarHeight + 'px' }">
      <view class="nav-inner">
        <view class="search-box">
          <Icon name="search" :size="16" color="#FF8C42" />
          <input
            v-model="keyword"
            class="search-input"
            type="text"
            confirm-type="search"
            placeholder="搜索养宠知识"
            placeholder-class="input-placeholder"
            :focus="autoFocus"
            @confirm="onSearch"
          />
          <view v-if="keyword" class="clear-btn" @click="clearKeyword">
            <Icon name="close_circle" :size="16" color="#C0C4CC" />
          </view>
        </view>
        <text class="cancel-btn" @click="onCancel">取消</text>
      </view>
    </view>

    <!-- 未搜索：搜索历史 + 热门关键词 -->
    <view v-if="!hasSearched">
      <!-- 搜索历史（用户可删除单条 / 清空 / 选择不显示） -->
      <view class="history-wrap" v-if="historyVisible && historyList.length">
        <view class="block-head">
          <text class="block-title">搜索历史</text>
          <view class="head-actions">
            <view class="head-action pet-press" @click="hideHistory">
              <Icon name="eye_off" :size="15" color="#8A8D9A" />
              <text class="action-text">不显示</text>
            </view>
            <view class="head-action pet-press" @click="clearHistory">
              <Icon name="trash" :size="15" color="#8A8D9A" />
              <text class="action-text">清空</text>
            </view>
          </view>
        </view>
        <view class="history-list">
          <view
            class="history-item pet-press"
            v-for="word in historyList"
            :key="word"
            @click="useKeyword(word)"
          >
            <text class="history-text">{{ word }}</text>
            <view class="history-del" @click.stop="deleteHistory(word)">
              <Icon name="close" :size="12" color="#C0C4CC" />
            </view>
          </view>
        </view>
      </view>

      <!-- 历史已关闭：提供重新开启入口 -->
      <view class="history-open pet-press" v-else-if="!historyVisible" @click="showHistory">
        <Icon name="undo" :size="14" color="#FF8C42" />
        <text class="history-open-text">开启搜索历史</text>
      </view>

      <!-- 热门关键词 -->
      <view class="hot-wrap">
        <text class="hot-title">大家都在搜</text>
        <view class="hot-list">
          <view
            class="hot-item pet-press"
            v-for="word in hotWords"
            :key="word"
            @click="useKeyword(word)"
          >
            <text class="hot-text">{{ word }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 搜索中 -->
    <view class="loading-wrap" v-if="loading">
      <view class="loading-paw">
        <Icon name="search" :size="26" color="#FF8C42" />
      </view>
      <text class="loading-text">正在搜索...</text>
    </view>

    <!-- 搜索结果 -->
    <view class="result-wrap" v-else-if="hasSearched">
      <view class="result-count" v-if="articleList.length">
        <text class="count-text">共找到 {{ resultTotal }} 篇相关文章</text>
      </view>

      <view class="article-list" v-if="articleList.length">
        <view
          class="article-card pet-press"
          v-for="item in articleList"
          :key="item.id"
          @click="goDetail(item.id)"
        >
          <view class="article-main">
            <text class="article-title pet-line2">{{ item.title }}</text>
            <text class="article-summary pet-line2" v-if="item.summary">{{ item.summary }}</text>
            <view class="article-meta">
              <text v-if="item.category" class="meta-chip">{{ item.category }}</text>
              <text class="meta-time">{{ formatDateTime(item.createTime) }}</text>
            </view>
          </view>
          <image
            v-if="item.coverImage"
            class="article-thumb"
            :src="fullImageUrl(item.coverImage)"
            mode="aspectFill"
          />
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

      <!-- 空结果 -->
      <view class="empty-wrap" v-else>
        <view class="empty-art">
          <Icon name="search" :size="44" color="#FFB07A" />
        </view>
        <text class="empty-title">没有找到相关文章</text>
        <text class="empty-desc">换个关键词试试，比如「疫苗」「换粮」「体重」</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import Icon from '@/components/Icon.vue';
import { ref } from 'vue';
import { onLoad, onShow, onReachBottom } from '@dcloudio/uni-app';
import { getArticleList } from '@/api/article.js';
import { showToast, formatDateTime, fullImageUrl } from '@/utils/index.js';
import {
  getSearchHistory,
  addSearchHistory,
  removeSearchHistory,
  clearSearchHistory,
  isHistoryVisible,
  setHistoryVisible,
} from '@/utils/searchHistory.js';

/** 每页条数 */
const PAGE_SIZE = 10;

const navBarHeight = ref(44);
// #ifdef MP-WEIXIN
try {
  const menuRect = uni.getMenuButtonBoundingClientRect();
  navBarHeight.value = menuRect.bottom + 8;
} catch (e) {}
// #endif

const autoFocus = ref(false);
const keyword = ref('');
const hasSearched = ref(false);
const loading = ref(false);
const loadingMore = ref(false);
const finished = ref(false);
const articleList = ref([]);
const resultTotal = ref(0);
const pageNum = ref(1);

const hotWords = ['疫苗', '换粮', '体重', '驱虫', '新手养猫', '训练'];

/* ==================== 搜索历史 ==================== */
const historyVisible = ref(true);
const historyList = ref([]);

/** 读取搜索历史与"是否展示"偏好 */
const loadHistory = () => {
  historyVisible.value = isHistoryVisible();
  // 关闭展示时不读取内容，避免无谓渲染
  historyList.value = historyVisible.value ? getSearchHistory() : [];
};

/** 删除单条记录 */
const deleteHistory = (word) => {
  historyList.value = removeSearchHistory(word);
};

/** 清空全部记录 */
const clearHistory = () => {
  uni.showModal({
    title: '提示',
    content: '确定要清空全部搜索记录吗？',
    success: (res) => {
      if (!res.confirm) return;
      clearSearchHistory();
      historyList.value = [];
    },
  });
};

/** 选择"不显示"：仅隐藏展示，不删除已有记录 */
const hideHistory = () => {
  setHistoryVisible(false);
  historyVisible.value = false;
  historyList.value = [];
  showToast('已关闭搜索历史');
};

/** 重新开启历史记录展示 */
const showHistory = () => {
  setHistoryVisible(true);
  loadHistory();
};

/* ==================== 搜索 ==================== */
const clearKeyword = () => {
  keyword.value = '';
  articleList.value = [];
  resultTotal.value = 0;
  hasSearched.value = false;
  finished.value = false;
};

const onCancel = () => {
  uni.navigateBack();
};

/** 执行搜索（第一页） */
const onSearch = async () => {
  const kw = keyword.value.trim();
  if (!kw) {
    showToast('请输入搜索内容');
    return;
  }
  keyword.value = kw;
  // 仅在用户开启搜索历史时记录，关闭后不写入本地缓存
  if (historyVisible.value) {
    addSearchHistory(kw);
  }

  hasSearched.value = true;
  loading.value = true;
  loadingMore.value = false;
  finished.value = false;
  pageNum.value = 1;
  try {
    const res = await getArticleList({ pageNum: 1, pageSize: PAGE_SIZE, keyword: kw });
    const rows = res.rows || [];
    articleList.value = rows;
    resultTotal.value = res.total || rows.length;
    // 到底判定以 total 为准：PageHelper 分页合理化会把超界的 pageNum 回退到最后一页，
    // 返回同一批数据，只看"本页不足一页"永远触发不了到底。
    const total = Number(res.total);
    if (Number.isFinite(total) && total > 0) {
      finished.value = articleList.value.length >= total;
    } else {
      finished.value = rows.length < PAGE_SIZE;
    }
  } catch (e) {
    console.error('[搜索] 获取养宠知识失败:', e?.code || e?.msg || e);
    articleList.value = [];
    resultTotal.value = 0;
    finished.value = true;
  } finally {
    loading.value = false;
  }
};

/** 触底加载下一页搜索结果 */
const loadMore = async () => {
  if (!hasSearched.value || loading.value || loadingMore.value || finished.value) return;
  const kw = keyword.value.trim();
  if (!kw) return;

  loadingMore.value = true;
  try {
    const nextPage = pageNum.value + 1;
    const res = await getArticleList({ pageNum: nextPage, pageSize: PAGE_SIZE, keyword: kw });
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
    console.error('[搜索] 加载更多失败:', e?.code || e?.msg || e);
    finished.value = true;
  } finally {
    loadingMore.value = false;
  }
};

/** 使用历史记录 / 热门词搜索 */
const useKeyword = (word) => {
  keyword.value = word;
  onSearch();
};

const goDetail = (id) => uni.navigateTo({ url: `/pages/article/detail?id=${id}` });

onLoad((options = {}) => {
  // 从首页搜索框进入时自动聚焦
  autoFocus.value = options.mode === 'article';
  if (options.keyword) {
    keyword.value = decodeURIComponent(options.keyword);
    onSearch();
  }
});

// 每次进入（含从文章详情返回）都重新读取历史与展示偏好
onShow(() => {
  loadHistory();
});

onReachBottom(() => {
  loadMore();
});
</script>

<style lang="scss" scoped>
.search-page {
  min-height: 100vh;
  background-color: $bg-page;
}

/* ===== 搜索导航栏 ===== */
.search-nav {
  position: sticky;
  top: 0;
  z-index: 100;
  background-color: #fff;
  box-shadow: 0 2rpx 12rpx rgba(52, 59, 76, 0.05);

  .nav-inner {
    display: flex;
    align-items: center;
    gap: 20rpx;
    height: 96rpx;
    padding: 0 28rpx;
  }

  .search-box {
    flex: 1;
    display: flex;
    align-items: center;
    gap: 12rpx;
    height: 72rpx;
    background-color: $bg-page;
    border-radius: $radius-round;
    padding: 0 24rpx;

    .search-input {
      flex: 1;
      font-size: $font-sm;
      color: $text-primary;
      height: 100%;
    }

    .clear-btn {
      padding: 8rpx;
      display: flex;
      align-items: center;
    }
  }

  .cancel-btn {
    font-size: $font-md;
    color: $text-secondary;
    flex-shrink: 0;
  }
}

.input-placeholder {
  color: $text-placeholder;
  font-size: $font-sm;
}

/* ===== 区块标题（历史/热门通用） ===== */
.block-head {
  display: flex;
  align-items: center;
  justify-content: space-between;

  .block-title {
    font-size: $font-md;
    font-weight: $font-weight-bold;
    color: $text-primary;
  }

  .head-actions {
    display: flex;
    align-items: center;
    gap: 28rpx;
  }

  .head-action {
    display: flex;
    align-items: center;
    gap: 6rpx;

    .action-text {
      font-size: $font-xs;
      color: $text-hint;
    }
  }
}

/* ===== 搜索历史 ===== */
.history-wrap {
  padding: 40rpx 32rpx 8rpx;

  .history-list {
    display: flex;
    flex-wrap: wrap;
    gap: 20rpx;
    margin-top: 28rpx;
  }

  .history-item {
    display: flex;
    align-items: center;
    gap: 12rpx;
    max-width: 100%;
    padding: 14rpx 20rpx 14rpx 32rpx;
    border-radius: $radius-round;
    background-color: #fff;
    box-shadow: $shadow-sm;

    .history-text {
      font-size: $font-sm;
      color: $text-secondary;
      max-width: 380rpx;
      overflow: hidden;
      white-space: nowrap;
      text-overflow: ellipsis;
    }

    .history-del {
      width: 32rpx;
      height: 32rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }
  }
}

/* 历史被关闭时的开启入口 */
.history-open {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  margin: 36rpx 32rpx 0;
  padding: 14rpx 32rpx;
  border-radius: $radius-round;
  background-color: #fff;
  box-shadow: $shadow-sm;

  .history-open-text {
    font-size: $font-sm;
    color: $primary;
  }
}

/* ===== 热门关键词 ===== */
.hot-wrap {
  padding: 48rpx 32rpx;

  .hot-title {
    font-size: $font-md;
    font-weight: $font-weight-bold;
    color: $text-primary;
  }

  .hot-list {
    display: flex;
    flex-wrap: wrap;
    gap: 20rpx;
    margin-top: 28rpx;
  }

  .hot-item {
    padding: 14rpx 32rpx;
    border-radius: $radius-round;
    background-color: #fff;
    box-shadow: $shadow-sm;

    .hot-text {
      font-size: $font-sm;
      color: $text-secondary;
    }
  }
}

/* ===== 加载 ===== */
.loading-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 140rpx 0;

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

/* ===== 结果 ===== */
.result-wrap {
  padding: 24rpx 24rpx 60rpx;
}

.result-count {
  padding: 0 8rpx 20rpx;

  .count-text {
    font-size: $font-xs;
    color: $text-hint;
  }
}

.article-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.article-card {
  display: flex;
  gap: 22rpx;
  padding: 24rpx;
  border-radius: $radius-lg;
  background-color: #fff;
  box-shadow: $shadow-card;

  .article-main {
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
      }

      .meta-time {
        font-size: $font-xs;
        color: $text-hint;
      }
    }
  }

  .article-thumb {
    width: 160rpx;
    height: 120rpx;
    border-radius: $radius-md;
    flex-shrink: 0;
    background-color: $bg-input;
  }
}

/* ===== 加载更多 / 到底提示 ===== */
.list-foot {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 36rpx 0 8rpx;

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

/* ===== 空结果 ===== */
.empty-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100rpx 72rpx;

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
