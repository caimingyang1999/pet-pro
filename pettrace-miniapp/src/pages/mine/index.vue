<template>
  <view class="mine-page">
    <!-- ========== 渐变吸顶导航 ========== -->
    <view class="custom-nav" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="nav-content">
        <text class="nav-title">我的</text>
      </view>
    </view>

    <!-- ========== 已登录状态 ========== -->
    <scroll-view
      v-if="isLogin"
      scroll-y
      class="page-scroll"
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <!-- 顶部占位：避开状态栏 + 导航栏 -->
      <view :style="{ height: navBarHeight + 'px' }" />

      <!-- 用户信息卡 -->
      <view class="user-card-wrap">
        <view class="user-card-bg" />
        <text class="watermark-paw">🐾</text>
        <text class="deco-heart">🦴</text>

        <!-- 卡顶：签到 + 设置 -->
        <view class="card-top-row">
          <view
            class="sign-chip"
            :class="{ signed: hasSigned, loading: signInLoading }"
            @click="handleSignIn"
          >
            <text class="sign-emoji">📅</text>
            <text v-if="hasSigned">今日已签到</text>
            <text v-else-if="signInLoading">签到中...</text>
            <text v-else>每日签到 +5</text>
          </view>
          <view class="settings-btn pet-press" @click="handleMenuClick(settingItem)">
            <Icon name="settings" :size="20" color="rgba(255,255,255,0.95)" />
          </view>
        </view>

        <!-- 用户信息主体 -->
        <view class="user-main">
          <view class="avatar-box" @click="changeAvatar">
            <image
              v-if="userInfo?.avatar"
              class="avatar"
              :src="fullImageUrl(userInfo?.avatar)"
              mode="aspectFill"
            />
            <view v-else class="avatar avatar-placeholder">
              <text class="avatar-emoji">🐾</text>
            </view>
            <view class="avatar-edit">
              <Icon name="camera" :size="12" color="#FF8C42" />
            </view>
          </view>

          <view class="user-meta">
            <view class="nickname-line">
              <text class="nickname">{{ userInfo?.nickName || '爱宠主人' }}</text>
              <view class="points-pill" @click="goPointsDetail">
                <text class="points-coin">🪙</text>
                <text class="points-num">{{ userInfo?.points || 0 }}</text>
              </view>
            </view>
            <view class="id-line">
              <text class="id-label">ID: {{ userInfo?.userId || '--' }}</text>
            </view>

            <!-- 数据统计 -->
            <view class="stats-row">
              <view class="stat-item" @click="goPointsDetail">
                <text class="stat-value">{{ userInfo?.points || 0 }}</text>
                <text class="stat-label">积分</text>
              </view>
              <view class="stat-divider" />
              <view class="stat-item" @click="goOrders">
                <text class="stat-value">{{ orderCount }}</text>
                <text class="stat-label">订单</text>
              </view>
              <view class="stat-divider" />
              <view class="stat-item" @click="goPosts">
                <text class="stat-value">{{ postCount }}</text>
                <text class="stat-label">动态</text>
              </view>
              <view class="stat-divider" />
              <view class="stat-item" @click="goFollows">
                <text class="stat-value">{{ followCount }}</text>
                <text class="stat-label">关注</text>
              </view>
            </view>
            <view class="sub-stats-row">
              <text class="sub-stat" @click="goFollows('follower')">
                粉丝 {{ followerCount }} · 点击查看
              </text>
            </view>
          </view>
        </view>
      </view>

      <!-- 功能菜单（与顶部卡片重叠） -->
      <view class="menu-card overlap-card">
        <view
          class="menu-item"
          v-for="item in visibleServiceMenu"
          :key="item.key"
          @click="handleMenuClick(item)"
        >
          <view class="menu-icon" :style="{ background: menuMeta(item).bg }">
            <text>{{ menuMeta(item).emoji }}</text>
          </view>
          <text class="menu-title">{{ item.title }}</text>
          <Icon name="chevron_right" :size="16" color="#C0C2CE" />
        </view>
      </view>

      <!-- 其他菜单 -->
      <view class="menu-card">
        <view
          class="menu-item"
          v-for="item in otherMenuList"
          :key="item.key"
          @click="handleMenuClick(item)"
        >
          <view class="menu-icon" :style="{ background: menuMeta(item).bg }">
            <text>{{ menuMeta(item).emoji }}</text>
          </view>
          <text class="menu-title">{{ item.title }}</text>
          <text class="menu-desc" v-if="item.desc">{{ item.desc }}</text>
          <Icon name="chevron_right" :size="16" color="#C0C2CE" />
        </view>
      </view>

      <!-- 退出登录 -->
      <view class="logout-wrap">
        <view class="logout-btn pet-press" @click="handleLogout">
          <Icon name="logout" :size="18" color="#FF6B6B" />
          <text>退出登录</text>
        </view>
      </view>

      <view class="footer-tips">宠迹 · 记录毛孩子的每一个瞬间 🐾</view>
    </scroll-view>

    <!-- ========== 未登录状态 ========== -->
    <view v-else class="login-guide-page">
      <view :style="{ height: navBarHeight + 'px' }" />

      <view class="guide-logo-wrap">
        <view class="guide-logo-ring">
          <text class="guide-logo-emoji">🐾</text>
        </view>
        <view class="guide-float-paw paw-a">🐾</view>
        <view class="guide-float-paw paw-b">🐾</view>
      </view>

      <text class="guide-title">宠迹</text>
      <text class="guide-desc">记录毛孩子的每一个瞬间 🐾</text>

      <view class="login-buttons">
        <button
          class="wx-login-btn pet-press"
          open-type="getPhoneNumber"
          @getphonenumber="handleWxPhoneLogin"
        >
          <view class="wx-icon-wrap">
            <Icon name="weixin" :size="18" color="#fff" />
          </view>
          <text>微信手机号登录</text>
        </button>

        <view class="account-login-btn pet-press" @click="goToLogin">
          <text>账号密码登录</text>
        </view>

        <view class="register-divider">
          <view class="divider-line" />
          <text class="divider-text">还没有账号？</text>
          <view class="divider-line" />
        </view>

        <view class="register-link pet-press" @click="goToRegister">
          <text>注册新账号</text>
        </view>
      </view>

      <view class="agreement-tips">
        <text>登录即表示同意</text>
        <text class="link" @click="goAgreement">《用户协议》</text>
        <text>和</text>
        <text class="link" @click="goPrivacy">《隐私政策》</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, reactive } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import Icon from '@/components/Icon.vue';
