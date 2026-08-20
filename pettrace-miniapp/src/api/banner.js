import { get } from './request.js';

/**
 * 获取启用的轮播图列表（小程序端，无需登录）
 * 返回当前启用且在展示时间段内的轮播图列表，按排序号升序、ID降序排列
 * @returns {Promise<Array>} 轮播图列表
 */
export function getBannerList() {
  return get('/banner/list');
}
