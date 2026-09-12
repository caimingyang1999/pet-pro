<template>
  <view class="login-page">
    <!-- 顶部渐变氛围 -->
    <view class="page-bg">
      <text class="bg-paw paw-1">🐾</text>
      <text class="bg-paw paw-2">🐾</text>
      <text class="bg-paw paw-3">🐾</text>
    </view>

    <!-- ========== 入口视图 ========== -->
    <view v-if="!showForm" class="login-entry">
      <!-- 品牌 Logo -->
      <view class="brand-zone">
        <view class="logo-paw-wrap">
          <view class="paw-ring">
            <text class="ring-emoji">🐾</text>
          </view>
        </view>
        <text class="brand-name">宠迹</text>
        <text class="welcome-slogan">欢迎来到宠迹 🐾</text>
        <text class="welcome-sub">记录毛孩子的成长点滴</text>
      </view>

      <view class="login-actions">
        <!--
          手机号快捷登录：依赖微信「获取手机号」能力，个人主体小程序不支持，
          点击会返回「未开通获取手机号权限」错误，故上线期间隐藏。
          后续升级为企业主体后，取消下方注释即可恢复。
        -->
        <!--
        <button
          v-if="agreed"
          class="primary-btn pet-press"
          open-type="getPhoneNumber"
          @getphonenumber="handleWxPhoneLogin"
        >
          <view class="btn-icon-wrap">
            <Icon name="phone" color="#fff" size="20" />
          </view>
          <text>手机号快捷登录</text>
        </button>
        <view v-else class="primary-btn pet-press" @click="remindAgreement">
          <view class="btn-icon-wrap">
            <Icon name="phone" color="#fff" size="20" />
          </view>
          <text>手机号快捷登录</text>
        </view>
        -->

        <!-- 主按钮：账号密码登录（当前唯一可用登录方式，协议勾选移至表单页） -->
        <view class="primary-btn pet-press" @click="showForm = true">
          <view class="btn-icon-wrap">
            <Icon name="user" color="#fff" size="20" />
          </view>
          <text>账号密码登录</text>
        </view>

        <!-- 协议勾选：随「提交登录」动作一起移到表单页，此处不再展示 -->
        <!--
        <view class="agreement-row">
          <view class="checkbox" :class="{ checked: agreed }" @click="toggleAgreement">
            <Icon v-if="agreed" name="check" color="#fff" size="12" />
          </view>
          <view class="agreement-text">
            <text>我已阅读并同意</text>
            <text class="link" @click.stop="goAgreement">《用户协议》</text>
            <text>与</text>
            <text class="link" @click.stop="goPrivacy">《隐私政策》</text>
          </view>
        </view>
        -->

        <!-- 分隔线 -->
        <view class="divider-row">
          <view class="divider-line" />
          <text class="divider-text">还没有账号？</text>
          <view class="divider-line" />
        </view>

        <view class="register-link pet-press" @click="goRegister">
          <text>注册新账号</text>
        </view>
      </view>
    </view>

    <!-- ========== 账号密码表单视图 ========== -->
    <view v-else class="login-form">
      <view class="form-header">
        <view class="form-logo">
          <text>🐾</text>
        </view>
        <text class="form-title">欢迎回来</text>
        <text class="welcome-sub">登录后继续管理毛孩子的档案吧</text>
      </view>

      <view class="form-wrap">
        <view class="input-group">
          <view class="input-icon">
            <Icon name="user" color="#FF8C42" size="18" />
          </view>
          <input
            v-model="form.username"
            class="form-input"
            type="text"
            placeholder="请输入手机号 / 用户名"
            placeholder-class="input-placeholder"
          />
        </view>

        <view class="input-group">
          <view class="input-icon">
            <Icon name="locked" color="#FF8C42" size="18" />
          </view>
          <input
            v-model="form.password"
            class="form-input"
            :type="showPassword ? 'text' : 'password'"
            placeholder="请输入密码"
            placeholder-class="input-placeholder"
          />
          <view class="eye-icon" :class="{ open: showPassword }" @click="showPassword = !showPassword">
            <Icon :name="showPassword ? 'eye-off' : 'eye'" color="#FF8C42" size="18" />
          </view>
        </view>

        <!-- 忘记密码：暂未实现，上线提审期间先隐藏，避免点击后提示"功能开发中" -->
        <!-- <view class="forgot-wrap" @click="goForgot">
          <text class="forgot-text">忘记密码？</text>
        </view> -->

        <!-- 协议勾选：随「提交登录」动作一起（原在入口页，手机号登录隐藏后移到表单页） -->
        <view class="agreement-row">
          <view class="checkbox" :class="{ checked: agreed }" @click="toggleAgreement">
            <Icon v-if="agreed" name="check" color="#fff" size="12" />
          </view>
          <view class="agreement-text">
            <text>我已阅读并同意</text>
            <text class="link" @click.stop="goAgreement">《用户协议》</text>
            <text>与</text>
            <text class="link" @click.stop="goPrivacy">《隐私政策》</text>
          </view>
        </view>

        <view class="submit-btn pet-press" :class="{ loading }" @click="handleLogin">
          <view v-if="loading" class="loading-ring" />
          <text v-if="!loading">登 录</text>
          <text v-else>登录中...</text>
        </view>
      </view>

      <view class="form-footer">
        <text>没有账号？</text>
        <text class="link" @click="goRegister">立即注册</text>
      </view>

      <!-- 其他登录方式：手机号快捷登录（个人主体不支持获取手机号，暂隐藏） -->
      <!--
      <view class="other-login">
        <view class="other-divider">
          <view class="line" />
          <text class="other-text">其他登录方式</text>
          <view class="line" />
        </view>
        <view class="other-icons">
          <view class="phone-circle pet-press" @click="backToEntry">
            <Icon name="phone" color="#fff" size="26" />
          </view>
        </view>
        <text class="other-tip">使用手机号快捷登录，无需记忆密码</text>
      </view>
      -->    </view>
  </view>
