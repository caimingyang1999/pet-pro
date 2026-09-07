package com.ruoyi.web.controller.admin;

import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
import com.ruoyi.system.domain.PetPost;
import com.ruoyi.system.service.IPostService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 后台-动态管理 控制器
 *
 * @author ruoyi
 */
@Api(tags = "后台-动态管理")
@RestController
@RequestMapping("/api/v1/admin/posts")
@PreAuthorize("@ss.hasPermi('admin')")
public class AdminPostController extends BaseController
{
    @Resource
    private IPostService postService;

    /**
     * 动态列表（支持按状态、用户、时间筛选）
     *
     * @param status    审核状态（0-待审核 1-通过 2-拒绝）
     * @param userId    发布用户ID
     * @param beginTime 起始时间
     * @param endTime   结束时间
     * @return 动态分页列表
     */
    @ApiOperation("动态列表")
    @Log(title = "动态管理", businessType = BusinessType.OTHER)
    @GetMapping
    public TableDataInfo list(
            @ApiParam(name = "status", value = "审核状态（0-待审核 1-通过 2-拒绝）") @RequestParam(required = false) String status,
            @ApiParam(name = "userId", value = "发布用户ID") @RequestParam(required = false) Long userId,
            @ApiParam(name = "beginTime", value = "起始时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date beginTime,
            @ApiParam(name = "endTime", value = "结束时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endTime)
    {
        startPage();
        List<PetPost> list = postService.getPostListAdmin(status, beginTime, endTime);
        return getDataTable(list);
    }

    /**
     * 动态详情
     *
     * @param postId 动态ID
     * @return 动态详情
     */
    @ApiOperation("动态详情")
    @Log(title = "动态管理", businessType = BusinessType.OTHER)
    @GetMapping("/{postId}")
    public AjaxResult getInfo(
            @ApiParam(name = "postId", value = "动态ID", required = true)
            @PathVariable Long postId)
    {
        return AjaxResult.success(postService.getPostDetail(postId));
    }

    /**
     * 审核动态（通过/拒绝）
     * 通过时发放积分，拒绝时不发放
     *
     * @param postId 动态ID
     * @param post   审核信息（status-1通过 2-拒绝）
     * @return 操作结果
     */
    @ApiOperation("审核动态")
    @Log(title = "动态管理", businessType = BusinessType.UPDATE)
    @PutMapping("/{postId}/audit")
    public AjaxResult audit(
            @ApiParam(name = "postId", value = "动态ID", required = true)
            @PathVariable Long postId,
            @RequestBody PetPost post)
    {
        if (!"1".equals(post.getStatus()) && !"2".equals(post.getStatus()))
        {
            return AjaxResult.error("审核状态非法，仅支持 1-通过 2-拒绝");
        }
        if ("1".equals(post.getStatus()))
        {
            // 审核通过：更新状态并发放积分
            return toAjax(postService.approvePost(postId));
        }
        else
        {
            // 审核拒绝：仅更新状态
            LambdaUpdateWrapper<PetPost> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(PetPost::getId, postId).set(PetPost::getStatus, post.getStatus());
            return toAjax(postService.update(wrapper));
        }
    }

    /**
     * 删除动态
     *
     * @param postId 动态ID
     * @return 操作结果
     */
    @ApiOperation("删除动态")
    @Log(title = "动态管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{postId}")
    public AjaxResult remove(
            @ApiParam(name = "postId", value = "动态ID", required = true)
            @PathVariable Long postId)
    {
        return toAjax(postService.removeById(postId));
    }
}
