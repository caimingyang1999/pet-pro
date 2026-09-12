import request from '@/utils/request'

/**
 * 获取宠物列表
 * @param {Object} query - 查询参数
 * @param {number} query.pageNum - 页码
 * @param {number} query.pageSize - 每页数量
 * @param {string} [query.name] - 宠物名称（模糊）
 * @param {string} [query.userKeyword] - 所属用户（昵称/账号/手机号，模糊）
 * @param {string} [query.breed] - 品种（模糊）
 * @returns {Promise<Object>} 宠物列表数据（含 userName 所属用户昵称、vaccineCount 疫苗记录数）
 */
export function getPetList(query) {
  return request({
    url: '/api/v1/admin/pets',
    method: 'get',
    params: query
  })
}

/**
 * 获取品种选项列表（去重，筛选下拉用）
 * @returns {Promise<Object>} 品种集合
 */
export function getBreedOptions() {
  return request({
    url: '/api/v1/admin/pets/breeds',
    method: 'get'
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
