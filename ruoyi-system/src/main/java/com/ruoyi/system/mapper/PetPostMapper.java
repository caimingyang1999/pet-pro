package com.ruoyi.system.mapper;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.system.domain.PetPost;
import com.ruoyi.system.domain.dto.PostQueryDTO;

/**
 * 动态 数据层
 *
 * @author ruoyi
 */
public interface PetPostMapper extends BaseMapper<PetPost>
{
    /**
     * 通用动态列表查询（支持：关键词、用户ID、推荐/关注/最新 tab 排序、当前用户点赞状态回显）
     *
     * @param dto 查询参数
     * @return 动态集合
     */
    List<PetPost> selectPostList(PostQueryDTO dto);

    /**
     * 管理端-查询动态列表
     */
    List<PetPost> selectPostListAdmin(@Param("status") String status,
                                      @Param("beginTime") Date beginTime,
                                      @Param("endTime") Date endTime);

    /**
     * 动态详情（含点赞/关注状态回显）
     *
     * @param postId        动态ID
     * @param currentUserId 当前登录用户ID（可空）
     * @return 动态详情
     */
    PetPost selectPostById(@Param("postId") Long postId, @Param("currentUserId") Long currentUserId);

    /**
     * 我的点赞列表（用户点赞过的动态）
     *
     * @param userId  用户ID
     * @param keyword 关键词（可选）
     * @return 动态集合
     */
    List<PetPost> selectPostListByLike(@Param("userId") Long userId,
                                       @Param("keyword") String keyword);
}
