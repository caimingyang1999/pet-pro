package com.ruoyi.system.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.ShopCategory;
import com.ruoyi.system.domain.ShopOrder;
import com.ruoyi.system.domain.ShopProduct;
import com.ruoyi.system.domain.dto.OrderCreateDTO;
import com.ruoyi.system.domain.dto.ProductQueryDTO;
import com.ruoyi.system.mapper.ShopCategoryMapper;
import com.ruoyi.system.mapper.ShopOrderMapper;
import com.ruoyi.system.mapper.ShopProductMapper;
import com.ruoyi.system.service.IShopService;
import com.ruoyi.system.service.IUserPointsService;

/**
 * 商城 服务层实现
 *
 * @author ruoyi
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopProductMapper, ShopProduct> implements IShopService
{
    @Resource
    private ShopCategoryMapper shopCategoryMapper;

    @Resource
    private ShopOrderMapper shopOrderMapper;

    @Resource
    private IUserPointsService userPointsService;

    /**
     * 获取分类树
     *
     * @return 分类树形列表
     */
    @Override
    public List<ShopCategory> getCategoryList()
    {
        LambdaQueryWrapper<ShopCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(ShopCategory::getSortOrder);
        List<ShopCategory> allCategories = shopCategoryMapper.selectList(wrapper);

        // 构建树形结构
        Map<Long, List<ShopCategory>> parentMap = allCategories.stream()
                .collect(Collectors.groupingBy(c -> c.getParentId() == null ? 0L : c.getParentId()));

        for (ShopCategory category : allCategories)
        {
            category.setChildren(parentMap.getOrDefault(category.getId(), new ArrayList<>()));
        }

        // 返回一级分类（parentId = 0）
        return parentMap.getOrDefault(0L, new ArrayList<>());
    }

    /**
     * 商品列表
     *
     * @param dto 查询参数
     * @return 商品集合
     */
    @Override
    public List<ShopProduct> getProductList(ProductQueryDTO dto)
    {
        PageHelper.startPage(dto.getPageNum(), dto.getPageSize());
        return baseMapper.selectProductList(dto.getCategoryId(), dto.getKeyword());
    }

    /**
     * 商品详情
     *
     * @param productId 商品ID
     * @return 商品信息
     */
    @Override
    public ShopProduct getProductDetail(Long productId)
    {
        ShopProduct product = baseMapper.selectById(productId);
        if (product == null)
        {
            throw new ServiceException("商品不存在");
        }
        return product;
    }

    /**
     * 兑换商品（需校验库存和积分，使用事务）
     *
     * @param dto 订单创建参数
     * @return 订单编号
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String exchangeProduct(OrderCreateDTO dto)
    {
        // 1. 校验商品
        ShopProduct product = baseMapper.selectById(dto.getProductId());
        if (product == null)
        {
            throw new ServiceException("商品不存在");
        }
        if (!"1".equals(product.getStatus()))
        {
            throw new ServiceException("商品已下架");
        }

        // 2. 校验库存
        Integer quantity = dto.getQuantity() == null ? 1 : dto.getQuantity();
        if (product.getStock() < quantity)
        {
            throw new ServiceException("商品库存不足");
        }

        // 3. 校验积分
        Integer totalPoints = product.getPointsPrice() * quantity;
        // 扣减用户积分（addPoints 传负数即扣减）
        userPointsService.addPoints(dto.getUserId(), -totalPoints, "exchange", null);

        // 4. 扣减库存、增加兑换数
        LambdaUpdateWrapper<ShopProduct> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ShopProduct::getId, product.getId())
                     .setSql("stock = stock - " + quantity)
                     .setSql("total_exchange = total_exchange + " + quantity);
        baseMapper.update(null, updateWrapper);

        // 5. 创建订单
        ShopOrder order = new ShopOrder();
        String orderNo = generateOrderNo();
        order.setOrderNo(orderNo);
        order.setUserId(dto.getUserId());
        order.setProductId(product.getId());
        order.setProductName(product.getProductName());
        // 取商品第一张图片作为快照
        order.setProductImage(product.getProductImages());
        order.setPointsPrice(product.getPointsPrice());
        order.setQuantity(quantity);
        order.setTotalPoints(totalPoints);
        order.setAddressId(dto.getAddressId());
        order.setStatus("0"); // 待发货
        shopOrderMapper.insert(order);

        return orderNo;
    }

    /**
     * 生成订单编号（时间戳 + 随机数）
     *
     * @return 订单编号
     */
    private String generateOrderNo()
    {
        return System.currentTimeMillis() + String.format("%04d", (int) (Math.random() * 10000));
    }
}
