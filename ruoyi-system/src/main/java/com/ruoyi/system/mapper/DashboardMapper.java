package com.ruoyi.system.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.PetPost;
import com.ruoyi.system.domain.ShopProduct;

/**
 * 后台首页数据总览 Mapper
 *
 * @author ruoyi
 */
public interface DashboardMapper
{
    /** 订单总数 */
    int countOrders();

    /** 上架商品总数（未删除） */
    int countProducts();

    /** 动态总数（未删除） */
    int countPosts();

    /** 宠物总数（未删除） */
    int countPets();

    /** 用户总数（未删除） */
    int countUsers();

    /** 订单状态分布：返回 {status, cnt} */
    List<Map<String, Object>> orderStatusDistribution();

    /**
     * 每日新增订单：返回 {day(MM-dd), cnt}
     *
     * @param startDate 统计起始时间（yyyy-MM-dd HH:mm:ss）
     */
    List<Map<String, Object>> dailyOrderCounts(@Param("startDate") String startDate);

    /**
     * 每日新增商品：返回 {day(MM-dd), cnt}
     *
     * @param startDate 统计起始时间（yyyy-MM-dd HH:mm:ss）
     */
    List<Map<String, Object>> dailyProductCounts(@Param("startDate") String startDate);

    /**
     * 每日新增动态：返回 {day(MM-dd), cnt}
     *
     * @param startDate 统计起始时间（yyyy-MM-dd HH:mm:ss）
     */
    List<Map<String, Object>> dailyPostCounts(@Param("startDate") String startDate);

    /**
     * 每日新增宠物：返回 {day(MM-dd), cnt}
     *
     * @param startDate 统计起始时间（yyyy-MM-dd HH:mm:ss）
     */
    List<Map<String, Object>> dailyPetCounts(@Param("startDate") String startDate);

    /**
     * 每日新增用户：返回 {day(MM-dd), cnt}
     *
     * @param startDate 统计起始时间（yyyy-MM-dd HH:mm:ss）
     */
    List<Map<String, Object>> dailyUserCounts(@Param("startDate") String startDate);

    /** 待审核动态数（status=0） */
    int countPendingPosts();

    /** 待发货订单数（status=0） */
    int countPendingShipOrders();

    /** 库存预警商品数（stock < threshold） */
    int countLowStockProducts(@Param("threshold") int threshold);

    /** 最新动态（含发布用户昵称/头像） */
    List<PetPost> selectLatestPosts(@Param("limit") int limit);

    /** 热门商品 TOP N（按总兑换量倒序） */
    List<ShopProduct> selectHotProducts(@Param("limit") int limit);
}
