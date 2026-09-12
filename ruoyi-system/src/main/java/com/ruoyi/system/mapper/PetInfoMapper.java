package com.ruoyi.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.system.domain.PetInfo;

/**
 * 宠物信息 数据层
 *
 * @author ruoyi
 */
public interface PetInfoMapper extends BaseMapper<PetInfo>
{
    /**
     * 查询宠物列表（关联用户表）
     *
     * @param userId 用户ID
     * @return 宠物信息集合
     */
    public List<PetInfo> selectPetList(@Param("userId") Long userId);

    /**
     * 后台查询宠物列表（关联用户表，附带疫苗记录数）
     *
     * @param name        宠物名称（模糊）
     * @param userKeyword 所属用户（昵称/账号/手机号，模糊）
     * @param breed       品种（模糊）
     * @param petType     宠物类型（cat-猫 dog-狗 other-其他，精确匹配，可选）
     * @return 宠物信息集合
     */
    public List<PetInfo> selectAdminPetList(@Param("name") String name,
                                            @Param("userKeyword") String userKeyword,
                                            @Param("breed") String breed,
                                            @Param("petType") String petType);

    /**
     * 查询宠物详情（关联用户表，带出所属用户昵称）
     *
     * @param id 宠物ID
     * @return 宠物信息
     */
    public PetInfo selectPetDetailById(@Param("id") Long id);

    /**
     * 查询品种去重列表（用于后台筛选下拉，避免使用商城分类）
     *
     * @return 品种集合
     */
    public List<String> selectDistinctBreeds();
}
