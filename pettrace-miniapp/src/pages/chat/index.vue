<template>
  <view class="chat-page">
    <!-- 自定义导航栏 -->
    <view class="custom-nav" :style="{ paddingTop: navBarHeight + 'px' }">
      <view class="nav-content">
        <view class="nav-left">
          <text class="nav-title">养宠助手</text>
          <view class="nav-status">
            <view class="status-dot" :class="{ thinking: loading, offline: !adviserEnabled }" />
            <text class="status-text">{{ statusText }}</text>
          </view>
        </view>
        <view class="nav-actions" v-if="adviserEnabled">
          <view class="nav-action" @click="goHistory">
            <Icon name="file_text" :size="20" color="#fff" />
          </view>
          <view class="nav-action" @click="handleClearHistory">
            <Icon name="refresh" :size="20" color="#fff" />
          </view>
        </view>
      </view>
    </view>

    <!-- 功能关闭态：升级维护提示 -->
    <view v-if="!adviserEnabled" class="maintain-view">
      <view class="maintain-icon-wrap">
        <Icon name="message" :size="48" color="#FFB07A" />
      </view>
      <text class="maintain-title">养宠助手升级中</text>
      <text class="maintain-desc">功能正在打磨升级，敬请期待～</text>
    </view>

    <!-- 消息列表 -->
    <scroll-view
      v-if="adviserEnabled"
      scroll-y
      class="msg-scroll"
      :scroll-top="scrollTop"
      :scroll-with-animation="true"
      :show-scrollbar="false"
    >
      <!-- 欢迎首页：AI 介绍 + 推荐问题标签云 -->
      <view class="welcome-card" v-if="!messages.length">
        <view class="assistant-card">
          <view class="ai-avatar-wrap">
            <text class="ai-avatar-emoji">🤖</text>
            <view class="online-dot" />
          </view>
          <view class="assistant-meta">
            <text class="welcome-title">宠迹 AI 助手</text>
            <view class="welcome-status">
              <text class="welcome-status-dot" />
              <text class="welcome-status-text">在线 · 随时为你解答</text>
            </view>
          </view>
        </view>

        <text class="welcome-desc">科学养宠、健康咨询、日常训练…有疑问就来找我 🐾</text>

        <!-- 能力介绍 -->
        <view class="ability-row">
          <view class="ability-chip"><text class="chip-emoji">🍚</text><text>喂养建议</text></view>
          <view class="ability-chip"><text class="chip-emoji">💉</text><text>疫苗提醒</text></view>
          <view class="ability-chip"><text class="chip-emoji">🏥</text><text>健康咨询</text></view>
          <view class="ability-chip"><text class="chip-emoji">🎓</text><text>行为训练</text></view>
        </view>

        <!-- 推荐问题标签云 -->
        <view class="suggest-title">
          <text class="suggest-title-bar" />
          <text class="suggest-title-text">你可以这样问我</text>
        </view>

        <view class="suggest-tags">
          <view v-if="suggestionsLoading" class="suggest-loading">
            <view class="loading-dot" />
            <view class="loading-dot" />
            <view class="loading-dot" />
            <text class="suggest-loading-text">正在加载推荐问题…</text>
          </view>
          <template v-else>
            <view
              v-for="(s, i) in suggestions"
              :key="i"
              class="suggest-tag"
              :class="'tone-' + (i % 4)"
              @click="sendMessage(s)"
            >
              <text class="suggest-text">{{ s }}</text>
            </view>
            <view v-if="!suggestions.length" class="suggest-empty">
              <text class="suggest-empty-text">直接输入你的问题，和我聊起来吧～</text>
            </view>
          </template>
        </view>
      </view>

      <!-- 消息气泡 -->
      <view
        v-for="(msg, idx) in messages"
        :key="idx"
        :id="`msg-${idx}`"
        class="msg-row"
        :class="msg.role"
      >
        <view class="msg-avatar" v-if="msg.role === 'assistant'">
          <Icon name="message" :size="16" color="#fff" />
        </view>
        <view class="bubble" :class="msg.role">
          <!-- 思考中状态：显示加载点动画 -->
          <view class="thinking-indicator" v-if="msg.thinking && !msg.content">
            <view class="thinking-dot" />
            <view class="thinking-dot" />
            <view class="thinking-dot" />
          </view>
          <!-- 流式输出内容 -->
          <text class="bubble-text">{{ msg.content }}</text>
          <!-- 闪烁光标（正在生成时显示） -->
          <text class="cursor" v-if="msg.generating">|</text>
        </view>
        <view class="msg-avatar user-avatar" v-if="msg.role === 'user'">
          <image
            v-if="userAvatar"
            class="avatar-img"
            :src="userAvatar"
            mode="aspectFill"
          />
          <Icon v-else name="user" :size="16" color="#fff" />
        </view>
      </view>

      <!-- 底部留白：确保最后一条消息不被输入框遮挡 -->
      <view class="scroll-bottom-spacer" />
    </scroll-view>

    <!-- 底部输入栏 -->
    <view v-if="adviserEnabled" class="input-bar" :style="{ paddingBottom: safeBottom + 'px' }">
      <view class="input-wrap">
        <input
          class="input"
          v-model="inputText"
          placeholder="输入你的问题…"
          placeholder-class="input-placeholder"
          confirm-type="send"
          :disabled="loading"
          @confirm="sendMessage()"
          :adjust-position="true"
        />
      </view>
      <view
        class="send-btn"
        :class="{ active: inputText.trim() && !loading, loading: loading }"
        @click="loading ? handleAbort() : sendMessage()"
      >
        <Icon :name="loading ? 'close' : 'share'" :size="16" color="#fff" />
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, nextTick, onUnmounted, watch } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import Icon from '@/components/Icon.vue';
import { useUserStore } from '@/store/user.js';
import { fullImageUrl } from '@/utils/index.js';
import { chatStream, clearChatHistory, getChatHistory, getSuggestWords } from '@/api/adviser.js';
import { showToast, showConfirm } from '@/utils/index.js';
import { features, fetchFeatures } from '@/config/features.js';

