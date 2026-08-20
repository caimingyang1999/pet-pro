import request from '@/utils/request'

/**
 * 获取宠物列表
 * @param {Object} query - 查询参数
 * @param {number} query.pageNum - 页码
 * @param {number} query.pageSize - 每页数量
 * @param {string} [query.keyword] - 关键词
 * @param {number} [query.userId] - 用户ID
 * @returns {Promise<Object>} 宠物列表数据
 */
export function getPetList(query) {
  return request({
    url: '/api/v1/admin/pets',
    method: 'get',
    params: query
  })
}

/**
 * 获取宠物详情
 * @param {string|number} id - 宠物ID
 * @returns {Promise<Object>} 宠物详情数据
 */
export function getPetDetail(id) {
  return request({
    url: '/api/v1/admin/pets/' + id,
    method: 'get'
  })
}

/**
 * 删除宠物
 * @param {string|number} id - 宠物ID
 * @returns {Promise<Object>} 删除结果
 */
export function deletePet(id) {
  return request({
    url: '/api/v1/admin/pets/' + id,
    method: 'delete'
  })
}
