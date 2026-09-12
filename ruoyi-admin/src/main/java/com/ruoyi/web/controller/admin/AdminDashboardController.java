package com.ruoyi.web.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.vo.DashboardOverviewVO;
import com.ruoyi.system.service.IDashboardService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 后台-首页数据总览 控制器
 *
 * @author ruoyi
 */
@Api(tags = "后台-首页数据总览")
@RestController
@RequestMapping("/api/v1/admin/dashboard")
@PreAuthorize("@ss.hasPermi('admin')")
public class AdminDashboardController extends BaseController
{
    @Autowired
    private IDashboardService dashboardService;

    /**
     * 获取首页数据总览
     *
     * @param days 趋势统计天数（7/14/30，默认 7）
     * @return 首页看板数据（统计卡片、趋势、订单状态、待处理、最新动态、热门商品、用户增长）
     */
    @ApiOperation("首页数据总览")
    @Log(title = "首页数据总览", businessType = BusinessType.OTHER)
    @GetMapping("/overview")
    public AjaxResult overview(
            @ApiParam(name = "days", value = "趋势统计天数（7/14/30）")
            @RequestParam(defaultValue = "7") int days)
    {
        DashboardOverviewVO vo = dashboardService.getOverview(days);
        return AjaxResult.success(vo);
    }
}