import { fullImageUrl } from '@/utils/index.js';
import { useUserStore } from '@/store/user.js';
import { SERVER_BASE } from '@/api/request.js';
import { pickUploadedPath } from '@/api/request.js';
import { getOrderList } from '@/api/shop.js';
import { getMyPosts } from '@/api/post.js';
import { getSignInStatus, signIn } from '@/api/user.js';
import { getFollowCount } from '@/api/follow.js';
import { showToast, showLoading, hideLoading } from '@/utils/index.js';
import { features } from '@/config/features.js';

const userStore = useUserStore();
const isLogin = computed(() => userStore.isLogin);
const userInfo = computed(() => userStore.userInfo);

const refreshing = ref(false);
const orderCount = ref(0);
const postCount = ref(0);
const followCount = ref(0);
const followerCount = ref(0);
const hasSigned = ref(false); // 今日是否已签到
const signInLoading = ref(false); // 签到按钮防重复点击

const statusBarHeight = ref(20);
const navBarHeight = ref(64); // 默认状态栏20 + 导航栏44
// #ifdef MP-WEIXIN
try {
  const sysInfo = uni.getSystemInfoSync();
  statusBarHeight.value = sysInfo.statusBarHeight || 20;
  // 获取胶囊按钮位置，底部作为导航栏占位高度
  const menuRect = uni.getMenuButtonBoundingClientRect();
  navBarHeight.value = menuRect.bottom + 8; // 胶囊底部 + 8px 间距
} catch (e) {}
// #endif

const serviceMenu = reactive([
  { key: 'pets', title: '我的宠物', path: '/pages/pet/list', needLogin: true },
  { key: 'chats', title: '问答记录', path: '/pages/chat/history', needLogin: true },
  { key: 'posts', title: '我的动态', path: '/pages/mine/posts', needLogin: true },
  { key: 'likes', title: '我的点赞', path: '/pages/mine/likes', needLogin: true },
  { key: 'follows', title: '我的关注', path: '/pages/mine/follows', needLogin: true },
  { key: 'orders', title: '我的订单', path: '/pages/mine/orders', needLogin: true },
  { key: 'address', title: '收货地址', path: '/pages/mine/address', needLogin: true },
]);

