<template>
  <view class="history-page">
    <!-- 会话列表视图 -->
    <scroll-view
      v-if="!activeSession"
      scroll-y
      class="content-scroll"
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <!-- 加载骨架 -->
      <view v-if="loading && !sessions.length" class="loading-wrap">
        <LoadingState mode="skeleton" type="post" :count="4" />
      </view>

      <!-- 会话列表 -->
      <view v-else-if="sessions.length" class="session-list">
        <view
          class="session-item"
          v-for="s in sessions"
          :key="s.id"
          @click="openSession(s)"
        >
          <view class="session-icon">
            <text>💬</text>
          </view>
          <view class="session-info">
            <text class="session-title">{{ s.sessionTitle || '未命名提问' }}</text>
            <view class="session-meta">
              <text class="meta-text">{{ s.messageCount || 0 }} 条消息</text>
              <text class="meta-dot">·</text>
              <text class="meta-text">{{ formatTime(s.updateTime || s.createTime) }}</text>
            </view>
          </view>
          <Icon name="chevron_right" :size="16" color="#C0C4CC" />
        </view>
      </view>

      <!-- 空状态 -->
      <view v-else class="empty-section">
        <view class="empty-icon-wrap">
          <text class="empty-emoji">💬</text>
        </view>
        <text class="empty-title">还没有问答记录</text>
        <text class="empty-desc">有任何养宠问题，随时来问宠迹 AI 助手吧～</text>
        <view class="empty-btn" @click="goChat">
          <text class="empty-btn-text">开始提问 🐾</text>
        </view>
      </view>

      <view class="safe-bottom" />
    </scroll-view>

    <!-- 消息记录视图 -->
    <view v-else class="msg-view">
      <scroll-view scroll-y class="msg-scroll" :scroll-into-view="scrollAnchor">
        <view
          v-for="(msg, idx) in messages"
          :key="msg.id || idx"
          :id="`m-${idx}`"
          class="msg-row"
          :class="msg.role"
        >
          <view class="msg-avatar" v-if="msg.role === 'assistant'">
            <text class="avatar-emoji">🤖</text>
          </view>
          <view class="bubble" :class="msg.role">
            <text class="bubble-text">{{ msg.content }}</text>
          </view>
          <view class="msg-avatar user-avatar" v-if="msg.role === 'user'">
            <image
              v-if="userAvatar"
              class="avatar-img"
              :src="userAvatar"
              mode="aspectFill"
            />
            <Icon v-else name="user" :size="14" color="#fff" />
          </view>
        </view>
        <view class="scroll-bottom-anchor" id="m-bottom" />
      </scroll-view>

      <view class="msg-actions">
        <view class="action-btn primary" @click="continueChat">
          <Icon name="chat" :size="16" color="#fff" />
          <text>继续提问</text>
        </view>
        <view class="action-btn" @click="backToList">
          <Icon name="chevron_left" :size="16" color="#FF8C42" />
          <text>返回列表</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue';
import { onShow, onBackPress } from '@dcloudio/uni-app';
import LoadingState from '@/components/LoadingState.vue';
import Icon from '@/components/Icon.vue';
import { useUserStore } from '@/store/user.js';
import { fullImageUrl } from '@/utils/index.js';
import { getSessionList, getChatHistory } from '@/api/adviser.js';
import { features, fetchFeatures } from '@/config/features.js';
import { showToast } from '@/utils/index.js';

const userStore = useUserStore();
const userInfo = computed(() => userStore.userInfo);

// 用户头像：与对话页保持一致
const userAvatar = computed(() => {
  const url = userInfo.value?.avatar;
  if (!url) return '';
  return fullImageUrl(url);
});

const loading = ref(false);
const refreshing = ref(false);
const sessions = ref([]);

// 消息记录视图状态
const activeSession = ref(null); // 当前查看的会话对象
const messages = ref([]);
const scrollAnchor = ref('');

/**
 * 拉取会话列表
 */
