package com.ruoyi.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.system.domain.ShopOrder;
import com.ruoyi.system.domain.vo.OrderDetailVO;

/**
 * 兑换订单 数据层
 *
 * @author ruoyi
 */
public interface ShopOrderMapper extends BaseMapper<ShopOrder>
{
    /**
     * 查询订单列表（关联用户表，支持用户ID、订单编号、订单状态筛选）
     *
     * @param userId 用户ID
     * @param orderNo 订单编号
     * @param status 订单状态
     * @return 订单集合
     */
    public List<ShopOrder> selectOrderList(@Param("userId") Long userId, @Param("orderNo") String orderNo, @Param("status") String status);

    /**
     * 查询订单详情（关联用户表和收货地址表）
     *
     * @param orderId 订单ID
     * @return 订单详情（含用户和地址信息）
     */
    public OrderDetailVO selectOrderDetailById(@Param("orderId") Long orderId);
}
