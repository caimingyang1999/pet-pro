package com.ruoyi.system.service.impl;

import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.PetInfo;
import com.ruoyi.system.domain.PetVaccine;
import com.ruoyi.system.mapper.PetInfoMapper;
import com.ruoyi.system.mapper.PetVaccineMapper;
import com.ruoyi.system.service.IPetInfoService;
import com.ruoyi.system.service.IUserPointsService;

/**
 * 宠物信息 服务层实现
 *
 * @author ruoyi
 */
@Service
public class PetInfoServiceImpl extends ServiceImpl<PetInfoMapper, PetInfo> implements IPetInfoService
{
    @Resource
    private PetVaccineMapper petVaccineMapper;

    @Resource
    private IUserPointsService userPointsService;

    /**
     * 获取用户宠物列表（含疫苗记录）
     *
     * @param userId 用户ID
     * @return 宠物信息集合
     */
    @Override
    public List<PetInfo> getPetList(Long userId)
    {
        List<PetInfo> petList = baseMapper.selectPetList(userId);
        // 为每只宠物查询疫苗记录
        for (PetInfo pet : petList)
        {
            LambdaQueryWrapper<PetVaccine> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(PetVaccine::getPetId, pet.getId())
                   .orderByDesc(PetVaccine::getInoculationDate);
            pet.setVaccineList(petVaccineMapper.selectList(wrapper));
        }
        return petList;
    }

    /**
     * 获取宠物详情
     *
     * @param petId 宠物ID
     * @return 宠物信息
     */
    @Override
    public PetInfo getPetDetail(Long petId)
    {
        // 走关联查询，带出所属用户昵称（后台详情页需要展示）
        PetInfo pet = baseMapper.selectPetDetailById(petId);
        if (pet == null)
        {
            throw new ServiceException("宠物不存在");
        }
        // 查询疫苗记录
        LambdaQueryWrapper<PetVaccine> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PetVaccine::getPetId, petId)
               .orderByDesc(PetVaccine::getInoculationDate);
        pet.setVaccineList(petVaccineMapper.selectList(wrapper));
        return pet;
    }

    /**
     * 添加宠物（同时处理疫苗记录）
     * 首次完善宠物信息时奖励 +20 积分（每用户仅一次）
     *
     * @param petInfo 宠物信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addPet(PetInfo petInfo)
    {
        // 保存宠物信息
        baseMapper.insert(petInfo);
        // 保存疫苗记录
        saveVaccines(petInfo.getId(), petInfo.getVaccineList());
        // 首次完善宠物信息奖励积分（每用户仅一次）
        if (!userPointsService.hasReceivedReward(petInfo.getUserId(), "pet"))
        {
            userPointsService.addPoints(petInfo.getUserId(), 20, "pet", petInfo.getId());
        }
        return true;
    }

    /**
     * 更新宠物（先删旧疫苗再新增）
     *
     * @param petInfo 宠物信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePet(PetInfo petInfo)
    {
        // 更新宠物信息
        baseMapper.updateById(petInfo);
        // 先删除旧疫苗记录
        LambdaQueryWrapper<PetVaccine> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PetVaccine::getPetId, petInfo.getId());
        petVaccineMapper.delete(wrapper);
        // 再新增新疫苗记录
        saveVaccines(petInfo.getId(), petInfo.getVaccineList());
        return true;
    }

    /**
     * 逻辑删除宠物
     *
     * @param petId 宠物ID
     * @return 结果
     */
    @Override
    public boolean deletePet(Long petId)
    {
        PetInfo pet = baseMapper.selectById(petId);
        if (pet == null)
        {
            throw new ServiceException("宠物不存在");
        }
        // 逻辑删除宠物（MyBatis-Plus @TableLogic 自动处理）
        return baseMapper.deleteById(petId) > 0;
    }

    /**
     * 后台：查询宠物列表（含所属用户昵称、疫苗记录数）
     *
     * @param name        宠物名称（模糊，可选）
     * @param userKeyword 所属用户（昵称/账号/手机号模糊，可选）
     * @param breed       品种（模糊，可选）
     * @param petType     宠物类型（cat-猫 dog-狗 other-其他，精确匹配，可选）
     * @return 宠物信息集合
     */
    @Override
    public List<PetInfo> getAdminPetList(String name, String userKeyword, String breed, String petType)
    {
        return baseMapper.selectAdminPetList(name, userKeyword, breed, petType);
    }

    /**
     * 后台：查询品种去重列表（筛选下拉用）
     *
     * @return 品种集合
     */
    @Override
    public List<String> getBreedOptions()
    {
        return baseMapper.selectDistinctBreeds();
    }

    /**
     * 批量保存疫苗记录
     *
     * @param petId       宠物ID
     * @param vaccineList 疫苗记录列表
     */
    private void saveVaccines(Long petId, List<PetVaccine> vaccineList)
    {
        if (vaccineList != null && !vaccineList.isEmpty())
        {
            for (PetVaccine vaccine : vaccineList)
            {
                vaccine.setPetId(petId);
                petVaccineMapper.insert(vaccine);
            }
        }
    }
}
