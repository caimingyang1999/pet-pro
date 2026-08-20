package com.ruoyi.system.service;

import java.util.List;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.system.domain.ShopOrder;
import com.ruoyi.system.domain.vo.OrderDetailVO;

/**
 * 订单 服务层
 *
 * @author ruoyi
 */
public interface IOrderService extends IService<ShopOrder>
{
    /**
     * 订单列表
     *
     * @param userId 用户ID
     * @param orderNo 订单编号
     * @param status 订单状态
     * @return 订单集合
     */
    List<ShopOrder> getOrderList(Long userId, String orderNo, String status);

    /**
     * 订单详情
     *
     * @param orderId 订单ID
     * @return 订单信息
     */
    ShopOrder getOrderDetail(Long orderId);

    /**
     * 订单详情（含关联用户和收货地址信息）
     *
     * @param orderId 订单ID
     * @return 订单详情（订单信息 + 用户信息 + 地址信息）
     */
    OrderDetailVO getOrderDetailWithAssoc(Long orderId);

    /**
     * 更新订单状态
     *
     * @param orderId    订单ID
     * @param status     订单状态
     * @param expressNo  快递单号
     * @return 结果
     */
    boolean updateOrderStatus(Long orderId, String status, String expressNo);
}
