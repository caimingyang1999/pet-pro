package com.ruoyi.web.controller.pet;

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
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.PetInfo;
import com.ruoyi.system.service.IPetInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 宠物信息 控制器
 *
 * @author ruoyi
 */
@Api(tags = "宠物管理")
@RestController
@RequestMapping("/api/v1/pets")
public class PetController extends BaseController
{
    @Resource
    private IPetInfoService petInfoService;

    /**
     * 获取当前用户宠物列表
     *
     * @return 宠物列表（含疫苗记录）
     */
    @ApiOperation("获取当前用户宠物列表")
    @PreAuthorize("@ss.isAuthenticated()")
    @GetMapping("/list")
    public AjaxResult list()
    {
        Long userId = getUserId();
        List<PetInfo> list = petInfoService.getPetList(userId);
        return AjaxResult.success(list);
    }

    /**
     * 获取宠物详情
     *
     * @param petId 宠物ID
     * @return 宠物详情（含疫苗记录）
     */
    @ApiOperation("获取宠物详情")
    @GetMapping("/{petId}")
    public AjaxResult getInfo(
            @ApiParam(name = "petId", value = "宠物ID", required = true)
            @PathVariable Long petId)
    {
        return AjaxResult.success(petInfoService.getPetDetail(petId));
    }

    /**
     * 添加宠物
     *
     * @param petInfo 宠物信息（含疫苗记录）
     * @return 操作结果
     */
    @ApiOperation("添加宠物")
    @PreAuthorize("@ss.isAuthenticated()")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody PetInfo petInfo)
    {
        petInfo.setUserId(getUserId());
        return toAjax(petInfoService.addPet(petInfo));
    }

    /**
     * 更新宠物（校验是否为宠物主人）
     *
     * @param petId   宠物ID
     * @param petInfo 宠物信息
     * @return 操作结果
     */
    @ApiOperation("更新宠物")
    @PreAuthorize("@ss.isAuthenticated()")
    @PutMapping("/{petId}")
    public AjaxResult edit(
            @ApiParam(name = "petId", value = "宠物ID", required = true)
            @PathVariable Long petId,
            @Validated @RequestBody PetInfo petInfo)
    {
        // 校验是否为宠物主人
        PetInfo existPet = petInfoService.getById(petId);
        if (existPet == null || !existPet.getUserId().equals(getUserId()))
        {
            throw new ServiceException("无权操作他人宠物");
        }
        petInfo.setId(petId);
        petInfo.setUserId(getUserId());
        return toAjax(petInfoService.updatePet(petInfo));
    }

    /**
     * 删除宠物
     *
     * @param petId 宠物ID
     * @return 操作结果
     */
    @ApiOperation("删除宠物")
    @PreAuthorize("@ss.isAuthenticated()")
    @DeleteMapping("/{petId}")
    public AjaxResult remove(
            @ApiParam(name = "petId", value = "宠物ID", required = true)
            @PathVariable Long petId)
    {
        // 校验是否为宠物主人
        PetInfo existPet = petInfoService.getById(petId);
        if (existPet == null || !existPet.getUserId().equals(getUserId()))
        {
            throw new ServiceException("无权删除他人宠物");
        }
        return toAjax(petInfoService.deletePet(petId));
    }
}
