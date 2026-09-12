<template>
  <view class="mine-page">
    <!-- ========== 统一标题栏（吸顶） ==========
         停在顶部时保持透明，让下方用户卡的同一块渐变透上来；
         页面滚动后才加渐变底，避免内容从标题下穿过 -->
    <NavBar title="我的" fixed :solid="scrolled" />

    <!-- ========== 页面主体（登录 / 未登录共用同一布局） ========== -->
    <scroll-view
      scroll-y
      class="page-scroll"
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
      @scroll="onPageScroll"
    >
      <!-- 用户信息卡（顶部占位放在卡片内部，让卡片背景一直延伸到屏幕顶部） -->
      <view class="user-card-wrap">
        <view class="user-card-bg" />
        <text class="watermark-paw">🐾</text>
        <text class="deco-heart">🦴</text>

        <!-- 顶部占位：避开状态栏 + 导航栏 -->
        <view :style="{ height: navBarHeight + 'px' }" />

        <!-- 卡顶：每日签到（右上角，未登录不显示，点功能时统一引导登录） -->
        <view class="card-top-row" v-if="isLogin">
          <view
            class="sign-chip"
            :class="{ signed: hasSigned, loading: signInLoading }"
            @click="handleSignIn"
          >
            <Icon name="calendar" :size="15" color="#fff" />
            <text v-if="hasSigned">今日已签到</text>
            <text v-else-if="signInLoading">签到中...</text>
            <text v-else>每日签到 +5</text>
          </view>
        </view>

        <!-- 用户信息主体：未登录时整体可点，跳转登录页 -->
        <view class="user-main" @click="handleUserAreaClick">
          <view class="avatar-box">
            <image
              v-if="userInfo?.avatar"
              class="avatar"
              :src="fullImageUrl(userInfo?.avatar)"
              mode="aspectFill"
            />
            <view v-else class="avatar avatar-placeholder">
              <text class="avatar-emoji">🐾</text>
            </view>
            <view class="avatar-edit" v-if="isLogin">
              <Icon name="camera" :size="13" color="#FF8C42" />
            </view>
          </view>

          <view class="user-meta">
            <text class="nickname">{{ isLogin ? (userInfo?.nickName || '爱宠主人') : '未登录' }}</text>
            <text class="id-label">{{ isLogin ? `ID: ${userInfo?.userId || '--'}` : '点击登录，管理爱宠档案' }}</text>
          </view>
        </view>

        <!-- 数据统计：独占整行，不再挤在头像右侧 -->
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
          <view class="stat-item" @click="goPets">
            <text class="stat-value">{{ petCount }}</text>
            <text class="stat-label">爱宠</text>
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
            <Icon :name="menuMeta(item).icon" :color="menuMeta(item).color" :size="18" />
          </view>
          <text class="menu-title">{{ item.title }}</text>
          <Icon name="chevron_right" :size="16" color="#C0C2CE" />
        </view>
      </view>

      <!-- 其他菜单（当前为空，无入口时整块不渲染，避免留下空白卡片） -->
      <view class="menu-card" v-if="otherMenuList.length">
        <view
          class="menu-item"
          v-for="item in otherMenuList"
          :key="item.key"
          @click="handleMenuClick(item)"
        >
          <view class="menu-icon" :style="{ background: menuMeta(item).bg }">
            <Icon :name="menuMeta(item).icon" :color="menuMeta(item).color" :size="18" />
          </view>
          <text class="menu-title">{{ item.title }}</text>
          <text class="menu-desc" v-if="item.desc">{{ item.desc }}</text>
          <Icon name="chevron_right" :size="16" color="#C0C2CE" />
        </view>
      </view>

      <!-- 退出登录（仅登录后显示） -->
      <view class="logout-wrap" v-if="isLogin">
        <view class="logout-btn pet-press" @click="handleLogout">
          <Icon name="logout" :size="17" color="#FF6B6B" />
          <text>退出登录</text>
        </view>
      </view>

      <view class="footer-tips">宠迹 · 记录毛孩子的每一个瞬间 🐾</view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed, reactive } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import Icon from '@/components/Icon.vue';