const otherMenuList = reactive([
  { key: 'about', title: '关于宠迹', desc: '版本 v1.0.0', path: '', needLogin: false },
  { key: 'setting', title: '设置', path: '', needLogin: false },
]);

const settingItem = { key: 'setting', title: '设置', path: '', needLogin: false };

/** 菜单图标元数据：不同菜单不同色彩的柔和底 + emoji */
const MENU_META = {
  pets: { emoji: '🐾', bg: '#FFF0E6' },
  chats: { emoji: '💬', bg: '#F0EBFF' },
  posts: { emoji: '📝', bg: '#FFF3E0' },
  likes: { emoji: '❤️', bg: '#FFEBF0' },
  follows: { emoji: '👥', bg: '#E8F3FF' },
  orders: { emoji: '📦', bg: '#E5F4FF' },
  address: { emoji: '📍', bg: '#E8F7EB' },
  about: { emoji: '💡', bg: '#FFF7DE' },
  setting: { emoji: '⚙️', bg: '#F0F1F5' },
};

const menuMeta = (item) => MENU_META[item.key] || { emoji: '✨', bg: '#F0F1F5' };

// 养宠助手功能关闭时，隐藏"问答记录"入口
const visibleServiceMenu = computed(() =>
  serviceMenu.filter((item) => item.key !== 'chats' || features.adviserEnabled !== false)
);

const fetchUserInfo = async () => {
  if (!isLogin.value) return;
  try {
    await userStore.fetchUserInfo();
  } catch (e) {
    console.error('[mine页] 获取用户信息失败:', e?.code, e?.msg);
  }
};

/**
 * 获取我的页面统计数据（订单数、动态数）用于回显
 */
const fetchStats = async () => {
  if (!isLogin.value) return;
  try {
    const [orderRes, postRes] = await Promise.all([
      getOrderList({ pageNum: 1, pageSize: 1 }),
      getMyPosts({ pageNum: 1, pageSize: 1 }),
    ]);
    orderCount.value = orderRes.total || 0;
    postCount.value = postRes.total || 0;
  } catch (e) {
    console.error('[mine页] 获取统计数据失败:', e?.code, e?.msg);
    orderCount.value = 0;
    postCount.value = 0;
  }
};

const onRefresh = async () => {
  refreshing.value = true;
  await Promise.all([fetchUserInfo(), fetchStats(), fetchSignInStatus()]);
  refreshing.value = false;
};

/**
 * 获取今日签到状态
 */
const fetchSignInStatus = async () => {
  if (!isLogin.value) {
    hasSigned.value = false;
    return;
  }
  try {
    const res = await getSignInStatus();
    const data = res.data || res;
    hasSigned.value = !!data.signed;
  } catch (e) {
    console.error('[mine页] 获取签到状态失败:', e?.code, e?.msg);
    hasSigned.value = false;
  }
};

/**
 * 每日签到（防重复点击）
 */
const handleSignIn = async () => {
  if (signInLoading.value) return; // 防重复点击
  if (hasSigned.value) {
    showToast('今日已签到');
    return;
  }
  signInLoading.value = true;
  try {
    const res = await signIn();
    const data = res.data || res;
    hasSigned.value = true;
    // 更新本地积分余额
    if (data.pointsBalance != null) {
      userStore.setUserInfo({ ...userStore.userInfo, points: data.pointsBalance });
    }
    showToast(data.message || `签到成功，获得 ${data.pointsReward || 5} 积分`, 'success');
  } catch (e) {
    // 后端返回"今日已签到"时，同步状态
    if (e?.msg && e.msg.includes('已签到')) {
      hasSigned.value = true;
    }
    showToast(e?.msg || '签到失败，请重试');
  } finally {
    signInLoading.value = false;
  }
};

onShow(() => {
  if (!userInfo.value) userStore.initUserInfo();
  fetchUserInfo();
  fetchStats();
  fetchSignInStatus();
  uni.setNavigationBarTitle({ title: '我的' });
});

/* ================== 微信手机号登录 ================== */
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
    await userStore.wxPhoneLogin({ code, phoneCode });
    showToast('登录成功', 'success');
  } catch (err) {
    console.error('[微信登录失败]', err);
    showToast(err?.msg || err?.message || '登录失败');
  } finally {
    hideLoading();
  }
};

/* ================== 跳转登录/注册 ================== */
const goToLogin = () => uni.navigateTo({ url: '/pages/login/index' });
const goToRegister = () => uni.navigateTo({ url: '/pages/login/register' });
const goAgreement = () => showToast('用户协议开发中');
const goPrivacy = () => showToast('隐私政策开发中');