const userStore = useUserStore();
const userInfo = computed(() => userStore.userInfo);

// 养宠助手功能开关（关闭时展示升级维护视图）
const adviserEnabled = computed(() => features.adviserEnabled !== false);

// 用户头像：有头像用完整 URL，没有则空串（模板会降级为图标）
const userAvatar = computed(() => {
  const url = userInfo.value?.avatar;
  if (!url) return '';
  return fullImageUrl(url);
});

const navBarHeight = ref(44);
const safeBottom = ref(0);
// #ifdef MP-WEIXIN
try {
  const menuRect = uni.getMenuButtonBoundingClientRect();
  navBarHeight.value = menuRect.bottom + 8;
  const sysInfo = uni.getSystemInfoSync();
  safeBottom.value = sysInfo.safeAreaInsets?.bottom || 0;
} catch (e) {}
// #endif

const inputText = ref('');
const messages = ref([]);
const loading = ref(false);

// ========== 滚动控制：使用 scroll-top 精确滚动到底部 ==========
// 通过查询 scroll-view 的 scrollHeight 来确定滚动位置
const scrollTop = ref(0);
let scrollTimer = null;
const SCROLL_THROTTLE = 80; // 节流时间（ms）

/**
 * 请求滚动到底部（带节流，流式高频调用下合并为一次实际滚动）
 */
const scheduleScrollToBottom = () => {
  if (scrollTimer) return;
  scrollTimer = setTimeout(() => {
    scrollTimer = null;
    scrollToBottom();
  }, SCROLL_THROTTLE);
};

/**
 * 实际执行滚动：测量 scroll-view 内容高度后滚动到底部
 */
