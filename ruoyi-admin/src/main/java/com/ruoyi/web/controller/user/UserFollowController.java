package com.ruoyi.web.controller.user;

import java.util.List;
import javax.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.UserFollow;
import com.ruoyi.system.service.IUserFollowService;
import com.github.pagehelper.PageHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 用户关注 控制器
 *
 * @author ruoyi
 */
@Api(tags = "用户关注管理")
@RestController
@RequestMapping("/api/v1/follows")
public class UserFollowController extends BaseController
{
    @Resource
    private IUserFollowService userFollowService;

    /**
     * 关注 / 取关（切换）
     *
     * @param followeeId 被关注者用户ID
     * @return { followed: true/false }
     */
    @ApiOperation("关注/取关")
    @PreAuthorize("@ss.isAuthenticated()")
    @PostMapping("/{followeeId}")
    public AjaxResult toggle(
            @ApiParam(name = "followeeId", value = "被关注者用户ID", required = true)
            @PathVariable Long followeeId)
    {
        if (getUserId().equals(followeeId))
        {
            throw new ServiceException("不能关注自己");
        }
        boolean followed = userFollowService.toggleFollow(getUserId(), followeeId);
        AjaxResult ajax = AjaxResult.success();
        ajax.put("followed", followed);
        return ajax;
    }

    /**
     * 判断是否已关注某用户
     *
     * @param followeeId 被关注者用户ID
     * @return { followed: true/false }
     */
    @ApiOperation("查询是否已关注")
    @PreAuthorize("@ss.isAuthenticated()")
    @GetMapping("/check/{followeeId}")
    public AjaxResult checkFollowed(
            @ApiParam(name = "followeeId", value = "被关注者用户ID", required = true)
            @PathVariable Long followeeId)
    {
        AjaxResult ajax = AjaxResult.success();
        ajax.put("followed", userFollowService.isFollowed(getUserId(), followeeId));
        return ajax;
    }

    /**
     * 查询某用户的关注列表
     *
     * @param userId 用户ID（不传则查当前用户）
     * @return 关注列表
     */
    @ApiOperation("关注列表")
    @GetMapping
    public TableDataInfo followList(
            @ApiParam(name = "userId", value = "用户ID（不传则查当前用户）")
            @RequestParam(required = false) Long userId,
            @ApiParam(name = "pageNum", value = "当前页码")
            @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam(name = "pageSize", value = "每页条数")
            @RequestParam(defaultValue = "20") Integer pageSize)
    {
        PageHelper.startPage(pageNum, pageSize);
        Long targetUserId = userId != null ? userId : getUserId();
        List<UserFollow> list = userFollowService.getFollowList(targetUserId);
        return getDataTable(list);
    }

    /**
     * 查询某用户的粉丝列表
     *
     * @param userId 用户ID（不传则查当前用户）
     * @return 粉丝列表
     */
    @ApiOperation("粉丝列表")
    @GetMapping("/followers")
    public TableDataInfo followerList(
            @ApiParam(name = "userId", value = "用户ID（不传则查当前用户）")
            @RequestParam(required = false) Long userId,
            @ApiParam(name = "pageNum", value = "当前页码")
            @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam(name = "pageSize", value = "每页条数")
            @RequestParam(defaultValue = "20") Integer pageSize)
    {
        PageHelper.startPage(pageNum, pageSize);
        Long targetUserId = userId != null ? userId : getUserId();
        List<UserFollow> list = userFollowService.getFollowerList(targetUserId);
        return getDataTable(list);
    }

    /**
     * 查询某用户的关注数和粉丝数
     *
     * @param userId 用户ID（不传则查当前用户）
     * @return { followCount, followerCount }
     */
    @ApiOperation("统计关注数/粉丝数")
    @GetMapping("/count")
    public AjaxResult followCount(
            @ApiParam(name = "userId", value = "用户ID（不传则查当前用户）")
            @RequestParam(required = false) Long userId)
    {
        Long targetUserId = userId != null ? userId : getUserId();
        AjaxResult ajax = AjaxResult.success();
        ajax.put("followCount", userFollowService.countFollow(targetUserId));
        ajax.put("followerCount", userFollowService.countFollower(targetUserId));
        return ajax;
    }
}
