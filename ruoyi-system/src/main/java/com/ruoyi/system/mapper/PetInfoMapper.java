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
}
