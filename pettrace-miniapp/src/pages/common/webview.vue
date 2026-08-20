<template>
  <view class="webview-page">
    <web-view :src="url" v-if="url"></web-view>
    <view v-else class="empty-tip">
      <text>链接无效</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';

const url = ref('');

onLoad((options) => {
  if (options.url) {
    // 对编码后的 URL 进行解码
    url.value = decodeURIComponent(options.url);
    // 动态设置导航栏标题
    if (options.title) {
      uni.setNavigationBarTitle({ title: decodeURIComponent(options.title) });
    }
  }
});
</script>

<style lang="scss" scoped>
.webview-page {
  width: 100%;
  height: 100vh;

  .empty-tip {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
    font-size: 28rpx;
    color: #999;
  }
}
</style>
