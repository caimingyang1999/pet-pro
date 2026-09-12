package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.PetAlbum;
import com.ruoyi.system.domain.PetWeightLog;

/**
 * 宠物健康记录（体重 / 成长相册） 服务层
 *
 * 所有方法均以 userId 做归属校验，用户只能操作自己宠物的数据，
 * 成长相册内容仅本人可见，不提供任何公开或互动能力。
 *
 * @author ruoyi
 */
public interface IPetHealthService
{
    /**
     * 添加体重记录
     *
     * @param userId 当前登录用户ID
     * @param log    体重记录（petId 必填）
     * @return 新增记录ID
     */
    public Long addWeightLog(Long userId, PetWeightLog log);

    /**
     * 查询某宠物的体重记录（按记录日期升序，便于绘制趋势）
     *
     * @param userId 当前登录用户ID
     * @param petId  宠物ID
     * @return 体重记录集合
     */
    public List<PetWeightLog> getWeightLogList(Long userId, Long petId);

    /**
     * 删除体重记录
     *
     * @param userId 当前登录用户ID
     * @param id     记录ID
     * @return 影响行数
     */
    public int deleteWeightLog(Long userId, Long id);

    /**
     * 添加成长相册记录
     *
     * @param userId 当前登录用户ID
     * @param album  相册记录（petId 必填）
     * @return 新增记录ID
     */
    public Long addAlbum(Long userId, PetAlbum album);

    /**
     * 查询某宠物的成长相册（按记录日期倒序）
     *
     * @param userId 当前登录用户ID
     * @param petId  宠物ID
     * @return 相册记录集合
     */
    public List<PetAlbum> getAlbumList(Long userId, Long petId);

    /**
     * 查询相册记录详情
     *
     * @param userId 当前登录用户ID
     * @param id     记录ID
     * @return 相册记录
     */
    public PetAlbum getAlbumDetail(Long userId, Long id);

    /**
     * 更新成长相册记录
     *
     * @param userId 当前登录用户ID
     * @param album  相册记录（id 必填）
     * @return 影响行数
     */
    public int updateAlbum(Long userId, PetAlbum album);

    /**
     * 删除成长相册记录
     *
     * @param userId 当前登录用户ID
     * @param id     记录ID
     * @return 影响行数
     */
    public int deleteAlbum(Long userId, Long id);
}
