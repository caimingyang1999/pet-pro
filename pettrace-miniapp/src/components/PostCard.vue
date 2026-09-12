<template>
  <view class="post-card">
    <!-- 头部：头像 + 用户信息 + 关注 / 更多 -->
    <view class="post-header">
      <view class="header-left" @click="goUserHome">
        <image class="avatar" :src="avatarUrl" mode="aspectFill" />
        <view class="user-info">
          <view class="name-line">
            <text class="nickname">{{ post.userName || '匿名用户' }}</text>
            <text class="owner-tag" v-if="isOwner">我</text>
          </view>
          <view class="time-line">
            <text class="paw-icon">🐾</text>
            <text class="time">{{ formatTime(post.createTime) }}</text>
          </view>
        </view>
      </view>

      <!-- 关注按钮（仅自己不显示，搜索等场景可隐藏） -->
      <view
        v-if="!isOwner && !hideFollow"
        class="follow-btn"
        :class="{ active: post.isFollowed }"
        @click.stop="handleToggleFollow"
      >
        <text>{{ post.isFollowed ? '已关注' : '+ 关注' }}</text>
      </view>

      <!-- 更多操作（自己 -> 删除，别人 -> 评论） -->
      <view class="header-more" @click.stop="handleMore">
        <Icon name="more" :size="18" color="#B0B2BE" />
      </view>
    </view>

    <!-- 正文 -->
    <view class="post-body">
      <!-- 内容：最多 3 行，超出显示查看全文 -->
      <view v-if="post.content" class="content-area" @click="handleComment(post.id)">
        <text class="content-text" :class="{ clamped: contentTooLong }">{{ post.content }}</text>
        <text v-if="contentTooLong" class="read-more">... 查看全文</text>
      </view>

      <!-- 视频区域 -->
      <view v-if="videoUrl" class="video-wrap">
        <video
          :src="videoUrl"
          :poster="videoCover || ''"
          class="video-player"
          controls
          show-center-play-btn
          object-fit="cover"
        />
      </view>

      <!-- 图片九宫格（无视频时才显示） -->
      <view v-else-if="images.length" class="image-grid" :class="'grid-' + gridClass">
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

    <!-- 底部操作栏 -->
    <view class="action-bar">
      <view
        class="action-item like-item"
        :class="{ active: post.isLike, liking }"
        @click.stop="handleLikeTap"
      >
        <Icon
          :name="post.isLike ? 'heart_fill' : 'heart'"
          :size="20"
          :color="post.isLike ? '#FF6B6B' : '#8A8D9A'"
        />
        <text class="action-label">{{ post.likeCount || 0 }}</text>
      </view>

      <view class="action-item" @click.stop="handleComment(post.id)">
        <Icon name="chat" :size="19" color="#8A8D9A" />
        <text class="action-label">{{ post.commentCount || 0 }}</text>
      </view>

      <view class="action-item" @click.stop="handleShare">
        <Icon name="share" :size="19" color="#8A8D9A" />
        <text class="action-label share-label">分享</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, nextTick, computed } from 'vue';
import { formatTime, fullImageUrl } from '@/utils/index.js';
import { useUserStore } from '@/store/user.js';
import Icon from '@/components/Icon.vue';
import { toggleFollow as toggleFollowApi } from '@/api/follow.js';
import { showToast } from '@/utils/index.js';
import { requireLogin } from '@/utils/auth.js';

