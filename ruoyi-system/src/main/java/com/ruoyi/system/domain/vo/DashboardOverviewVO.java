package com.ruoyi.system.domain.vo;

import java.util.List;
import lombok.Data;

/**
 * 后台首页数据总览 VO
 *
 * <p>统一返回首页看板所需的全部数据，前端一次性拉取后按区块拆分使用。</p>
 *
 * @author ruoyi
 */
@Data
public class DashboardOverviewVO
{
    /** 统计卡片（订单总数/商品总数/宠物动态总数/宠物总数） */
    private List<StatItem> stats;

    /** 数据趋势（近 N 天 订单/商品/动态/宠物 每日新增） */
    private DataTrend trend;

    /** 订单状态分布 */
    private OrderStatus orderStatus;

    /** 待处理事项（待审核动态/待发货订单/库存预警商品） */
    private List<PendingTask> pendingTasks;

    /** 最新动态 */
    private List<LatestPost> latestActivities;

    /** 热门商品 TOP5 */
    private List<HotProduct> hotProducts;

    /** 用户增长趋势（近 N 天每日新增用户） */
    private UserGrowth userGrowth;

    @Data
    public static class StatItem
    {
        /** 标识：orders/product/posts/pets */
        private String key;
        /** 卡片标题 */
        private String title;
        /** 总量 */
        private Long value;
        /** 较昨日增长率（百分比，可正可负，如 12.5 表示 +12.5%） */
        private Double growth;
        /** 图标类型，前端映射图标 */
        private String iconType;
        /** 主色 */
        private String color;
        /** 卡片背景渐变 */
        private String bgGradient;
    }

    @Data
    public static class DataTrend
    {
        /** 日期轴（MM-dd） */
        private List<String> dates;
        /** 多条趋势线 */
        private List<TrendSeries> series;
    }

    @Data
    public static class TrendSeries
    {
        /** 系列名称（订单数量/商品数量/动态数量/宠物数量） */
        private String name;
        /** 颜色 */
        private String color;
        /** 每日数据 */
        private List<Long> data;
    }

    @Data
    public static class OrderStatus
    {
        /** 订单总数 */
        private Long total;
        /** 总数标签 */
        private String totalLabel;
        /** 各状态明细 */
        private List<OrderStatusItem> legend;
    }

    @Data
    public static class OrderStatusItem
    {
        /** 状态名称（待发货/已发货/已完成/已取消） */
        private String name;
        /** 数量 */
        private Long value;
        /** 占比（百分比） */
        private Double percent;
        /** 颜色 */
        private String color;
    }

    @Data
    public static class PendingTask
    {
        private Long id;
        /** 事项标题 */
        private String title;
        /** 待处理数量 */
        private Long count;
        /** 图标类型，前端映射图标 */
        private String iconType;
        /** 主色 */
        private String color;
    }

    @Data
    public static class LatestPost
    {
        private Long id;
        /** 发布用户昵称 */
        private String user;
        /** 发布用户头像 */
        private String avatar;
        /** 动态内容 */
        private String content;
        /** 相对时间（x分钟前 / x小时前 / x天前） */
        private String time;
        /** 状态文案（待审核/已通过/已拒绝） */
        private String status;
        /** 状态类型（pending/success/reject），前端映射标签颜色 */
        private String statusType;
    }

    @Data
    public static class HotProduct
    {
        /** 排名 1-5 */
        private Integer rank;
        /** 商品名称 */
        private String name;
        /** 兑换量 */
        private Long exchanges;
        /** 相对最高兑换量的占比（百分比） */
        private Double percent;
        /** 商品主图 */
        private String image;
    }

    @Data
    public static class UserGrowth
    {
        /** 日期轴（MM-dd） */
        private List<String> dates;
        /** 每日新增用户 */
        private List<Long> data;
    }
}