</template>

<script setup>
import Icon from '@/components/Icon.vue';
import { ref, reactive } from 'vue';
import { onShow, onUnload } from '@dcloudio/uni-app';
import { useUserStore } from '@/store/user.js';
import { showToast, showLoading, hideLoading } from '@/utils/index.js';
import { goUserAgreement, goPrivacyPolicy } from '@/utils/auth.js';
import { hasPendingRequests, releasePendingRequests } from '@/utils/session.js';

const userStore = useUserStore();

const showForm = ref(false);

/** 是否已勾选同意《用户协议》与《隐私政策》 */
const agreed = ref(false);

const form = reactive({
  username: '',
  password: ''
});

const showPassword = ref(false);
const loading = ref(false);

/** 切换到账号密码表单视图 */
const backToEntry = () => {
  showForm.value = false;
};

/** 切换协议勾选状态 */
const toggleAgreement = () => {
  agreed.value = !agreed.value;
};

/** 未勾选协议时点击登录按钮的提示 */
const remindAgreement = () => {
  showToast('请先阅读并勾选同意《用户协议》和《隐私政策》');
};

/**
 * 登录成功后的统一收尾
 *
 * 若本次是被"登录状态已过期"引导过来的（存在因 401 而挂起的请求），
 * 则原路返回上一页，并释放挂起请求让它们自动重放，用户可继续刚才被打断的操作；
 * 否则维持原有行为：回到首页。
 */
let finishing = false;
const finishLogin = () => {
  if (finishing) return;
  finishing = true;

  const backToPrevious = hasPendingRequests();
  releasePendingRequests(true);
  showToast('登录成功', 'success');

  setTimeout(() => {
    if (backToPrevious) {
      uni.navigateBack({
        delta: 1,
        fail: () => {
          // 页面栈异常等场景兜底，避免停留在登录页无响应
          uni.switchTab({ url: '/pages/index/index' });
        },
      });
    } else {
      uni.switchTab({ url: '/pages/index/index' });
    }
  }, 1000);
};

