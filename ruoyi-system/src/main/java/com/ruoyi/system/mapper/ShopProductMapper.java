package com.ruoyi.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.system.domain.ShopProduct;

/**
 * 商品 数据层
 *
 * @author ruoyi
 */
public interface ShopProductMapper extends BaseMapper<ShopProduct>
{
    /**
     * 查询商品列表（关联分类表，支持分类ID、关键词筛选）
     *
     * @param categoryId 分类ID
     * @param keyword    关键词
     * @return 商品集合
     */
    public List<ShopProduct> selectProductList(@Param("categoryId") Long categoryId, @Param("keyword") String keyword);
}