import NavBar from '@/components/NavBar.vue';
import { getNavBarHeight } from '@/utils/navbar.js';
import { fullImageUrl } from '@/utils/index.js';
import { useUserStore } from '@/store/user.js';
import { SERVER_BASE, pickUploadedPath } from '@/api/request.js';
import { getOrderList } from '@/api/shop.js';
import { getPetList } from '@/api/pet.js';
import { getSignInStatus, signIn } from '@/api/user.js';
import { showToast, hideLoading } from '@/utils/index.js';
import { requireLogin, goLogin } from '@/utils/auth.js';
import { features } from '@/config/features.js';

const userStore = useUserStore();
const isLogin = computed(() => userStore.isLogin);
const userInfo = computed(() => userStore.userInfo);

const refreshing = ref(false);
const orderCount = ref(0);
const petCount = ref(0);
const hasSigned = ref(false); // 今日是否已签到
const signInLoading = ref(false); // 签到按钮防重复点击

/** 吸顶标题栏高度：用户卡顶部据此留占位，保证渐变与标题栏无缝衔接 */
const navBarHeight = getNavBarHeight();

/** 页面是否已滚动：滚动后导航栏才需要自带底色，停在顶部时保持透明以消除分割线 */
const scrolled = ref(false);
const onPageScroll = (e) => {
  scrolled.value = (e?.detail?.scrollTop || 0) > 4;
};

// 服务菜单：tab=true 表示目标是 tabBar 页面，需用 switchTab 跳转
const serviceMenu = reactive([
  { key: 'pets', title: '我的宠物', path: '/pages/pet/list', needLogin: true, tab: true },
  { key: 'health', title: '健康记录', path: '/pages/pet/health', needLogin: true },
  { key: 'orders', title: '我的订单', path: '/pages/mine/orders', needLogin: true },
  { key: 'address', title: '收货地址', path: '/pages/mine/address', needLogin: true },
  { key: 'points', title: '积分明细', path: '/pages/mine/points', needLogin: true },
  { key: 'chats', title: '问答记录', path: '/pages/chat/history', needLogin: true },
]);

/**
 * 其他菜单
 *
 * 说明：「关于宠迹」「设置」两个入口目前没有落地页，点击会提示"功能开发中"，
 * 上线提审期间先隐藏（页面底部已有"退出登录"）。后续补齐对应页面后，
 * 把 path 指向真实路由并取消注释即可恢复。
 */
const otherMenuList = reactive([
  // { key: 'about', title: '关于宠迹', desc: '版本 v1.0.0', path: '', needLogin: false },
  // { key: 'setting', title: '设置', path: '', needLogin: false },
]);

/** 菜单图标元数据：不同菜单不同色彩的柔和底 + 主题色矢量图标 */
const MENU_META = {
  pets: { icon: 'pet', color: '#FF8C42', bg: '#FFF0E6' },
  health: { icon: 'medal', color: '#4CAF7D', bg: '#E8F7EB' },
  orders: { icon: 'order', color: '#3D9BE9', bg: '#E5F4FF' },
  address: { icon: 'address', color: '#58B77A', bg: '#E8F7EB' },
  points: { icon: 'wallet', color: '#E8A33D', bg: '#FFF7DE' },
  chats: { icon: 'chat', color: '#8A7BE0', bg: '#F0EBFF' },
  about: { icon: 'info', color: '#D9A93D', bg: '#FFF7DE' },
  setting: { icon: 'settings', color: '#8A8D9A', bg: '#F0F1F5' },
};

const menuMeta = (item) => MENU_META[item.key] || { icon: 'star_fill', color: '#8A8D9A', bg: '#F0F1F5' };

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
 * 获取我的页面统计数据（订单数、爱宠数）用于回显
 */