const scrollToBottom = () => {
  const query = uni.createSelectorQuery();
  query.select('.msg-scroll').scrollOffset();
  query.select('.msg-scroll').boundingClientRect();
  query.exec((res) => {
    if (!res || !res[0] || !res[1]) return;
    const scrollInfo = res[0];
    const viewRect = res[1];
    // 计算需要滚动到的位置 = 内容总高度 - 可视区域高度
    const maxScroll = Math.max(0, scrollInfo.scrollHeight - viewRect.height);
    // 如果已经接近底部（差值小于20px），则不需要滚动
    const diff = maxScroll - scrollInfo.scrollTop;
    if (diff > 20) {
      // 滚动到底部，加1确保总是触发
      scrollTop.value = maxScroll + 1;
    }
  });
};

// 多轮记忆：sessionId 持久化到本地，跨次进入保持上下文
const SESSION_KEY = 'adviser_session_id';
const RESTORE_KEY = 'adviser_restore';
const sessionId = ref(uni.getStorageSync(SESSION_KEY) || '');

// 状态文案：功能关闭→维护中；问答进行中→回复中；空闲→在线
const statusText = computed(() => {
  if (!adviserEnabled.value) return '维护中';
  return loading.value ? '回复中…' : '在线';
});

// 默认推荐词（接口失败时兜底）
const DEFAULT_SUGGESTIONS = [];
const suggestions = ref([...DEFAULT_SUGGESTIONS]);
const suggestionsLoading = ref(false);

let streamTask = null;

const pushAssistantBubble = () => {
  messages.value.push({ role: 'assistant', content: '', thinking: true, generating: false });
};

/**
 * 发送消息（接入真实 SSE 流式接口）
 */
const sendMessage = async (text) => {
  const content = (text ?? inputText.value).trim();
  if (!content || loading.value) return;

  // 功能关闭拦截
  if (!adviserEnabled.value) {
    showToast('功能升级中，敬请期待');
    return;
  }

  // 未登录拦截
  const token = uni.getStorageSync('token');
  if (!token) {
    showToast('请先登录后再提问');
    return;
  }

  // 推入用户消息
  messages.value.push({ role: 'user', content });
  inputText.value = '';
  loading.value = true;
  pushAssistantBubble();
  // 立即滚动：等待 DOM 更新后执行
  nextTick(() => {
    scrollToBottom();
  });

  // 记录当前回复气泡在数组中的下标，用于流式更新
  const assistantIdx = messages.value.length - 1;

  streamTask = chatStream({
    message: content,
    sessionId: sessionId.value || undefined,
    onChunk: (payload) => {
      // 持续追加增量内容
      const bubble = messages.value[assistantIdx];
      if (bubble) {
        bubble.content += payload.content || '';
        // 收到第一个 chunk 后，从"思考中"切换到"生成中"
        if (bubble.thinking) {
          bubble.thinking = false;
          bubble.generating = true;
        }
      }
      // 服务端首次会返回 sessionId，保存以维持多轮记忆
      if (payload.sessionId && !sessionId.value) {
        sessionId.value = payload.sessionId;
        uni.setStorageSync(SESSION_KEY, payload.sessionId);
      }
      // 滚动：降低频率，每 100ms 最多滚一次
      scheduleScrollToBottom();
    },
    onComplete: (payload) => {
      // 整轮完成：用完整 reply 兜底（防止 chunk 丢失）
      const bubble = messages.value[assistantIdx];
      if (bubble && payload.data?.reply) {
        bubble.content = payload.data.reply;
      }
      if (bubble) {
        bubble.thinking = false;
        bubble.generating = false;
      }
      if (payload.sessionId) {
        sessionId.value = payload.sessionId;
        uni.setStorageSync(SESSION_KEY, payload.sessionId);
      }
    },
    onError: (payload) => {
      const bubble = messages.value[assistantIdx];
      if (bubble) {
        bubble.content = payload.message || '抱歉，出了点小问题，请稍后再试～';
        bubble.thinking = false;
        bubble.generating = false;
      }
      // 401 登录过期由 request.js 统一处理，这里不再重复 toast
      if (payload.message && payload.message.indexOf('登录') === -1) {
        showToast(payload.message);
      }
    },
    onDone: () => {
      loading.value = false;
      streamTask = null;
      const bubble = messages.value[assistantIdx];
      if (bubble) {
        bubble.thinking = false;
        bubble.generating = false;
      }
      scheduleScrollToBottom();
    },
  });
};

