import request from '@/utils/request'

/**
 * 分页查询轮播图列表
 * @param {Object} query - 查询参数
 * @param {number} query.pageNum - 页码
 * @param {number} query.pageSize - 每页条数
 * @param {string} [query.title] - 标题（模糊搜索）
 * @param {string} [query.status] - 状态（0-停用 1-启用）
 * @returns {Promise<Object>} 轮播图列表数据
 */
export function getBannerList(query) {
  return request({
    url: '/api/v1/admin/banner/list',
    method: 'get',
    params: query
  })
}

/**
 * 获取轮播图详情
 * @param {string|number} id - 轮播图ID
 * @returns {Promise<Object>} 轮播图详情数据
 */
export function getBannerDetail(id) {
  return request({
    url: '/api/v1/admin/banner/' + id,
    method: 'get'
  })
}

/**
 * 新增轮播图
 * @param {Object} data - 轮播图数据
 * @param {string} data.title - 标题（必填）
 * @param {string} data.imageUrl - 图片地址（必填，通过公共上传接口获取）
 * @param {string} data.jumpType - 跳转类型（必填，none/post/product/url/miniapp）
 * @param {string} [data.jumpTarget] - 跳转目标
 * @param {number} [data.sortOrder] - 排序号（默认当前最大值+1）
 * @param {string} [data.status] - 状态（0-停用 1-启用，默认1）
 * @param {string} [data.startTime] - 展示开始时间
 * @param {string} [data.endTime] - 展示结束时间
 * @param {string} [data.remark] - 备注
 * @returns {Promise<Object>} 新增结果
 */
export function addBanner(data) {
  return request({
    url: '/api/v1/admin/banner/',
    method: 'post',
    data: data
  })
}

/**
 * 编辑轮播图
 * @param {Object} data - 轮播图数据
 * @param {number} data.id - 轮播图ID
 * @returns {Promise<Object>} 编辑结果
 */
export function updateBanner(data) {
  return request({
    url: '/api/v1/admin/banner/' + data.id,
    method: 'put',
    data: data
  })
}

/**
 * 删除轮播图（逻辑删除）
 * @param {string|number} id - 轮播图ID
 * @returns {Promise<Object>} 删除结果
 */
export function deleteBanner(id) {
  return request({
    url: '/api/v1/admin/banner/' + id,
    method: 'delete'
  })
}
