package com.ruoyi.web.controller.pet;

import javax.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
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
import com.ruoyi.system.domain.PetAlbum;
import com.ruoyi.system.domain.PetWeightLog;
import com.ruoyi.system.service.IPetHealthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 宠物健康记录（体重 / 成长相册） 控制器
 *
 * 全部接口均需登录，且只能操作自己宠物的数据；
 * 成长相册内容仅本人可见，不提供公开、评论、点赞、分享等任何互动能力。
 *
 * @author ruoyi
 */
@Api(tags = "宠物健康记录")
@RestController
@RequestMapping("/api/v1/pet")
public class PetHealthController extends BaseController
{
    @Resource
    private IPetHealthService petHealthService;

    /* ==================== 体重记录 ==================== */

    /**
     * 添加体重记录
     *
     * @param log 体重记录（petId、weight、recordDate、remark）
     */
    @ApiOperation("添加体重记录")
    @PreAuthorize("@ss.isAuthenticated()")
    @PostMapping("/weight")
    public AjaxResult addWeight(@RequestBody PetWeightLog log)
    {
        Long id = petHealthService.addWeightLog(getUserId(), log);
        AjaxResult ajax = AjaxResult.success("记录成功");
        ajax.put("id", id);
        return ajax;
    }

    /**
     * 获取某宠物的体重记录列表（按记录日期升序）
     *
     * @param petId 宠物ID
     */
    @ApiOperation("体重记录列表")
    @PreAuthorize("@ss.isAuthenticated()")
    @GetMapping("/{petId}/weight")
    public AjaxResult weightList(
            @ApiParam(name = "petId", value = "宠物ID", required = true)
            @PathVariable Long petId)
    {
        return AjaxResult.success(petHealthService.getWeightLogList(getUserId(), petId));
    }

    /**
     * 删除体重记录
     *
     * @param id 记录ID
     */
    @ApiOperation("删除体重记录")
    @PreAuthorize("@ss.isAuthenticated()")
    @DeleteMapping("/weight/{id}")
    public AjaxResult removeWeight(
            @ApiParam(name = "id", value = "记录ID", required = true)
            @PathVariable Long id)
    {
        return toAjax(petHealthService.deleteWeightLog(getUserId(), id));
    }

    /* ==================== 成长相册 ==================== */

    /**
     * 添加成长相册记录
     *
     * @param album 相册记录（petId、title、content、images、recordDate）
     */
    @ApiOperation("添加成长相册记录")
    @PreAuthorize("@ss.isAuthenticated()")
    @PostMapping("/album")
    public AjaxResult addAlbum(@RequestBody PetAlbum album)
    {
        Long id = petHealthService.addAlbum(getUserId(), album);
        AjaxResult ajax = AjaxResult.success("记录成功");
        ajax.put("id", id);
        return ajax;
    }

    /**
     * 获取某宠物的成长相册列表（按记录日期倒序）
     *
     * @param petId 宠物ID
     */
    @ApiOperation("成长相册列表")
    @PreAuthorize("@ss.isAuthenticated()")
    @GetMapping("/{petId}/album")
    public AjaxResult albumList(
            @ApiParam(name = "petId", value = "宠物ID", required = true)
            @PathVariable Long petId)
    {
        return AjaxResult.success(petHealthService.getAlbumList(getUserId(), petId));
    }

    /**
     * 获取成长相册记录详情
     *
     * @param id 记录ID
     */
    @ApiOperation("成长相册详情")
    @PreAuthorize("@ss.isAuthenticated()")
    @GetMapping("/album/{id}")
    public AjaxResult albumDetail(
            @ApiParam(name = "id", value = "记录ID", required = true)
            @PathVariable Long id)
    {
        return AjaxResult.success(petHealthService.getAlbumDetail(getUserId(), id));
    }

    /**
     * 更新成长相册记录
     *
     * @param id    记录ID
     * @param album 相册记录
     */
    @ApiOperation("更新成长相册记录")
    @PreAuthorize("@ss.isAuthenticated()")
    @PutMapping("/album/{id}")
    public AjaxResult editAlbum(
            @ApiParam(name = "id", value = "记录ID", required = true)
            @PathVariable Long id,
            @RequestBody PetAlbum album)
    {
        album.setId(id);
        return toAjax(petHealthService.updateAlbum(getUserId(), album));
    }

    /**
     * 删除成长相册记录
     *
     * @param id 记录ID
     */
    @ApiOperation("删除成长相册记录")
    @PreAuthorize("@ss.isAuthenticated()")
    @DeleteMapping("/album/{id}")
    public AjaxResult removeAlbum(
            @ApiParam(name = "id", value = "记录ID", required = true)
            @PathVariable Long id)
    {
        return toAjax(petHealthService.deleteAlbum(getUserId(), id));
    }
}
