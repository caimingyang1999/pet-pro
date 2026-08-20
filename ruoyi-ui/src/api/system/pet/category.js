import request from '@/utils/request'

/**
 * 获取分类树
 * @returns {Promise<Array>} 分类树结构数据
 */
export function getCategoryList() {
  return request({
    url: '/api/v1/admin/categories',
    method: 'get'
  })
}

/**
 * 新增分类
 * @param {Object} data - 分类数据
 * @param {string} data.categoryName - 分类名称
 * @param {number} [data.parentId] - 父分类ID
 * @param {number} [data.sortOrder] - 排序
 * @returns {Promise<Object>} 新增结果
 */
export function addCategory(data) {
  return request({
    url: '/api/v1/admin/categories',
    method: 'post',
    data: data
  })
}

/**
 * 编辑分类
 * @param {Object} data - 分类数据
 * @param {number} data.id - 分类ID
 * @param {string} data.categoryName - 分类名称
 * @param {number} [data.parentId] - 父分类ID
 * @param {number} [data.sortOrder] - 排序
 * @returns {Promise<Object>} 编辑结果
 */
export function updateCategory(data) {
  return request({
    url: '/api/v1/admin/categories/' + data.id,
    method: 'put',
    data: data
  })
}

/**
 * 删除分类
 * @param {string|number} id - 分类ID
 * @returns {Promise<Object>} 删除结果
 */
export function deleteCategory(id) {
  return request({
    url: '/api/v1/admin/categories/' + id,
    method: 'delete'
  })
}
