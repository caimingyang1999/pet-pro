package com.ruoyi.system.service;

import java.util.List;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.system.domain.UserFollow;

/**
 * 用户关注关系 服务层
 *
 * @author ruoyi
 */
public interface IUserFollowService extends IService<UserFollow>
{
    /**
     * 关注 / 取关（切换）
     *
     * @param followerId 关注者用户ID
     * @param followeeId 被关注者用户ID
     * @return true-已关注 false-已取关
     */
    boolean toggleFollow(Long followerId, Long followeeId);

    /**
     * 判断是否已关注
     *
     * @param followerId 关注者
     * @param followeeId 被关注者
     * @return true-已关注
     */
    boolean isFollowed(Long followerId, Long followeeId);

    /**
     * 查询某用户的关注列表（分页由调用方处理）
     *
     * @param followerId 关注者用户ID
     * @return 关注列表
     */
    List<UserFollow> getFollowList(Long followerId);

    /**
     * 查询某用户的粉丝列表
     *
     * @param followeeId 被关注者用户ID
     * @return 粉丝列表
     */
    List<UserFollow> getFollowerList(Long followeeId);

    /**
     * 查询关注数
     *
     * @param followerId 关注者用户ID
     * @return 关注数
     */
    int countFollow(Long followerId);

    /**
     * 查询粉丝数
     *
     * @param followeeId 被关注者用户ID
     * @return 粉丝数
     */
    int countFollower(Long followeeId);

    /**
     * 批量获取用户关注的 followeeId 列表
     *
     * @param followerId 关注者用户ID
     * @return 被关注用户ID列表
     */
    List<Long> getFolloweeIds(Long followerId);
}
