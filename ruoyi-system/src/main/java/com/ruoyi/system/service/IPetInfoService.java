package com.ruoyi.system.service;

import java.util.List;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.system.domain.PetInfo;

/**
 * 宠物信息 服务层
 *
 * @author ruoyi
 */
public interface IPetInfoService extends IService<PetInfo>
{
    /**
     * 获取用户宠物列表（含疫苗记录）
     *
     * @param userId 用户ID
     * @return 宠物信息集合
     */
    List<PetInfo> getPetList(Long userId);

    /**
     * 获取宠物详情
     *
     * @param petId 宠物ID
     * @return 宠物信息
     */
    PetInfo getPetDetail(Long petId);

    /**
     * 添加宠物（同时处理疫苗记录）
     *
     * @param petInfo 宠物信息
     * @return 结果
     */
    boolean addPet(PetInfo petInfo);

    /**
     * 更新宠物（先删旧疫苗再新增）
     *
     * @param petInfo 宠物信息
     * @return 结果
     */
    boolean updatePet(PetInfo petInfo);

    /**
     * 逻辑删除宠物
     *
     * @param petId 宠物ID
     * @return 结果
     */
    boolean deletePet(Long petId);

    /**
     * 后台：查询宠物列表（含所属用户昵称、疫苗记录数）
     *
     * @param name        宠物名称（模糊，可选）
     * @param userKeyword 所属用户（昵称/账号/手机号模糊，可选）
     * @param breed       品种（模糊，可选）
     * @param petType     宠物类型（cat-猫 dog-狗 other-其他，精确匹配，可选）
     * @return 宠物信息集合
     */
    List<PetInfo> getAdminPetList(String name, String userKeyword, String breed, String petType);

    /**
     * 后台：查询品种去重列表（筛选下拉用）
     *
     * @return 品种集合
     */
    List<String> getBreedOptions();
}
