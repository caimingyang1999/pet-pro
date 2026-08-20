package com.ruoyi.system.service;

import java.util.List;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.system.domain.UserAddress;

/**
 * 用户收货地址 服务层
 *
 * @author ruoyi
 */
public interface IUserAddressService extends IService<UserAddress>
{
    /**
     * 查询用户地址列表
     *
     * @param userId 用户ID
     * @return 地址集合
     */
    List<UserAddress> getAddressList(Long userId);

    /**
     * 添加地址
     *
     * @param address 地址信息
     * @return 结果
     */
    boolean addAddress(UserAddress address);

    /**
     * 更新地址（校验归属）
     *
     * @param address 地址信息
     * @param userId  当前用户ID
     * @return 结果
     */
    boolean updateAddress(UserAddress address, Long userId);

    /**
     * 删除地址（校验归属）
     *
     * @param addressId 地址ID
     * @param userId    当前用户ID
     * @return 结果
     */
    boolean deleteAddress(Long addressId, Long userId);
}
