import { get, post } from './request.js';

/**
 * 获取商品分类树
 * @returns {Promise} data: [{id, parentId, categoryName, icon, sortOrder, status, children}]
 */
export function getCategories() {
  return get('/shop/categories');
}

/**
 * 获取商品列表（分页）
 * @param {Object} params - { pageNum, pageSize, categoryId, keyword }
 * @returns {Promise} rows: 商品列表, total: 总数
 */
export function getProductList(params) {
  return get('/shop/products', params);
}

/**
 * 获取商品详情
 * @param {number|string} id - 商品ID
 */
export function getProductDetail(id) {
  return get(`/shop/products/${id}`);
}

/**
 * 兑换商品（创建订单）
 * @param {Object} data - { productId, quantity, addressId }
 */
export function createOrder(data) {
  return post('/shop/orders', data);
}

/**
 * 获取我的订单列表
 * @param {Object} params - { status }
 */
export function getOrderList(params) {
  return get('/shop/orders', params);
}

/**
 * 获取订单详情
 * @param {number|string} id - 订单ID
 */
export function getOrderDetail(id) {
  return get(`/shop/orders/${id}`);
}
