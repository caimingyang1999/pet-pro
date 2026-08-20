<template>
  <view class="register-page">
    <!-- 返回按钮 -->
    <view class="nav-back" @click="goBack">
      <u-icon name="arrow-left" color="#FF7E3D" size="20" />
    </view>

    <view class="register-content">
      <!-- 标题 -->
      <view class="register-header">
        <text class="title">创建账号</text>
        <text class="subtitle">加入宠迹大家庭 🐾</text>
      </view>

      <!-- 表单 -->
      <view class="form-wrap">
        <view class="input-group">
          <u-icon name="phone" color="#FF7E3D" size="18" />
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
          <u-icon name="lock" color="#FF7E3D" size="18" />
          <input
            v-model="form.password"
            class="form-input"
            :type="showPassword ? 'text' : 'password'"
            placeholder="设置密码（6-20位）"
            placeholder-class="input-placeholder"
          />
          <view class="eye-icon" @click="showPassword = !showPassword">
            <u-icon :name="showPassword ? 'eye-off' : 'eye'" color="#FF7E3D" size="18" />
          </view>
        </view>

        <view class="input-group">
          <u-icon name="lock" color="#FF7E3D" size="18" />
          <input
            v-model="form.confirmPassword"
            class="form-input"
            :type="showConfirmPassword ? 'text' : 'password'"
            placeholder="确认密码"
            placeholder-class="input-placeholder"
          />
          <view class="eye-icon" @click="showConfirmPassword = !showConfirmPassword">
            <u-icon :name="showConfirmPassword ? 'eye-off' : 'eye'" color="#FF7E3D" size="18" />
          </view>
        </view>

        <view class="input-group">
          <u-icon name="account" color="#FF7E3D" size="18" />
          <input
            v-model="form.nickname"
            class="form-input"
            type="text"
            placeholder="昵称（选填）"
            placeholder-class="input-placeholder"
          />
        </view>

        <view
          class="submit-btn"
          :class="{ loading: loading }"
          @click="handleRegister"
        >
          <text v-if="!loading">注 册</text>
          <text v-else>注册中...</text>
        </view>
      </view>

      <!-- 其他注册方式 -->
      <view class="other-login">
        <view class="divider-wrap">
          <view class="divider-line"></view>
          <text class="divider-text">其他方式</text>
          <view class="divider-line"></view>
        </view>

        <button
          class="wx-btn-plain"
          open-type="getPhoneNumber"
          @getphonenumber="handleWxPhoneLogin"
        >
          <u-icon name="weixin-fill" color="#07C160" size="20" />
          <text>微信手机号一键注册/登录</text>
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

  // 授权未成功：区分“用户取消”和“接口/权限错误”，避免误导
  if (!errMsg.includes('ok')) {
    if (/cancel|deny|denied|用户取消|拒绝/i.test(errMsg)) {
      showToast('已取消授权');
    } else if (detail.errno === 102 || /jsapi has no permission|no permission/i.test(errMsg)) {
      showToast('当前小程序未开通“获取手机号”权限：个人主体或未认证的小程序不支持，请使用已认证的企业主体 AppID');
    } else {
      showToast(`授权失败：${errMsg}`);
    }
    return;
  }

  const phoneCode = detail.code;
  if (!phoneCode) {
    showToast('获取手机号失败，请确认小程序已开通“获取手机号”权限');
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
  min-height: 100vh;
  background-color: #F6F7FB;
  position: relative;
}

.nav-back {
  position: absolute;
  top: calc(var(--status-bar-height) + 20rpx);
  left: 24rpx;
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;

  &:active {
    opacity: 0.6;
  }
}

.register-content {
  padding: 180rpx 56rpx 60rpx;
}

.register-header {
  margin-bottom: 80rpx;

  .title {
    display: block;
    font-size: 44rpx;
    font-weight: 700;
    color: #FF7E3D;
    margin-bottom: 16rpx;
  }

  .subtitle {
    display: block;
    font-size: 28rpx;
    color: #A8A8B0;
  }
}

.form-wrap {
  margin-bottom: 60rpx;
}

.input-group {
  display: flex;
  align-items: center;
  height: 100rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 0 28rpx;
  margin-bottom: 24rpx;
  border: 2rpx solid #FFE8D6;
  box-shadow: 0 4rpx 16rpx rgba(255, 126, 61, 0.05);
  transition: all 0.2s;

  &:focus-within {
    border-color: #FF7E3D;
    box-shadow: 0 4rpx 20rpx rgba(255, 126, 61, 0.12);
  }

  .form-input {
    flex: 1;
    margin-left: 16rpx;
    font-size: 28rpx;
    color: #333;
    height: 100%;
  }

  .eye-icon {
    padding: 16rpx;
    margin-right: -16rpx;

    &:active {
      opacity: 0.6;
    }
  }
}

.input-placeholder {
  color: #BBBBBB;
  font-size: 28rpx;
}

.submit-btn {
  width: 100%;
  height: 100rpx;
  background: linear-gradient(135deg, #FF934F 0%, #FF7E3D 55%, #F4672A 100%);
  color: #fff;
  border-radius: 50rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  font-weight: 600;
  box-shadow: 0 12rpx 28rpx rgba(255, 126, 61, 0.32);
  transition: all 0.2s;
  margin-top: 48rpx;

  &:active {
    transform: scale(0.96);
  }

  &.loading {
    opacity: 0.8;
  }
}

.other-login {
  margin-bottom: 48rpx;

  .divider-wrap {
    display: flex;
    align-items: center;
    margin-bottom: 32rpx;
  }

  .divider-line {
    flex: 1;
    height: 1rpx;
    background: #FFE8D6;
  }

  .divider-text {
    font-size: 24rpx;
    color: #A8A8B0;
    padding: 0 20rpx;
  }

  .wx-btn-plain {
    width: 100%;
    height: 88rpx;
    background: #fff;
    border: 2rpx solid #FFD9B8;
    border-radius: 44rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28rpx;
    color: #FF7E3D;
    font-weight: 500;
    padding: 0;
    line-height: 88rpx;

    &::after {
      border: none;
    }

    &:active {
      background: #FFF7F0;
      transform: scale(0.96);
    }

    text {
      margin-left: 12rpx;
    }
  }
}

.login-tip {
  text-align: center;
  font-size: 26rpx;
  color: #9B9BA5;

  .link {
    color: #FF7E3D;
    font-weight: 600;
  }

  &:active {
    opacity: 0.8;
  }
}
</style>
