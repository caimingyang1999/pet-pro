<template>
  <view class="mine-page">
    <!-- 自定义导航栏（固定顶部） -->
    <view class="custom-nav" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="nav-content">
        <view class="nav-left" />
        <view class="nav-center">
          <text class="nav-title">我的</text>
        </view>
        <view class="nav-right" />
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
      @scrolltolower="() => {}"
    >
      <!-- 顶部占位：避开状态栏 + 导航栏 -->
      <view :style="{ height: navBarHeight + 'px' }" />

      <!-- 1. 顶部用户卡片 -->
      <view class="user-card-wrap">
        <view class="user-card-bg" />
        <!-- 签到按钮（右上角） -->
        <view
          class="sign-in-btn"
          :class="{ 'signed': hasSigned, 'loading': signInLoading }"
          @click="handleSignIn"
        >
          <text v-if="hasSigned">已签到</text>
          <text v-else-if="signInLoading">签到中...</text>
          <text v-else>签到 +5</text>
        </view>
        <view class="user-info">
          <!-- 头像 -->
          <view class="avatar-box" @click="changeAvatar">
            <image
              v-if="userInfo?.avatar"
              class="avatar"
              :src="fullImageUrl(userInfo?.avatar)"
              mode="aspectFill"
            />
            <view v-else class="avatar avatar-placeholder">
              <Icon name="user" :size="30" color="#fff" />
            </view>
            <view class="avatar-edit">
              <Icon name="camera" :size="12" color="#FF7E3D" />
            </view>
          </view>

          <!-- 用户信息 -->
          <view class="user-meta">
            <text class="nickname">{{ userInfo?.nickName || '未设置昵称' }}</text>
            <view class="id-row">
              <text class="id-label">ID: {{ userInfo?.userId || '--' }}</text>
            </view>

            <!-- 积分 + 快捷入口 -->
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
              <text class="sub-stat" @click="goFollows('follower')">粉丝 {{ followerCount }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 2. 功能菜单列表 -->
      <view class="menu-card">
        <view
            class="menu-item"
            v-for="item in serviceMenu"
            :key="item.key"
            @click="handleMenuClick(item)"
          >
            <text class="menu-title">{{ item.title }}</text>
            <Icon name="chevron_right" :size="16" color="#C0C4CC" />
          </view>
      </view>

      <!-- 3. 其他菜单列表 -->
      <view class="menu-card">
        <view
          class="menu-item"
          v-for="item in otherMenuList"
          :key="item.key"
          @click="handleMenuClick(item)"
        >
          <text class="menu-title">{{ item.title }}</text>
          <text class="menu-desc" v-if="item.desc">{{ item.desc }}</text>
          <Icon name="chevron_right" :size="16" color="#C0C4CC" />
        </view>
      </view>

      <!-- 4. 退出登录 -->
      <view class="logout-wrap">
        <view class="logout-btn" @click="handleLogout">
          <text>退出登录</text>
        </view>
      </view>

      <view class="footer-tips">宠迹 · 陪伴每一段美好时光</view>
    </scroll-view>

    <!-- ========== 未登录状态 ========== -->
    <view v-else class="login-guide-page">
      <!-- 顶部占位 -->
      <view :style="{ height: navBarHeight + 'px' }" />
      <view class="guide-content">
        <view class="guide-logo-wrap">
          <view class="guide-logo-ring">
            <image class="guide-logo-img" src="/static/log2.png" mode="aspectFit" />
          </view>
        </view>

        <text class="guide-title">宠迹</text>
        <text class="guide-desc">记录宠物生活，分享美好时光</text>

        <view class="login-buttons">
          <button
            class="wx-login-btn"
            open-type="getPhoneNumber"
            @getphonenumber="handleWxPhoneLogin"
          >
            <view class="wx-icon-wrap">
              <Icon name="weixin" :size="18" color="#fff" />
            </view>
            <text>微信手机号登录</text>
          </button>

          <view class="account-login-btn" @click="goToLogin">
            <text>账号密码登录</text>
          </view>

          <view class="register-link" @click="goToRegister">
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

// 养宠顾问功能关闭时，隐藏"问答记录"入口
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
    title: '提示',
    content: '确认退出登录？',
    confirmColor: '#FF7E3D',
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
  background-color: #F6F7FB;
}

/* ========== 自定义导航栏 ========== */
.custom-nav {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 999;
  background-color: #F6F7FB;

  .nav-content {
    display: flex;
    align-items: center;
    height: 88rpx;
    padding: 0 24rpx;
  }

  .nav-left,
  .nav-right {
    width: 120rpx;
    flex-shrink: 0;
  }

  .nav-center {
    flex: 1;
    text-align: center;
  }

  .nav-title {
    font-size: 34rpx;
    font-weight: 700;
    color: #1A1A1A;
  }
}

.page-scroll {
  height: 100vh;
}