const handleLogin = async () => {
  if (!form.username.trim() || !form.password.trim()) {
    showToast('请填写完整信息');
    return;
  }

  // 协议前置校验：未勾选同意《用户协议》《隐私政策》不提交登录
  if (!agreed.value) {
    remindAgreement();
    return;
  }

  loading.value = true;
  try {
    await userStore.accountLogin({
      username: form.username.trim(),
      password: form.password
    });
    finishLogin();
  } catch (err) {
    console.error('[登录失败]', err);
    showToast(err?.msg || err?.message || '账号或密码错误');
  } finally {
    loading.value = false;
  }
};

const handleWxPhoneLogin = async (e) => {
  // 协议前置校验：未勾选时终止后续流程
  if (!agreed.value) {
    remindAgreement();
    return;
  }

  const detail = e.detail || {};
  console.log('[getPhoneNumber返回]', detail);
  const errMsg = detail.errMsg || '';

  // 授权未成功：区分"用户取消"和"接口/权限错误"，避免误导
  if (!errMsg.includes('ok')) {
    if (/cancel|deny|denied|用户取消|拒绝/i.test(errMsg)) {
      showToast('已取消授权');
    } else if (detail.errno === 102 || /jsapi has no permission|no permission/i.test(errMsg)) {
      showToast('当前小程序未开通"获取手机号"权限：个人主体或未认证的小程序不支持，请使用已认证的企业主体 AppID');
    } else {
      showToast(`授权失败：${errMsg}`);
    }
    return;
  }

  const phoneCode = detail.code;
  if (!phoneCode) {
    showToast('获取手机号失败，请确认小程序已开通"获取手机号"权限');
    return;
  }

  try {
    showLoading('登录中...');
    const code = await userStore.getWxCode();
    await userStore.wxPhoneLogin({
      code,
      phoneCode,
    });
    finishLogin();
  } catch (err) {
    console.error('[手机号快捷登录失败]', err);
    showToast(err?.msg || err?.message || '登录失败');
  } finally {
    hideLoading();
  }
};

const goForgot = () => {
  showToast('功能开发中');
};

/** 查看《用户协议》 */
const goAgreement = () => {
  goUserAgreement();
};

/** 查看《隐私政策》 */
const goPrivacy = () => {
  goPrivacyPolicy();
};

const goRegister = () => {
  uni.navigateTo({ url: '/pages/login/register' });
};

/**
 * 从注册页返回时补收尾
 * 走注册流程完成登录后回到本页，此时同样需要继续被打断的操作
 */
onShow(() => {
  if (userStore.isLogin && hasPendingRequests()) {
    finishLogin();
  }
});

/** 用户未登录就返回上一页：释放挂起请求，让原页面按游客态继续 */
onUnload(() => {
  if (!userStore.isLogin && hasPendingRequests()) {
    releasePendingRequests(false);
  }
});
</script>

<style lang="scss" scoped>
.login-page {
  position: relative;
  min-height: 100vh;
  box-sizing: border-box;
  background: linear-gradient(175deg, #FFE8D6 0%, #FFFAF5 38%, #F8F9FC 100%);
  overflow: hidden;
}

/* 背景装饰爪印 */
.page-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;

  .bg-paw {
    position: absolute;
    opacity: 0.14;
  }

  .paw-1 {
    top: 100rpx;
    right: -36rpx;
    font-size: 200rpx;
    transform: rotate(22deg);
  }

  .paw-2 {
    top: 560rpx;
    left: -50rpx;
    font-size: 160rpx;
    transform: rotate(-18deg);
  }

  .paw-3 {
    right: 80rpx;
    bottom: 180rpx;
    font-size: 90rpx;
    opacity: 0.09;
    transform: rotate(10deg);
  }
}

