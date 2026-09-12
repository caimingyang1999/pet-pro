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
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
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
     * 返回值含所属用户昵称（userName）与疫苗记录数（vaccineCount），供后台表格展示。
     *
     * @param name        宠物名称（模糊查询）
     * @param userKeyword 所属用户（昵称/账号/手机号，模糊查询）
     * @param breed       品种（模糊查询）
     * @param petType     宠物类型（cat-猫 dog-狗 other-其他，筛选下拉）
     * @return 宠物分页列表
     */
    @ApiOperation("宠物列表")
    @Log(title = "宠物管理", businessType = BusinessType.OTHER)
    @GetMapping
    public TableDataInfo list(
            @ApiParam(name = "name", value = "宠物名称") @RequestParam(required = false) String name,
            @ApiParam(name = "userKeyword", value = "所属用户昵称/账号/手机号") @RequestParam(required = false) String userKeyword,
            @ApiParam(name = "breed", value = "品种") @RequestParam(required = false) String breed,
            @ApiParam(name = "petType", value = "宠物类型（cat/dog/other）") @RequestParam(required = false) String petType)
    {
        startPage();
        List<PetInfo> list = petInfoService.getAdminPetList(name, userKeyword, breed, petType);
        return getDataTable(list);
    }

    /**
     * 品种选项列表（去重，供筛选下拉使用）
     *
     * 宠物品种存于 pet_info.breed 自由文本，与商城分类无关，
     * 因此直接取库内已有品种，避免误用商城分类做筛选。
     */
    @ApiOperation("品种选项列表")
    @GetMapping("/breeds")
    public AjaxResult breeds()
    {
        return AjaxResult.success(petInfoService.getBreedOptions());
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