/* ========== 用户卡片 ========== */
.user-card-wrap {
  position: relative;
  margin: 8rpx 32rpx 0;
  overflow: hidden;
  border-radius: 32rpx;
  box-shadow: 0 8rpx 32rpx rgba(255, 126, 61, 0.10);

  .user-card-bg {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 100%;
    background: linear-gradient(135deg, #FFFFFF 0%, #FFF7F0 100%);
    z-index: 0;
  }

  /* 签到按钮 */
  .sign-in-btn {
    position: absolute;
    top: 24rpx;
    right: 24rpx;
    z-index: 3;
    padding: 12rpx 24rpx;
    border-radius: 32rpx;
    background: linear-gradient(135deg, #FF7E3D 0%, #FF5722 100%);
    color: #fff;
    font-size: 24rpx;
    font-weight: 600;
    box-shadow: 0 4rpx 12rpx rgba(255, 126, 61, 0.35);
    transition: opacity 0.2s;

    &.signed {
      background: #E0E0E0;
      color: #999;
      box-shadow: none;
    }

    &.loading {
      opacity: 0.7;
    }

    &:active:not(.signed) {
      opacity: 0.85;
    }
  }
}

.user-info {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: flex-start;
  padding: 40rpx 32rpx 36rpx;
}

.avatar-box {
  position: relative;
  margin-right: 28rpx;
  flex-shrink: 0;

  .avatar {
    width: 128rpx;
    height: 128rpx;
    border-radius: 50%;
    border: 4rpx solid #FFF0E6;
    box-shadow: 0 8rpx 24rpx rgba(255, 126, 61, 0.14);
    background-color: #F6F7FB;
  }

  .avatar-placeholder {
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #FFB27A 0%, #FF7E3D 100%);
  }

  .avatar-edit {
    position: absolute;
    right: -4rpx;
    bottom: -4rpx;
    width: 44rpx;
    height: 44rpx;
    background: #FFFFFF;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 2rpx solid #FFD9B8;
    box-shadow: 0 4rpx 12rpx rgba(255, 126, 61, 0.18);
  }
}

.user-meta {
  flex: 1;
  min-width: 0;
  padding-top: 16rpx;
}

.nickname {
  font-size: 38rpx;
  font-weight: 700;
  color: #3D2B1D;
  line-height: 1.3;
  max-width: 380rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.id-row {
  margin-top: 10rpx;

  .id-label {
    font-size: 24rpx;
    color: #A8A8B0;
  }
}

.stats-row {
  display: flex;
  align-items: center;
  background-color: #FFFFFF;
  border: 2rpx solid #FFE8D6;
  border-radius: 24rpx;
  padding: 24rpx 0;
  margin-top: 28rpx;
  box-shadow: 0 4rpx 16rpx rgba(255, 126, 61, 0.06);

  .stat-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;

    .stat-value {
      font-size: 34rpx;
      font-weight: 700;
      color: #FF7E3D;
      line-height: 1.2;
    }

    .stat-label {
      font-size: 22rpx;
      color: #9B9BA5;
      margin-top: 4rpx;
    }
  }

  .stat-divider {
    width: 1rpx;
    height: 44rpx;
    background-color: #FFE8D6;
  }
}

.sub-stats-row {
  margin-top: 12rpx;
  padding-left: 8rpx;

  .sub-stat {
    font-size: 24rpx;
    color: #A8A8B0;

    &:active {
      color: #FF7E3D;
    }
  }
}

/* ========== 菜单列表 ========== */
.menu-card {
  margin: 24rpx 32rpx 0;
  background-color: #fff;
  border-radius: 28rpx;
  padding: 0 28rpx;
  box-shadow: 0 8rpx 28rpx rgba(150, 90, 40, 0.06);
}

.menu-item {
  display: flex;
  align-items: center;
  min-height: 108rpx;
  padding: 4rpx 0;
  border-bottom: 1rpx solid #F4F3F8;

  &:last-child {
    border-bottom: none;
  }

  &:active {
    background-color: #FFF9F4;
  }
}

.menu-title {
  flex: 1;
  font-size: 29rpx;
  color: #3D2B1D;
  font-weight: 500;
}

.menu-desc {
  font-size: 24rpx;
  color: #A8A8B0;
  margin-right: 12rpx;
}

/* ========== 退出登录 ========== */
.logout-wrap {
  padding: 40rpx 32rpx 24rpx;
}

.logout-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fff;
  color: #FF7E3D;
  font-size: 30rpx;
  text-align: center;
  padding: 30rpx 0;
  border-radius: 28rpx;
  font-weight: 600;
  gap: 12rpx;
  border: 2rpx solid #FFE8D6;
  box-shadow: 0 8rpx 28rpx rgba(150, 90, 40, 0.05);

  &:active {
    background-color: #FFF7F0;
  }
}

.footer-tips {
  text-align: center;
  color: #B8B8C2;
  font-size: 22rpx;
  padding: 16rpx 0 48rpx;
}

/* ========== 未登录状态 ========== */
.login-guide-page {
  min-height: 100vh;
  background-color: #F6F7FB;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 160rpx 64rpx 60rpx;
}

.guide-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.guide-logo-wrap {
  width: 220rpx;
  height: 220rpx;
  background: linear-gradient(150deg, #FFF0E6 0%, #FFE0C7 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 16rpx 40rpx rgba(255, 126, 61, 0.12);
  margin-bottom: 36rpx;
}

.guide-logo-ring {
  width: 152rpx;
  height: 152rpx;
  border-radius: 50%;
  background: linear-gradient(150deg, #FFB27A 0%, #FF7E3D 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: inset 0 -6rpx 16rpx rgba(230, 106, 26, 0.25), 0 8rpx 20rpx rgba(255, 140, 66, 0.35);
}

.guide-logo-img {
  width: 104rpx;
  height: 104rpx;
}

.guide-title {
  font-size: 52rpx;
  font-weight: 700;
  color: #FF7E3D;
  letter-spacing: 8rpx;
  margin-bottom: 16rpx;
}

.guide-desc {
  font-size: 28rpx;
  color: #A8A8B0;
  margin-bottom: 96rpx;
}

.login-buttons {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 28rpx;
}

.wx-login-btn {
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
  }

  .wx-icon-wrap {
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

.account-login-btn {
  width: 100%;
  height: 96rpx;
  border: 2rpx solid #FFD9B8;
  color: #FF7E3D;
  border-radius: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  font-weight: 500;
  background: #fff;
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

  .link {
    color: #FF7E3D;
  }
}
</style>
