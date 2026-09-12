package com.ruoyi.system.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.PetPost;
import com.ruoyi.system.domain.vo.DashboardOverviewVO;
import com.ruoyi.system.domain.vo.DashboardOverviewVO.HotProduct;
import com.ruoyi.system.domain.vo.DashboardOverviewVO.LatestPost;
import com.ruoyi.system.domain.vo.DashboardOverviewVO.OrderStatus;
import com.ruoyi.system.domain.vo.DashboardOverviewVO.OrderStatusItem;
import com.ruoyi.system.domain.vo.DashboardOverviewVO.PendingTask;
import com.ruoyi.system.domain.vo.DashboardOverviewVO.StatItem;
import com.ruoyi.system.domain.vo.DashboardOverviewVO.TrendSeries;
import com.ruoyi.system.mapper.DashboardMapper;
import com.ruoyi.system.service.IDashboardService;

/**
 * 后台首页数据总览 服务层实现
 *
 * @author ruoyi
 */
@Service
public class DashboardServiceImpl implements IDashboardService
{
    /** 库存预警阈值：库存低于该值视为预警 */
    private static final int LOW_STOCK_THRESHOLD = 10;

    /** 统计卡片元数据：key -> {标题, 图标类型, 主色, 背景渐变} */
    private static final LinkedHashMap<String, String[]> STAT_META = new LinkedHashMap<>();
    static
    {
        STAT_META.put("orders", new String[] { "订单总数", "order", "#3b82f6", "linear-gradient(135deg, #dbeafe 0%, #eff6ff 100%)" });
        STAT_META.put("products", new String[] { "商品总数", "product", "#10b981", "linear-gradient(135deg, #d1fae5 0%, #ecfdf5 100%)" });
        STAT_META.put("posts", new String[] { "宠物动态总数", "activity", "#8b5cf6", "linear-gradient(135deg, #ede9fe 0%, #f5f3ff 100%)" });
        STAT_META.put("pets", new String[] { "宠物总数", "pet", "#f97316", "linear-gradient(135deg, #fed7aa 0%, #fff7ed 100%)" });
    }

    /** 趋势序列元数据：name -> 颜色 */
    private static final LinkedHashMap<String, String> TREND_META = new LinkedHashMap<>();
    static
    {
        TREND_META.put("订单数量", "#3b82f6");
        TREND_META.put("商品数量", "#10b981");
        TREND_META.put("动态数量", "#8b5cf6");
        TREND_META.put("宠物数量", "#f97316");
    }

    /** 订单状态元数据：status -> {名称, 颜色} */
    private static final Map<String, String[]> ORDER_STATUS_META = new HashMap<>();
    static
    {
        ORDER_STATUS_META.put("0", new String[] { "待发货", "#3b82f6" });
        ORDER_STATUS_META.put("1", new String[] { "已发货", "#10b981" });
        ORDER_STATUS_META.put("2", new String[] { "已完成", "#8b5cf6" });
        ORDER_STATUS_META.put("3", new String[] { "已取消", "#f97316" });
    }

    @Resource
    private DashboardMapper dashboardMapper;

    @Override
    public DashboardOverviewVO getOverview(int days)
    {
        if (days != 7 && days != 14 && days != 30)
        {
            days = 7;
        }

        // 日期轴（近 days 天，MM-dd）
        List<String> dateAxis = buildDateAxis(days);

        // 统计窗口起始时间（含今天在内的 days 天，取当天 00:00:00）
        String startDate = LocalDate.now().minusDays(days - 1)
                .atStartOfDay()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 各表近 days 天每日新增
        List<Long> orderDaily = fillDaily(dashboardMapper.dailyOrderCounts(startDate), dateAxis);
        List<Long> productDaily = fillDaily(dashboardMapper.dailyProductCounts(startDate), dateAxis);
        List<Long> postDaily = fillDaily(dashboardMapper.dailyPostCounts(startDate), dateAxis);
        List<Long> petDaily = fillDaily(dashboardMapper.dailyPetCounts(startDate), dateAxis);
        List<Long> userDaily = fillDaily(dashboardMapper.dailyUserCounts(startDate), dateAxis);

        DashboardOverviewVO vo = new DashboardOverviewVO();
        vo.setStats(buildStats(orderDaily, productDaily, postDaily, petDaily));
        vo.setTrend(buildTrend(dateAxis, orderDaily, productDaily, postDaily, petDaily));
        vo.setOrderStatus(buildOrderStatus());
        vo.setPendingTasks(buildPendingTasks());
        vo.setLatestActivities(buildLatestPosts());
        vo.setHotProducts(buildHotProducts());
        vo.setUserGrowth(buildUserGrowth(dateAxis, userDaily));
        return vo;
    }