const fetchSessions = async () => {
  loading.value = true;
  try {
    const res = await getSessionList();
    sessions.value = res.data || [];
  } catch (err) {
    // request.js 已统一处理 toast
  } finally {
    loading.value = false;
  }
};

const onRefresh = async () => {
  refreshing.value = true;
  await fetchSessions();
  refreshing.value = false;
};

/**
 * 打开会话，拉取消息记录
 */
const openSession = async (session) => {
  activeSession.value = session;
  messages.value = [];
  try {
    const sessionId = `pet-${session.id}`;
    const res = await getChatHistory(sessionId);
    messages.value = res.data || [];
    await nextTick();
    scrollAnchor.value = '';
    await nextTick();
    scrollAnchor.value = 'm-bottom';
  } catch (err) {
    // request.js 已统一处理 toast
  }
};

/**
 * 继续对话：把 sessionId 写入本地后跳转到对话页
 * 同时写入恢复标记，让对话页加载历史消息并清空当前对话
 */
const RESTORE_KEY = 'adviser_restore';
const SESSION_KEY = 'adviser_session_id';

const continueChat = () => {
  if (!activeSession.value) return;
  const sessionId = `pet-${activeSession.value.id}`;
  // 1. 写入目标会话 ID（对话页会用此 sessionId 加载历史）
  uni.setStorageSync(SESSION_KEY, sessionId);
  // 2. 写入恢复标记（对话页 onShow 时检测到该标记会：清空当前消息 + 加载历史）
  uni.setStorageSync(RESTORE_KEY, '1');
  uni.switchTab({ url: '/pages/chat/index' });
};

const backToList = () => {
  activeSession.value = null;
  messages.value = [];
};

// 原生导航栏点击返回按钮：先判断是否在会话详情子视图
onBackPress(() => {
  if (activeSession.value) {
    backToList();
    return true; // 拦截本次返回，切换回列表
  }
  return false;
});

const goChat = () => uni.switchTab({ url: '/pages/chat/index' });

/**
 * 时间格式化：2026-08-19 15:30:00 → 08-19 15:30
 */
const formatTime = (time) => {
  if (!time) return '';
  // 兼容 "2026-08-19 15:30:00" 与 ISO 格式
  const m = String(time).match(/(\d{2})-(\d{2})[\sT](\d{2}):(\d{2})/);
  return m ? `${m[1]}-${m[2]} ${m[3]}:${m[4]}` : time;
};

onShow(async () => {
  // 拉取功能开关；功能关闭时提示并返回上一页
  await fetchFeatures();
  if (features.adviserEnabled === false) {
    showToast('功能升级中，敬请期待');
    setTimeout(() => {
      const pages = getCurrentPages();
      if (pages.length > 1) {
        uni.navigateBack();
      } else {
        uni.switchTab({ url: '/pages/index/index' });
      }
    }, 800);
    return;
  }
  // 从问答页返回时刷新列表（可能新增了会话）
  if (!activeSession.value) fetchSessions();
});
</script>

<style lang="scss" scoped>
.history-page {
  background-color: $bg-page;
  min-height: 100vh;
}

/* ========== 会话列表视图 ========== */
.content-scroll {
  height: 100vh;
}

.loading-wrap {
  padding: 24rpx;
}

.session-list {
  padding: 24rpx;
}

.session-item {
  display: flex;
  align-items: center;
  background-color: $bg-card;
  border-radius: $radius-lg;
  padding: 26rpx 24rpx;
  margin-bottom: 20rpx;
  box-shadow: $shadow-card;
  transition: transform 0.3s ease;

  &:active {
    transform: scale(0.975);
  }
}

.session-icon {
  width: 76rpx;
  height: 76rpx;
  border-radius: 24rpx;
  background: $gradient-primary-soft;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 22rpx;
  flex-shrink: 0;

  text {
    font-size: 36rpx;
  }
}

.session-info {
  flex: 1;
  min-width: 0;
}

