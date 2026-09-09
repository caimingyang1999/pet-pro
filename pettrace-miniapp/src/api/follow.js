import { get, post } from './request.js';

/**
 * 关注 / 取关（切换）
 * @param {number|string} followeeId 被关注者用户ID
 * @returns {Promise<{ data: { followed: boolean } }>} followed=true 表示已关注
 */
export function toggleFollow(followeeId) {
  return post(`/follows/${followeeId}`);
}

/**
 * 查询当前用户是否已关注某用户
 * @param {number|string} followeeId
 * @returns {Promise<{ data: { followed: boolean } }>}
 */
export function checkFollowed(followeeId) {
  return get(`/follows/check/${followeeId}`);
}

/**
 * 查询某用户的关注列表
 * @param {Object} params - { userId?, pageNum, pageSize }
 */
export function getFollowList(params) {
  return get('/follows', params);
}

/**
 * 查询某用户的粉丝列表
 * @param {Object} params - { userId?, pageNum, pageSize }
 */
export function getFollowerList(params) {
  return get('/follows/followers', params);
}

/**
 * 查询某用户的关注数 / 粉丝数
 * @param {Object} params - { userId? }
 * @returns {Promise<{ data: { followCount: number, followerCount: number } }>}
 */
export function getFollowCount(params) {
  return get('/follows/count', params);
}
