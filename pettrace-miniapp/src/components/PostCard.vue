<template>
  <view class="post-card">
    <!-- 头部：头像 + 用户信息 -->
    <view class="post-header">
      <image
        class="avatar"
        :src="avatarUrl"
        mode="aspectFill"
      />
      <view class="user-info">
        <text class="nickname">{{ post.userName || '匿名用户' }}</text>
        <text class="time">{{ formatTime(post.createTime) }}</text>
      </view>
      <view class="header-more" @click.stop="handleMore">
        <Icon name="more" :size="18" color="#999" />
      </view>
    </view>

    <!-- 正文 -->
    <view class="post-body">
      <text class="content-text">{{ post.content }}</text>

      <!-- 图片九宫格 -->
      <view v-if="images.length" class="image-grid" :class="'grid-' + gridClass">
        <image
          v-for="(img, idx) in images"
          :key="idx"
          class="grid-image"
          :src="img"
          mode="aspectFill"
          @click.stop="previewImage(idx)"
        />
      </view>
    </view>

    <!-- 操作栏 -->
    <view class="action-bar">
      <view
        class="action-item"
        :class="{ active: post.isLike }"
        @click.stop="handleLike"
      >
        <Icon
          :name="post.isLike ? 'heart_fill' : 'heart'"
          :size="18"
          :color="post.isLike ? '#FF8C42' : '#666'"
        />
        <text class="action-label">{{ post.likeCount || 0 }}</text>
      </view>
      <view class="action-item" @click.stop="handleComment(post.id)">
        <Icon name="chat" :size="18" color="#666" />
        <text class="action-label">{{ post.commentCount || 0 }}</text>
      </view>
      <view class="action-item" @click.stop="handleShare">
        <Icon name="share" :size="18" color="#666" />
        <text class="action-label">分享</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue';
import { formatTime, fullImageUrl } from '@/utils/index.js';
import { useUserStore } from '@/store/user.js';
import Icon from '@/components/Icon.vue';

const props = defineProps({
  post: {
    type: Object,
    default: () => ({}),
  },
});

const emit = defineEmits(['like', 'comment', 'preview', 'delete']);

const userStore = useUserStore();

const images = computed(() => {
  const val = props.post.images;
  if (!val) return [];
  let arr = val;
  if (!Array.isArray(val)) {
    try { arr = JSON.parse(val); } catch { return []; }
  }
  if (!Array.isArray(arr)) return [];
  return arr.map(fullImageUrl);
});

const avatarUrl = computed(() => {
  return fullImageUrl(props.post.userAvatar) || '/static/default-avatar.png';
});

const isOwner = computed(() => {
  const uInfo = userStore.userInfo;
  if (!uInfo) return false;
  return uInfo.userId === props.post.userId || uInfo.id === props.post.userId;
});

const gridClass = computed(() => {
  const len = images.value.length;
  if (len === 1) return 'one';
  if (len === 2 || len === 4) return 'two';
  return 'multi';
});

const handleLike = () => emit('like', props.post.id);
const handleComment = (id) => emit('comment', id);
const previewImage = (idx) => {
  emit('preview', { images: images.value, current: idx });
};

const handleShare = () => {
  uni.showToast({ title: '分享功能开发中', icon: 'none' });
};

const handleMore = () => {
  if (isOwner.value) {
    uni.showActionSheet({
      itemList: ['删除'],
      itemColor: '#FF4D4F',
      success: (res) => {
        if (res.tapIndex === 0) emit('delete', props.post.id);
      },
    });
  } else {
    emit('comment', props.post.id);
  }
};
</script>

<style lang="scss" scoped>
.post-card {
  background-color: #fff;
  padding: 28rpx 24rpx 20rpx;
  border-bottom: 1rpx solid #F5F5F5;
  position: relative;
}

/* ========== 头部 ========== */
.post-header {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;

  .avatar {
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
    margin-right: 20rpx;
    flex-shrink: 0;
    background-color: #F0F0F0;
  }

  .user-info {
    flex: 1;
    min-width: 0;

    .nickname {
      display: block;
      font-size: 28rpx;
      color: #333;
      font-weight: 600;
      line-height: 1.4;
    }

    .time {
      display: block;
      font-size: 22rpx;
      color: #999;
      margin-top: 4rpx;
    }
  }

  .header-more {
    width: 56rpx;
    height: 56rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;

    &:active {
      background-color: #F5F5F5;
    }
  }
}

/* ========== 正文 ========== */
.post-body {
  .content-text {
    font-size: 30rpx;
    color: #1A1A1A;
    line-height: 1.6;
    word-break: break-all;
    display: block;
    margin-bottom: 16rpx;
  }

  .image-grid {
    display: grid;
    gap: 8rpx;
    border-radius: 12rpx;
    overflow: hidden;
  }

  .grid-one {
    grid-template-columns: 1fr;
    max-width: 500rpx;
    .grid-image { width: 500rpx; height: 500rpx; }
  }

  .grid-two {
    grid-template-columns: repeat(2, 1fr);
    max-width: 520rpx;
    .grid-image { width: 256rpx; height: 256rpx; }
  }

  .grid-multi {
    grid-template-columns: repeat(3, 1fr);
    max-width: 540rpx;
    .grid-image { width: 174rpx; height: 174rpx; }
  }

  .grid-image {
    display: block;
    background-color: #F0F0F0;
    border-radius: 12rpx;
  }
}

/* ========== 操作栏 ========== */
.action-bar {
  display: flex;
  align-items: center;
  margin-top: 16rpx;
  padding-top: 12rpx;
  border-top: 1rpx solid #F5F5F5;

  .action-item {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 12rpx 0;
    border-radius: 12rpx;
    gap: 8rpx;

    &:active {
      background-color: #F7F8FA;
    }

    .action-label {
      font-size: 26rpx;
      color: #666;
    }

    &.active .action-label {
      color: #FF8C42;
    }
  }
}
</style>
