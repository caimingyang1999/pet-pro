package com.ruoyi.system.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.PetPost;
import com.ruoyi.system.domain.PostComment;
import com.ruoyi.system.domain.PostLike;
import com.ruoyi.system.domain.dto.PostQueryDTO;
import com.ruoyi.system.mapper.PetPostMapper;
import com.ruoyi.system.mapper.PostCommentMapper;
import com.ruoyi.system.mapper.PostLikeMapper;
import com.ruoyi.system.service.IPostService;
import com.ruoyi.system.service.IUserPointsService;

/**
 * 动态 服务层实现
 *
 * @author ruoyi
 */
@Service
public class PostServiceImpl extends ServiceImpl<PetPostMapper, PetPost> implements IPostService
{
    @Resource
    private PostLikeMapper postLikeMapper;

    @Resource
    private PostCommentMapper postCommentMapper;

    @Resource
    private IUserPointsService userPointsService;

    /**
     * 分页查询动态列表（含点赞状态、是否已点赞）
     *
     * @param dto 查询参数
     * @return 动态集合
     */
    @Override
    public List<PetPost> getPostList(PostQueryDTO dto)
    {
        PageHelper.startPage(dto.getPageNum(), dto.getPageSize());
        return baseMapper.selectPostList(dto.getKeyword(), dto.getUserId());
    }

    /**
     * 获取动态详情（关联用户和宠物信息）
     *
     * @param postId 动态ID
     * @return 动态信息
     */
    @Override
    public PetPost getPostDetail(Long postId)
    {
        PetPost post = baseMapper.selectPostById(postId);
        if (post == null)
        {
            throw new ServiceException("动态不存在");
        }
        return post;
    }

    /**
     * 管理端-分页查询动态列表（关联用户和宠物信息，支持状态、时间范围筛选）
     *
     * @param status    审核状态
     * @param beginTime 起始时间
     * @param endTime   结束时间
     * @return 动态集合
     */
    @Override
    public List<PetPost> getPostListAdmin(String status, Date beginTime, Date endTime)
    {
        return baseMapper.selectPostListAdmin(status, beginTime, endTime);
    }

    /**
     * 发布动态（待审核，暂不发放积分，审核通过后发放）
     *
     * @param post 动态信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addPost(PetPost post)
    {
        // 待审核状态
        post.setStatus("0");
        baseMapper.insert(post);
        return true;
    }

    /**
     * 审核通过动态（发放积分，需防重复发放）
     *
     * @param postId 动态ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approvePost(Long postId)
    {
        PetPost post = baseMapper.selectById(postId);
        if (post == null)
        {
            throw new ServiceException("动态不存在");
        }
        if (!"0".equals(post.getStatus()))
        {
            throw new ServiceException("该动态已审核，请勿重复操作");
        }
        // 更新状态为审核通过
        LambdaUpdateWrapper<PetPost> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PetPost::getId, postId).set(PetPost::getStatus, "1");
        baseMapper.update(null, updateWrapper);
        // 审核通过后发放积分（+10分）
        userPointsService.addPoints(post.getUserId(), 10, "post", post.getId());
        return true;
    }

    /**
     * 删除动态（校验是否为发布者）
     *
     * @param postId 动态ID
     * @return 结果
     */
    @Override
    public boolean deletePost(Long postId)
    {
        PetPost post = baseMapper.selectById(postId);
        if (post == null)
        {
            throw new ServiceException("动态不存在");
        }
        // 逻辑删除（MyBatis-Plus @TableLogic 自动处理）
        return baseMapper.deleteById(postId) > 0;
    }

    /**
     * 点赞/取消点赞（更新点赞数）
     *
     * @param postId 动态ID
     * @param userId 用户ID
     * @return true-点赞 false-取消点赞
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean likePost(Long postId, Long userId)
    {
        // 查询是否已点赞
        LambdaQueryWrapper<PostLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PostLike::getPostId, postId)
                    .eq(PostLike::getUserId, userId);
        PostLike existLike = postLikeMapper.selectOne(queryWrapper);

        if (existLike != null)
        {
            // 已点赞 -> 取消点赞
            postLikeMapper.deleteById(existLike.getId());
            // 点赞数 -1
            LambdaUpdateWrapper<PetPost> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(PetPost::getId, postId)
                         .setSql("like_count = like_count - 1");
            baseMapper.update(null, updateWrapper);
            return false;
        }
        else
        {
            // 未点赞 -> 点赞
            PostLike postLike = new PostLike();
            postLike.setPostId(postId);
            postLike.setUserId(userId);
            postLikeMapper.insert(postLike);
            // 点赞数 +1
            LambdaUpdateWrapper<PetPost> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(PetPost::getId, postId)
                         .setSql("like_count = like_count + 1");
            baseMapper.update(null, updateWrapper);
            return true;
        }
    }

    /**
     * 评论列表（树形结构）
     *
     * @param postId    动态ID
     * @param pageNum   当前页码
     * @param pageSize  每页条数
     * @return 评论集合
     */
    @Override
    public List<PostComment> getCommentList(Long postId, Integer pageNum, Integer pageSize)
    {
        // 分页查询一级评论
        PageHelper.startPage(pageNum, pageSize);
        LambdaQueryWrapper<PostComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostComment::getPostId, postId)
               .eq(PostComment::getParentId, 0L)
               .orderByDesc(PostComment::getCreateTime);
        List<PostComment> rootComments = postCommentMapper.selectList(wrapper);

        if (rootComments.isEmpty())
        {
            return rootComments;
        }

        // 查询所有子评论
        List<Long> rootIds = rootComments.stream().map(PostComment::getId).collect(Collectors.toList());
        LambdaQueryWrapper<PostComment> childWrapper = new LambdaQueryWrapper<>();
        childWrapper.eq(PostComment::getPostId, postId)
                    .in(PostComment::getParentId, rootIds)
                    .orderByDesc(PostComment::getCreateTime);
        List<PostComment> allChildren = postCommentMapper.selectList(childWrapper);

        // 按父ID分组
        Map<Long, List<PostComment>> childMap = allChildren.stream()
                .collect(Collectors.groupingBy(PostComment::getParentId));

        // 组装树形结构
        for (PostComment root : rootComments)
        {
            root.setChildren(childMap.getOrDefault(root.getId(), new ArrayList<>()));
        }
        return rootComments;
    }

    /**
     * 发表评论
     *
     * @param postId   动态ID
     * @param userId   用户ID
     * @param parentId 父评论ID（0-一级评论）
     * @param content  评论内容
     * @return 结果
     */
    @Override
    public boolean addComment(Long postId, Long userId, Long parentId, String content)
    {
        PostComment comment = new PostComment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setParentId(parentId == null ? 0L : parentId);
        comment.setContent(content);
        return postCommentMapper.insert(comment) > 0;
    }
}