/* ================== 已登录功能 ================== */
const changeAvatar = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      const tempFilePath = res.tempFilePaths[0];
      uni.uploadFile({
        url: `${SERVER_BASE}/system/user/profile/avatar`,
        filePath: tempFilePath,
        name: 'avatarfile',
        header: { Authorization: `Bearer ${userStore.token}` },
        success: (res) => {
          try {
            const data = JSON.parse(res.data);
            if (data.code === 200) {
              // 统一读取响应字段并做磁盘绝对路径归一化
              const avatarUrl = pickUploadedPath(data);
              if (avatarUrl) {
                const fullUrl = avatarUrl.startsWith('http') ? avatarUrl : `${SERVER_BASE}${avatarUrl.startsWith('/') ? '' : '/'}${avatarUrl}`;
                userStore.setUserInfo({ ...userStore.userInfo, avatar: fullUrl });
              } else {
                showToast('服务器未返回头像地址');
              }
              showToast('头像更新成功', 'success');
            } else {
              showToast(data.msg || '上传失败');
            }
          } catch (e) {
            showToast('上传失败');
          }
        },
        fail: () => showToast('网络异常，请重试'),
        complete: () => hideLoading(),
      });
    },
  });
};

const goPointsDetail = () => uni.navigateTo({ url: '/pages/mine/points' });
const goOrders = () => uni.navigateTo({ url: '/pages/mine/orders' });
const goPosts = () => uni.navigateTo({ url: '/pages/mine/posts' });
const goFollows = (type) => {
  const url = type === 'follower' ? '/pages/mine/follows?type=follower' : '/pages/mine/follows';
  uni.navigateTo({ url });
};

const handleMenuClick = (item) => {
  if (item.needLogin && !isLogin.value) {
    showToast('请先登录');
    return;
  }
  if (item.key === 'chats' && features.adviserEnabled === false) {
    showToast('功能升级中，敬请期待');
    return;
  }
  if (!item.path) {
    showToast('功能开发中');
    return;
  }
  uni.navigateTo({ url: item.path });
};

const handleLogout = () => {
  uni.showModal({
    title: '退出登录',
    content: '确认退出登录吗？宠物档案和动态不会丢失哦~',
    confirmColor: '#FF6B6B',
    success: (res) => {
      if (res.confirm) {
        userStore.logout();
        showToast('已退出登录');
      }
    },
  });
};
</script>

<style lang="scss" scoped>
.mine-page {
  min-height: 100vh;
  background-color: $bg-page;
}

/* ========== 渐变吸顶导航 ========== */
.custom-nav {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 999;
  background: $gradient-primary;
  box-shadow: 0 4rpx 16rpx rgba(216, 75, 16, 0.12);

  .nav-content {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 88rpx;

    .nav-title {
      font-size: $font-lg;
      font-weight: $font-weight-bold;
      color: #fff;
      letter-spacing: 2rpx;
      text-shadow: 0 2rpx 8rpx rgba(185, 61, 9, 0.18);
    }
  }
}

.page-scroll {
  height: 100vh;
}

/* ========== 已登录：用户信息卡 ========== */
.user-card-wrap {
  position: relative;
  margin: 8rpx 28rpx 0;
  overflow: hidden;
  border-radius: $radius-xl;
  box-shadow: $shadow-primary;

  .user-card-bg {
    position: absolute;
    inset: 0;
    background: $gradient-primary;
  }

  .watermark-paw {
    position: absolute;
    right: -12rpx;
    bottom: -54rpx;
    font-size: 250rpx;
    opacity: 0.1;
    transform: rotate(-14deg);
    z-index: 1;
  }

  .deco-heart {
    position: absolute;
    top: 150rpx;
    right: 150rpx;
    font-size: 60rpx;
    opacity: 0.14;
    transform: rotate(18deg);
  }
}

