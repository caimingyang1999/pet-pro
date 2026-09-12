<script setup>
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app';
import { useUserStore } from '@/store/user.js';
import { fetchFeatures } from '@/config/features.js';

onLaunch(() => {
  console.log('App Launch');
  const userStore = useUserStore();
  // 恢复本地缓存的登录态（未登录时保持游客身份，不做任何强制跳转）
  userStore.initUserInfo();
  // 拉取远程功能开关（静默失败，不影响启动；结果全局缓存复用）
  fetchFeatures();
});

onShow(() => {
  console.log('App Show');
});

onHide(() => {
  console.log('App Hide');
});
</script>

<style lang="scss">
@import "uview-plus/index.scss";

page {
  background-color: $bg-page;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: $text-primary;
  font-size: $font-md;

  /* uView Plus 组件库主题变量覆盖 */
  --up-primary: #FF8C42;
  --up-primary-dark: #F06E2D;
  --up-primary-disabled: #FFC8A2;
  --up-primary-light: #FFE8D6;
  --u-primary: #FF8C42;
  --u-primary-dark: #F06E2D;
  --u-primary-disabled: #FFC8A2;
  --u-primary-light: #FFE8D6;

  --up-warning: #FFC94D;
  --up-warning-dark: #F0AE2A;
  --up-warning-light: #FFF6DF;
  --up-success: #7BC67E;
  --up-success-dark: #5DAD61;
  --up-success-light: #EBF7EC;
  --up-error: #FF6B6B;
  --up-error-dark: #F04F4F;
  --up-error-light: #FFEBEE;
  --up-info: #A0A0A0;
  --up-info-dark: #7F818C;
  --up-info-light: #F0F1F5;

  --u-main-color: #2D2D2D;
  --u-content-color: #6B6B6B;
  --u-tips-color: #A0A0A0;
  --u-border-color: #F0F1F5;
  --u-bg-color: #F8F9FC;
}

view, text, scroll-view {
  box-sizing: border-box;
}

image {
  display: block;
}

button {
  &::after {
    border: none;
  }
}

/* ============================================================
 *  全局通用工具类（跨页面复用，避免重复声明）
 * ============================================================ */
.pet-card {
  background: $bg-card;
  border-radius: $radius-lg;
  box-shadow: $shadow-card;
}

.pet-press {
  transition: transform 0.3s ease, opacity 0.3s ease;
  &:active {
    transform: scale(0.96);
  }
}

.pet-ellipsis {
  @include pet-ellipsis;
}

.pet-line2 {
  @include pet-multi-ellipsis(2);
}

.pet-line3 {
  @include pet-multi-ellipsis(3);
}

/* ============================================================
 *  全局关键帧动画（pet- 前缀避免与组件库冲突）
 * ============================================================ */

/* 悬浮按钮脉冲光圈 */
@keyframes pet-pulse {
  0% { box-shadow: 0 0 0 0 rgba(255, 140, 66, 0.42); }
  70% { box-shadow: 0 0 0 24rpx rgba(255, 140, 66, 0); }
  100% { box-shadow: 0 0 0 0 rgba(255, 140, 66, 0); }
}

/* 弹跳动画（积分 / 标签 / 图标） */
@keyframes pet-bounce {
  0% { transform: scale(1); }
  30% { transform: scale(1.32); }
  50% { transform: scale(0.9); }
  70% { transform: scale(1.12); }
  100% { transform: scale(1); }
}

/* 卡片淡入上移 */
@keyframes pet-fadeInUp {
  from { opacity: 0; transform: translateY(24rpx); }
  to { opacity: 1; transform: translateY(0); }
}

/* 卡片淡入缩放 */
@keyframes pet-fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

/* AI 三点跳动 */
@keyframes pet-dotBounce {
  0%, 80%, 100% { transform: translateY(0) scale(0.72); opacity: 0.55; }
  40% { transform: translateY(-10rpx) scale(1); opacity: 1; }
}

/* 旋转（加载 / 眼睛切换） */
@keyframes pet-spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 抖动（添加卡片按钮 / 提示） */
@keyframes pet-shake {
  0%, 100% { transform: translateX(0); }
  20% { transform: translateX(-10rpx) rotate(-2deg); }
  40% { transform: translateX(10rpx) rotate(2deg); }
  60% { transform: translateX(-6rpx); }
  80% { transform: translateX(6rpx); }
}

/* 轻微上下漂浮（爪印 / 装饰元素） */
@keyframes pet-float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-12rpx); }
}

/* 进入屏动画：放大后回弹 */
@keyframes pet-pop {
  0% { transform: scale(0.6); opacity: 0; }
  70% { transform: scale(1.08); opacity: 1; }
  100% { transform: scale(1); opacity: 1; }
}

/* 呼吸动效（操作栏等） */
@keyframes pet-breath {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.72; transform: scale(0.97); }
}

/* 列表进入时的交错淡入 */
@keyframes pet-slideIn {
  from { opacity: 0; transform: translateY(16rpx) scale(0.985); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

/* 无滚动条内容区（横向分类导航） */
.pet-scroll-x {
  white-space: nowrap;
  overflow-x: scroll;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
  &::-webkit-scrollbar {
    display: none;
    width: 0;
    height: 0;
  }
}
</style>
