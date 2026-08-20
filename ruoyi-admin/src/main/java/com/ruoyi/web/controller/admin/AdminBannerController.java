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
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.Banner;
import com.ruoyi.system.domain.dto.BannerDTO;
import com.ruoyi.system.service.IBannerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 后台-轮播图管理 控制器
 *
 * @author ruoyi
 */
@Api(tags = "后台-轮播图管理")
@RestController
@RequestMapping("/api/v1/admin/banner")
@PreAuthorize("@ss.hasPermi('banner:list')")
public class AdminBannerController extends BaseController
{
    @Resource
    private IBannerService bannerService;

    /**
     * 分页查询轮播图列表
     *
     * @param dto 查询参数（title-标题，status-状态，pageNum-页码，pageSize-每页条数）
     * @return 轮播图分页列表
     */
    @ApiOperation("分页查询轮播图列表")
    @Log(title = "轮播图管理", businessType = BusinessType.OTHER)
    @GetMapping("/list")
    public TableDataInfo getBannerList(BannerDTO dto)
    {
        startPage();
        List<Banner> list = bannerService.getBannerList(dto);
        return getDataTable(list);
    }

    /**
     * 获取轮播图详情
     *
     * @param id 轮播图ID
     * @return 轮播图信息
     */
    @ApiOperation("获取轮播图详情")
    @Log(title = "轮播图管理", businessType = BusinessType.OTHER)
    @GetMapping("/{id}")
    public AjaxResult getBannerDetail(
            @ApiParam(name = "id", value = "轮播图ID", required = true)
            @PathVariable Long id)
    {
        return AjaxResult.success(bannerService.getBannerById(id));
    }

    /**
     * 新增轮播图
     *
     * @param dto 轮播图信息
     * @return 操作结果
     */
    @ApiOperation("新增轮播图")
    @Log(title = "轮播图管理", businessType = BusinessType.INSERT)
    @PostMapping("/")
    public AjaxResult addBanner(@Validated @RequestBody BannerDTO dto)
    {
        bannerService.addBanner(dto);
        return AjaxResult.success("新增成功");
    }

    /**
     * 编辑轮播图
     *
     * @param id  轮播图ID
     * @param dto 轮播图信息
     * @return 操作结果
     */
    @ApiOperation("编辑轮播图")
    @Log(title = "轮播图管理", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}")
    public AjaxResult updateBanner(
            @ApiParam(name = "id", value = "轮播图ID", required = true)
            @PathVariable Long id,
            @Validated @RequestBody BannerDTO dto)
    {
        dto.setId(id);
        bannerService.updateBanner(dto);
        return AjaxResult.success("编辑成功");
    }

    /**
     * 删除轮播图（逻辑删除）
     *
     * @param id 轮播图ID
     * @return 操作结果
     */
    @ApiOperation("删除轮播图")
    @Log(title = "轮播图管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult deleteBanner(
            @ApiParam(name = "id", value = "轮播图ID", required = true)
            @PathVariable Long id)
    {
        bannerService.deleteBanner(id);
        return AjaxResult.success("删除成功");
    }
}
