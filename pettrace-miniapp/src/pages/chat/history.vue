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
            <Icon name="message" :size="18" color="#FF7E3D" />
          </view>
          <view class="session-info">
            <text class="session-title">{{ s.sessionTitle || '未命名对话' }}</text>
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
          <Icon name="message" :size="64" color="#FFD4A8" />
        </view>
        <text class="empty-title">还没有对话记录</text>
        <text class="empty-desc">去和 AI 助手聊聊吧</text>
        <view class="empty-btn" @click="goChat">
          <Icon name="plus" :size="16" color="#fff" />
          <text>开始对话</text>
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
            <Icon name="message" :size="14" color="#fff" />
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
          <text>继续对话</text>
        </view>
        <view class="action-btn" @click="backToList">
          <Icon name="chevron_left" :size="16" color="#FF7E3D" />
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
import { getSessionList, getChatHistory } from '@/api/ai.js';

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
const RESTORE_KEY = 'ai_chat_restore';
const SESSION_KEY = 'ai_chat_session_id';

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

onShow(() => {
  // 从对话页返回时刷新列表（可能新增了会话）
  if (!activeSession.value) fetchSessions();
});
</script>

<style lang="scss" scoped>
.history-page {
  background-color: #F6F7FB;
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
  background-color: #fff;
  border-radius: 24rpx;
  padding: 28rpx 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);

  &:active {
    background-color: #FFF9F4;
    transform: scale(0.99);
  }
}

.session-icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: 20rpx;
  background-color: #FFF1E7;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
  flex-shrink: 0;
}

.session-info {
  flex: 1;
  min-width: 0;
}

.session-title {
  display: block;
  font-size: 30rpx;
  font-weight: 600;
  color: #3D2B1D;
  margin-bottom: 8rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-meta {
  display: flex;
  align-items: center;

  .meta-text {
    font-size: 22rpx;
    color: #A8A8B0;
  }

  .meta-dot {
    margin: 0 8rpx;
    color: #C0C4CC;
  }
}

/* ========== 空状态 ========== */
.empty-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 60rpx 0;

  .empty-icon-wrap {
    width: 140rpx;
    height: 140rpx;
    background: linear-gradient(135deg, #FFF0E6, #FFE4CC);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 28rpx;
  }

  .empty-title {
    font-size: 32rpx;
    color: #3D2B1D;
    font-weight: 600;
    margin-bottom: 12rpx;
  }

  .empty-desc {
    font-size: 26rpx;
    color: #909399;
    margin-bottom: 40rpx;
  }

  .empty-btn {
    display: flex;
    align-items: center;
    background: linear-gradient(135deg, #FF934F, #FF7E3D);
    color: #fff;
    font-size: 30rpx;
    padding: 22rpx 56rpx;
    border-radius: 44rpx;
    box-shadow: 0 8rpx 20rpx rgba(255, 126, 61, 0.3);

    text { margin-left: 8rpx; }

    &:active { transform: scale(0.96); }
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
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #FF934F, #FF7E3D);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &.user-avatar {
    background: linear-gradient(135deg, #9CA3AF, #6B7280);
    margin-left: 14rpx; /* 与气泡的间距 */
  }

  .avatar-img {
    width: 100%;
    height: 100%;
    border-radius: 50%;
  }
}

.bubble {
  max-width: 460rpx;
  padding: 18rpx 22rpx;
  border-radius: 22rpx;
  font-size: 28rpx;
  line-height: 1.6;
  word-break: break-all;

  &.assistant {
    background-color: #fff;
    color: #3D2B1D;
    margin-left: 14rpx;
    border-bottom-left-radius: 6rpx;
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
  }

  &.user {
    background: linear-gradient(135deg, #FF934F, #FF7E3D);
    color: #fff;
    margin-right: 0;
    border-bottom-right-radius: 6rpx;
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
  background-color: #fff;
  border-top: 1rpx solid #F0F0F0;
}

.action-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  height: 80rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
  background-color: #FFF1E7;
  color: #FF7E3D;
  border: 2rpx solid #FFE0C7;

  &.primary {
    background: linear-gradient(135deg, #FF934F, #FF7E3D);
    color: #fff;
    border-color: transparent;
    box-shadow: 0 6rpx 16rpx rgba(255, 126, 61, 0.25);
  }

  &:active { transform: scale(0.98); }
}

.safe-bottom {
  height: calc(env(safe-area-inset-bottom) + 40rpx);
}
</style>
