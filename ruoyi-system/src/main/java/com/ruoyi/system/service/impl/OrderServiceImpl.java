package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.ShopOrder;
import com.ruoyi.system.domain.vo.OrderDetailVO;
import com.ruoyi.system.mapper.ShopOrderMapper;
import com.ruoyi.system.service.IOrderService;

/**
 * 订单 服务层实现
 *
 * @author ruoyi
 */
@Service
public class OrderServiceImpl extends ServiceImpl<ShopOrderMapper, ShopOrder> implements IOrderService
{
    /**
     * 订单列表
     *
     * @param userId 用户ID
     * @param orderNo 订单编号
     * @param status 订单状态
     * @return 订单集合
     */
    @Override
    public List<ShopOrder> getOrderList(Long userId, String orderNo, String status)
    {
        return baseMapper.selectOrderList(userId, orderNo, status);
    }

    /**
     * 订单详情
     *
     * @param orderId 订单ID
     * @return 订单信息
     */
    @Override
    public ShopOrder getOrderDetail(Long orderId)
    {
        ShopOrder order = baseMapper.selectById(orderId);
        if (order == null)
        {
            throw new ServiceException("订单不存在");
        }
        return order;
    }

    /**
     * 订单详情（含关联用户和收货地址信息）
     *
     * @param orderId 订单ID
     * @return 订单详情（订单信息 + 用户信息 + 地址信息）
     */
    @Override
    public OrderDetailVO getOrderDetailWithAssoc(Long orderId)
    {
        OrderDetailVO vo = baseMapper.selectOrderDetailById(orderId);
        if (vo == null)
        {
            throw new ServiceException("订单不存在");
        }
        return vo;
    }

    /**
     * 更新订单状态
     *
     * @param orderId    订单ID
     * @param status     订单状态
     * @param expressNo  快递单号
     * @return 结果
     */
    @Override
    public boolean updateOrderStatus(Long orderId, String status, String expressNo)
    {
        ShopOrder order = baseMapper.selectById(orderId);
        if (order == null)
        {
            throw new ServiceException("订单不存在");
        }
        order.setStatus(status);
        if (expressNo != null && !expressNo.isEmpty())
        {
            order.setExpressNo(expressNo);
        }
        return baseMapper.updateById(order) > 0;
    }
}
