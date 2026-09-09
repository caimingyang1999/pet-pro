package com.ruoyi.system.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.system.domain.PetPost;
import com.ruoyi.system.domain.PostComment;
import com.ruoyi.system.domain.PostLike;
import com.ruoyi.system.domain.dto.PostQueryDTO;
import com.ruoyi.system.mapper.PetPostMapper;
import com.ruoyi.system.mapper.PostCommentMapper;
import com.ruoyi.system.mapper.PostLikeMapper;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.system.service.IPostService;
import com.ruoyi.system.service.IUserFollowService;
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

    @Resource
    private IUserFollowService userFollowService;

    @Resource
    private SysUserMapper sysUserMapper;

    /**
     * 分页查询动态列表（支持 tab 切换/关注过滤/点赞状态回显）
     */
    @Override
    public List<PetPost> getPostList(PostQueryDTO dto)
    {
        // 关注 tab 需先获取当前用户关注的人
        if ("follow".equals(dto.getTab()) && dto.getCurrentUserId() != null)
        {
            List<Long> followeeIds = userFollowService.getFolloweeIds(dto.getCurrentUserId());
            dto.setFolloweeIds(followeeIds);
        }

        PageHelper.startPage(dto.getPageNum(), dto.getPageSize());
        return baseMapper.selectPostList(dto);
    }

    /**
     * 获取动态详情
     */
    @Override
    public PetPost getPostDetail(Long postId, Long currentUserId)
    {
        PetPost post = baseMapper.selectPostById(postId, currentUserId);
        if (post == null)
        {
            throw new ServiceException("动态不存在");
        }
        return post;
    }

    /**
     * 管理端-动态列表
     */
    @Override
    public List<PetPost> getPostListAdmin(String status, Date beginTime, Date endTime)
    {
        return baseMapper.selectPostListAdmin(status, beginTime, endTime);
    }

    /**
     * 发布动态（待审核）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addPost(PetPost post)
    {
        post.setStatus("0");
        baseMapper.insert(post);
        return true;
    }

    /**
     * 审核通过动态
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
        LambdaUpdateWrapper<PetPost> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PetPost::getId, postId).set(PetPost::getStatus, "1");
        baseMapper.update(null, updateWrapper);
        // 审核通过后发放积分（+10分）
        userPointsService.addPoints(post.getUserId(), 10, "post", post.getId());
        return true;
    }

    /**
     * 删除动态
     */
    @Override
    public boolean deletePost(Long postId)
    {
        PetPost post = baseMapper.selectById(postId);
        if (post == null)
        {
            throw new ServiceException("动态不存在");
        }
        return baseMapper.deleteById(postId) > 0;
    }

    /**
     * 点赞/取消点赞
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean likePost(Long postId, Long userId)
    {
        LambdaQueryWrapper<PostLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PostLike::getPostId, postId)
                    .eq(PostLike::getUserId, userId);
        PostLike existLike = postLikeMapper.selectOne(queryWrapper);

        if (existLike != null)
        {
            postLikeMapper.deleteById(existLike.getId());
            LambdaUpdateWrapper<PetPost> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(PetPost::getId, postId).setSql("like_count = like_count - 1");
            baseMapper.update(null, updateWrapper);
            return false;
        }
        else
        {
            PostLike postLike = new PostLike();
            postLike.setPostId(postId);
            postLike.setUserId(userId);
            postLikeMapper.insert(postLike);
            LambdaUpdateWrapper<PetPost> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(PetPost::getId, postId).setSql("like_count = like_count + 1");
            baseMapper.update(null, updateWrapper);
            return true;
        }
    }

    /**
     * 评论列表（树形）
     */
    @Override
    public List<PostComment> getCommentList(Long postId, Integer pageNum, Integer pageSize)
    {
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

        List<Long> rootIds = rootComments.stream().map(PostComment::getId).collect(Collectors.toList());
        LambdaQueryWrapper<PostComment> childWrapper = new LambdaQueryWrapper<>();
        childWrapper.eq(PostComment::getPostId, postId)
                    .in(PostComment::getParentId, rootIds)
                    .orderByDesc(PostComment::getCreateTime);
        List<PostComment> allChildren = postCommentMapper.selectList(childWrapper);

        Map<Long, List<PostComment>> childMap = allChildren.stream()
                .collect(Collectors.groupingBy(PostComment::getParentId));

        for (PostComment root : rootComments)
        {
            root.setChildren(childMap.getOrDefault(root.getId(), new ArrayList<>()));
        }
        return rootComments;
    }

    /**
     * 发表评论
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

    /**
     * 我的点赞列表
     */
    @Override
    public List<PetPost> getPostListByLike(Long userId, String keyword)
    {
        return baseMapper.selectPostListByLike(userId, keyword);
    }

    /**
     * 搜索用户（模糊匹配昵称）
     */
    @Override
    public List<Object> searchUsers(String keyword, Integer limit)
    {
        List<SysUser> users = sysUserMapper.selectUsersByNickNameLike(keyword, limit != null ? limit : 20);

        List<Object> result = new ArrayList<>();
        for (SysUser u : users)
        {
            Map<String, Object> item = new HashMap<>();
            item.put("userId", u.getUserId());
            item.put("nickName", u.getNickName());
            item.put("avatar", u.getAvatar());
            result.add(item);
        }
        return result;
    }
}
