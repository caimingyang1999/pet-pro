import request from '@/utils/request'

/**
 * 获取动态列表
 * @param {Object} query - 查询参数
 * @param {number} query.pageNum - 页码
 * @param {number} query.pageSize - 每页数量
 * @returns {Promise<Object>} 动态列表数据
 */
export function getPostList(query) {
  return request({
    url: '/api/v1/admin/posts',
    method: 'get',
    params: query
  })
}

/**
 * 获取动态详情
 * @param {string|number} postId - 动态ID
 * @returns {Promise<Object>} 动态详情数据
 */
export function getPostDetail(postId) {
  return request({
    url: '/api/v1/admin/posts/' + postId,
    method: 'get'
  })
}

/**
 * 审核动态
 * @param {string|number} postId - 动态ID
 * @param {Object} data - 审核数据
 * @param {string} data.status - 审核状态
 * @param {string} [data.remark] - 审核备注
 * @returns {Promise<Object>} 审核结果
 */
export function auditPost(postId, data) {
  return request({
    url: '/api/v1/admin/posts/' + postId + '/audit',
    method: 'put',
    data: data
  })
}

/**
 * 删除动态
 * @param {string|number} postId - 动态ID
 * @returns {Promise<Object>} 删除结果
 */
export function deletePost(postId) {
  return request({
    url: '/api/v1/admin/posts/' + postId,
    method: 'delete'
  })
}