/**
 * 中断当前对话
 */
const handleAbort = () => {
  if (streamTask && streamTask.abort) {
    streamTask.abort();
  }
  loading.value = false;
  streamTask = null;
  // 清理最后一个气泡状态
  const last = messages.value[messages.value.length - 1];
  if (last && last.role === 'assistant') {
    if (!last.content) {
      messages.value.pop();
    } else {
      last.thinking = false;
      last.generating = false;
    }
  }
};

/**
 * 清空当前会话记忆
 */
const handleClearHistory = async () => {
  if (!sessionId.value) {
    showToast('当前没有对话记录');
    return;
  }
  const confirmed = await showConfirm('确定清空当前会话的对话记忆吗？');
  if (!confirmed) return;
  try {
    await clearChatHistory(sessionId.value);
    sessionId.value = '';
    uni.removeStorageSync(SESSION_KEY);
    messages.value = [];
    showToast('对话记忆已清空', 'success');
  } catch (err) {
    // request.js 已统一处理 toast
  }
};

/**
 * 跳转对话记录页
 */
const goHistory = () => {
  uni.navigateTo({ url: '/pages/chat/history' });
};

/**
 * 从历史会话加载消息（延续对话）
 */
const loadHistory = async () => {
  if (!sessionId.value) return;
  try {
    const res = await getChatHistory(sessionId.value);
    // 清空当前对话记录，填入历史消息，并确保每条消息有正确的状态字段
    messages.value = (res.data || []).map(msg => ({
      ...msg,
      thinking: false,
      generating: false,
    }));
    // 等待 DOM 更新后滚动到底部
    nextTick(() => {
      scrollToBottom();
    });
  } catch (err) {
    // request.js 已统一处理 toast
  }
};

/**
 * 获取推荐提问词
 */
const fetchSuggestions = async () => {
  // 功能关闭或未登录时不调用接口，保留默认推荐词
  if (!adviserEnabled.value || !userStore.isLogin) return;
  suggestionsLoading.value = true;
  try {
    const res = await getSuggestWords(4);
    if (res.data && res.data.length) {
      suggestions.value = res.data.map((item) => item.word);
    }
  } catch (err) {
    // 接口失败时保留默认推荐词，不阻断页面
    console.error('获取推荐词失败:', err);
  } finally {
    suggestionsLoading.value = false;
  }
};

// 页面显示时同步本地可能被其他页面修改的 sessionId 和用户头像
onShow(async () => {
  // 拉取功能开关（全局只请求一次）；关闭态下中断进行中的请求并跳过数据加载
  await fetchFeatures();
  if (!adviserEnabled.value) {
    handleAbort();
    return;
  }
  sessionId.value = uni.getStorageSync(SESSION_KEY) || '';
  // 检测是否从"对话记录"跳转过来（需要恢复历史会话）
  const needRestore = uni.getStorageSync(RESTORE_KEY);
  if (needRestore) {
    uni.removeStorageSync(RESTORE_KEY);
    // 清空当前正在对话的记录，加载历史会话
    messages.value = [];
    await loadHistory();
  }
  // 确保用户头像最新（可能在其他页面更新了头像）
  if (userStore.isLogin) {
    userStore.fetchUserInfo().catch(() => {});
  }
  // 获取推荐提问词
  fetchSuggestions();
});

