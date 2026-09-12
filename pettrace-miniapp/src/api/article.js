import { get } from './request.js';

/**
 * 养宠知识文章列表（平台发布，只读；无需登录）
 * @param {Object} params - { pageNum, pageSize, category, keyword }
 */
export function getArticleList(params) {
  return get('/articles/list', params);
}

/**
 * 养宠知识文章详情（浏览量 +1）
 * @param {number|string} id 文章ID
 */
export function getArticleDetail(id) {
  return get(`/articles/${id}`);
}