const fetchStats = async () => {
  if (!isLogin.value) return;
  try {
    const [orderRes, petRes] = await Promise.all([
      getOrderList({ pageNum: 1, pageSize: 1 }),
      getPetList(),
    ]);
    orderCount.value = orderRes.total || 0;
    petCount.value = (petRes.data || []).length;
  } catch (e) {
    console.error('[mine页] 获取统计数据失败:', e?.code, e?.msg);
    orderCount.value = 0;
    petCount.value = 0;
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
 * 每日签到（防重复点击；未登录统一走 requireLogin 弹窗引导）
 */
const handleSignIn = async () => {
  if (!(await requireLogin('每日签到'))) return;
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

/* ================== 用户区 / 登录引导 ==================
 * 登录方式（手机号快捷登录 / 账号密码登录 / 协议勾选）统一在 pages/login/index 中实现，
 * 本页只保留跳转入口，避免同一套登录 UI 存在两份。
 * 未登录时点击用户信息区 → 跳登录页；点需要登录的功能 → requireLogin 弹窗引导。
 */
const handleUserAreaClick = () => {
  if (!isLogin.value) {
    goLogin();
    return;
  }
  changeAvatar();
};

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

const goPointsDetail = async () => {
  if (!(await requireLogin('查看积分明细'))) return;
  uni.navigateTo({ url: '/pages/mine/points' });
};
const goOrders = async () => {
  if (!(await requireLogin('查看订单'))) return;
  uni.navigateTo({ url: '/pages/mine/orders' });
};
// 「我的宠物」是 tabBar 页面，必须用 switchTab
const goPets = async () => {
  if (!(await requireLogin('管理爱宠档案'))) return;
  uni.switchTab({ url: '/pages/pet/list' });
};

const handleMenuClick = async (item) => {
  if (item.needLogin && !(await requireLogin(item.title))) return;
  if (item.key === 'chats' && features.adviserEnabled === false) {
    showToast('功能升级中，敬请期待');
    return;
  }
  if (!item.path) {
    showToast('功能开发中');
    return;
  }
  // tabBar 页面只能用 switchTab，用 navigateTo 会失败
  if (item.tab) {
    uni.switchTab({ url: item.path });
    return;
  }
  uni.navigateTo({ url: item.path });
};

const handleLogout = () => {
  uni.showModal({
    title: '退出登录',
    content: '确认退出登录吗？宠物档案与健康记录不会丢失哦~',
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

.page-scroll {
  height: 100vh;
}

/* ========== 已登录：用户信息卡 ========== */
.user-card-wrap {
  position: relative;
  /* 通栏铺满，并与顶部导航栏无缝衔接（只有下方两个角是圆角） */
  margin: 0;
  overflow: hidden;
  border-radius: 0 0 $radius-xl $radius-xl;
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

  /* 装饰骨头的定位改用 bottom：卡片顶部现在包含导航栏占位，高度不固定 */
  .deco-heart {
    position: absolute;
    right: 110rpx;
    bottom: 230rpx;
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
  justify-content: flex-end;
  padding: 20rpx 32rpx 0;

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
}

.user-main {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  padding: 24rpx 32rpx 24rpx;
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

  .nickname {
    display: block;
    font-size: 42rpx;
    font-weight: $font-weight-bold;
    color: #fff;
    line-height: 1.3;
    text-shadow: 0 2rpx 10rpx rgba(185, 61, 9, 0.2);
    @include pet-ellipsis;
  }

  .id-label {
    display: block;
    margin-top: 10rpx;
    font-size: $font-xs;
    color: rgba(255, 255, 255, 0.75);
  }
}

/* 数据统计：独立一行，左右与卡片内边距对齐 */
.stats-row {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  margin: 0 32rpx 32rpx;
  padding: 22rpx 6rpx;
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
</style>
