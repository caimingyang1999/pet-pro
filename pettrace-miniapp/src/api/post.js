import { get, post, del } from './request.js';

/**
 * 获取动态列表（支持 tab 切换：recommend/follow/latest；支持关键词；支持指定用户动态）
 * @param {Object} params - { pageNum, pageSize, keyword, tab, userId }
 */
export function getPostList(params) {
  return get('/posts/list', params);
}

/**
 * 获取我的动态列表（需登录）
 */
export function getMyPosts(params) {
  return get('/posts/my', params);
}

/**
 * 获取我的点赞列表（需登录）
 * @param {Object} params - { pageNum, pageSize, keyword }
 */
export function getMyLikePosts(params) {
  return get('/posts/my-likes', params);
}

/**
 * 搜索用户（模糊匹配昵称）
 * @param {Object} params - { keyword, limit }
 */
export function searchUsers(params) {
  return get('/posts/search/users', params);
}

/**
 * 获取动态详情
 */
export function getPostDetail(id) {
  return get(`/posts/${id}`);
}

/**
 * 发布动态（支持图片 + 视频）
 * @param {Object} data - { petId?, content, images, videoUrl?, videoCover? }
 */
export function addPost(data) {
  return post('/posts', data);
}

/**
 * 删除动态
 */
export function deletePost(id) {
  return del(`/posts/${id}`);
}

/**
 * 点赞/取消点赞
 * @returns 返回 { liked: true/false }
 */
export function likePost(id) {
  return post(`/posts/${id}/like`);
}

/**
 * 获取评论列表
 */
export function getComments(postId, params) {
  return get(`/posts/${postId}/comments`, params);
}

/**
 * 添加评论
 */
export function addComment(postId, data) {
  return post(`/posts/${postId}/comments`, data);
}
