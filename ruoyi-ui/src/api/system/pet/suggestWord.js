import request from '@/utils/request'

/**
 * 分页查询AI搜索推荐词列表
 * @param {Object} query - 查询参数
 * @param {number} query.pageNum - 页码
 * @param {number} query.pageSize - 每页条数
 * @param {string} [query.word] - 提示词内容（模糊查询）
 * @param {string} [query.petType] - 宠物类型（general/cat/dog/rabbit/bird/fish/other）
 * @param {string} [query.category] - 分类（general/care/diet/medical/behavior/training）
 * @param {string} [query.status] - 状态（0-停用 1-启用）
 * @returns {Promise<Object>} 推荐词列表数据
 */
export function getSuggestWordList(query) {
  return request({
    url: '/api/v1/admin/suggest-words/list',
    method: 'get',
    params: query
  })
}

/**
 * 获取推荐词详情
 * @param {string|number} id - 推荐词ID
 * @returns {Promise<Object>} 推荐词详情数据
 */
export function getSuggestWordDetail(id) {
  return request({
    url: '/api/v1/admin/suggest-words/' + id,
    method: 'get'
  })
}

/**
 * 新增推荐词
 * @param {Object} data - 推荐词数据
 * @param {string} data.word - 提示词内容（必填）
 * @param {string} data.petType - 宠物类型（必填，general/cat/dog/rabbit/bird/fish/other）
 * @param {string} [data.category] - 分类（默认 general，general/care/diet/medical/behavior/training）
 * @param {number} [data.weight] - 基础权重（默认 100，数字越大越靠前）
 * @param {string} [data.status] - 状态（默认 1，0-停用 1-启用）
 * @returns {Promise<Object>} 新增结果
 */
export function addSuggestWord(data) {
  return request({
    url: '/api/v1/admin/suggest-words',
    method: 'post',
    data: data
  })
}

/**
 * 编辑推荐词
 * @param {Object} data - 推荐词数据
 * @param {number} data.id - 推荐词ID
 * @returns {Promise<Object>} 编辑结果
 */
export function updateSuggestWord(data) {
  return request({
    url: '/api/v1/admin/suggest-words/' + data.id,
    method: 'put',
    data: data
  })
}

/**
 * 删除推荐词（逻辑删除）
 * @param {string|number} id - 推荐词ID
 * @returns {Promise<Object>} 删除结果
 */
export function deleteSuggestWord(id) {
  return request({
    url: '/api/v1/admin/suggest-words/' + id,
    method: 'delete'
  })
}
