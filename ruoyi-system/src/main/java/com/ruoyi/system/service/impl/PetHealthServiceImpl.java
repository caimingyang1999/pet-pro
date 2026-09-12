package com.ruoyi.system.service.impl;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.PetAlbum;
import com.ruoyi.system.domain.PetInfo;
import com.ruoyi.system.domain.PetWeightLog;
import com.ruoyi.system.mapper.PetAlbumMapper;
import com.ruoyi.system.mapper.PetInfoMapper;
import com.ruoyi.system.mapper.PetWeightLogMapper;
import com.ruoyi.system.service.IPetHealthService;

/**
 * 宠物健康记录（体重 / 成长相册） 服务层实现
 *
 * @author ruoyi
 */
@Service
public class PetHealthServiceImpl implements IPetHealthService
{
    @Resource
    private PetWeightLogMapper petWeightLogMapper;

    @Resource
    private PetAlbumMapper petAlbumMapper;

    @Resource
    private PetInfoMapper petInfoMapper;

    /**
     * 校验宠物归属：宠物必须存在且属于当前用户
     *
     * @param userId 当前登录用户ID
     * @param petId  宠物ID
     */
    private void checkPetOwner(Long userId, Long petId)
    {
        if (petId == null)
        {
            throw new ServiceException("请先选择宠物");
        }
        PetInfo pet = petInfoMapper.selectById(petId);
        if (pet == null)
        {
            throw new ServiceException("宠物不存在");
        }
        if (!pet.getUserId().equals(userId))
        {
            throw new ServiceException("无权操作他人的宠物数据");
        }
    }

    /* ==================== 体重记录 ==================== */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addWeightLog(Long userId, PetWeightLog log)
    {
        checkPetOwner(userId, log.getPetId());
        if (log.getWeight() == null)
        {
            throw new ServiceException("请填写体重");
        }
        if (log.getRecordDate() == null)
        {
            log.setRecordDate(LocalDate.now());
        }
        log.setId(null);
        log.setUserId(userId);
        petWeightLogMapper.insert(log);
        return log.getId();
    }

    @Override
    public List<PetWeightLog> getWeightLogList(Long userId, Long petId)
    {
        checkPetOwner(userId, petId);
        LambdaQueryWrapper<PetWeightLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PetWeightLog::getPetId, petId)
               .orderByAsc(PetWeightLog::getRecordDate)
               .orderByAsc(PetWeightLog::getId);
        return petWeightLogMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteWeightLog(Long userId, Long id)
    {
        PetWeightLog exist = petWeightLogMapper.selectById(id);
        if (exist == null)
        {
            throw new ServiceException("记录不存在");
        }
        if (!exist.getUserId().equals(userId))
        {
            throw new ServiceException("无权删除他人的记录");
        }
        return petWeightLogMapper.deleteById(id);
    }

    /* ==================== 成长相册 ==================== */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addAlbum(Long userId, PetAlbum album)
    {
        checkPetOwner(userId, album.getPetId());
        album.setId(null);
        album.setUserId(userId);
        if (album.getRecordDate() == null)
        {
            album.setRecordDate(LocalDate.now());
        }
        petAlbumMapper.insert(album);
        return album.getId();
    }

    @Override
    public List<PetAlbum> getAlbumList(Long userId, Long petId)
    {
        checkPetOwner(userId, petId);
        LambdaQueryWrapper<PetAlbum> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PetAlbum::getPetId, petId)
               .orderByDesc(PetAlbum::getRecordDate)
               .orderByDesc(PetAlbum::getId);
        return petAlbumMapper.selectList(wrapper);
    }

    @Override
    public PetAlbum getAlbumDetail(Long userId, Long id)
    {
        PetAlbum album = petAlbumMapper.selectById(id);
        if (album == null)
        {
            throw new ServiceException("相册记录不存在");
        }
        // 成长相册仅本人可见，非本人一律视为不存在，避免泄露他人内容
        if (!album.getUserId().equals(userId))
        {
            throw new ServiceException("无权查看他人的相册记录");
        }
        return album;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateAlbum(Long userId, PetAlbum album)
    {
        PetAlbum exist = getAlbumDetail(userId, album.getId());
        // 归属与宠物都不可被篡改
        album.setUserId(exist.getUserId());
        album.setPetId(exist.getPetId());
        return petAlbumMapper.updateById(album);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteAlbum(Long userId, Long id)
    {
        getAlbumDetail(userId, id);
        return petAlbumMapper.deleteById(id);
    }
}