/* ========== 入口视图 ========== */
/* 内容整体居中且不超出视口，避免出现页面滚动条 */
.login-entry {
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  box-sizing: border-box;
  padding: 60rpx 64rpx 48rpx;
}

.brand-zone {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 48rpx;

  .logo-paw-wrap {
    width: 220rpx;
    height: 220rpx;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.68);
    border: 1rpx solid rgba(255, 140, 66, 0.14);
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 18rpx 48rpx rgba(255, 140, 66, 0.16);
    margin-bottom: 36rpx;

    .paw-ring {
      width: 158rpx;
      height: 158rpx;
      border-radius: 50%;
      background: $gradient-primary;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: inset 0 -8rpx 18rpx rgba(202, 78, 19, 0.24), $shadow-primary;
      animation: pet-float 3.4s ease-in-out infinite;

      .ring-emoji {
        font-size: 84rpx;
        filter: drop-shadow(0 4rpx 6rpx rgba(183, 59, 8, 0.2));
      }
    }
  }

  .brand-name {
    font-size: 72rpx;
    font-weight: $font-weight-bold;
    color: $primary-dark;
    letter-spacing: 16rpx;
    text-indent: 16rpx;
    text-shadow: 0 4rpx 16rpx rgba(255, 140, 66, 0.18);
  }

  .welcome-slogan {
    margin-top: 18rpx;
    font-size: $font-lg;
    font-weight: $font-weight-medium;
    color: $text-primary;
  }

  .welcome-sub {
    margin-top: 10rpx;
    font-size: $font-sm;
    color: $text-hint;
  }
}

.login-actions {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 22rpx;
}

.primary-btn {
  width: 100%;
  height: 100rpx;
  background: $gradient-primary;
  color: #fff;
  border-radius: $radius-round;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: $font-lg;
  font-weight: $font-weight-bold;
  box-shadow: $shadow-primary;
  border: none;
  padding: 0;
  line-height: 100rpx;

  &::after {
    border: none;
  }

  &:active {
    transform: scale(0.96) translateY(2rpx);
  }

  .btn-icon-wrap {
    width: 48rpx;
    height: 48rpx;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.24);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 16rpx;
  }
}

.secondary-btn {
  width: 100%;
  height: 100rpx;
  background: rgba(255, 255, 255, 0.86);
  border: 2rpx solid rgba(255, 140, 66, 0.4);
  color: $primary-dark;
  border-radius: $radius-round;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  font-size: $font-lg;
  font-weight: $font-weight-medium;
  box-shadow: $shadow-sm;
}

.divider-row {
  display: flex;
  align-items: center;
  width: 100%;
  gap: 20rpx;
  margin-top: 16rpx;

  .divider-line {
    flex: 1;
    height: 1rpx;
    background: rgba(160, 160, 160, 0.35);
  }

  .divider-text {
    font-size: $font-xs;
    color: $text-hint;
  }
}

.register-link {
  padding: 12rpx 44rpx;
  border-radius: $radius-round;

  text {
    font-size: $font-md;
    color: $primary-dark;
    font-weight: $font-weight-medium;
  }

  &:active {
    background: rgba(255, 140, 66, 0.12);
  }
}

.agreement-row {
  display: flex;
  align-items: flex-start;
  width: 100%;
  margin-top: 4rpx;
  padding: 0 4rpx;

  .checkbox {
    width: 34rpx;
    height: 34rpx;
    border-radius: 50%;
    border: 2rpx solid rgba(255, 140, 66, 0.55);
    background: rgba(255, 255, 255, 0.9);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    margin: 4rpx 12rpx 0 0;
    transition: all 0.2s ease;

    &.checked {
      background: $gradient-primary;
      border-color: transparent;
    }
  }

  .agreement-text {
    flex: 1;
    font-size: $font-xs;
    color: $text-hint;
    line-height: 1.6;

    .link {
      color: $primary;
      text-decoration: underline;
    }
  }
}

