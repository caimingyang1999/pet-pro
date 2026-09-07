import { get, post, put, del } from './request.js';

/**
 * 微信小程序登录
 * 实际请求路径：POST /api/v1/wx/login
 * @param {Object} data { code, nickName, avatar, gender }
 * @param {Object} options 可选请求选项（如 silent: true 静默失败）
 */
export function wxLogin(data, options = {}) {
  return post('/wx/login', data, options);
}

/**
 * 微信手机号登录
 * @param {Object} data { code, phoneCode }
 * @param {Object} options 可选请求选项（如 silent: true 静默失败）
 */
export function wxPhoneLogin(data, options = {}) {
  return post('/wx/phone-login', data, options);
}

/**
 * 绑定手机号
 * @param {Object} data { code }
 */
export function bindPhone(data) {
  return post('/wx/bind-phone', data);
}

/**
 * 账号密码登录
 * 实际请求路径：POST /api/v1/user/uertlogin
 * @param {Object} data { username, password }
 */
export function accountLogin(data) {
  return post('/user/uertlogin', data);
}

/**
 * 账号注册
 * 实际请求路径：POST /api/v1/register
 * @param {Object} data { phone, password, nickname }
 * @returns {Promise} 成功返回 { token, user | userInfo, ... }
 */
export function register(data) {
  return post('/register', data);
}

/**
 * 获取用户信息
 */
export function getUserInfo() {
  return get('/user/info');
}

/**
 * 获取积分记录
 * @param {Object} params
 */
export function getPointsLog(params) {
  return get('/user/points/log', params);
}

/**
 * 获取今日签到状态
 */
export function getSignInStatus() {
  return get('/user/sign-in/status');
}

/**
 * 每日签到
 */
export function signIn() {
  return post('/user/sign-in');
}

/**
 * 获取收货地址列表
 */
export function getAddressList() {
  return get('/user/addresses');
}

/**
 * 添加收货地址
 * @param {Object} data
 */
export function addAddress(data) {
  return post('/user/addresses', data);
}

/**
 * 更新收货地址
 * @param {number|string} id
 * @param {Object} data
 */
export function updateAddress(id, data) {
  return put(`/user/addresses/${id}`, data);
}

/**
 * 删除收货地址
 * @param {number|string} id
 */
export function deleteAddress(id) {
  return del(`/user/addresses/${id}`);
}

/**
 * 更新用户头像
 * （注：实际上传通过 uni.uploadFile，此处仅保留接口封装以备调用）
 * @param {FormData|Object} data
 */
export function updateAvatar(data) {
  return post('/user/avatar', data);
}