// 监听登录/退出状态变化，自动更新头像显示和推荐词
watch(() => userStore.isLogin, (isLogin) => {
  if (isLogin && adviserEnabled.value) {
    userStore.fetchUserInfo().catch(() => {});
    fetchSuggestions();
  }
});

onUnmounted(() => {
  handleAbort();
});
</script>

<style lang="scss" scoped>
.chat-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: linear-gradient(180deg, #FFEDDC 0%, #F8F9FC 26%, #F8F9FC 100%);
}

/* ========== 功能关闭态：升级维护提示 ========== */
.maintain-view {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 0 60rpx;

  .maintain-icon-wrap {
    width: 140rpx;
    height: 140rpx;
    border-radius: 50%;
    background: linear-gradient(135deg, #FFF0E6, #FFE4CC);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 32rpx;
  }

  .maintain-title {
    font-size: 34rpx;
    font-weight: 700;
    color: #3D2B1D;
    margin-bottom: 16rpx;
  }

  .maintain-desc {
    font-size: 26rpx;
    color: #A8A8B0;
    text-align: center;
  }
}

/* ========== 自定义导航栏 ========== */
.custom-nav {
  background: $gradient-primary;
  padding: 0 32rpx 28rpx;
  box-shadow: 0 6rpx 24rpx rgba(255, 126, 61, 0.18);

  .nav-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16rpx 0 8rpx;
    position: relative;
  }

  .nav-left {
    position: absolute;
    left: 0;
    right: 0;
    display: flex;
    flex-direction: column;
    align-items: center;
  }

  .nav-title {
    font-size: 38rpx;
    font-weight: 700;
    color: #fff;
    line-height: 1.3;
  }

  .nav-status {
    display: flex;
    align-items: center;
    margin-top: 6rpx;

    .status-dot {
      width: 12rpx;
      height: 12rpx;
      border-radius: 50%;
      background-color: #4ADE80;
      margin-right: 8rpx;
      box-shadow: 0 0 8rpx rgba(74, 222, 128, 0.8);

      &.thinking {
        background-color: #FBBF24;
        box-shadow: 0 0 8rpx rgba(251, 191, 36, 0.8);
        animation: pulse 1s infinite;
      }

      &.offline {
        background-color: rgba(255, 255, 255, 0.55);
        box-shadow: none;
      }
    }

    .status-text {
      font-size: 22rpx;
      color: rgba(255, 255, 255, 0.9);
    }
  }

  .nav-actions {
    display: flex;
    align-items: center;
    gap: 16rpx;
    margin-left: auto;
    position: relative;
    z-index: 1;
  }

  .nav-action {
    width: 56rpx;
    height: 56rpx;
    border-radius: 50%;
    background-color: rgba(255, 255, 255, 0.25);
    display: flex;
    align-items: center;
    justify-content: center;
    border: 2rpx solid rgba(255, 255, 255, 0.4);

    &:active {
      background-color: rgba(255, 255, 255, 0.4);
    }
  }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

/* ========== 消息列表 ========== */
.msg-scroll {
  flex: 1;
  min-height: 0; /* 关键：允许 flex 子项收缩，从而使内部滚动生效 */
  padding: 24rpx 24rpx 0; /* 只设顶部和左右 padding，底部靠 spacer 撑开 */
  box-sizing: border-box;
  // 自动锚定到底部内容，减少流式输出时的跳动
  overflow-anchor: auto;
  -webkit-overflow-scrolling: touch;
}

.scroll-bottom-spacer {
  height: 160rpx; /* 足够容纳输入栏（约 72rpx）+ 安全间距 */
}

.welcome-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40rpx 28rpx 24rpx;

  /* AI 助手介绍 */
  .assistant-card {
    display: flex;
    align-items: center;
    width: 100%;
    padding: 32rpx 36rpx;
    border-radius: $radius-xl;
    background: $gradient-card;
    border: 1rpx solid rgba(255, 140, 66, 0.12);
    box-shadow: $shadow-card;

    .ai-avatar-wrap {
      position: relative;
      width: 116rpx;
      height: 116rpx;
      border-radius: 38rpx;
      background: $gradient-primary;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: $shadow-primary;
      flex-shrink: 0;
      animation: pet-float 3.4s ease-in-out infinite;

      .ai-avatar-emoji {
        font-size: 60rpx;
      }

      .online-dot {
        position: absolute;
        right: -4rpx;
        bottom: -4rpx;
        width: 26rpx;
        height: 26rpx;
        border-radius: 50%;
        background: $accent-green;
        border: 4rpx solid #fff;
        box-shadow: 0 2rpx 8rpx rgba(123, 198, 126, 0.5);
      }
    }

    .assistant-meta {
      margin-left: 30rpx;
      flex: 1;
      min-width: 0;
    }

    .welcome-title {
      display: block;
      font-size: 42rpx;
      font-weight: $font-weight-bold;
      color: $text-primary;
    }

    .welcome-status {
      display: flex;
      align-items: center;
      margin-top: 12rpx;

      .welcome-status-dot {
        width: 12rpx;
        height: 12rpx;
        border-radius: 50%;
        background: $accent-green;
        margin-right: 8rpx;
        animation: pet-breath 2.2s ease-in-out infinite;
      }

      .welcome-status-text {
        font-size: $font-xs;
        color: $text-secondary;
      }
    }
  }

  .welcome-desc {
    width: 100%;
    font-size: $font-sm;
    color: $text-secondary;
    line-height: 1.7;
    margin-top: 24rpx;
    text-align: left;
  }

  /* 能力介绍标签 */
  .ability-row {
    display: flex;
    flex-wrap: wrap;
    gap: 14rpx;
    width: 100%;
    margin-top: 24rpx;

    .ability-chip {
      display: inline-flex;
      align-items: center;
      gap: 8rpx;
      padding: 10rpx 22rpx;
      border-radius: $radius-round;
      background: #fff;
      box-shadow: $shadow-sm;
      border: 1rpx solid $bg-input;

      text {
        font-size: $font-xs;
        color: $text-secondary;
        font-weight: $font-weight-medium;
      }

      .chip-emoji {
        font-size: 26rpx;
      }
    }
  }

  /* 推荐标题 */
  .suggest-title {
    display: flex;
    align-items: center;
    align-self: flex-start;
    margin-top: 40rpx;

    .suggest-title-bar {
      width: 8rpx;
      height: 30rpx;
      border-radius: $radius-round;
      background: $gradient-primary;
      margin-right: 14rpx;
    }

    .suggest-title-text {
      font-size: $font-md;
      color: $text-primary;
      font-weight: $font-weight-bold;
    }
  }

  /* 标签云 */
  .suggest-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 16rpx;
    width: 100%;
    margin-top: 24rpx;
    justify-content: center;
  }

  .suggest-tag {
    padding: 20rpx 30rpx;
    border-radius: $radius-round;
    transition: transform 0.3s ease, box-shadow 0.3s ease;
    box-shadow: $shadow-sm;

    .suggest-text {
      font-size: $font-sm;
      font-weight: $font-weight-medium;
      line-height: 1.4;
    }

    &:active {
      animation: pet-pop 0.45s ease;
      box-shadow: $shadow-md;
    }

    &.tone-0 {
      background: $primary-lighter;
      .suggest-text { color: $primary-dark; }
    }

    &.tone-1 {
      background: #E8F3FF;
      .suggest-text { color: #3C78C2; }
    }

    &.tone-2 {
      background: #E8F7EB;
      .suggest-text { color: #3E8F46; }
    }

    &.tone-3 {
      background: #FFEBF0;
      .suggest-text { color: #C25A76; }
    }
  }

  .suggest-empty {
    padding: 30rpx 0;

    .suggest-empty-text {
      font-size: $font-sm;
      color: $text-hint;
    }
  }

  .suggest-loading {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10rpx;
    width: 100%;
    padding: 50rpx 0;

    .loading-dot {
      width: 14rpx;
      height: 14rpx;
      border-radius: 50%;
      background-color: $primary;
      animation: pet-dotBounce 1.2s infinite ease-in-out;

      &:nth-child(2) { animation-delay: 0.15s; }
      &:nth-child(3) { animation-delay: 0.3s; }
    }

    .suggest-loading-text {
      font-size: $font-xs;
      color: $text-hint;
      margin-left: 8rpx;
    }
  }
}

.msg-row {
  display: flex;
  align-items: flex-end;
  margin-bottom: 28rpx;
  // 消息出现时的平滑动画
  animation: msgFadeIn 0.3s ease-out;

  /* 用户消息：气泡在左，头像在右 */
  &.user {
    justify-content: flex-end;
  }
}

@keyframes msgFadeIn {
  from {
    opacity: 0;
    transform: translateY(12rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.msg-avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: $gradient-primary;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: $shadow-primary;
  border: 3rpx solid #fff;

  &.user-avatar {
    background: linear-gradient(135deg, #A9B8D8 0%, #7E93B8 100%);
    box-shadow: 0 4rpx 12rpx rgba(126, 147, 184, 0.24);
    border-color: #fff;
  }

  .avatar-img {
    width: 100%;
    height: 100%;
    border-radius: 50%;
    border: 2rpx solid #fff;
  }
}

.bubble {
  max-width: 500rpx;
  padding: 22rpx 26rpx;
  border-radius: $radius-lg;
  font-size: $font-md;
  line-height: 1.6;
  word-break: break-all;

  &.assistant {
    background-color: #fff;
    color: $text-primary;
    margin-left: 16rpx;
    border-top-left-radius: $radius-sm;
    border: 1rpx solid rgba(0, 0, 0, 0.04);
    box-shadow: $shadow-card;
    // 流式输出时的平滑过渡
    transition: min-height 0.2s ease;
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

  // 思考中指示器
  .thinking-indicator {
    display: flex;
    align-items: center;
    gap: 8rpx;
    padding: 8rpx 0;
  }

  // 闪烁光标
  .cursor {
    display: inline-block;
    color: $primary;
    font-weight: 300;
    animation: cursorBlink 0.8s infinite;
    margin-left: 2rpx;
  }
}

// 思考中的加载点
.thinking-dot {
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
  background-color: $primary;
  animation: pet-dotBounce 1.3s infinite ease-in-out;

  &:nth-child(2) { animation-delay: 0.2s; }
  &:nth-child(3) { animation-delay: 0.4s; }
}

@keyframes cursorBlink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

// 建议词加载使用的动画
@keyframes dotBounce {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.4; }
  30% { transform: translateY(-8rpx); opacity: 1; }
}

/* ========== 底部输入栏 ========== */
.input-bar {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 16rpx 24rpx 20rpx;
  background-color: #fff;
  border-top: 1rpx solid rgba(0, 0, 0, 0.04);
  box-shadow: 0 -4rpx 20rpx rgba(52, 59, 76, 0.05);
}

.input-wrap {
  flex: 1;
  background-color: $bg-input;
  border-radius: $radius-round;
  padding: 0 28rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  border: 2rpx solid transparent;
}

.input {
  width: 100%;
  height: 72rpx;
  font-size: 28rpx;
  color: #3D2B1D;
}

.input-placeholder {
  color: #C0C4CC;
  font-size: 28rpx;
}

.send-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background-color: #E0E0E0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;

  &.active {
    background: $gradient-primary;
    box-shadow: $shadow-primary;
  }

  &.loading {
    background: linear-gradient(135deg, $danger, #E54949);
    box-shadow: 0 4rpx 12rpx rgba(255, 107, 107, 0.3);
  }

  &:active {
    transform: scale(0.92);
  }
}
</style>
