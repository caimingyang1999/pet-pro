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
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.ShopCategory;
import com.ruoyi.system.mapper.ShopCategoryMapper;
import com.ruoyi.system.service.IShopService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 后台-商品分类管理 控制器（树形结构）
 *
 * @author ruoyi
 */
@Api(tags = "后台-商品分类管理")
@RestController
@RequestMapping("/api/v1/admin/categories")
@PreAuthorize("@ss.hasPermi('admin')")
public class AdminCategoryController extends BaseController
{
    @Resource
    private IShopService shopService;

    @Resource
    private ShopCategoryMapper shopCategoryMapper;

    /**
     * 分类树列表
     *
     * @return 分类树形列表
     */
    @ApiOperation("分类树列表")
    @Log(title = "商品分类管理", businessType = BusinessType.OTHER)
    @GetMapping
    public AjaxResult list()
    {
        List<ShopCategory> list = shopService.getCategoryList();
        return AjaxResult.success(list);
    }

    /**
     * 分类详情
     *
     * @param categoryId 分类ID
     * @return 分类信息
     */
    @ApiOperation("分类详情")
    @Log(title = "商品分类管理", businessType = BusinessType.OTHER)
    @GetMapping("/{categoryId}")
    public AjaxResult getInfo(
            @ApiParam(name = "categoryId", value = "分类ID", required = true)
            @PathVariable Long categoryId)
    {
        return AjaxResult.success(shopCategoryMapper.selectById(categoryId));
    }

    /**
     * 新增分类
     *
     * @param category 分类信息（parentId-父ID，categoryName-名称，icon-图标，sortOrder-排序，status-状态）
     * @return 操作结果
     */
    @ApiOperation("新增分类")
    @Log(title = "商品分类管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ShopCategory category)
    {
        if (category.getParentId() == null)
        {
            category.setParentId(0L);
        }
        return toAjax(shopCategoryMapper.insert(category));
    }

    /**
     * 编辑分类
     *
     * @param categoryId 分类ID
     * @param category   分类信息
     * @return 操作结果
     */
    @ApiOperation("编辑分类")
    @Log(title = "商品分类管理", businessType = BusinessType.UPDATE)
    @PutMapping("/{categoryId}")
    public AjaxResult edit(
            @ApiParam(name = "categoryId", value = "分类ID", required = true)
            @PathVariable Long categoryId,
            @Validated @RequestBody ShopCategory category)
    {
        ShopCategory exist = shopCategoryMapper.selectById(categoryId);
        if (exist == null)
        {
            throw new ServiceException("分类不存在");
        }
        // 不允许将自身设为父分类
        if (category.getParentId() != null && category.getParentId().equals(categoryId))
        {
            return AjaxResult.error("上级分类不能选择自身");
        }
        category.setId(categoryId);
        return toAjax(shopCategoryMapper.updateById(category));
    }

    /**
     * 删除分类
     *
     * @param categoryId 分类ID
     * @return 操作结果
     */
    @ApiOperation("删除分类")
    @Log(title = "商品分类管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{categoryId}")
    public AjaxResult remove(
            @ApiParam(name = "categoryId", value = "分类ID", required = true)
            @PathVariable Long categoryId)
    {
        return toAjax(shopCategoryMapper.deleteById(categoryId));
    }
}
