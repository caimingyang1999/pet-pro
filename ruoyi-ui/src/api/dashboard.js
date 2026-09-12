import request from '@/utils/request'

/**
 * 获取后台首页数据总览
 * @param {number} days 趋势统计天数（7/14/30）
 * @returns {Promise<DashboardOverviewVO>}
 */
export function getDashboardOverview(days = 7) {
  return request({
    url: '/api/v1/admin/dashboard/overview',
    method: 'get',
    params: { days }
  })
}