    /**
     * 统计卡片（总量 + 较昨日增长率）
     */
    private List<StatItem> buildStats(List<Long> orderDaily, List<Long> productDaily,
            List<Long> postDaily, List<Long> petDaily)
    {
        long orders = dashboardMapper.countOrders();
        long products = dashboardMapper.countProducts();
        long posts = dashboardMapper.countPosts();
        long pets = dashboardMapper.countPets();

        Map<String, Long> totals = new LinkedHashMap<>();
        totals.put("orders", orders);
        totals.put("products", products);
        totals.put("posts", posts);
        totals.put("pets", pets);

        Map<String, List<Long>> dailys = new LinkedHashMap<>();
        dailys.put("orders", orderDaily);
        dailys.put("products", productDaily);
        dailys.put("posts", postDaily);
        dailys.put("pets", petDaily);

        List<StatItem> list = new ArrayList<>();
        for (Map.Entry<String, String[]> entry : STAT_META.entrySet())
        {
            String key = entry.getKey();
            String[] meta = entry.getValue();
            StatItem item = new StatItem();
            item.setKey(key);
            item.setTitle(meta[0]);
            item.setIconType(meta[1]);
            item.setColor(meta[2]);
            item.setBgGradient(meta[3]);
            item.setValue(totals.get(key));
            item.setGrowth(calcGrowth(dailys.get(key)));
            list.add(item);
        }
        return list;
    }

    /**
     * 数据趋势
     */
    private DashboardOverviewVO.DataTrend buildTrend(List<String> dateAxis, List<Long> orderDaily,
            List<Long> productDaily, List<Long> postDaily, List<Long> petDaily)
    {
        Map<String, List<Long>> seriesData = new LinkedHashMap<>();
        seriesData.put("订单数量", orderDaily);
        seriesData.put("商品数量", productDaily);
        seriesData.put("动态数量", postDaily);
        seriesData.put("宠物数量", petDaily);

        List<TrendSeries> series = new ArrayList<>();
        for (Map.Entry<String, String> entry : TREND_META.entrySet())
        {
            TrendSeries s = new TrendSeries();
            s.setName(entry.getKey());
            s.setColor(entry.getValue());
            s.setData(seriesData.get(entry.getKey()));
            series.add(s);
        }

        DashboardOverviewVO.DataTrend trend = new DashboardOverviewVO.DataTrend();
        trend.setDates(dateAxis);
        trend.setSeries(series);
        return trend;
    }

    /**
     * 订单状态分布
     */
    private OrderStatus buildOrderStatus()
    {
        List<Map<String, Object>> rows = dashboardMapper.orderStatusDistribution();
        long total = 0;
        for (Map<String, Object> r : rows)
        {
            total += ((Number) r.get("cnt")).longValue();
        }

        List<OrderStatusItem> legend = new ArrayList<>();
        for (Map<String, Object> r : rows)
        {
            String status = String.valueOf(r.get("status"));
            String[] meta = ORDER_STATUS_META.getOrDefault(status, new String[] { "未知", "#9ca3af" });
            long cnt = ((Number) r.get("cnt")).longValue();
            OrderStatusItem item = new OrderStatusItem();
            item.setName(meta[0]);
            item.setColor(meta[1]);
            item.setValue(cnt);
            item.setPercent(total == 0 ? 0.0 : Math.round(cnt * 1000.0 / total) / 10.0);
            legend.add(item);
        }

        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setTotal(total);
        orderStatus.setTotalLabel("订单总数");
        orderStatus.setLegend(legend);
        return orderStatus;
    }

    /**
     * 待处理事项（不含用户投诉：系统无投诉数据支撑）
     */
    private List<PendingTask> buildPendingTasks()
    {
        long pendingPosts = dashboardMapper.countPendingPosts();
        long pendingShip = dashboardMapper.countPendingShipOrders();
        long lowStock = dashboardMapper.countLowStockProducts(LOW_STOCK_THRESHOLD);

        List<PendingTask> list = new ArrayList<>();
        list.add(buildTask(1L, "待审核动态", pendingPosts, "review", "#3b82f6"));
        list.add(buildTask(2L, "待发货订单", pendingShip, "ship", "#10b981"));
        list.add(buildTask(3L, "库存预警商品", lowStock, "warning", "#f97316"));
        return list;
    }

    private PendingTask buildTask(Long id, String title, long count, String iconType, String color)
    {
        PendingTask task = new PendingTask();
        task.setId(id);
        task.setTitle(title);
        task.setCount(count);
        task.setIconType(iconType);
        task.setColor(color);
        return task;
    }

    /**
     * 最新动态
     */
    private List<LatestPost> buildLatestPosts()
    {
        List<PetPost> posts = dashboardMapper.selectLatestPosts(6);
        List<LatestPost> list = new ArrayList<>();
        for (PetPost p : posts)
        {
            LatestPost item = new LatestPost();
            item.setId(p.getId());
            item.setUser(p.getUserName() != null ? p.getUserName() : "未知用户");
            item.setAvatar(p.getUserAvatar());
            item.setContent(p.getContent() != null ? p.getContent() : "");
            item.setTime(relativeTime(p.getCreateTime()));
            if ("0".equals(p.getStatus()))
            {
                item.setStatus("待审核");
                item.setStatusType("pending");
            }
            else if ("2".equals(p.getStatus()))
            {
                item.setStatus("已拒绝");
                item.setStatusType("reject");
            }
            else
            {
                item.setStatus("已通过");
                item.setStatusType("success");
            }
            list.add(item);
        }
        return list;
    }

