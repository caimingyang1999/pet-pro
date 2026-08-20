import request from '@/utils/request'

/**
 * 获取订单列表
 * @param {Object} query - 查询参数
 * @param {number} query.pageNum - 页码
 * @param {number} query.pageSize - 每页数量
 * @param {string} [query.orderNo] - 订单号
 * @param {string} [query.status] - 订单状态（0-待发货 1-已发货 2-已完成 3-已取消）
 * @param {string} [query.startTime] - 开始时间
 * @param {string} [query.endTime] - 结束时间
 * @returns {Promise<Object>} 订单列表数据
 */
export function getOrderList(query) {
  return request({
    url: '/api/v1/admin/orders',
    method: 'get',
    params: query
  })
}

/**
 * 获取订单详情（含下单用户和收货地址）
 * @param {string|number} id - 订单ID
 * @returns {Promise<Object>} 订单详情数据（data.user-用户信息，data.address-收货地址）
 */
export function getOrderDetail(id) {
  return request({
    url: '/api/v1/admin/orders/' + id + '/detail',
    method: 'get'
  })
}

/**
 * 更新订单状态/发货
 * @param {string|number} id - 订单ID
 * @param {Object} data - 状态数据
 * @param {string} data.status - 订单状态（0-待发货 1-已发货 2-已完成 3-已取消）
 * @param {string} [data.expressCompany] - 快递公司
 * @param {string} [data.expressNo] - 快递单号
 * @returns {Promise<Object>} 操作结果
 */
export function updateOrderStatus(id, data) {
  return request({
    url: '/api/v1/admin/orders/' + id + '/status',
    method: 'put',
    data: data
  })
}
