package com.ruoyi.web.controller.admin;

import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.ShopOrder;
import com.ruoyi.system.service.IOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 后台-订单管理 控制器
 *
 * @author ruoyi
 */
@Api(tags = "后台-订单管理")
@RestController
@RequestMapping("/api/v1/admin/orders")
@PreAuthorize("@ss.hasPermi('admin')")
public class AdminOrderController extends BaseController
{
    @Resource
    private IOrderService orderService;

    /**
     * 订单列表（支持状态筛选）
     *
     * @param status 订单状态（0-待发货 1-已发货 2-已完成 3-已取消）
     * @return 订单分页列表
     */
    @ApiOperation("订单列表")
    @Log(title = "订单管理", businessType = BusinessType.OTHER)
    @GetMapping
    public TableDataInfo list(
            @ApiParam(name = "orderNo", value = "订单编号（模糊搜索）") @RequestParam(required = false) String orderNo,
            @ApiParam(name = "status", value = "订单状态（0-待发货 1-已发货 2-已完成 3-已取消）") @RequestParam(required = false) String status)
    {
        startPage();
        // userId 传 null 查询全部用户订单
        List<ShopOrder> list = orderService.getOrderList(null, orderNo, status);
        return getDataTable(list);
    }

    /**
     * 导出订单列表
     *
     * @param response 响应对象
     * @param status   订单状态（0-待发货 1-已发货 2-已完成 3-已取消）
     */
    @ApiOperation("导出订单列表")
    @Log(title = "订单管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response,
            @ApiParam(name = "orderNo", value = "订单编号（模糊搜索）") @RequestParam(required = false) String orderNo,
            @ApiParam(name = "status", value = "订单状态（0-待发货 1-已发货 2-已完成 3-已取消）") @RequestParam(required = false) String status)
    {
        // userId 传 null 查询全部用户订单
        List<ShopOrder> list = orderService.getOrderList(null, orderNo, status);
        ExcelUtil<ShopOrder> util = new ExcelUtil<ShopOrder>(ShopOrder.class);
        util.exportExcel(response, list, "订单数据");
    }

    /**
     * 订单详情
     *
     * @param orderId 订单ID
     * @return 订单详情
     */
    @ApiOperation("订单详情")
    @Log(title = "订单管理", businessType = BusinessType.OTHER)
    @GetMapping("/{orderId}")
    public AjaxResult getInfo(
            @ApiParam(name = "orderId", value = "订单ID", required = true)
            @PathVariable Long orderId)
    {
        return AjaxResult.success(orderService.getOrderDetail(orderId));
    }

    /**
     * 订单详情（含关联用户和收货地址信息）
     *
     * @param orderId 订单ID
     * @return 订单详情（订单信息 + 用户信息 + 地址信息）
     */
    @ApiOperation("订单详情（含用户和地址）")
    @Log(title = "订单管理", businessType = BusinessType.OTHER)
    @GetMapping("/{orderId}/detail")
    public AjaxResult detail(
            @ApiParam(name = "orderId", value = "订单ID", required = true)
            @PathVariable Long orderId)
    {
        return AjaxResult.success(orderService.getOrderDetailWithAssoc(orderId));
    }

    /**
     * 更新订单状态（发货等）
     *
     * @param orderId 订单ID
     * @param order   订单信息（status-订单状态，expressNo-快递单号，expressCompany-快递公司）
     * @return 操作结果
     */
    @ApiOperation("更新订单状态")
    @Log(title = "订单管理", businessType = BusinessType.UPDATE)
    @PutMapping("/{orderId}/status")
    public AjaxResult status(
            @ApiParam(name = "orderId", value = "订单ID", required = true)
            @PathVariable Long orderId,
            @RequestBody ShopOrder order)
    {
        if (StringUtils.isEmpty(order.getStatus()))
        {
            return AjaxResult.error("订单状态不能为空");
        }
        // 发货（status=1）时校验快递信息
        if ("1".equals(order.getStatus()))
        {
            if (StringUtils.isEmpty(order.getExpressNo()) || StringUtils.isEmpty(order.getExpressCompany()))
            {
                return AjaxResult.error("发货时请填写快递单号和快递公司");
            }
        }
        ShopOrder update = new ShopOrder();
        update.setId(orderId);
        update.setStatus(order.getStatus());
        update.setExpressNo(order.getExpressNo());
        update.setExpressCompany(order.getExpressCompany());
        return toAjax(orderService.updateById(update));
    }
}