    /**
     * 热门商品 TOP5
     */
    private List<HotProduct> buildHotProducts()
    {
        List<com.ruoyi.system.domain.ShopProduct> products = dashboardMapper.selectHotProducts(5);
        long max = 0;
        for (com.ruoyi.system.domain.ShopProduct p : products)
        {
            long ex = p.getTotalExchange() == null ? 0 : p.getTotalExchange();
            if (ex > max)
            {
                max = ex;
            }
        }

        List<HotProduct> list = new ArrayList<>();
        int rank = 1;
        for (com.ruoyi.system.domain.ShopProduct p : products)
        {
            long ex = p.getTotalExchange() == null ? 0 : p.getTotalExchange();
            HotProduct item = new HotProduct();
            item.setRank(rank++);
            item.setName(p.getProductName());
            item.setExchanges(ex);
            item.setPercent(max == 0 ? 0.0 : Math.round(ex * 100.0 / max));
            item.setImage(firstImage(p.getProductImages()));
            list.add(item);
        }
        return list;
    }

    /**
     * 用户增长趋势
     */
    private DashboardOverviewVO.UserGrowth buildUserGrowth(List<String> dateAxis, List<Long> userDaily)
    {
        DashboardOverviewVO.UserGrowth growth = new DashboardOverviewVO.UserGrowth();
        growth.setDates(dateAxis);
        growth.setData(userDaily);
        return growth;
    }

    /* ---------------------- 工具方法 ---------------------- */

    /**
     * 生成近 days 天日期轴（MM-dd，含今天）
     */
    private List<String> buildDateAxis(int days)
    {
        List<String> axis = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        for (int i = days - 1; i >= 0; i--)
        {
            axis.add(today.minusDays(i).format(fmt));
        }
        return axis;
    }

    /**
     * 将数据库按天统计结果补全到连续日期轴（缺失天补 0）
     */
    private List<Long> fillDaily(List<Map<String, Object>> rows, List<String> dateAxis)
    {
        Map<String, Long> map = new HashMap<>();
        if (rows != null)
        {
            for (Map<String, Object> r : rows)
            {
                String day = (String) r.get("day");
                Long cnt = ((Number) r.get("cnt")).longValue();
                map.put(day, cnt);
            }
        }
        List<Long> out = new ArrayList<>();
        for (String d : dateAxis)
        {
            out.add(map.getOrDefault(d, 0L));
        }
        return out;
    }

    /**
     * 计算较昨日增长率（百分比，保留一位小数）
     */
    private double calcGrowth(List<Long> daily)
    {
        if (daily == null || daily.size() < 2)
        {
            return 0.0;
        }
        long today = daily.get(daily.size() - 1);
        long yesterday = daily.get(daily.size() - 2);
        if (yesterday == 0)
        {
            return today > 0 ? 100.0 : 0.0;
        }
        return Math.round((today - yesterday) * 1000.0 / yesterday) / 10.0;
    }

    /**
     * 相对时间格式化
     */
    private String relativeTime(Date createTime)
    {
        if (createTime == null)
        {
            return "";
        }
        long diff = System.currentTimeMillis() - createTime.getTime();
        long minutes = diff / 60000;
        if (minutes < 1)
        {
            return "刚刚";
        }
        if (minutes < 60)
        {
            return minutes + "分钟前";
        }
        long hours = minutes / 60;
        if (hours < 24)
        {
            return hours + "小时前";
        }
        long days = hours / 24;
        if (days < 30)
        {
            return days + "天前";
        }
        long months = days / 30;
        if (months < 12)
        {
            return months + "个月前";
        }
        return (days / 365) + "年前";
    }

    /**
    /**
     * 取商品主图（第一张）
     *
     * 数据库中 product_images 存在两种格式：JSON 数组 ["a.png","b.png"] 或逗号分隔 a.png,b.png，
     * 这里兼容两种写法，避免首页商品图渲染成带方括号/引号的脏数据。
     *
     * @param images 商品图原始值
     * @return 第一张图片地址，无图时返回空串
     */
    private String firstImage(String images)
    {
        if (images == null || images.trim().isEmpty())
        {
            return "";
        }
        String raw = images.trim();
        // JSON 数组格式：去掉中括号与引号后再按逗号切分
        if (raw.startsWith("["))
        {
            raw = raw.replace("[", "").replace("]", "").replace("\"", "").replace("'", "");
        }
        String[] arr = raw.split(",");
        return arr.length > 0 ? arr[0].trim() : "";
    }
}
