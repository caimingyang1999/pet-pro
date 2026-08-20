package com.ruoyi.web.controller.admin;

import java.util.List;
import javax.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.PetInfo;
import com.ruoyi.system.service.IPetInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 后台-宠物管理 控制器
 *
 * @author ruoyi
 */
@Api(tags = "后台-宠物管理")
@RestController
@RequestMapping("/api/v1/admin/pets")
@PreAuthorize("@ss.hasPermi('admin')")
public class AdminPetController extends BaseController
{
    @Resource
    private IPetInfoService petInfoService;

    /**
     * 宠物列表
     *
     * @param name   宠物名称（模糊查询）
     * @param userId 所属用户ID
     * @return 宠物分页列表
     */
    @ApiOperation("宠物列表")
    @Log(title = "宠物管理", businessType = BusinessType.OTHER)
    @GetMapping
    public TableDataInfo list(
            @ApiParam(name = "name", value = "宠物名称") @RequestParam(required = false) String name,
            @ApiParam(name = "userId", value = "所属用户ID") @RequestParam(required = false) Long userId)
    {
        startPage();
        LambdaQueryWrapper<PetInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotEmpty(name), PetInfo::getName, name)
               .eq(userId != null, PetInfo::getUserId, userId)
               .orderByDesc(PetInfo::getCreateTime);
        List<PetInfo> list = petInfoService.list(wrapper);
        return getDataTable(list);
    }

    /**
     * 宠物详情
     *
     * @param petId 宠物ID
     * @return 宠物详情（含疫苗记录）
     */
    @ApiOperation("宠物详情")
    @Log(title = "宠物管理", businessType = BusinessType.OTHER)
    @GetMapping("/{petId}")
    public AjaxResult getInfo(
            @ApiParam(name = "petId", value = "宠物ID", required = true)
            @PathVariable Long petId)
    {
        return AjaxResult.success(petInfoService.getPetDetail(petId));
    }

    /**
     * 删除宠物（违规处理）
     *
     * @param petId 宠物ID
     * @return 操作结果
     */
    @ApiOperation("删除宠物")
    @Log(title = "宠物管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{petId}")
    public AjaxResult remove(
            @ApiParam(name = "petId", value = "宠物ID", required = true)
            @PathVariable Long petId)
    {
        return toAjax(petInfoService.deletePet(petId));
    }
}
