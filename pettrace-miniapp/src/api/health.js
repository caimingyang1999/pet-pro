import { get, post, put, del } from './request.js';

/**
 * 宠物健康记录 API（体重 / 成长相册）
 *
 * 全部接口均需登录，且只能操作自己宠物的数据；
 * 成长相册内容仅本人可见。
 */

/* ==================== 体重记录 ==================== */

/**
 * 添加体重记录
 * @param {Object} data - { petId, weight, recordDate, remark }
 */
export function addWeightLog(data) {
  return post('/pet/weight', data);
}

/**
 * 获取某宠物的体重记录列表（按记录日期升序）
 * @param {number|string} petId
 */
export function getWeightLogList(petId) {
  return get(`/pet/${petId}/weight`);
}

/**
 * 删除体重记录
 * @param {number|string} id 记录ID
 */
export function deleteWeightLog(id) {
  return del(`/pet/weight/${id}`);
}

/* ==================== 成长相册 ==================== */

/**
 * 添加成长相册记录
 * @param {Object} data - { petId, title, content, images, recordDate }
 */
export function addAlbum(data) {
  return post('/pet/album', data);
}

/**
 * 获取某宠物的成长相册列表（按记录日期倒序）
 * @param {number|string} petId
 */
export function getAlbumList(petId) {
  return get(`/pet/${petId}/album`);
}

/**
 * 获取成长相册记录详情
 * @param {number|string} id 记录ID
 */
export function getAlbumDetail(id) {
  return get(`/pet/album/${id}`);
}

/**
 * 更新成长相册记录
 * @param {number|string} id 记录ID
 * @param {Object} data - { title, content, images, recordDate }
 */
export function updateAlbum(id, data) {
  return put(`/pet/album/${id}`, data);
}

/**
 * 删除成长相册记录
 * @param {number|string} id 记录ID
 */
export function deleteAlbum(id) {
  return del(`/pet/album/${id}`);
}
