<template>
  <view class="article-page">
    <!-- 加载中 -->
    <view v-if="loading" class="loading-wrap">
      <view class="loading-paw">
        <Icon name="file_text" :size="30" color="#FF8C42" />
      </view>
      <text class="loading-text">正在加载文章...</text>
    </view>

    <!-- 加载失败 / 文章不存在 -->
    <view v-else-if="!article" class="error-wrap">
      <view class="error-art">
        <Icon name="file_text" :size="42" color="#FFB07A" />
      </view>
      <text class="error-title">文章不存在或已下架</text>
      <view class="error-btn pet-press" @click="goBack">
        <text>返回首页</text>
      </view>
    </view>

    <!-- 正文 -->
    <scroll-view v-else scroll-y class="article-scroll">
      <view class="article-header">
        <text class="article-title">{{ article.title }}</text>
        <view class="article-meta">
          <text v-if="article.category" class="meta-chip">{{ article.category }}</text>
          <text v-if="petTypeText" class="meta-chip meta-chip--pet">{{ petTypeText }}</text>
          <text v-if="article.source" class="meta-text">{{ article.source }}</text>
          <text class="meta-text">{{ formatDateTime(article.createTime) }}</text>
          <text class="meta-text">{{ article.viewCount || 0 }} 次阅读</text>
        </view>
      </view>

      <image
        v-if="coverUrl"
        class="article-cover"
        :src="coverUrl"
        mode="aspectFill"
      />

      <view class="article-body">
        <rich-text v-if="contentNodes" :nodes="contentNodes" />
        <text v-else class="article-empty">暂无正文内容</text>
      </view>

      <!-- 声明：平台内容，无互动入口 -->
      <view class="article-notice">
        <Icon name="info" :size="13" color="#8A8D9A" />
        <text class="notice-text">本文由宠迹编辑部提供，仅供养宠参考，不构成兽医诊疗建议。</text>
      </view>

      <view class="safe-bottom" />
    </scroll-view>
  </view>
</template>

<script setup>
import Icon from '@/components/Icon.vue';
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { getArticleDetail } from '@/api/article.js';
import { showToast, formatDateTime, fullImageUrl } from '@/utils/index.js';
import { SERVER_BASE } from '@/api/request.js';
import { petTypeLabel } from '@/config/petTypes.js';

const articleId = ref('');
const article = ref(null);
const loading = ref(true);

const coverUrl = computed(() => {
  const cover = article.value?.coverImage;
  return cover ? fullImageUrl(cover) : '';
});

/**
 * 正文图片统一样式。
 * rich-text 内部节点不会继承页面的 scoped 样式，若不内联宽度约束，
 * 微信会按图片原始尺寸（AI 生成的图为 1024px）渲染，导致撑破屏幕。
 */
const ARTICLE_IMG_STYLE = 'width:100%;height:auto;display:block;border-radius:8px;margin:16px 0;';