.card-top-row {
  position: relative;
  z-index: 3;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 24rpx 0;

  .sign-chip {
    display: flex;
    align-items: center;
    gap: 8rpx;
    padding: 10rpx 26rpx;
    border-radius: $radius-round;
    background: rgba(255, 255, 255, 0.24);
    border: 1rpx solid rgba(255, 255, 255, 0.3);
    backdrop-filter: blur(8rpx);
    -webkit-backdrop-filter: blur(8rpx);

    .sign-emoji {
      font-size: $font-md;
    }

    text {
      font-size: $font-sm;
      color: #fff;
      font-weight: $font-weight-medium;
    }

    &.signed {
      background: rgba(255, 255, 255, 0.18);

      text {
        color: rgba(255, 255, 255, 0.88);
      }
    }

    &.loading {
      opacity: 0.7;
    }

    &:active:not(.signed) {
      transform: scale(0.95);
    }
  }

  .settings-btn {
    width: 64rpx;
    height: 64rpx;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.22);
    border: 1rpx solid rgba(255, 255, 255, 0.28);
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.user-main {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  padding: 24rpx 32rpx 36rpx;
}

.avatar-box {
  position: relative;
  margin-right: 30rpx;
  flex-shrink: 0;

  .avatar {
    width: 148rpx;
    height: 148rpx;
    border-radius: 50%;
    border: 5rpx solid rgba(255, 255, 255, 0.95);
    box-shadow: 0 8rpx 24rpx rgba(190, 70, 18, 0.28);
    background-color: #fff;
  }

  .avatar-placeholder {
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255, 255, 255, 0.3);

    .avatar-emoji {
      font-size: 68rpx;
    }
  }

  .avatar-edit {
    position: absolute;
    right: -2rpx;
    bottom: -2rpx;
    width: 48rpx;
    height: 48rpx;
    background: #fff;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: $shadow-sm;
  }
}

.user-meta {
  flex: 1;
  min-width: 0;

  .nickname-line {
    display: flex;
    align-items: center;

    .nickname {
      font-size: 42rpx;
      font-weight: $font-weight-bold;
      color: #fff;
      line-height: 1.3;
      text-shadow: 0 2rpx 10rpx rgba(185, 61, 9, 0.2);
      max-width: 220rpx;
      @include pet-ellipsis;
    }

    .points-pill {
      display: flex;
      align-items: center;
      gap: 4rpx;
      margin-left: 16rpx;
      padding: 6rpx 18rpx;
      border-radius: $radius-round;
      background: rgba(255, 255, 255, 0.9);
      box-shadow: 0 4rpx 12rpx rgba(185, 61, 9, 0.18);
      flex-shrink: 0;

      &:active {
        animation: pet-bounce 0.55s ease;
      }

      .points-coin {
        font-size: $font-sm;
      }

      .points-num {
        font-size: $font-md;
        color: $primary-dark;
        font-weight: $font-weight-bold;
      }
    }
  }

  .id-line {
    margin-top: 8rpx;

    .id-label {
      font-size: $font-xs;
      color: rgba(255, 255, 255, 0.75);
    }
  }

  .stats-row {
    display: flex;
    align-items: center;
    margin-top: 22rpx;
    padding: 20rpx 6rpx;
    background: rgba(255, 255, 255, 0.18);
    border: 1rpx solid rgba(255, 255, 255, 0.24);
    border-radius: $radius-md;
    backdrop-filter: blur(10rpx);
    -webkit-backdrop-filter: blur(10rpx);

    .stat-item {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;

      &:active {
        transform: scale(0.94);
      }

      .stat-value {
        font-size: 36rpx;
        font-weight: $font-weight-bold;
        color: #fff;
        line-height: 1.2;
      }

      .stat-label {
        margin-top: 4rpx;
        font-size: $font-xs;
        color: rgba(255, 255, 255, 0.82);
      }
    }

    .stat-divider {
      width: 1rpx;
      height: 40rpx;
      background: rgba(255, 255, 255, 0.25);
    }
  }

  .sub-stats-row {
    margin-top: 10rpx;

    .sub-stat {
      font-size: $font-xs;
      color: rgba(255, 255, 255, 0.72);
    }
  }
}

/* ========== 功能菜单卡片（负 margin 与用户卡重叠） ========== */
.menu-card {
  margin: 0 28rpx;
  padding: 0 28rpx;
  background: #fff;
  border-radius: $radius-lg;
  box-shadow: $shadow-card;
  overflow: hidden;
}

.overlap-card {
  margin-top: -28rpx;
  position: relative;
  z-index: 5;
  padding-top: 28rpx;
}

.menu-item {
  display: flex;
  align-items: center;
  min-height: 104rpx;
  border-bottom: 1rpx solid $bg-input;
  transition: transform 0.25s ease;

  &:last-child {
    border-bottom: none;
  }

  &:active {
    background: #FFF9F4;
    transform: translateX(6rpx);
  }

  .menu-icon {
    width: 64rpx;
    height: 64rpx;
    border-radius: 20rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 22rpx;
    flex-shrink: 0;

    text {
      font-size: 32rpx;
    }
  }

  .menu-title {
    flex: 1;
    font-size: $font-md;
    color: $text-primary;
    font-weight: $font-weight-medium;
  }

  .menu-desc {
    font-size: $font-xs;
    color: $text-hint;
    margin-right: 12rpx;
  }
}

