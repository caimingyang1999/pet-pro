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
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
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
     * 动态列表（支持分页、关键词搜索，无需登录也可查看）
     *
     * @param pageNum  当前页码（默认1）
     * @param pageSize 每页条数（默认10）
     * @param keyword  搜索关键词
     * @return 动态分页列表
     */
    @ApiOperation("动态列表")
    @GetMapping("/list")
    public TableDataInfo list(
            @ApiParam(name = "pageNum", value = "当前页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam(name = "pageSize", value = "每页条数") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam(name = "keyword", value = "搜索关键词") @RequestParam(required = false) String keyword)
    {
        startPage();
        PostQueryDTO dto = new PostQueryDTO();
        dto.setPageNum(pageNum);
        dto.setPageSize(pageSize);
        dto.setKeyword(keyword);
        List<PetPost> list = postService.getPostList(dto);
        return getDataTable(list);
    }

    /**
     * 我的动态（需要登录）
     *
     * @return 当前用户发布的动态列表
     */
    @ApiOperation("我的动态")
    @PreAuthorize("@ss.isAuthenticated()")
    @GetMapping("/my")
    public TableDataInfo my()
    {
        startPage();
        PostQueryDTO dto = new PostQueryDTO();
        dto.setUserId(getUserId());
        List<PetPost> list = postService.getPostList(dto);
        return getDataTable(list);
    }

    /**
     * 动态详情
     *
     * @param postId 动态ID
     * @return 动态详情
     */
    @ApiOperation("动态详情")
    @GetMapping("/{postId}")
    public AjaxResult getInfo(
            @ApiParam(name = "postId", value = "动态ID", required = true)
            @PathVariable Long postId)
    {
        return AjaxResult.success(postService.getPostDetail(postId));
    }

    /**
     * 发布动态
     *
     * @param post 动态信息（content-内容，images-图片JSON，petId-关联宠物）
     * @return 操作结果
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
     * 删除动态（校验是否为发布者）
     *
     * @param postId 动态ID
     * @return 操作结果
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
     *
     * @param postId 动态ID
     * @return 操作结果（true-已点赞 false-已取消）
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
     * 评论列表（树形结构）
     *
     * @param postId   动态ID
     * @param pageNum  当前页码
     * @param pageSize 每页条数
     * @return 评论分页列表
     */
    @ApiOperation("评论列表")
    @GetMapping("/{postId}/comments")
    public TableDataInfo comments(
            @ApiParam(name = "postId", value = "动态ID", required = true)
            @PathVariable Long postId,
            @ApiParam(name = "pageNum", value = "当前页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam(name = "pageSize", value = "每页条数") @RequestParam(defaultValue = "10") Integer pageSize)
    {
        startPage();
        List<PostComment> list = postService.getCommentList(postId, pageNum, pageSize);
        return getDataTable(list);
    }

    /**
     * 发表评论
     *
     * @param postId  动态ID
     * @param comment 评论信息（content-内容，parentId-父评论ID）
     * @return 操作结果
     */
    @ApiOperation("发表评论")
    @PreAuthorize("@ss.isAuthenticated()")
    @PostMapping("/{postId}/comments")
    public AjaxResult addComment(
            @ApiParam(name = "postId", value = "动态ID", required = true)
            @PathVariable Long postId,
            @RequestBody PostComment comment)
    {
        return toAjax(postService.addComment(postId, getUserId(), comment.getParentId(), comment.getContent()));
    }
}
