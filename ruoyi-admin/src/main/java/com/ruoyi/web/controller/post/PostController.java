package com.ruoyi.web.controller.post;

import java.util.List;
import javax.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.github.pagehelper.PageHelper;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.PetPost;
import com.ruoyi.system.domain.PostComment;
import com.ruoyi.system.domain.dto.PostQueryDTO;
import com.ruoyi.system.service.IPostService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 动态 控制器
 *
 * @author ruoyi
 */
@Api(tags = "动态管理")
@RestController
@RequestMapping("/api/v1/posts")
public class PostController extends BaseController
{
    @Resource
    private IPostService postService;

    /**
     * 动态列表（支持 tab 切换：recommend-推荐 follow-关注 latest-最新；支持关键词搜索）
     *
     * @param pageNum  当前页码
     * @param pageSize 每页条数
     * @param keyword  搜索关键词
     * @param tab      列表 Tab
     * @param userId   指定用户动态（可选）
     */
    @ApiOperation("动态列表")
    @GetMapping("/list")
    public TableDataInfo list(
            @ApiParam(name = "pageNum", value = "当前页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam(name = "pageSize", value = "每页条数") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam(name = "keyword", value = "搜索关键词") @RequestParam(required = false) String keyword,
            @ApiParam(name = "tab", value = "列表Tab：recommend/follow/latest") @RequestParam(required = false) String tab,
            @ApiParam(name = "userId", value = "指定用户动态ID") @RequestParam(required = false) Long userId)
    {
        PostQueryDTO dto = new PostQueryDTO();
        dto.setPageNum(pageNum);
        dto.setPageSize(pageSize);
        dto.setKeyword(keyword);
        dto.setTab(tab);
        dto.setUserId(userId);
        // 关注 tab 需要 currentUserId；推荐/最新也用来回显点赞状态
        if ("follow".equals(tab))
        {
            if (SecurityUtils.getUserId() == null)
            {
                throw new ServiceException("登录后可查看关注动态");
            }
            dto.setCurrentUserId(getUserId());
        }
        else if (SecurityUtils.getUserId() != null)
        {
            dto.setCurrentUserId(getUserId());
        }
        List<PetPost> list = postService.getPostList(dto);
        return getDataTable(list);
    }

    /**
     * 我的动态（需登录）
     */
    @ApiOperation("我的动态")
    @PreAuthorize("@ss.isAuthenticated()")
    @GetMapping("/my")
    public TableDataInfo my()
    {
        startPage();
        PostQueryDTO dto = new PostQueryDTO();
        dto.setUserId(getUserId());
        dto.setCurrentUserId(getUserId());
        List<PetPost> list = postService.getPostList(dto);
        return getDataTable(list);
    }

    /**
     * 我的点赞列表
     */
    @ApiOperation("我的点赞列表")
    @PreAuthorize("@ss.isAuthenticated()")
    @GetMapping("/my-likes")
    public TableDataInfo myLikes(
            @ApiParam(name = "pageNum", value = "当前页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam(name = "pageSize", value = "每页条数") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam(name = "keyword", value = "搜索关键词") @RequestParam(required = false) String keyword)
    {
        PageHelper.startPage(pageNum, pageSize);
        List<PetPost> list = postService.getPostListByLike(getUserId(), keyword);
        return getDataTable(list);
    }

    /**
     * 动态详情（含点赞/关注状态回显）
     */
    @ApiOperation("动态详情")
    @GetMapping("/{postId}")
    public AjaxResult getInfo(
            @ApiParam(name = "postId", value = "动态ID", required = true)
            @PathVariable Long postId)
    {
        Long currentUserId = SecurityUtils.getUserId() != null ? getUserId() : null;
        return AjaxResult.success(postService.getPostDetail(postId, currentUserId));
    }

    /**
     * 发布动态（支持图片 + 视频）
     */
    @ApiOperation("发布动态")
    @PreAuthorize("@ss.isAuthenticated()")
    @PostMapping
    public AjaxResult add(@RequestBody PetPost post)
    {
        post.setUserId(getUserId());
        return toAjax(postService.addPost(post));
    }

    /**
     * 删除动态
     */
    @ApiOperation("删除动态")
    @PreAuthorize("@ss.isAuthenticated()")
    @DeleteMapping("/{postId}")
    public AjaxResult remove(
            @ApiParam(name = "postId", value = "动态ID", required = true)
            @PathVariable Long postId)
    {
        PetPost post = postService.getById(postId);
        if (post == null || !post.getUserId().equals(getUserId()))
        {
            throw new ServiceException("无权删除他人动态");
        }
        return toAjax(postService.deletePost(postId));
    }

    /**
     * 点赞/取消点赞
     */
    @ApiOperation("点赞/取消点赞")
    @PreAuthorize("@ss.isAuthenticated()")
    @PostMapping("/{postId}/like")
    public AjaxResult like(
            @ApiParam(name = "postId", value = "动态ID", required = true)
            @PathVariable Long postId)
    {
        boolean liked = postService.likePost(postId, getUserId());
        AjaxResult ajax = AjaxResult.success();
        ajax.put("liked", liked);
        return ajax;
    }

    /**
     * 评论列表
     */
    @ApiOperation("评论列表")
    @GetMapping("/{postId}/comments")
    public TableDataInfo comments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize)
    {
        startPage();
        List<PostComment> list = postService.getCommentList(postId, pageNum, pageSize);
        return getDataTable(list);
    }

    /**
     * 发表评论
     */
    @ApiOperation("发表评论")
    @PreAuthorize("@ss.isAuthenticated()")
    @PostMapping("/{postId}/comments")
    public AjaxResult addComment(@PathVariable Long postId, @RequestBody PostComment comment)
    {
        return toAjax(postService.addComment(postId, getUserId(), comment.getParentId(), comment.getContent()));
    }

    /**
     * 搜索用户（模糊匹配昵称）
     */
    @ApiOperation("搜索用户")
    @GetMapping("/search/users")
    public AjaxResult searchUsers(
            @ApiParam(name = "keyword", value = "搜索关键词") @RequestParam String keyword,
            @ApiParam(name = "limit", value = "返回条数") @RequestParam(defaultValue = "10") Integer limit)
    {
        return AjaxResult.success(postService.searchUsers(keyword, limit));
    }
}
