import { reactive } from 'vue';
import request from '@/api/request.js';

/**
 * 功能开关（Feature Toggle）
 *
 * adviserEnabled：养宠助手模块开关
 *  - 本地默认值：可在 .env 中设置 VITE_ADVISER_ENABLED=false 硬关闭（提审包建议关闭）
 *  - 远程下发：App 启动时请求 GET /config/features，以后端返回为准
 *  - 降级策略：远程接口不可用时，回退到上次成功拉取的本地缓存，再不行用本地默认值
 *  - 审核通过后，后端把开关置为 true 即可恢复功能，无需重新发版
 */

const FEATURES_CACHE_KEY = 'app_features';

// 本地默认开关（.env 未配置时默认开启）
const LOCAL_DEFAULT = {
  adviserEnabled: import.meta.env.VITE_ADVISER_ENABLED !== 'false',
  // 商城演示模式：个人作品，兑换不真实发货，界面统一展示「功能演示」提示。
  // 日后升级企业主体并真实发货时，配置 VITE_SHOP_DEMO_MODE=false 或后端下发 false 即可关闭。
  shopDemoMode: import.meta.env.VITE_SHOP_DEMO_MODE !== 'false',
};

export const features = reactive({ ...LOCAL_DEFAULT });

/** 养宠助手模块是否启用 */
export const isAdviserEnabled = () => features.adviserEnabled !== false;

let fetchPromise = null;

/**
 * 拉取远程功能开关（全局只请求一次，重复调用复用同一个 Promise）
 */
export function fetchFeatures() {
  if (fetchPromise) return fetchPromise;
  fetchPromise = (async () => {
    try {
      const res = await request({ url: '/config/features', method: 'GET', silent: true });
      const remote = res?.data;
      if (remote && typeof remote === 'object') {
        if (typeof remote.adviserEnabled === 'boolean') {
          features.adviserEnabled = remote.adviserEnabled;
        }
        if (typeof remote.shopDemoMode === 'boolean') {
          features.shopDemoMode = remote.shopDemoMode;
        }
        uni.setStorageSync(FEATURES_CACHE_KEY, remote);
      }
    } catch (e) {
      // 远程不可用：回退到上次缓存，无缓存则保持本地默认值
      const cached = uni.getStorageSync(FEATURES_CACHE_KEY);
      if (cached && typeof cached.adviserEnabled === 'boolean') {
        features.adviserEnabled = cached.adviserEnabled;
      }
      if (cached && typeof cached.shopDemoMode === 'boolean') {
        features.shopDemoMode = cached.shopDemoMode;
      }
    }
  })();
  return fetchPromise;
}