const props = defineProps({
  post: {
    type: Object,
    default: () => ({}),
  },
  /** 是否隐藏关注按钮（搜索页等场景下避免误触） */
  hideFollow: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(['like', 'comment', 'preview', 'delete', 'follow', 'goUser']);

const userStore = useUserStore();
const liking = ref(false);

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

const videoUrl = computed(() => {
  return props.post.videoUrl ? fullImageUrl(props.post.videoUrl) : '';
});

const videoCover = computed(() => {
  return props.post.videoCover ? fullImageUrl(props.post.videoCover) : '';
});

const avatarUrl = computed(() => {
  return fullImageUrl(props.post.userAvatar) || '/static/default-avatar.png';
});

const isOwner = computed(() => {
  const uInfo = userStore.userInfo;
  if (!uInfo) return false;
  return uInfo.userId === props.post.userId || uInfo.id === props.post.userId;
});

/** 判断正文是否过长（约 3 行 / 66 字），过长时展示"查看全文" */
const contentTooLong = computed(() => {
  const content = props.post.content || '';
  return content.length > 66;
});

const gridClass = computed(() => {
  const len = images.value.length;
  if (len === 1) return 'one';
  if (len === 2 || len === 4) return 'two';
  return 'multi';
});

/** 点赞：本地触发一次弹跳动画 */
const handleLikeTap = () => {
  liking.value = false;
  nextTick(() => {
    liking.value = true;
  });
  emit('like', props.post.id);
};

const handleComment = (id) => emit('comment', id);
const previewImage = (idx) => {
  emit('preview', { images: images.value, current: idx });
};

const handleShare = () => {
  uni.showToast({ title: '分享功能开发中', icon: 'none' });
};

const goUserHome = () => {
  if (props.post.userId && !isOwner.value) {
    uni.navigateTo({ url: `/pages/user/home?userId=${props.post.userId}` });
  }
  emit('goUser', props.post.userId);
};

const handleToggleFollow = async () => {
  // 关注需登录，由用户自行选择是否登录
  if (!(await requireLogin('关注 TA'))) return;
  try {
    const res = await toggleFollowApi(props.post.userId);
    const nowFollowed = res.followed !== undefined ? res.followed : !props.post.isFollowed;
    emit('follow', { userId: props.post.userId, followed: nowFollowed });
  } catch (err) {
    showToast(err?.msg || '操作失败');
  }
};

const handleMore = () => {
  if (isOwner.value) {
    uni.showActionSheet({
      itemList: ['删除'],
      itemColor: '#FF6B6B',
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
  margin: 0 24rpx 24rpx;
  padding: 28rpx 24rpx 16rpx;
  background: $bg-card;
  border-radius: $radius-lg;
  box-shadow: $shadow-card;
  animation: pet-fadeInUp 0.45s ease both;
  transition: transform 0.3s ease, box-shadow 0.3s ease;

  &:active {
    transform: scale(0.985);
    box-shadow: $shadow-sm;
  }
}

/* ========== 头部 ========== */
.post-header {
  display: flex;
  align-items: center;
  margin-bottom: 22rpx;

  .header-left {
    display: flex;
    align-items: center;
    flex: 1;
    min-width: 0;
  }

  .avatar {
    width: 84rpx;
    height: 84rpx;
    border-radius: 50%;
    margin-right: 20rpx;
    flex-shrink: 0;
    background-color: $bg-input;
    border: 2rpx solid $primary;
    padding: 2rpx;
    box-sizing: content-box;
  }

  .user-info {
    flex: 1;
    min-width: 0;

    .name-line {
      display: flex;
      align-items: center;
      gap: 10rpx;
    }

    .nickname {
      font-size: $font-lg;
      color: $text-primary;
      font-weight: $font-weight-bold;
      line-height: 1.3;
      @include pet-ellipsis;
      max-width: 240rpx;
    }

    .owner-tag {
      font-size: $font-xs;
      color: $primary;
      background: $primary-lighter;
      padding: 2rpx 14rpx;
      border-radius: $radius-round;
      line-height: 1.6;
      flex-shrink: 0;
    }

    .time-line {
      display: flex;
      align-items: center;
      gap: 6rpx;
      margin-top: 6rpx;

      .paw-icon {
        font-size: 20rpx;
      }

      .time {
        font-size: $font-xs;
        color: $text-hint;
      }
    }
  }

  .follow-btn {
    padding: 8rpx 22rpx;
    border-radius: $radius-round;
    font-size: $font-sm;
    color: $text-white;
    background: $gradient-primary;
    box-shadow: $shadow-primary;
    flex-shrink: 0;
    margin-right: 12rpx;
    transition: all 0.3s ease;

    &.active {
      color: $text-secondary;
      background: $bg-input;
      box-shadow: none;
    }

    &:active {
      transform: scale(0.96);
      opacity: 0.86;
    }
  }

  .header-more {
    width: 56rpx;
    height: 56rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    flex-shrink: 0;
    transition: background-color 0.3s ease;

    &:active {
      background-color: $bg-input;
    }
  }
}

/* ========== 正文 ========== */
.post-body {
  .content-area {
    display: flex;
    flex-wrap: wrap;
    margin-bottom: 16rpx;
  }

  .content-text {
    font-size: $font-md;
    color: #3A3A3A;
    line-height: 1.6;
    word-break: break-all;
    width: 100%;

    &.clamped {
      @include pet-multi-ellipsis(3);
    }
  }

  .read-more {
    margin-top: 6rpx;
    font-size: $font-sm;
    color: $primary;
  }

  .video-wrap {
    margin-top: 4rpx;
  }

  .video-player {
    width: 100%;
    height: 460rpx;
    border-radius: $radius-sm;
    background-color: #000;
  }

  .image-grid {
    display: grid;
    gap: 8rpx;
    margin-top: 4rpx;
  }

  .grid-one {
    grid-template-columns: 1fr;
    max-width: 500rpx;
    .grid-image { width: 500rpx; height: 460rpx; }
  }

  .grid-two {
    grid-template-columns: repeat(2, 1fr);
    max-width: 540rpx;
    .grid-image { width: 262rpx; height: 262rpx; }
  }

  .grid-multi {
    grid-template-columns: repeat(3, 1fr);
    max-width: 540rpx;
    .grid-image { width: 172rpx; height: 172rpx; }
  }

  .grid-image {
    display: block;
    background-color: $bg-input;
    border-radius: $radius-sm;
  }
}

/* ========== 操作栏 ========== */
.action-bar {
  display: flex;
  align-items: center;
  margin-top: 16rpx;
  padding: 8rpx 0 4rpx;

  .action-item {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 12rpx 0;
    border-radius: $radius-md;
    gap: 10rpx;
    transition: background-color 0.3s ease, transform 0.3s ease;

    &:active {
      background-color: $bg-input;
      transform: scale(0.96);
    }

    .action-label {
      font-size: $font-sm;
      color: $text-hint;
      font-weight: $font-weight-medium;
    }

    &.active {
      .action-label {
        color: $danger;
      }

      &.liking {
        animation: pet-bounce 0.55s ease;
      }
    }
  }

  .share-label {
    animation: pet-breath 3.2s ease-in-out infinite;
  }
}
</style>
