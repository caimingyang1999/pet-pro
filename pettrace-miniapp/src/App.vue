<script setup>
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app';
import { useUserStore } from '@/store/user.js';

onLaunch(() => {
  console.log('App Launch');
  const userStore = useUserStore();
  userStore.initUserInfo();
  // 检查登录状态，未登录则跳转登录页
  checkLoginAndRedirect();
});

onShow(() => {
  console.log('App Show');
  // 每次回到前台也检查一次
  checkLoginAndRedirect();
});

onHide(() => {
  console.log('App Hide');
});

/**
 * 检查登录状态，如果未登录则跳转到全屏登录页（非 tabbar）
 */
const checkLoginAndRedirect = () => {
  const token = uni.getStorageSync('token');
  if (!token) {
    // 延迟跳转，避免与页面初始化冲突
    setTimeout(() => {
      const pages = getCurrentPages();
      if (pages.length === 0) return;
      const currentRoute = pages[pages.length - 1]?.route;
      // 如果已经在登录/注册页面，不再跳转
      if (
        currentRoute === 'pages/login/index' ||
        currentRoute === 'pages/login/register'
      ) {
        return;
      }
      // reLaunch 到全屏登录页，清空页面栈，不显示 tabbar
      uni.reLaunch({ url: '/pages/login/index' });
    }, 200);
  }
};
</script>

<style lang="scss">
@import "uview-plus/index.scss";
@import "@/static/iconfont/iconfont.scss";

page {
  background-color: #F5F6FA;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

view, text, scroll-view {
  box-sizing: border-box;
}

image {
  display: block;
}

.iconfont {
  font-family: "iconfont", "Apple Color Emoji", "Segoe UI Emoji", "Noto Color Emoji", sans-serif;
  font-style: normal;
  line-height: 1;
}
</style>
