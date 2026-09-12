<template>
  <view class="agreement-page">
    <!-- 顶部标题区 -->
    <view class="doc-header">
      <view class="doc-badge">
        <Icon :name="isPrivacy ? 'locked' : 'file_text'" :size="26" color="#fff" />
      </view>
      <text class="doc-title">{{ doc.docTitle }}</text>
      <text class="doc-meta">{{ doc.docSubTitle }}</text>
    </view>

    <!-- 正文 -->
    <view class="doc-body">
      <!-- 引言 -->
      <view class="intro-card">
        <text class="intro-text">{{ doc.intro }}</text>
      </view>

      <!-- 章节 -->
      <view class="section" v-for="(sec, idx) in doc.sections" :key="idx">
        <text class="section-title">{{ sec.title }}</text>
        <text
          class="section-p"
          v-for="(p, pIdx) in sec.paragraphs || []"
          :key="'p-' + pIdx"
        >{{ p }}</text>

        <view class="section-list" v-if="sec.list && sec.list.length">
          <view class="list-item" v-for="(li, lIdx) in sec.list" :key="'l-' + lIdx">
            <view class="list-dot" />
            <text class="list-text">{{ li }}</text>
          </view>
        </view>
      </view>

      <!-- 结尾 -->
      <view class="doc-footer">
        <text class="footer-text">宠迹运营团队</text>
        <text class="footer-date">{{ doc.docSubTitle.split('　')[0] }}</text>
      </view>

      <view class="safe-bottom" />
    </view>
  </view>
</template>

<script setup>
import Icon from '@/components/Icon.vue';
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { getLegalDoc } from '@/config/legal.js';

/** 文档类型：user-用户协议 privacy-隐私政策 */
const type = ref('user');
const isPrivacy = computed(() => type.value === 'privacy');
const doc = computed(() => getLegalDoc(type.value));

onLoad((options = {}) => {
  type.value = options.type === 'privacy' ? 'privacy' : 'user';
  uni.setNavigationBarTitle({ title: isPrivacy.value ? '隐私政策' : '用户协议' });
});
</script>

<style lang="scss" scoped>
.agreement-page {
  min-height: 100vh;
  background-color: #F8F9FC;
  padding: 24rpx 28rpx 0;
  box-sizing: border-box;
}

/* ===== 顶部标题区 ===== */
.doc-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 36rpx 24rpx 40rpx;

  .doc-badge {
    width: 108rpx;
    height: 108rpx;
    border-radius: 32rpx;
    background: $gradient-primary;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: $shadow-primary;
    margin-bottom: 24rpx;

    .badge-emoji {
      font-size: 52rpx;
    }
  }

  .doc-title {
    font-size: 40rpx;
    font-weight: $font-weight-bold;
    color: $text-primary;
    text-align: center;
  }

  .doc-meta {
    margin-top: 14rpx;
    font-size: $font-xs;
    color: $text-hint;
    text-align: center;
  }
}

/* ===== 正文 ===== */
.doc-body {
  background: #fff;
  border-radius: $radius-lg;
  box-shadow: $shadow-card;
  padding: 36rpx 32rpx 24rpx;
}

.intro-card {
  background: $primary-lighter;
  border-radius: $radius-md;
  padding: 26rpx 24rpx;
  margin-bottom: 36rpx;

  .intro-text {
    font-size: $font-sm;
    color: $primary-dark;
    line-height: 1.85;
    white-space: pre-wrap;
  }
}

.section {
  margin-bottom: 40rpx;

  .section-title {
    display: block;
    font-size: $font-md;
    font-weight: $font-weight-bold;
    color: $text-primary;
    margin-bottom: 18rpx;
    padding-left: 20rpx;
    border-left: 6rpx solid $primary;
    line-height: 1.4;
  }

  .section-p {
    display: block;
    font-size: $font-sm;
    color: $text-secondary;
    line-height: 1.9;
    margin-bottom: 14rpx;
  }

  .section-list {
    margin-top: 12rpx;
  }

  .list-item {
    display: flex;
    align-items: flex-start;
    margin-bottom: 16rpx;

    .list-dot {
      width: 10rpx;
      height: 10rpx;
      border-radius: 50%;
      background: $primary;
      opacity: 0.72;
      margin: 16rpx 16rpx 0 4rpx;
      flex-shrink: 0;
    }

    .list-text {
      flex: 1;
      font-size: $font-sm;
      color: $text-secondary;
      line-height: 1.9;
    }
  }
}

.doc-footer {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  padding: 20rpx 4rpx 8rpx;
  border-top: 1rpx solid $bg-input;
  margin-top: 12rpx;

  .footer-text {
    font-size: $font-sm;
    color: $text-secondary;
    font-weight: $font-weight-medium;
  }

  .footer-date {
    margin-top: 8rpx;
    font-size: $font-xs;
    color: $text-hint;
  }
}

.safe-bottom {
  height: calc(env(safe-area-inset-bottom) + 60rpx);
}
</style>
