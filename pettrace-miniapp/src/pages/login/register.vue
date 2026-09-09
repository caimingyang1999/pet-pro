<template>
  <view class="register-page">
    <!-- 背景爪印 -->
    <view class="page-bg">
      <text class="bg-paw paw-1">🐾</text>
      <text class="bg-paw paw-2">🐾</text>
    </view>

    <!-- 返回按钮 -->
    <view class="nav-back pet-press" @click="goBack">
      <u-icon name="arrow-left" color="#fff" size="20" />
    </view>

    <view class="register-content">
      <!-- 标题 -->
      <view class="register-header">
        <view class="header-logo">
          <text>🐾</text>
        </view>
        <text class="title">创建账号</text>
        <text class="subtitle">加入宠迹大家庭，记录毛孩子的每一天</text>
      </view>

      <!-- 表单 -->
      <view class="form-wrap">
        <view class="input-group">
          <view class="input-icon"><text>📱</text></view>
          <input
            v-model="form.phone"
            class="form-input"
            type="number"
            maxlength="11"
            placeholder="请输入手机号"
            placeholder-class="input-placeholder"
          />
        </view>

        <view class="input-group">
          <view class="input-icon"><text>🔒</text></view>
          <input
            v-model="form.password"
            class="form-input"
            :type="showPassword ? 'text' : 'password'"
            placeholder="设置密码（6-20位）"
            placeholder-class="input-placeholder"
          />
          <view class="eye-icon" :class="{ open: showPassword }" @click="showPassword = !showPassword">
            <u-icon :name="showPassword ? 'eye-off' : 'eye'" color="#FF8C42" size="18" />
          </view>
        </view>

        <view class="input-group">
          <view class="input-icon"><text>🔐</text></view>
          <input
            v-model="form.confirmPassword"
            class="form-input"
            :type="showConfirmPassword ? 'text' : 'password'"
            placeholder="确认密码"
            placeholder-class="input-placeholder"
          />
          <view class="eye-icon" :class="{ open: showConfirmPassword }" @click="showConfirmPassword = !showConfirmPassword">
            <u-icon :name="showConfirmPassword ? 'eye-off' : 'eye'" color="#FF8C42" size="18" />
          </view>
        </view>

        <view class="input-group">
          <view class="input-icon"><text>🧸</text></view>
          <input
            v-model="form.nickname"
            class="form-input"
            type="text"
            placeholder="昵称（选填，如：毛毛妈妈）"
            placeholder-class="input-placeholder"
          />
        </view>

        <view class="submit-btn pet-press" :class="{ loading }" @click="handleRegister">
          <view v-if="loading" class="loading-ring" />
          <text v-if="!loading">注 册</text>
          <text v-else>注册中...</text>
        </view>
      </view>

      <!-- 其他注册方式 -->
      <view class="other-login">
        <view class="divider-wrap">
          <view class="divider-line" />
          <text class="divider-text">其他方式</text>
          <view class="divider-line" />
        </view>

        <button
          class="wx-btn-plain pet-press"
          open-type="getPhoneNumber"
          @getphonenumber="handleWxPhoneLogin"
        >
          <view class="wx-icon-circle">
            <u-icon name="weixin-fill" color="#fff" size="20" />
          </view>
          <text>微信手机号一键注册 / 登录</text>
        </button>
      </view>

      <!-- 登录入口 -->
      <view class="login-tip">
        <text>已有账号？</text>
        <text class="link" @click="goLogin">立即登录</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useUserStore } from '@/store/user.js';
import { showToast, showLoading, hideLoading } from '@/utils/index.js';

const userStore = useUserStore();

const form = reactive({
  phone: '',
  password: '',
  confirmPassword: '',
  nickname: ''
});

const showPassword = ref(false);
const showConfirmPassword = ref(false);
const loading = ref(false);

const goBack = () => {
  uni.navigateBack();
};

const validatePhone = (phone) => {
  return /^1[3-9]\d{9}$/.test(phone);
};

