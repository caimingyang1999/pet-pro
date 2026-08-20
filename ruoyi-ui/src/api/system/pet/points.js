import request from '@/utils/request'

/**
 * 获取用户列表
 * @param {Object} query - 查询参数
 * @param {number} query.pageNum - 页码
 * @param {number} query.pageSize - 每页数量
 * @param {string} [query.keyword] - 关键词（用户名/手机号）
 * @returns {Promise<Object>} 用户列表数据
 */
export function getUserList(query) {
  return request({
    url: '/api/v1/admin/users',
    method: 'get',
    params: query
  })
}

/**
 * 积分操作
 * @param {string|number} userId - 用户ID
 * @param {Object} data - 积分数据
 * @param {number} data.pointsChange - 变动积分（正数增加，负数扣减，必填，不能为0）
 * @param {string} [data.remark] - 备注
 * @returns {Promise<Object>} 操作结果
 */
export function operatePoints(userId, data) {
  return request({
    url: '/api/v1/admin/users/' + userId + '/points',
    method: 'post',
    data: data
  })
}

/**
 * 获取用户积分明细列表
 * @param {string|number} userId - 用户ID
 * @param {Object} query - 查询参数
 * @param {number} query.pageNum - 页码
 * @param {number} query.pageSize - 每页数量
 * @returns {Promise<Object>} 积分明细列表数据
 */
export function getPointsRecordList(userId, query) {
  return request({
    url: '/api/v1/admin/users/' + userId + '/points/records',
    method: 'get',
    params: query
  })
}