.session-title {
  display: block;
  font-size: $font-lg;
  font-weight: $font-weight-bold;
  color: $text-primary;
  margin-bottom: 8rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-meta {
  display: flex;
  align-items: center;

  .meta-text {
    font-size: $font-xs;
    color: $text-hint;
  }

  .meta-dot {
    margin: 0 8rpx;
    color: $text-placeholder;
  }
}

/* ========== 空状态 ========== */
.empty-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 60rpx 0;

  .empty-icon-wrap {
    width: 180rpx;
    height: 180rpx;
    background: $gradient-card;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 36rpx;
    box-shadow: $shadow-md;
    animation: pet-float 3.2s ease-in-out infinite;

    .empty-emoji {
      font-size: 80rpx;
    }
  }

  .empty-title {
    font-size: $font-lg;
    color: $text-primary;
    font-weight: $font-weight-bold;
    margin-bottom: 14rpx;
  }

  .empty-desc {
    font-size: $font-sm;
    color: $text-hint;
    margin-bottom: 44rpx;
    line-height: 1.6;
    text-align: center;
  }

  .empty-btn {
    display: flex;
    align-items: center;
    background: $gradient-primary;
    padding: 22rpx 56rpx;
    border-radius: $radius-round;
    box-shadow: $shadow-primary;

    &:active { transform: scale(0.96); }

    .empty-btn-text {
      color: #fff;
      font-size: $font-md;
      font-weight: $font-weight-medium;
    }
  }
}

/* ========== 消息记录视图 ========== */
.msg-view {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  height: 100vh;
}

.msg-scroll {
  flex: 1;
  height: calc(100vh - 140rpx); /* 扣除底部操作按钮高度 */
  padding: 24rpx;
  box-sizing: border-box;
}

.msg-row {
  display: flex;
  align-items: flex-end;
  margin-bottom: 24rpx;

  /* 用户消息：气泡在左，头像在右 */
  &.user {
    justify-content: flex-end;
  }
}

.msg-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: $gradient-primary;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: $shadow-primary;
  border: 3rpx solid #fff;

  .avatar-emoji {
    font-size: 32rpx;
  }

  &.user-avatar {
    background: linear-gradient(135deg, #A9B8D8 0%, #7E93B8 100%);
    margin-left: 14rpx; /* 与气泡的间距 */
  }

  .avatar-img {
    width: 100%;
    height: 100%;
    border-radius: 50%;
  }
}

.bubble {
  max-width: 500rpx;
  padding: 20rpx 24rpx;
  border-radius: $radius-lg;
  font-size: $font-md;
  line-height: 1.6;
  word-break: break-all;

  &.assistant {
    background-color: $bg-card;
    color: $text-primary;
    margin-left: 14rpx;
    border-top-left-radius: $radius-sm;
    box-shadow: $shadow-card;
    border: 1rpx solid rgba(0, 0, 0, 0.04);
  }

  &.user {
    background: $gradient-primary;
    color: #fff;
    margin-right: 0;
    border-top-right-radius: $radius-sm;
    box-shadow: $shadow-primary;
  }

  .bubble-text {
    white-space: pre-wrap;
  }
}

.scroll-bottom-anchor {
  height: 1rpx;
}

.msg-actions {
  display: flex;
  gap: 20rpx;
  padding: 20rpx 24rpx calc(env(safe-area-inset-bottom) + 20rpx);
  background-color: $bg-card;
  border-top: 1rpx solid rgba(0, 0, 0, 0.04);
  box-shadow: 0 -4rpx 20rpx rgba(52, 59, 76, 0.05);
}

.action-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  height: 80rpx;
  border-radius: $radius-round;
  font-size: $font-md;
  background: $primary-lighter;
  color: $primary-dark;
  border: 2rpx solid rgba(255, 140, 66, 0.28);

  &.primary {
    background: $gradient-primary;
    color: #fff;
    border-color: transparent;
    box-shadow: $shadow-primary;
  }

  &:active { transform: scale(0.98); }
}

.safe-bottom {
  height: calc(env(safe-area-inset-bottom) + 40rpx);
}
</style>
