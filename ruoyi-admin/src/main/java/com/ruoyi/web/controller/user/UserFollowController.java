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
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
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
     * 安全获取当前登录用户ID：未登录（游客）时返回 null，不抛异常。
     *
     * 关注/粉丝列表与统计接口已开放匿名访问，
     * 匿名上下文下 SecurityUtils.getUserId() 会抛 ServiceException，此处统一兜底。
     */
    private Long currentUserIdOrNull()
    {
        try
        {
            return SecurityUtils.getUserId();
        }
        catch (Exception e)
        {
            return null;
        }
    }

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
     * 允许匿名访问：游客未登录时直接返回 followed=false（前端用于回显关注按钮状态）。
     *
     * @param followeeId 被关注者用户ID
     * @return { followed: true/false }
     */
    @ApiOperation("查询是否已关注")
    @Anonymous
    @GetMapping("/check/{followeeId}")
    public AjaxResult checkFollowed(
            @ApiParam(name = "followeeId", value = "被关注者用户ID", required = true)
            @PathVariable Long followeeId)
    {
        Long currentUserId = currentUserIdOrNull();
        AjaxResult ajax = AjaxResult.success();
        // 游客（未登录）不存在关注关系，返回 false 而非 401，避免打断浏览体验
        ajax.put("followed", currentUserId != null && userFollowService.isFollowed(currentUserId, followeeId));
        return ajax;
    }

    /**
     * 查询某用户的关注列表
     *
     * 允许匿名访问：游客必须显式传入 userId。
     *
     * @param userId 用户ID（不传则查当前用户）
     * @return 关注列表
     */
    @ApiOperation("关注列表")
    @Anonymous
    @GetMapping
    public TableDataInfo followList(
            @ApiParam(name = "userId", value = "用户ID（不传则查当前用户）")
            @RequestParam(required = false) Long userId,
            @ApiParam(name = "pageNum", value = "当前页码")
            @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam(name = "pageSize", value = "每页条数")
            @RequestParam(defaultValue = "20") Integer pageSize)
    {
        Long targetUserId = userId != null ? userId : currentUserIdOrNull();
        if (targetUserId == null)
        {
            throw new ServiceException("请先登录或指定用户ID");
        }
        PageHelper.startPage(pageNum, pageSize);
        List<UserFollow> list = userFollowService.getFollowList(targetUserId);
        return getDataTable(list);
    }

    /**
     * 查询某用户的粉丝列表
     *
     * 允许匿名访问：游客必须显式传入 userId。
     *
     * @param userId 用户ID（不传则查当前用户）
     * @return 粉丝列表
     */
    @ApiOperation("粉丝列表")
    @Anonymous
    @GetMapping("/followers")
    public TableDataInfo followerList(
            @ApiParam(name = "userId", value = "用户ID（不传则查当前用户）")
            @RequestParam(required = false) Long userId,
            @ApiParam(name = "pageNum", value = "当前页码")
            @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam(name = "pageSize", value = "每页条数")
            @RequestParam(defaultValue = "20") Integer pageSize)
    {
        Long targetUserId = userId != null ? userId : currentUserIdOrNull();
        if (targetUserId == null)
        {
            throw new ServiceException("请先登录或指定用户ID");
        }
        PageHelper.startPage(pageNum, pageSize);
        List<UserFollow> list = userFollowService.getFollowerList(targetUserId);
        return getDataTable(list);
    }

    /**
     * 查询某用户的关注数和粉丝数
     *
     * 允许匿名访问：游客必须显式传入 userId。
     *
     * @param userId 用户ID（不传则查当前用户）
     * @return { followCount, followerCount }
     */
    @ApiOperation("统计关注数/粉丝数")
    @Anonymous
    @GetMapping("/count")
    public AjaxResult followCount(
            @ApiParam(name = "userId", value = "用户ID（不传则查当前用户）")
            @RequestParam(required = false) Long userId)
    {
        Long targetUserId = userId != null ? userId : currentUserIdOrNull();
        if (targetUserId == null)
        {
            throw new ServiceException("请先登录或指定用户ID");
        }
        AjaxResult ajax = AjaxResult.success();
        ajax.put("followCount", userFollowService.countFollow(targetUserId));
        ajax.put("followerCount", userFollowService.countFollower(targetUserId));
        return ajax;
    }
}
