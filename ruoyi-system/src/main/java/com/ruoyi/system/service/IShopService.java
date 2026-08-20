package com.ruoyi.system.service;

import java.util.List;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.system.domain.ShopCategory;
import com.ruoyi.system.domain.ShopProduct;
import com.ruoyi.system.domain.dto.OrderCreateDTO;
import com.ruoyi.system.domain.dto.ProductQueryDTO;

/**
 * 商城 服务层
 *
 * @author ruoyi
 */
public interface IShopService extends IService<ShopProduct>
{
    /**
     * 获取分类树
     *
     * @return 分类树形列表
     */
    List<ShopCategory> getCategoryList();

    /**
     * 商品列表
     *
     * @param dto 查询参数
     * @return 商品集合
     */
    List<ShopProduct> getProductList(ProductQueryDTO dto);

    /**
     * 商品详情
     *
     * @param productId 商品ID
     * @return 商品信息
     */
    ShopProduct getProductDetail(Long productId);

    /**
     * 兑换商品（需校验库存和积分，使用事务）
     *
     * @param dto 订单创建参数
     * @return 订单编号
     */
    String exchangeProduct(OrderCreateDTO dto);
}
