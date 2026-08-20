<template>
  <view class="login-page">
    <!-- 返回按钮 -->
    <view class="nav-back" @click="goBack">
      <u-icon name="arrow-left" color="#FF7E3D" size="20" />
    </view>

    <!-- 入口视图 -->
    <view v-if="!showForm" class="login-entry">
      <view class="guide-content">
        <!-- 品牌 Logo -->
        <view class="brand-zone">
          <view class="logo-paw-wrap">
            <view class="paw-ring">
              <image class="paw-img" src="/static/log2.png" mode="aspectFit" />
            </view>
          </view>
          <text class="brand-name">宠迹</text>
        </view>

        <view class="login-actions">
          <button
            class="primary-btn"
            open-type="getPhoneNumber"
            @getphonenumber="handleWxPhoneLogin"
          >
            <view class="btn-icon-wrap">
              <u-icon name="weixin-fill" color="#fff" size="20" />
            </view>
            <text>微信手机号登录</text>
          </button>

          <view class="secondary-btn" @click="showForm = true">
            <text>账号密码登录</text>
          </view>

          <view class="register-link" @click="goRegister">
            <text>注册新账号</text>
          </view>
        </view>
      </view>

      <view class="agreement-tips">
        <text>登录即表示同意</text>
        <text class="link" @click="goAgreement">《用户协议》</text>
        <text>和</text>
        <text class="link" @click="goPrivacy">《隐私政策》</text>
      </view>
    </view>

    <!-- 账号密码表单视图 -->
    <view v-else class="login-form">
      <view class="form-header">
        <view class="form-brand">
          <image class="form-logo" src="/static/log2.png" mode="aspectFit" />
          <text class="brand">宠迹</text>
        </view>
        <text class="welcome">欢迎回来</text>
      </view>

      <view class="form-wrap">
        <view class="input-group">
          <u-icon name="account" color="#FF7E3D" size="18" />
          <input
            v-model="form.username"
            class="form-input"
            type="text"
            placeholder="请输入手机号/用户名"
            placeholder-class="input-placeholder"
          />
        </view>

        <view class="input-group">
          <u-icon name="lock" color="#FF7E3D" size="18" />
          <input
            v-model="form.password"
            class="form-input"
            :type="showPassword ? 'text' : 'password'"
            placeholder="请输入密码"
            placeholder-class="input-placeholder"
          />
          <view class="eye-icon" @click="showPassword = !showPassword">
            <u-icon :name="showPassword ? 'eye-off' : 'eye'" color="#FF7E3D" size="18" />
          </view>
        </view>

        <view class="forgot-wrap" @click="goForgot">
          <text class="forgot-text">忘记密码？</text>
        </view>

        <view
          class="submit-btn"
          :class="{ loading: loading }"
          @click="handleLogin"
        >
          <text v-if="!loading">登 录</text>
          <text v-else>登录中...</text>
        </view>
      </view>

      <view class="form-footer">
        <text>没有账号？</text>
        <text class="link" @click="goRegister">立即注册</text>
      </view>

      <view class="back-to-entry" @click="showForm = false">
        <u-icon name="arrow-left" color="#FF7E3D" size="14" />
        <text>其他登录方式</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useUserStore } from '@/store/user.js';
import { showToast, showLoading, hideLoading } from '@/utils/index.js';

const userStore = useUserStore();

const showForm = ref(false);

const form = reactive({
  username: '',
  password: ''
});

const showPassword = ref(false);
const loading = ref(false);

const goBack = () => {
  if (showForm.value) {
    showForm.value = false;
  } else {
    uni.navigateBack();
  }
};

const handleLogin = async () => {
  if (!form.username.trim() || !form.password.trim()) {
    showToast('请填写完整信息');
    return;
  }

  loading.value = true;
  try {
    await userStore.accountLogin({
      username: form.username.trim(),
      password: form.password
    });
    showToast('登录成功', 'success');
    setTimeout(() => {
      uni.switchTab({ url: '/pages/index/index' });
    }, 1000);
  } catch (err) {
    console.error('[登录失败]', err);
    showToast(err?.msg || err?.message || '账号或密码错误');
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
      uni.switchTab({ url: '/pages/index/index' });
    }, 1000);
  } catch (err) {
    console.error('[微信登录失败]', err);
    showToast(err?.msg || err?.message || '登录失败');
  } finally {
    hideLoading();
  }
};

const goForgot = () => {
  showToast('功能开发中');
};

const goAgreement = () => {
  showToast('用户协议开发中');
};

const goPrivacy = () => {
  showToast('隐私政策开发中');
};

const goRegister = () => {
  uni.navigateTo({ url: '/pages/login/register' });
};
</script>

