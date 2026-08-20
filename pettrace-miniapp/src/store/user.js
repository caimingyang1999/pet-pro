import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import {
  wxLogin,
  wxPhoneLogin as wxPhoneLoginApi,
  bindPhone,
  getUserInfo,
  accountLogin as accountLoginApi,
  register as registerApi,
} from '@/api/user.js';
import { BASE_URL, SERVER_BASE } from '@/api/request.js';

export const useUserStore = defineStore('user', () => {
  // State
  const token = ref(uni.getStorageSync('token') || '');
  const userInfo = ref(null);

  // Getters
  const isLogin = computed(() => !!token.value);

  // Actions
  const setToken = (val) => {
    token.value = val;
    uni.setStorageSync('token', val);
  };

  const setUserInfo = (val) => {
    userInfo.value = val;
    uni.setStorageSync('userInfo', JSON.stringify(val));
  };

  const initUserInfo = () => {
    const stored = uni.getStorageSync('userInfo');
    if (stored) {
      try {
        const info = JSON.parse(stored);
        // 本地缓存的 avatar 可能是相对路径，补齐完整 URL
        if (info && info.avatar && !info.avatar.startsWith('http')) {
          info.avatar = `${SERVER_BASE}${info.avatar}`;
        }
        userInfo.value = info;
      } catch (e) {
        userInfo.value = null;
      }
    }
  };

  /**
   * 获取微信登录 code
   */
  const getWxCode = () => {
    return new Promise((resolve, reject) => {
      uni.login({
        provider: 'weixin',
        success: (res) => {
          console.log('[1.uni.login成功]', res);
          if (res.code) {
            resolve(res.code);
          } else {
            reject(new Error('获取微信code失败'));
          }
        },
        fail: (err) => {
          console.error('[1.uni.login失败]', err);
          reject(err);
        },
      });
    });
  };

  /**
   * 微信小程序静默登录（支持传入用户信息，不弹授权窗）
   * @param {Object} profile 可选 { nickName, avatar, gender }
   */
  const login = async (profile = {}) => {
    console.log('[静默登录开始] profile=', profile);
    const code = await getWxCode();
    const reqData = {
      code,
      nickName: profile.nickName || '',
      avatar: profile.avatar || '',
      gender: profile.gender || '',
    };
    const res = await wxLogin(reqData);
    const data = res.data || res || {};
    if (data.token) {
      setToken(data.token);
      try {
        await fetchUserInfo();
      } catch (e) {
        console.error('[login] 获取用户信息失败，code:', e?.code, '，msg:', e?.msg);
      }
    }
    return data;
  };

  /**
   * 微信手机号登录
   * 先获取 token，再调用 /api/v1/user/info 获取完整用户信息并回显
   * @param {Object} params { code: wx.login的code, phoneCode: getPhoneNumber的code }
   */
  const wxPhoneLogin = async ({ code, phoneCode }) => {
    let res;
    try {
      res = await wxPhoneLoginApi({ code, phoneCode }, { silent: true });
    } catch (err) {
      // 后端未提供 /wx/phone-login 时（404），降级为 openid 静默登录 + 绑定手机号
      if (err?.statusCode !== 404) {
        throw err;
      }
      console.warn('[wxPhoneLogin] /wx/phone-login 接口不存在(404)，降级为 openid 登录 + 绑定手机号');
      const loginRes = await wxLogin({ code }, { silent: true });
      const loginData = loginRes.data || loginRes || {};
      if (loginData.token) {
        setToken(loginData.token);
        await bindPhone({ code: phoneCode });
      }
      res = loginRes;
    }

    const data = res.data || res || {};
    if (data.token) {
      setToken(data.token);
      // 登录接口只返回 token，需要额外请求用户信息接口
      try {
        await fetchUserInfo();
      } catch (e) {
        console.error('[wxPhoneLogin] 获取用户信息失败，code:', e?.code, '，msg:', e?.msg);
      }
    }
    return data;
  };

  /**
   * 账号密码登录
   * 先调用 /api/v1/user/uertlogin 获取 token，
   * 再调用 /api/v1/user/info 获取完整用户信息并回显
   * @param {Object} params { username, password }
   */
  const accountLogin = async ({ username, password }) => {
    const res = await accountLoginApi({ username, password });
    const data = res.data || res || {};
    if (data.token) {
      setToken(data.token);
      // 登录接口只返回 token，需要额外请求用户信息接口
      try {
        await fetchUserInfo();
      } catch (e) {
        console.error('[accountLogin] 获取用户信息失败，code:', e?.code, '，msg:', e?.msg);
      }
    }
    return data;
  };

  /**
   * 注册
   * @param {Object} params { phone, password, confirmPassword, nickname }
   */
  const register = async ({ phone, password, confirmPassword, nickname }) => {
    const res = await registerApi({ phone, password, confirmPassword, nickname });
    const data = res.data || res || {};
    if (data.token) {
      setToken(data.token);
      // 优先使用注册接口返回的用户信息
      if (data.user || data.userInfo) {
        setUserInfo(data.user || data.userInfo);
      }
    }
    // 注册成功后再请求一次用户信息，确保数据完整
    if (data.token) {
      try {
        await fetchUserInfo();
      } catch (e) {
        console.error('[register] 获取用户信息失败，code:', e?.code, '，msg:', e?.msg);
      }
    }
    return data;
  };

  /**
   * 绑定手机号
   * @param {string} phoneCode 微信 getPhoneNumber 返回的 code
   */
  const bindUserPhone = async (phoneCode) => {
    const res = await bindPhone({ code: phoneCode });
    const data = res.data || {};
    if (data.token) {
      setToken(data.token);
    }
    if (data) {
      setUserInfo(data);
    }
    return data;
  };

  const fetchUserInfo = async () => {
    try {
      console.log('[fetchUserInfo] 开始请求 /user/info，当前 token:', token.value ? `${token.value.substring(0, 20)}...` : '空');
      const res = await getUserInfo();
      console.log('[fetchUserInfo] 接口返回:', JSON.stringify(res));
      if (res && res.data) {
        // 后端返回的相对路径 avatar 需要拼接完整 URL
        const info = res.data;
        if (info.avatar && !info.avatar.startsWith('http')) {
          info.avatar = `${SERVER_BASE}${info.avatar}`;
        }
        setUserInfo(info);
        console.log('[fetchUserInfo] 用户信息已保存:', JSON.stringify(info));
      } else {
        console.error('[fetchUserInfo] 返回数据格式异常，res:', JSON.stringify(res));
      }
      return res.data;
    } catch (err) {
      console.error('[fetchUserInfo] 请求失败，code:', err?.code, '，statusCode:', err?.statusCode, '，msg:', err?.msg, '，errMsg:', err?.errMsg);
      return Promise.reject(err);
    }
  };

  const logout = () => {
    token.value = '';
    userInfo.value = null;
    uni.removeStorageSync('token');
    uni.removeStorageSync('userInfo');
  };

  return {
    token,
    userInfo,
    isLogin,
    setToken,
    setUserInfo,
    initUserInfo,
    getWxCode,
    login,
    wxPhoneLogin,
    accountLogin,
    register,
    bindUserPhone,
    fetchUserInfo,
    logout,
  };
});
