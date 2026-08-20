package com.ruoyi.web.controller.admin;

import java.util.List;
import javax.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.ShopProduct;
import com.ruoyi.system.domain.dto.ProductQueryDTO;
import com.ruoyi.system.service.IShopService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 后台-商品管理 控制器
 *
 * @author ruoyi
 */
@Api(tags = "后台-商品管理")
@RestController
@RequestMapping("/api/v1/admin/products")
@PreAuthorize("@ss.hasPermi('admin')")
public class AdminProductController extends BaseController
{
    @Resource
    private IShopService shopService;

    /**
     * 商品列表（含分页、分类筛选）
     *
     * @param categoryId 分类ID
     * @param keyword    搜索关键词
     * @param pageNum    当前页码
     * @param pageSize   每页条数
     * @return 商品分页列表
     */
    @ApiOperation("商品列表")
    @Log(title = "商品管理", businessType = BusinessType.OTHER)
    @GetMapping
    public TableDataInfo list(
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
     * @param productId 商品ID
     * @return 商品详情
     */
    @ApiOperation("商品详情")
    @Log(title = "商品管理", businessType = BusinessType.OTHER)
    @GetMapping("/{productId}")
    public AjaxResult getInfo(
            @ApiParam(name = "productId", value = "商品ID", required = true)
            @PathVariable Long productId)
    {
        return AjaxResult.success(shopService.getProductDetail(productId));
    }

    /**
     * 新增商品
     *
     * @param product 商品信息（productImages-图片JSON数组字符串）
     * @return 操作结果
     */
    @ApiOperation("新增商品")
    @Log(title = "商品管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ShopProduct product)
    {
        return toAjax(shopService.save(product));
    }

    /**
     * 编辑商品
     *
     * @param productId 商品ID
     * @param product   商品信息（productImages-图片多个URL以逗号分隔）
     * @return 操作结果
     */
    @ApiOperation("编辑商品")
    @Log(title = "商品管理", businessType = BusinessType.UPDATE)
    @PutMapping("/{productId}")
    public AjaxResult edit(
            @ApiParam(name = "productId", value = "商品ID", required = true)
            @PathVariable Long productId,
            @Validated @RequestBody ShopProduct product)
    {
        product.setId(productId);
        return toAjax(shopService.updateById(product));
    }

    /**
     * 删除商品
     *
     * @param productId 商品ID
     * @return 操作结果
     */
    @ApiOperation("删除商品")
    @Log(title = "商品管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{productId}")
    public AjaxResult remove(
            @ApiParam(name = "productId", value = "商品ID", required = true)
            @PathVariable Long productId)
    {
        return toAjax(shopService.removeById(productId));
    }

    /**
     * 上下架
     *
     * @param productId 商品ID
     * @param product   商品信息（status-0下架 1上架）
     * @return 操作结果
     */
    @ApiOperation("商品上下架")
    @Log(title = "商品管理", businessType = BusinessType.UPDATE)
    @PutMapping("/{productId}/status")
    public AjaxResult status(
            @ApiParam(name = "productId", value = "商品ID", required = true)
            @PathVariable Long productId,
            @RequestBody ShopProduct product)
    {
        if (!"0".equals(product.getStatus()) && !"1".equals(product.getStatus()))
        {
            return AjaxResult.error("商品状态非法，仅支持 0-下架 1-上架");
        }
        LambdaUpdateWrapper<ShopProduct> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ShopProduct::getId, productId).set(ShopProduct::getStatus, product.getStatus());
        return toAjax(shopService.update(wrapper));
    }
}
