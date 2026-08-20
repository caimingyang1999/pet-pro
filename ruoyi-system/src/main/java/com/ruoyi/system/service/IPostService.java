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
     * 分页查询动态列表（含点赞状态、是否已点赞）
     *
     * @param dto 查询参数
     * @return 动态集合
     */
    List<PetPost> getPostList(PostQueryDTO dto);

    /**
     * 获取动态详情
     *
     * @param postId 动态ID
     * @return 动态信息
     */
    PetPost getPostDetail(Long postId);

    /**
     * 管理端-分页查询动态列表（关联用户和宠物信息，支持状态、时间范围筛选）
     *
     * @param status    审核状态
     * @param beginTime 起始时间
     * @param endTime   结束时间
     * @return 动态集合
     */
    List<PetPost> getPostListAdmin(String status, Date beginTime, Date endTime);

    /**
     * 发布动态（增加积分）
     *
     * @param post 动态信息
     * @return 结果
     */
    boolean addPost(PetPost post);

    /**
     * 删除动态（校验是否为发布者）
     *
     * @param postId 动态ID
     * @return 结果
     */
    boolean deletePost(Long postId);

    /**
     * 点赞/取消点赞（更新点赞数）
     *
     * @param postId 动态ID
     * @param userId 用户ID
     * @return true-点赞 false-取消点赞
     */
    boolean likePost(Long postId, Long userId);

    /**
     * 评论列表（树形结构）
     *
     * @param postId    动态ID
     * @param pageNum   当前页码
     * @param pageSize  每页条数
     * @return 评论集合
     */
    List<PostComment> getCommentList(Long postId, Integer pageNum, Integer pageSize);

    /**
     * 发表评论
     *
     * @param postId   动态ID
     * @param userId   用户ID
     * @param parentId 父评论ID（0-一级评论）
     * @param content  评论内容
     * @return 结果
     */
    boolean addComment(Long postId, Long userId, Long parentId, String content);
}