.menu-card + .menu-card {
  margin-top: 24rpx;
}

/* ========== 退出登录 ========== */
.logout-wrap {
  padding: 40rpx 28rpx 24rpx;
}

.logout-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  background: #fff;
  color: $danger;
  font-size: $font-md;
  padding: 28rpx 0;
  border-radius: $radius-round;
  font-weight: $font-weight-medium;
  box-shadow: $shadow-sm;

  text {
    color: $danger;
  }

  &:active {
    background: #FFF0F0;
  }
}

.footer-tips {
  text-align: center;
  color: $text-placeholder;
  font-size: $font-xs;
  padding: 8rpx 0 52rpx;
}

/* ========== 未登录状态 ========== */
.login-guide-page {
  min-height: 100vh;
  box-sizing: border-box;
  background: linear-gradient(180deg, #FFE8D6 0%, #F8F9FC 46%);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 64rpx 60rpx;
}

.guide-logo-wrap {
  position: relative;
  width: 240rpx;
  height: 240rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  margin-top: 40rpx;
  background: radial-gradient(circle at 32% 28%, rgba(255,255,255,0.95) 0%, rgba(255,232,214,0.6) 100%);
  box-shadow: 0 16rpx 48rpx rgba(255, 140, 66, 0.16);

  .guide-logo-ring {
    width: 170rpx;
    height: 170rpx;
    border-radius: 50%;
    background: $gradient-primary;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: inset 0 -8rpx 18rpx rgba(202, 78, 19, 0.24), $shadow-primary;

    .guide-logo-emoji {
      font-size: 86rpx;
      filter: drop-shadow(0 4rpx 6rpx rgba(183, 59, 8, 0.2));
    }
  }

  .guide-float-paw {
    position: absolute;
    font-size: 44rpx;
    opacity: 0.55;
  }

  .paw-a {
    top: 4rpx;
    right: 2rpx;
    transform: rotate(18deg);
    animation: pet-float 3s ease-in-out infinite;
  }

  .paw-b {
    bottom: 14rpx;
    left: 0;
    transform: rotate(-20deg);
    animation: pet-float 3.6s ease-in-out 0.4s infinite;
  }
}

.guide-title {
  margin-top: 40rpx;
  font-size: 64rpx;
  font-weight: $font-weight-bold;
  color: $primary-dark;
  letter-spacing: 10rpx;
}

.guide-desc {
  margin-top: 18rpx;
  font-size: $font-md;
  color: $text-secondary;
}

.login-buttons {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 100rpx;
  gap: 26rpx;
}

.wx-login-btn {
  width: 100%;
  height: 96rpx;
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
  line-height: 96rpx;

  &::after {
    border: none;
  }

  &:active {
    transform: scale(0.96) translateY(2rpx);
  }

  .wx-icon-wrap {
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

.account-login-btn {
  width: 100%;
  height: 96rpx;
  border: 2rpx solid rgba(255, 140, 66, 0.45);
  color: $primary-dark;
  border-radius: $radius-round;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: $font-lg;
  font-weight: $font-weight-medium;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: $shadow-sm;

  &:active {
    transform: scale(0.97) translateY(2rpx);
  }
}

.register-divider {
  display: flex;
  align-items: center;
  width: 100%;
  gap: 20rpx;
  margin-top: 12rpx;

  .divider-line {
    flex: 1;
    height: 1rpx;
    background: rgba(160, 160, 160, 0.32);
  }

  .divider-text {
    font-size: $font-xs;
    color: $text-hint;
  }
}

.register-link {
  padding: 12rpx 40rpx;
  border-radius: $radius-round;

  text {
    font-size: $font-md;
    color: $primary-dark;
    font-weight: $font-weight-medium;
  }

  &:active {
    background: rgba(255, 140, 66, 0.1);
  }
}

.agreement-tips {
  margin-top: auto;
  text-align: center;
  font-size: $font-xs;
  color: $text-hint;
  line-height: 1.7;
  padding-top: 30rpx;

  .link {
    color: $primary;
    text-decoration: underline;
  }
}
</style>
