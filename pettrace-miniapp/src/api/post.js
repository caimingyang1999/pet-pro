import { get, post, del } from './request.js';

/**
 * 获取动态列表（公开）
 * @param {Object} params - { pageNum, pageSize, keyword }
 */
export function getPostList(params) {
  return get('/posts/list', params);
}

/**
 * 获取我的动态列表（需登录）
 * @param {Object} params - { pageNum, pageSize }
 */
export function getMyPosts(params) {
  return get('/posts/my', params);
}

/**
 * 获取动态详情
 * @param {number|string} id
 */
export function getPostDetail(id) {
  return get(`/posts/${id}`);
}

/**
 * 发布动态（需登录）
 * @param {Object} data - { petId?, content, images }
 */
export function addPost(data) {
  return post('/posts', data);
}

/**
 * 删除动态（需登录，校验是否为发布者）
 * @param {number|string} id
 */
export function deletePost(id) {
  return del(`/posts/${id}`);
}

/**
 * 点赞/取消点赞（需登录）
 * @param {number|string} id
 * @returns 返回 { liked: true/false }
 */
export function likePost(id) {
  return post(`/posts/${id}/like`);
}

/**
 * 获取评论列表（公开，树形结构）
 * @param {number|string} postId
 * @param {Object} params - { pageNum, pageSize }
 */
export function getComments(postId, params) {
  return get(`/posts/${postId}/comments`, params);
}

/**
 * 添加评论（需登录）
 * @param {number|string} postId
 * @param {Object} data - { parentId?, content }
 */
export function addComment(postId, data) {
  return post(`/posts/${postId}/comments`, data);
}
