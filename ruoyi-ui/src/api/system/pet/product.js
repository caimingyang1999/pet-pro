import request from '@/utils/request'

/**
 * 获取商品列表
 * @param {Object} query - 查询参数
 * @param {number} query.pageNum - 页码
 * @param {number} query.pageSize - 每页数量
 * @param {string} [query.keyword] - 关键词
 * @param {number} [query.categoryId] - 分类ID
 * @param {number} [query.status] - 商品状态
 * @returns {Promise<Object>} 商品列表数据
 */
export function getProductList(query) {
  return request({
    url: '/api/v1/admin/products',
    method: 'get',
    params: query
  })
}

/**
 * 获取商品详情
 * @param {string|number} id - 商品ID
 * @returns {Promise<Object>} 商品详情数据
 */
export function getProductDetail(id) {
  return request({
    url: '/api/v1/admin/products/' + id,
    method: 'get'
  })
}

/**
 * 新增商品
 * @param {Object} data - 商品数据
 * @param {string} data.productName - 商品名称
 * @param {number} data.categoryId - 分类ID
 * @param {number} data.pointsPrice - 商品价格
 * @param {string} [data.description] - 商品描述
 * @param {string} [data.productImages] - 商品图片
 * @returns {Promise<Object>} 新增结果
 */
export function addProduct(data) {
  return request({
    url: '/api/v1/admin/products',
    method: 'post',
    data: data
  })
}

/**
 * 编辑商品
 * @param {Object} data - 商品数据
 * @param {number} data.id - 商品ID
 * @param {string} data.productName - 商品名称
 * @param {number} data.categoryId - 分类ID
 * @param {number} data.pointsPrice - 商品价格
 * @param {string} [data.description] - 商品描述
 * @param {string} [data.productImages] - 商品图片
 * @returns {Promise<Object>} 编辑结果
 */
export function updateProduct(data) {
  return request({
    url: '/api/v1/admin/products/' + data.id,
    method: 'put',
    data: data
  })
}

/**
 * 删除商品
 * @param {string|number} id - 商品ID
 * @returns {Promise<Object>} 删除结果
 */
export function deleteProduct(id) {
  return request({
    url: '/api/v1/admin/products/' + id,
    method: 'delete'
  })
}

/**
 * 商品上下架
 * @param {string|number} id - 商品ID
 * @param {number} status - 状态 0-下架 1-上架
 * @returns {Promise<Object>} 操作结果
 */
export function updateProductStatus(id, status) {
  const data = {
    status
  }
  return request({
    url: '/api/v1/admin/products/' + id + '/status',
    method: 'put',
    data: data
  })
}
