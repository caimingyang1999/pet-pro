package com.ruoyi.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.system.domain.UserFollow;

/**
 * 用户关注关系 数据层
 *
 * @author ruoyi
 */
public interface UserFollowMapper extends BaseMapper<UserFollow>
{
    /**
     * 查询某用户的关注列表（含被关注者昵称、头像）
     *
     * @param followerId 关注者用户ID
     * @return 关注列表
     */
    List<UserFollow> selectFollowList(@Param("followerId") Long followerId);

    /**
     * 查询某用户的粉丝列表（含关注者昵称、头像）
     *
     * @param followeeId 被关注者用户ID
     * @return 粉丝列表
     */
    List<UserFollow> selectFollowerList(@Param("followeeId") Long followeeId);

    /**
     * 批量查询某用户关注的所有 followeeId
     *
     * @param followerId 关注者用户ID
     * @return 被关注用户ID列表
     */
    List<Long> selectFolloweeIds(@Param("followerId") Long followerId);
}