/* ========== 表单视图 ========== */
.login-form {
  position: relative;
  z-index: 2;
  min-height: 100vh;
  box-sizing: border-box;
  padding: 64rpx 56rpx 48rpx;
  display: flex;
  flex-direction: column;
}

.form-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 80rpx;

  .form-logo {
    width: 130rpx;
    height: 130rpx;
    border-radius: 44rpx;
    background: $gradient-primary;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: $shadow-primary;
    margin-bottom: 30rpx;

    text {
      font-size: 66rpx;
    }
  }

  .form-title {
    font-size: 52rpx;
    font-weight: $font-weight-bold;
    color: $text-primary;
  }

  .welcome-sub {
    margin-top: 12rpx;
    font-size: $font-sm;
    color: $text-hint;
  }
}

.form-wrap {
  margin-bottom: 48rpx;
}

.input-group {
  display: flex;
  align-items: center;
  height: 104rpx;
  background: rgba(255, 255, 255, 0.92);
  border-radius: $radius-md;
  padding: 0 26rpx;
  margin-bottom: 26rpx;
  border: 2rpx solid rgba(255, 140, 66, 0.18);
  box-shadow: $shadow-sm;
  transition: all 0.3s ease;

  &:focus-within {
    border-color: $primary;
    box-shadow: 0 4rpx 24rpx rgba(255, 140, 66, 0.16);
    transform: translateY(-2rpx);
  }

  .input-icon {
    width: 56rpx;
    height: 56rpx;
    border-radius: 18rpx;
    background: $primary-lighter;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .form-input {
    flex: 1;
    margin-left: 18rpx;
    font-size: $font-md;
    color: $text-primary;
    height: 100%;
  }

  .eye-icon {
    padding: 18rpx;
    margin-right: -18rpx;
    transition: transform 0.3s ease;

    &.open {
      animation: pet-spin 0.35s ease;
    }

    &:active {
      opacity: 0.6;
    }
  }
}

.input-placeholder {
  color: $text-placeholder;
  font-size: $font-md;
}

.forgot-wrap {
  display: flex;
  justify-content: flex-end;
  margin: 4rpx 8rpx 42rpx;

  .forgot-text {
    font-size: $font-sm;
    color: $text-hint;
  }

  &:active {
    opacity: 0.7;
  }
}

.submit-btn {
  width: 100%;
  height: 104rpx;
  background: $gradient-primary;
  color: #fff;
  border-radius: $radius-round;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14rpx;
  font-size: $font-lg;
  font-weight: $font-weight-bold;
  box-shadow: $shadow-primary;
  letter-spacing: 8rpx;

  &:active {
    transform: scale(0.97) translateY(2rpx);
  }

  &.loading {
    opacity: 0.85;
  }

  .loading-ring {
    width: 32rpx;
    height: 32rpx;
    border-radius: 50%;
    border: 4rpx solid rgba(255, 255, 255, 0.35);
    border-top-color: #fff;
    animation: pet-spin 0.8s linear infinite;
  }
}

.form-footer {
  text-align: center;
  font-size: $font-sm;
  color: $text-secondary;
  margin-bottom: 36rpx;

  .link {
    color: $primary;
    font-weight: $font-weight-medium;
  }
}

.other-login {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: auto;

  .other-divider {
    display: flex;
    align-items: center;
    width: 100%;
    gap: 20rpx;

    .line {
      flex: 1;
      height: 1rpx;
      background: rgba(160, 160, 160, 0.3);
    }

    .other-text {
      font-size: $font-xs;
      color: $text-hint;
    }
  }

  .other-icons {
    margin-top: 28rpx;
  }

  .phone-circle {
    width: 96rpx;
    height: 96rpx;
    border-radius: 50%;
    background: $gradient-primary;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: $shadow-primary;

    &:active {
      transform: scale(0.9);
    }
  }

  .other-tip {
    margin-top: 16rpx;
    font-size: $font-xs;
    color: $text-placeholder;
  }
}
</style>
