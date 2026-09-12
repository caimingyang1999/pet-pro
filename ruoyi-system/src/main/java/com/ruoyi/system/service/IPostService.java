package com.ruoyi.system.service;

import java.util.Date;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.system.domain.PetPost;
import com.ruoyi.system.domain.PostComment;
import com.ruoyi.system.domain.dto.PostQueryDTO;

/**
 * 动态 服务层
 *
 * @author ruoyi
 */
public interface IPostService extends IService<PetPost>
{
    /**
     * 分页查询动态列表（支持 tab 切换/关注过滤/点赞状态回显）
     *
     * @param dto 查询参数
     * @return 动态集合
     */
    List<PetPost> getPostList(PostQueryDTO dto);

    /**
     * 获取动态详情
     *
     * @param postId        动态ID
     * @param currentUserId 当前登录用户ID（可为空，用于点赞/关注状态回显）
     * @return 动态信息
     */
    PetPost getPostDetail(Long postId, Long currentUserId);

    /**
     * 管理端-动态列表
     *
     * @param status    审核状态（0-待审核 1-通过 2-拒绝），可空
     * @param userId    发布用户ID，可空
     * @param beginTime 发布起始时间，可空
     * @param endTime   发布结束时间，可空
     * @return 动态集合
     */
    List<PetPost> getPostListAdmin(String status, Long userId, Date beginTime, Date endTime);

    /**
     * 发布动态
     */
    boolean addPost(PetPost post);

    /**
     * 审核通过动态
     */
    boolean approvePost(Long postId);

    /**
     * 删除动态
     */
    boolean deletePost(Long postId);

    /**
     * 点赞/取消点赞
     */
    boolean likePost(Long postId, Long userId);

    /**
     * 评论列表（树形）
     */
    List<PostComment> getCommentList(Long postId, Integer pageNum, Integer pageSize);

    /**
     * 发表评论
     */
    boolean addComment(Long postId, Long userId, Long parentId, String content);

    /**
     * 我的点赞列表（用户点赞过的动态）
     *
     * @param userId  用户ID
     * @param keyword 搜索关键词（可空）
     * @return 动态集合
     */
    List<PetPost> getPostListByLike(Long userId, String keyword);

    /**
     * 搜索用户（模糊匹配昵称）
     *
     * @param keyword 关键词
     * @param limit   返回条数
     * @return 用户信息列表
     */
    List<Object> searchUsers(String keyword, Integer limit);
}
