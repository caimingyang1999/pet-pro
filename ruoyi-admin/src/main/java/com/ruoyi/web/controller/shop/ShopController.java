package com.ruoyi.web.controller.shop;

import java.util.List;
import javax.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.system.domain.ShopCategory;
import com.ruoyi.system.domain.ShopOrder;
import com.ruoyi.system.domain.ShopProduct;
import com.ruoyi.system.domain.dto.OrderCreateDTO;
import com.ruoyi.system.domain.dto.ProductQueryDTO;
import com.ruoyi.system.service.IOrderService;
import com.ruoyi.system.service.IShopService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 商城 控制器
 *
 * @author ruoyi
 */
@Api(tags = "商城管理")
@RestController
@RequestMapping("/api/v1/shop")
public class ShopController extends BaseController
{
    @Resource
    private IShopService shopService;

    @Resource
    private IOrderService orderService;

    /**
     * 获取分类树
     *
     * 允许匿名访问：游客可浏览商城分类。
     *
     * @return 分类树形列表
     */
    @ApiOperation("获取分类树")
    @Anonymous
    @GetMapping("/categories")
    public AjaxResult categories()
    {
        List<ShopCategory> list = shopService.getCategoryList();
        return AjaxResult.success(list);
    }

    /**
     * 商品列表（支持分类ID、关键词、分页）
     *
     * 允许匿名访问：游客可浏览积分商城商品。
     *
     * @param categoryId 分类ID
     * @param keyword    搜索关键词
     * @param pageNum    当前页码
     * @param pageSize   每页条数
     * @return 商品分页列表
     */
    @ApiOperation("商品列表")
    @Anonymous
    @GetMapping("/products")
    public TableDataInfo products(
            @ApiParam(name = "categoryId", value = "分类ID") @RequestParam(required = false) Long categoryId,
            @ApiParam(name = "keyword", value = "搜索关键词") @RequestParam(required = false) String keyword,
            @ApiParam(name = "pageNum", value = "当前页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam(name = "pageSize", value = "每页条数") @RequestParam(defaultValue = "10") Integer pageSize)
    {
        startPage();
        ProductQueryDTO dto = new ProductQueryDTO();
        dto.setCategoryId(categoryId);
        dto.setKeyword(keyword);
        dto.setPageNum(pageNum);
        dto.setPageSize(pageSize);
        List<ShopProduct> list = shopService.getProductList(dto);
        return getDataTable(list);
    }

    /**
     * 商品详情
     *
     * 允许匿名访问：游客可查看商品详情。
     *
     * @param productId 商品ID
     * @return 商品详情
     */
    @ApiOperation("商品详情")
    @Anonymous
    @GetMapping("/products/{productId}")
    public AjaxResult productDetail(
            @ApiParam(name = "productId", value = "商品ID", required = true)
            @PathVariable Long productId)
    {
        return AjaxResult.success(shopService.getProductDetail(productId));
    }

    /**
     * 兑换商品（需登录，扣减积分+创建订单）
     *
     * @param dto 订单参数（productId-商品ID，quantity-数量，addressId-收货地址ID）
     * @return 订单编号
     */
    @ApiOperation("兑换商品")
    @PreAuthorize("@ss.isAuthenticated()")
    @PostMapping("/orders")
    public AjaxResult exchange(@RequestBody OrderCreateDTO dto)
    {
        dto.setUserId(getUserId());
        String orderNo = shopService.exchangeProduct(dto);
        AjaxResult ajax = AjaxResult.success("兑换成功");
        ajax.put("orderNo", orderNo);
        return ajax;
    }

    /**
     * 我的订单列表
     *
     * @param status 订单状态（可选：0-待发货 1-已发货 2-已完成 3-已取消）
     * @return 订单分页列表
     */
    @ApiOperation("我的订单列表")
    @PreAuthorize("@ss.isAuthenticated()")
    @GetMapping("/orders")
    public TableDataInfo orders(
            @ApiParam(name = "status", value = "订单状态") @RequestParam(required = false) String status)
    {
        startPage();
        List<ShopOrder> list = orderService.getOrderList(getUserId(), null, status);
        return getDataTable(list);
    }

    /**
     * 订单详情
     *
     * @param orderId 订单ID
     * @return 订单详情
     */
    @ApiOperation("订单详情")
    @PreAuthorize("@ss.isAuthenticated()")
    @GetMapping("/orders/{orderId}")
    public AjaxResult orderDetail(
            @ApiParam(name = "orderId", value = "订单ID", required = true)
            @PathVariable Long orderId)
    {
        return AjaxResult.success(orderService.getOrderDetail(orderId));
    }
}
