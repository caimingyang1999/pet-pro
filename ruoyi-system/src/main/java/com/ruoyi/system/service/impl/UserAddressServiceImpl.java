package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.UserAddress;
import com.ruoyi.system.mapper.UserAddressMapper;
import com.ruoyi.system.service.IUserAddressService;

/**
 * 用户收货地址 服务层实现
 *
 * @author ruoyi
 */
@Service
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress> implements IUserAddressService
{
    /**
     * 查询用户地址列表
     *
     * @param userId 用户ID
     * @return 地址集合
     */
    @Override
    public List<UserAddress> getAddressList(Long userId)
    {
        LambdaQueryWrapper<UserAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAddress::getUserId, userId)
               .orderByDesc(UserAddress::getIsDefault);
        return baseMapper.selectList(wrapper);
    }

    /**
     * 添加地址
     *
     * @param address 地址信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addAddress(UserAddress address)
    {
        // 如果是默认地址，先取消其他默认地址
        if ("1".equals(address.getIsDefault()))
        {
            clearDefaultAddress(address.getUserId());
        }
        return baseMapper.insert(address) > 0;
    }

    /**
     * 更新地址（校验归属）
     *
     * @param address 地址信息
     * @param userId  当前用户ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAddress(UserAddress address, Long userId)
    {
        UserAddress exist = baseMapper.selectById(address.getId());
        if (exist == null || !exist.getUserId().equals(userId))
        {
            throw new ServiceException("无权操作此地址");
        }
        // 如果设为默认地址，先取消其他默认地址
        if ("1".equals(address.getIsDefault()))
        {
            clearDefaultAddress(userId);
        }
        return baseMapper.updateById(address) > 0;
    }

    /**
     * 删除地址（校验归属）
     *
     * @param addressId 地址ID
     * @param userId    当前用户ID
     * @return 结果
     */
    @Override
    public boolean deleteAddress(Long addressId, Long userId)
    {
        UserAddress exist = baseMapper.selectById(addressId);
        if (exist == null || !exist.getUserId().equals(userId))
        {
            throw new ServiceException("无权删除此地址");
        }
        return baseMapper.deleteById(addressId) > 0;
    }

    /**
     * 清除用户的其他默认地址
     *
     * @param userId 用户ID
     */
    private void clearDefaultAddress(Long userId)
    {
        LambdaUpdateWrapper<UserAddress> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserAddress::getUserId, userId)
               .eq(UserAddress::getIsDefault, "1")
               .set(UserAddress::getIsDefault, "0");
        baseMapper.update(null, wrapper);
    }
}
