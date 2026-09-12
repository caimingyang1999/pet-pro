<template>
  <view class="likes-page">
    <view v-if="!list.length && !loading" class="empty-wrap">
      <Icon class="empty-icon" name="heart_fill" :size="58" color="#FFC8A2" />
      <text class="empty-text">还没有点赞任何动态</text>
      <text class="empty-hint">去首页发现精彩内容吧</text>
    </view>

    <scroll-view
      v-else
      scroll-y
      class="list-scroll"
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="loadMore"
    >
      <view class="post-list">
        <view
          v-for="post in list"
          :key="post.id"
          class="post-item"
          @click="goDetail(post.id)"
        >
          <!-- 左侧：作者信息 + 内容 -->
          <view class="post-main">
            <view class="author-row">
              <image
                class="avatar"
                :src="fullImageUrl(post.userAvatar)"
                mode="aspectFill"
              />
              <text class="nickname">{{ post.userName || '匿名用户' }}</text>
            </view>
            <text class="content" v-if="post.content">{{ truncate(post.content, 60) }}</text>
            <view class="meta-row">
              <view class="meta-item">
                <Icon class="meta-icon" name="heart_fill" :size="12" color="#E8607F" />
                <text>{{ post.likeCount || 0 }}</text>
              </view>
              <view class="meta-item">
                <Icon class="meta-icon" name="message" :size="12" color="#8A8D9A" />
                <text>{{ post.commentCount || 0 }}</text>
              </view>
            </view>
          </view>

          <!-- 右侧：缩略图 / 视频图标 -->
          <view class="post-thumb" v-if="getThumb(post)" :class="{ isVideo: post.videoUrl }">
            <image
              v-if="!post.videoUrl"
              class="thumb-img"
              :src="getThumb(post)"
              mode="aspectFill"
            />
            <view v-else class="video-cover">
              <image class="thumb-img" :src="fullImageUrl(post.videoCover)" mode="aspectFill" />
              <view class="play-icon">▶</view>
            </view>
          </view>
        </view>
      </view>

      <view v-if="list.length" class="load-more">
        <text v-if="loadStatus === 'loading'" class="load-text">加载中...</text>
        <text v-else-if="loadStatus === 'nomore'" class="load-text">- 没有更多了 -</text>
        <text v-else class="load-text" @click="loadMore">加载更多</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import Icon from '@/components/Icon.vue';
import { ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { getMyLikePosts } from '@/api/post.js';
import { showToast, fullImageUrl } from '@/utils/index.js';

const list = ref([]);
const loading = ref(false);
const loadStatus = ref('loadmore');
const refreshing = ref(false);
const pageParams = ref({ pageNum: 1, pageSize: 10 });

// 取第一张图片做缩略图
const getThumb = (post) => {
  if (post.videoUrl) return fullImageUrl(post.videoCover);
  let images = post.images;
  if (!images) return '';
  if (!Array.isArray(images)) {
    try { images = JSON.parse(images); } catch { return ''; }
  }
  if (Array.isArray(images) && images.length) {
    return fullImageUrl(images[0]);
  }
  return '';
};

const truncate = (str, n) => {
  if (!str) return '';
  return str.length > n ? str.slice(0, n) + '...' : str;
};

const fetchList = async (isRefresh = false) => {
  if (isRefresh) pageParams.value.pageNum = 1;
  loading.value = true;
  try {
    const res = await getMyLikePosts(pageParams.value);
    const rows = res.rows || [];
    if (isRefresh) {
      list.value = rows;
    } else {
      list.value = [...list.value, ...rows];
    }
    loadStatus.value = rows.length < pageParams.value.pageSize ? 'nomore' : 'loadmore';
  } catch (e) {
    loadStatus.value = 'loadmore';
    showToast(e?.msg || '获取数据失败');
  } finally {
    loading.value = false;
    refreshing.value = false;
  }
};

const loadMore = () => {
  if (loadStatus.value === 'nomore' || loadStatus.value === 'loading') return;
  pageParams.value.pageNum++;
  fetchList();
};

const onRefresh = async () => {
  refreshing.value = true;
  await fetchList(true);
};

const goDetail = (id) => {
  uni.navigateTo({ url: `/pages/index/detail?id=${id}` });
};

onShow(() => {
  fetchList(true);
  uni.setNavigationBarTitle({ title: '我的点赞' });
});
</script>

<style lang="scss" scoped>
.likes-page {
  min-height: 100vh;
  background-color: #F8F9FC;
}

.list-scroll {
  height: 100vh;
}

.post-list {
  background-color: #fff;
  border-radius: 24rpx 24rpx 0 0;
  margin-top: 20rpx;

  .post-item {
    display: flex;
    padding: 24rpx;
    border-bottom: 1rpx solid #F5F5F5;

    &:last-child { border-bottom: none; }

    .post-main {
      flex: 1;
      min-width: 0;
      padding-right: 20rpx;

      .author-row {
        display: flex;
        align-items: center;
        margin-bottom: 12rpx;

        .avatar {
          width: 48rpx;
          height: 48rpx;
          border-radius: 50%;
          background-color: #F0F0F0;
          margin-right: 12rpx;
        }

        .nickname {
          font-size: 26rpx;
          color: #666;
          font-weight: 500;
        }
      }

      .content {
        font-size: 28rpx;
        color: #333;
        line-height: 1.5;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
        margin-bottom: 12rpx;
      }

      .meta-row {
        display: flex;
        gap: 24rpx;

        .meta-item {
          font-size: 22rpx;
          color: #999;
          display: flex;
          align-items: center;

          .meta-icon {
            margin-right: 4rpx;
          }
        }
      }
    }

    .post-thumb {
      width: 180rpx;
      height: 180rpx;
      border-radius: 12rpx;
      overflow: hidden;
      background-color: #F0F0F0;
      flex-shrink: 0;
      position: relative;

      .thumb-img {
        width: 100%;
        height: 100%;
      }

      &.isVideo .video-cover {
        position: relative;
        width: 100%;
        height: 100%;

        .play-icon {
          position: absolute;
          top: 50%;
          left: 50%;
          transform: translate(-50%, -50%);
          width: 56rpx;
          height: 56rpx;
          background-color: rgba(0, 0, 0, 0.5);
          border-radius: 50%;
          color: #fff;
          font-size: 24rpx;
          display: flex;
          align-items: center;
          justify-content: center;
        }
      }
    }
  }
}

.empty-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 240rpx;

  .empty-icon { font-size: 140rpx; margin-bottom: 24rpx; }
  .empty-text { font-size: 32rpx; color: #333; font-weight: 600; }
  .empty-hint { font-size: 26rpx; color: #B0B0B0; margin-top: 16rpx; }
}

.load-more {
  text-align: center;
  padding: 40rpx 0;

  .load-text { font-size: 26rpx; color: #B0B0B0; }
}
</style>
