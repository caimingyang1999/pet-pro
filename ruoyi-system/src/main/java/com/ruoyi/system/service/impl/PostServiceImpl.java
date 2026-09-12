package com.ruoyi.system.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    public List<PetPost> getPostListAdmin(String status, Long userId, Date beginTime, Date endTime)
    {
        return baseMapper.selectPostListAdmin(status, userId, beginTime, endTime);
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
     * 评论列表（一级评论分页，回复按所属一级评论聚合展示）
     *
     * 说明：回复支持“回复某条回复”，后端会把所有层级的回复向上归类到对应的一级评论，
     * 并同步返回评论人昵称/头像以及被回复者昵称 replyUserName。
     */
    @Override
    public List<PostComment> getCommentList(Long postId, Integer pageNum, Integer pageSize)
    {
        PageHelper.startPage(pageNum, pageSize);
        LambdaQueryWrapper<PostComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostComment::getPostId, postId)
               .eq(PostComment::getParentId, 0L)
               .orderByDesc(PostComment::getCreateTime)
               .orderByDesc(PostComment::getId);
        List<PostComment> rootComments = postCommentMapper.selectList(wrapper);

        if (rootComments.isEmpty())
        {
            return rootComments;
        }

        List<Long> rootIds = rootComments.stream().map(PostComment::getId).collect(Collectors.toList());

        // 拉取本动态下所有回复（含嵌套回复），再统一归档到对应的一级评论下
        LambdaQueryWrapper<PostComment> childWrapper = new LambdaQueryWrapper<>();
        childWrapper.eq(PostComment::getPostId, postId)
                    .ne(PostComment::getParentId, 0L)
                    .orderByAsc(PostComment::getCreateTime)
                    .orderByAsc(PostComment::getId);
        List<PostComment> allReplies = postCommentMapper.selectList(childWrapper);

        Map<Long, PostComment> commentMap = new HashMap<>();
        Map<Long, List<PostComment>> childMap = new HashMap<>();
        for (PostComment root : rootComments)
        {
            commentMap.put(root.getId(), root);
        }
        for (PostComment reply : allReplies)
        {
            commentMap.put(reply.getId(), reply);
        }

        // 沿 parentId 向上找所属一级评论；一级评论不在当前页的回复不展示
        for (PostComment reply : allReplies)
        {
            Long rootId = findRootCommentId(reply, commentMap);
            if (rootId != null && rootIds.contains(rootId))
            {
                childMap.computeIfAbsent(rootId, k -> new ArrayList<>()).add(reply);
            }
        }

        // 回复按时间正序展示（同一时间按 ID 升序，保证稳定）
        for (Map.Entry<Long, List<PostComment>> entry : childMap.entrySet())
        {
            entry.getValue().sort((a, b) ->
            {
                Date at = a.getCreateTime();
                Date bt = b.getCreateTime();
                if (at == null || bt == null)
                {
                    return a.getId().compareTo(b.getId());
                }
                int cmp = at.compareTo(bt);
                return cmp != 0 ? cmp : a.getId().compareTo(b.getId());
            });
        }

        // 统一填充评论人昵称/头像与被回复者昵称
        fillCommentUserInfo(rootComments, childMap, commentMap);

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
    @Transactional(rollbackFor = Exception.class)
    public boolean addComment(Long postId, Long userId, Long parentId, String content)
    {
        PetPost post = baseMapper.selectById(postId);
        if (post == null)
        {
            throw new ServiceException("动态不存在");
        }

        // 回复时校验父评论归属，防止跨动态/无效评论
        Long targetParentId = parentId == null ? 0L : parentId;
        if (targetParentId > 0)
        {
            PostComment parent = postCommentMapper.selectById(targetParentId);
            if (parent == null || !postId.equals(parent.getPostId()))
            {
                throw new ServiceException("回复的评论不存在");
            }
        }

        PostComment comment = new PostComment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setParentId(targetParentId);
        comment.setContent(content);
        if (postCommentMapper.insert(comment) > 0)
        {
            // 同步累加动态评论数（含回复）
            LambdaUpdateWrapper<PetPost> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(PetPost::getId, postId)
                        .setSql("comment_count = ifnull(comment_count, 0) + 1");
            baseMapper.update(null, updateWrapper);
            return true;
        }
        return false;
    }

    /**
     * 沿 parentId 向上追溯评论所属的一级评论ID
     */
    private Long findRootCommentId(PostComment comment, Map<Long, PostComment> commentMap)
    {
        Long parentId = comment.getParentId();
        Set<Long> visited = new HashSet<>();
        while (parentId != null && parentId > 0 && visited.add(parentId))
        {
            PostComment parent = commentMap.get(parentId);
            if (parent == null)
            {
                return null;
            }
            Long grandParentId = parent.getParentId();
            if (grandParentId == null || grandParentId == 0)
            {
                return parent.getId();
            }
            parentId = grandParentId;
        }
        return null;
    }

    /**
     * 批量填充评论人昵称/头像，并为子评论计算被回复者昵称
     */
    private void fillCommentUserInfo(List<PostComment> rootComments,
                                     Map<Long, List<PostComment>> childMap,
                                     Map<Long, PostComment> commentMap)
    {
        List<PostComment> allComments = new ArrayList<>(rootComments);
        childMap.values().forEach(allComments::addAll);

        Map<Long, SysUser> userCache = new HashMap<>();
        for (PostComment comment : allComments)
        {
            fillCommentAuthor(comment, userCache);
        }

        for (PostComment comment : allComments)
        {
            Long parentId = comment.getParentId();
            if (parentId == null || parentId == 0)
            {
                continue;
            }
            PostComment parent = commentMap.get(parentId);
            comment.setReplyUserName(parent == null ? null : parent.getUserName());
        }
    }

    /**
     * 填充单条评论的用户昵称与头像
     */
    private void fillCommentAuthor(PostComment comment, Map<Long, SysUser> userCache)
    {
        if (comment.getUserId() == null)
        {
            return;
        }
        SysUser user = userCache.get(comment.getUserId());
        if (user == null)
        {
            user = sysUserMapper.selectUserById(comment.getUserId());
            if (user == null)
            {
                return;
            }
            userCache.put(comment.getUserId(), user);
        }
        comment.setUserName(user.getNickName());
        comment.setUserAvatar(user.getAvatar());
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