const handleRegister = async () => {
  if (!form.phone.trim() || !form.password || !form.confirmPassword) {
    showToast('请填写完整信息');
    return;
  }

  if (!validatePhone(form.phone.trim())) {
    showToast('请输入正确的手机号');
    return;
  }

  if (form.password.length < 6 || form.password.length > 20) {
    showToast('密码长度为6-20位');
    return;
  }

  if (form.password !== form.confirmPassword) {
    showToast('两次密码不一致');
    return;
  }

  loading.value = true;
  try {
    await userStore.register({
      phone: form.phone.trim(),
      password: form.password,
      confirmPassword: form.confirmPassword,
      nickname: form.nickname.trim() || '宠迹用户'
    });
    showToast('注册成功', 'success');
    setTimeout(() => {
      uni.navigateBack();
    }, 1000);
  } catch (err) {
    console.error('[注册失败]', err);
    // 识别后端常见业务错误
    const msg = err?.msg || err?.message || '注册失败，请稍后重试';
    if (/手机号|已注册|已被注册|exists|duplicate/i.test(msg)) {
      showToast('该手机号已被注册');
    } else if (/密码|password/i.test(msg) && /长度|length/i.test(msg)) {
      showToast('密码长度不符合要求');
    } else if (/网络|network|timeout/i.test(msg)) {
      showToast('网络连接失败，请检查网络设置');
    } else {
      showToast(msg);
    }
  } finally {
    loading.value = false;
  }
};

const handleWxPhoneLogin = async (e) => {
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
    showToast('登录成功', 'success');
    setTimeout(() => {
      uni.navigateBack();
    }, 1000);
  } catch (err) {
    console.error('[微信登录失败]', err);
    showToast(err?.msg || err?.message || '登录失败');
  } finally {
    hideLoading();
  }
};

const goLogin = () => {
  uni.navigateBack();
};
</script>

<style lang="scss" scoped>
.register-page {
  position: relative;
  min-height: 100vh;
  box-sizing: border-box;
  background: linear-gradient(175deg, #FFE8D6 0%, #FFFAF5 36%, #F8F9FC 100%);
  overflow: hidden;
}

.page-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;

  .bg-paw {
    position: absolute;
    opacity: 0.12;
    font-size: 170rpx;
  }

  .paw-1 {
    top: 130rpx;
    right: -30rpx;
    transform: rotate(20deg);
  }

  .paw-2 {
    bottom: 120rpx;
    left: -40rpx;
    transform: rotate(-18deg);
    font-size: 130rpx;
  }
}

.nav-back {
  position: absolute;
  top: calc(var(--status-bar-height) + 20rpx);
  left: 24rpx;
  width: 68rpx;
  height: 68rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 20;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.56);
  border: 1rpx solid rgba(255, 140, 66, 0.16);
  backdrop-filter: blur(10rpx);
  -webkit-backdrop-filter: blur(10rpx);
}

.register-content {
  position: relative;
  z-index: 2;
  padding: 150rpx 56rpx 50rpx;
}

.register-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 68rpx;

  .header-logo {
    width: 120rpx;
    height: 120rpx;
    border-radius: 40rpx;
    background: $gradient-primary;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: $shadow-primary;
    margin-bottom: 28rpx;

    text {
      font-size: 62rpx;
    }
  }

  .title {
    display: block;
    font-size: 52rpx;
    font-weight: $font-weight-bold;
    color: $text-primary;
  }

  .subtitle {
    display: block;
    margin-top: 12rpx;
    font-size: $font-sm;
    color: $text-hint;
    text-align: center;
  }
}

.form-wrap {
  margin-bottom: 56rpx;
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

    text {
      font-size: 30rpx;
    }
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
  margin-top: 44rpx;

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

.other-login {
  margin-bottom: 48rpx;

  .divider-wrap {
    display: flex;
    align-items: center;
    margin-bottom: 30rpx;
  }

  .divider-line {
    flex: 1;
    height: 1rpx;
    background: rgba(160, 160, 160, 0.32);
  }

  .divider-text {
    font-size: $font-xs;
    color: $text-hint;
    padding: 0 20rpx;
  }

  .wx-btn-plain {
    width: 100%;
    height: 96rpx;
    background: rgba(255, 255, 255, 0.9);
    border: 2rpx solid rgba(255, 140, 66, 0.4);
    border-radius: $radius-round;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: $font-md;
    color: $primary-dark;
    font-weight: $font-weight-medium;
    padding: 0;
    line-height: 96rpx;
    box-shadow: $shadow-sm;

    &::after {
      border: none;
    }

    &:active {
      transform: scale(0.96);
    }

    .wx-icon-circle {
      width: 52rpx;
      height: 52rpx;
      border-radius: 50%;
      background: linear-gradient(135deg, #5FC967 0%, #39B54A 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 14rpx;
    }
  }
}

.login-tip {
  text-align: center;
  font-size: $font-sm;
  color: $text-secondary;

  .link {
    color: $primary;
    font-weight: $font-weight-medium;
  }

  &:active {
    opacity: 0.8;
  }
}
</style>