/** 规范化正文里的 <img>：剔除旧尺寸属性，统一注入自适应样式 */
const normalizeArticleImages = (html) =>
  html.replace(/<img\b[^>]*>/gi, (tag) => {
    const cleaned = tag.replace(/\s+(style|width|height)\s*=\s*("[^"]*"|'[^']*')/gi, '');
    return cleaned.replace(/\s*\/?>\s*$/, ` style="${ARTICLE_IMG_STYLE}">`);
  });

/**
 * 富文本正文：把内容里的相对图片路径补全为完整域名，
 * 否则小程序里图片无法加载；同时统一图片尺寸避免过大。
 */
const contentNodes = computed(() => {
  const raw = article.value?.content;
  if (!raw) return '';
  const base = SERVER_BASE.endsWith('/') ? SERVER_BASE.slice(0, -1) : SERVER_BASE;
  const html = String(raw)
    // 已经带域名的保持原样
    .replace(/(<img[^>]+src=["'])(\/(?!\/))/g, `$1${base}/`)
    // 兜底：后端可能返回裸相对路径 profile/xxx
    .replace(/(<img[^>]+src=["'])(profile\/)/g, `$1${base}/$2`);
  // 统一约束正文图片尺寸，防止按原图尺寸渲染撑破屏幕
  return normalizeArticleImages(html);
});

/** 适用宠物文案（文章按宠物种类分类，与主题分类是两个维度） */
const petTypeText = computed(() => petTypeLabel(article.value?.petType, ''));

const fetchDetail = async () => {
  loading.value = true;
  try {
    const res = await getArticleDetail(articleId.value);
    article.value = res.data || null;
    if (article.value?.title) {
      uni.setNavigationBarTitle({ title: article.value.title });
    }
  } catch (e) {
    console.error('[养宠知识] 获取详情失败:', e?.code || e?.msg || e);
    article.value = null;
  } finally {
    loading.value = false;
  }
};

const goBack = () => {
  const pages = getCurrentPages();
  if (pages.length > 1) {
    uni.navigateBack();
  } else {
    uni.switchTab({ url: '/pages/index/index' });
  }
};

onLoad((options = {}) => {
  articleId.value = options.id || '';
  if (!articleId.value) {
    loading.value = false;
    showToast('缺少文章参数');
    return;
  }
  fetchDetail();
});
</script>

<style lang="scss" scoped>
.article-page {
  min-height: 100vh;
  background-color: #FFFFFF;
}

/* ===== 加载 / 失败 ===== */
.loading-wrap,
.error-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 200rpx 72rpx 0;
}

.loading-paw {
  width: 96rpx;
  height: 96rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: pet-breath 1.6s ease-in-out infinite;
}

.loading-text {
  margin-top: 24rpx;
  font-size: $font-sm;
  color: $text-hint;
}

.error-art {
  width: 176rpx;
  height: 176rpx;
  border-radius: 50%;
  background: $gradient-card;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: $shadow-md;
}

.error-title {
  margin-top: 32rpx;
  font-size: $font-md;
  color: $text-secondary;
}

.error-btn {
  margin-top: 40rpx;
  padding: 18rpx 56rpx;
  border-radius: $radius-round;
  background: $gradient-primary;
  box-shadow: $shadow-primary;

  text {
    font-size: $font-md;
    color: #fff;
    font-weight: $font-weight-medium;
  }
}

/* ===== 正文 ===== */
.article-scroll {
  height: 100vh;
}

.article-header {
  padding: 40rpx 36rpx 28rpx;

  .article-title {
    display: block;
    font-size: 44rpx;
    font-weight: $font-weight-bold;
    color: $text-primary;
    line-height: 1.45;
  }

  .article-meta {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 16rpx;
    margin-top: 24rpx;

    .meta-chip {
      font-size: $font-xs;
      color: $primary-dark;
      background: $primary-lighter;
      padding: 4rpx 16rpx;
      border-radius: $radius-round;

      /* 适用宠物：与主题分类（喂养/健康）区分开，用另一套配色 */
      &.meta-chip--pet {
        color: #4F9E62;
        background: #E8F6E9;
      }
    }

    .meta-text {
      font-size: $font-xs;
      color: $text-hint;
    }
  }
}

.article-cover {
  width: 100%;
  height: 380rpx;
  background-color: $bg-input;
}

.article-body {
  padding: 32rpx 36rpx 8rpx;
  font-size: $font-md;
  color: #3D3D3D;
  line-height: 1.9;

  .article-empty {
    display: block;
    font-size: $font-sm;
    color: $text-hint;
    padding: 40rpx 0;
    text-align: center;
  }
}

.article-notice {
  display: flex;
  align-items: flex-start;
  gap: 10rpx;
  margin: 40rpx 36rpx 0;
  padding: 24rpx;
  border-radius: $radius-md;
  background: $bg-page;

  .notice-text {
    flex: 1;
    font-size: $font-xs;
    color: $text-hint;
    line-height: 1.7;
  }
}

.safe-bottom {
  height: calc(env(safe-area-inset-bottom) + 60rpx);
}
</style>
