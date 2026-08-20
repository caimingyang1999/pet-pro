package com.ruoyi.system.mapper;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.system.domain.PetPost;

/**
 * 动态 数据层
 *
 * @author ruoyi
 */
public interface PetPostMapper extends BaseMapper<PetPost>
{
    /**
     * 查询动态列表（关联用户表，支持关键词、用户ID筛选，按创建时间倒序）
     *
     * @param keyword 关键词
     * @param userId  用户ID
     * @return 动态集合
     */
    public List<PetPost> selectPostList(@Param("keyword") String keyword, @Param("userId") Long userId);

    /**
     * 管理端-查询动态列表（关联用户和宠物表，支持状态、时间范围筛选）
     *
     * @param status    审核状态
     * @param beginTime 起始时间
     * @param endTime   结束时间
     * @return 动态集合
     */
    public List<PetPost> selectPostListAdmin(@Param("status") String status,
                                              @Param("beginTime") Date beginTime,
                                              @Param("endTime") Date endTime);

    /**
     * 管理端-按ID查动态详情（关联用户和宠物信息）
     *
     * @param postId 动态ID
     * @return 动态详情
     */
    public PetPost selectPostById(@Param("postId") Long postId);
}