<style lang="scss" scoped>
.login-page {
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

/* ========== 入口视图 ========== */
.login-entry {
  padding: 140rpx 64rpx 60rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  min-height: 100vh;
  box-sizing: border-box;
}

.guide-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

/* ---------- 品牌 Logo ---------- */
.brand-zone {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 60rpx;
  margin-bottom: 100rpx;
}

.logo-paw-wrap {
  width: 220rpx;
  height: 220rpx;
  border-radius: 50%;
  background: linear-gradient(150deg, #FFF0E6 0%, #FFE0C7 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 16rpx 40rpx rgba(255, 126, 61, 0.12);
  margin-bottom: 32rpx;
}

.paw-ring {
  width: 152rpx;
  height: 152rpx;
  border-radius: 50%;
  background: linear-gradient(150deg, #FFB27A 0%, #FF7E3D 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: inset 0 -6rpx 16rpx rgba(230, 106, 26, 0.25), 0 8rpx 20rpx rgba(255, 140, 66, 0.35);
}

.paw-img {
  width: 104rpx;
  height: 104rpx;
}

.brand-name {
  font-size: 52rpx;
  font-weight: 700;
  color: #FF7E3D;
  letter-spacing: 12rpx;
  text-indent: 12rpx;
}

/* ---------- 登录操作 ---------- */
.login-actions {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 28rpx;
}

.primary-btn {
  width: 100%;
  height: 96rpx;
  background: linear-gradient(135deg, #FF934F 0%, #FF7E3D 55%, #F4672A 100%);
  color: #fff;
  border-radius: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  font-weight: 600;
  box-shadow: 0 12rpx 28rpx rgba(255, 126, 61, 0.32);
  border: none;
  padding: 0;
  line-height: 96rpx;

  &::after {
    border: none;
  }

  &:active {
    transform: scale(0.97);
    box-shadow: 0 6rpx 16rpx rgba(255, 126, 61, 0.25);
  }

  .btn-icon-wrap {
    width: 48rpx;
    height: 48rpx;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.22);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 16rpx;
  }
}

.secondary-btn {
  width: 100%;
  height: 96rpx;
  background: #fff;
  border: 2rpx solid #FFD9B8;
  color: #FF7E3D;
  border-radius: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  font-weight: 500;
  box-shadow: 0 4rpx 12rpx rgba(255, 126, 61, 0.06);
  transition: all 0.2s;

  &:active {
    transform: scale(0.97);
    background: #FFF7F0;
  }
}

.register-link {
  margin-top: 12rpx;
  padding: 16rpx;

  text {
    font-size: 28rpx;
    color: #FF8A4D;
    font-weight: 500;
  }

  &:active {
    opacity: 0.7;
  }
}

.agreement-tips {
  text-align: center;
  font-size: 24rpx;
  color: #A8A8B0;
  line-height: 1.6;
  padding-bottom: 40rpx;

  .link {
    color: #FF7E3D;
  }
}

/* ========== 表单视图 ========== */
.login-form {
  padding: 180rpx 56rpx 60rpx;
}

.form-header {
  margin-bottom: 72rpx;

  .form-brand {
    display: flex;
    align-items: center;
    margin-bottom: 20rpx;

    .form-logo {
      width: 64rpx;
      height: 64rpx;
      border-radius: 50%;
      margin-right: 20rpx;
      background: linear-gradient(150deg, #FFB27A 0%, #FF7E3D 100%);
      padding: 8rpx;
      box-sizing: border-box;
    }

    .brand {
      font-size: 44rpx;
      font-weight: 700;
      color: #FF7E3D;
      letter-spacing: 6rpx;
    }
  }

  .welcome {
    display: block;
    font-size: 28rpx;
    color: #9B9BA5;
  }
}

.form-wrap {
  margin-bottom: 48rpx;
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
    margin-left: 18rpx;
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

.forgot-wrap {
  display: flex;
  justify-content: flex-end;
  margin: 8rpx 8rpx 44rpx;

  .forgot-text {
    font-size: 26rpx;
    color: #FF8A4D;
  }

  &:active {
    opacity: 0.7;
  }
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

  &:active {
    transform: scale(0.98);
  }

  &.loading {
    opacity: 0.8;
  }
}

.form-footer {
  text-align: center;
  font-size: 26rpx;
  color: #9B9BA5;
  margin-bottom: 40rpx;

  .link {
    color: #FF7E3D;
    font-weight: 600;
  }

  &:active {
    opacity: 0.8;
  }
}

.back-to-entry {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  font-size: 26rpx;
  color: #FF8A4D;

  &:active {
    opacity: 0.7;
  }
}
</style>
