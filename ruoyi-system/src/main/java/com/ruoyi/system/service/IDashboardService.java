package com.ruoyi.system.service;

import com.ruoyi.system.domain.vo.DashboardOverviewVO;

/**
 * 后台首页数据总览 服务层
 *
 * @author ruoyi
 */
public interface IDashboardService
{
    /**
     * 获取首页数据总览
     *
     * @param days 趋势统计天数（近 days 天，如 7/14/30）
     * @return 首页看板数据
     */
    DashboardOverviewVO getOverview(int days);
}
