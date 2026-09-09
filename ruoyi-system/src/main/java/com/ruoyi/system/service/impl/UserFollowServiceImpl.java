package com.ruoyi.system.service.impl;

import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.system.domain.UserFollow;
import com.ruoyi.system.mapper.UserFollowMapper;
import com.ruoyi.system.service.IUserFollowService;

/**
 * 用户关注关系 服务层实现
 *
 * @author ruoyi
 */
@Service
public class UserFollowServiceImpl extends ServiceImpl<UserFollowMapper, UserFollow>
        implements IUserFollowService
{
    @Override
    public boolean isFollowed(Long followerId, Long followeeId)
    {
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getFollowerId, followerId)
               .eq(UserFollow::getFolloweeId, followeeId);
        return baseMapper.selectCount(wrapper) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleFollow(Long followerId, Long followeeId)
    {
        // 不能关注自己
        if (followerId.equals(followeeId))
        {
            return false;
        }
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getFollowerId, followerId)
               .eq(UserFollow::getFolloweeId, followeeId);
        UserFollow exist = baseMapper.selectOne(wrapper);

        if (exist != null)
        {
            // 已关注 -> 取关（逻辑删除）
            baseMapper.deleteById(exist.getId());
            return false;
        }
        else
        {
            // 未关注 -> 新增
            UserFollow follow = new UserFollow();
            follow.setFollowerId(followerId);
            follow.setFolloweeId(followeeId);
            baseMapper.insert(follow);
            return true;
        }
    }

    @Override
    public List<UserFollow> getFollowList(Long followerId)
    {
        return baseMapper.selectFollowList(followerId);
    }

    @Override
    public List<UserFollow> getFollowerList(Long followeeId)
    {
        return baseMapper.selectFollowerList(followeeId);
    }

    @Override
    public int countFollow(Long followerId)
    {
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getFollowerId, followerId);
        return baseMapper.selectCount(wrapper).intValue();
    }

    @Override
    public int countFollower(Long followeeId)
    {
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getFolloweeId, followeeId);
        return baseMapper.selectCount(wrapper).intValue();
    }

    @Override
    public List<Long> getFolloweeIds(Long followerId)
    {
        return baseMapper.selectFolloweeIds(followerId);
    }
}
