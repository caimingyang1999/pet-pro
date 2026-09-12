<template>
  <div class="dashboard-container" v-loading="loading" element-loading-text="数据加载中...">
    <!-- 欢迎区域 -->
    <welcome-banner />

    <!-- 统计卡片 -->
    <stats-cards :stats="overview.stats" />

    <!-- 第二行：数据趋势 + 订单状态 + 待处理事项 -->
    <el-row :gutter="20" class="chart-row chart-row-2">
      <el-col :xs="24" :sm="24" :lg="12" :xl="12">
        <data-trend-chart :trend="overview.trend" :days="days" @range-change="handleDaysChange" />
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6" :xl="6">
        <order-status-pie :order-status="overview.orderStatus" />
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6" :xl="6">
        <pending-tasks :tasks="overview.pendingTasks" />
      </el-col>
    </el-row>

    <!-- 第三行：最新动态 + 热门商品 + 用户增长 -->
    <el-row :gutter="20" class="chart-row chart-row-3">
      <el-col :xs="24" :sm="24" :lg="8" :xl="8">
        <latest-activities :activities="overview.latestActivities" />
      </el-col>
      <el-col :xs="24" :sm="24" :lg="8" :xl="8">
        <hot-products :products="overview.hotProducts" />
      </el-col>
      <el-col :xs="24" :sm="24" :lg="8" :xl="8">
        <user-growth-chart :user-growth="overview.userGrowth" :days="days" @range-change="handleDaysChange" />
      </el-col>
    </el-row>
  </div>
</template>

<script>
// 异步组件懒加载 - Vue 2 写法
const WelcomeBanner = () => import('@/views/dashboard/home/WelcomeBanner.vue')
const StatsCards = () => import('@/views/dashboard/home/StatsCards.vue')
const DataTrendChart = () => import('@/views/dashboard/home/DataTrendChart.vue')
const OrderStatusPie = () => import('@/views/dashboard/home/OrderStatusPie.vue')
const PendingTasks = () => import('@/views/dashboard/home/PendingTasks.vue')
const LatestActivities = () => import('@/views/dashboard/home/LatestActivities.vue')
const HotProducts = () => import('@/views/dashboard/home/HotProducts.vue')
const UserGrowthChart = () => import('@/views/dashboard/home/UserGrowthChart.vue')

import { getDashboardOverview } from '@/api/dashboard'

export default {
  name: 'Index',
  components: {
    WelcomeBanner,
    StatsCards,
    DataTrendChart,
    OrderStatusPie,
    PendingTasks,
    LatestActivities,
    HotProducts,
    UserGrowthChart
  },
  data() {
    return {
      loading: false,
      days: 7,
      // 初始空结构，避免子组件在接口返回前渲染异常
      overview: {
        stats: [],
        trend: { dates: [], series: [] },
        orderStatus: { total: 0, totalLabel: '', legend: [] },
        pendingTasks: [],
        latestActivities: [],
        hotProducts: [],
        userGrowth: { dates: [], data: [] }
      }
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    loadData() {
      this.loading = true
      getDashboardOverview(this.days)
        .then(res => {
          if (res && res.data) {
            this.overview = res.data
          }
        })
        .catch(() => {})
        .finally(() => {
          this.loading = false
        })
    },
    // 趋势/用户增长图切换时间范围时，重新拉取整页数据
    handleDaysChange(days) {
      // 天数未变化时忽略，避免子组件同步天数触发重复请求
      if (this.days === days) return
      this.days = days
      this.loadData()
    }
  }
}
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 20px;
  background: #f3f4f6;
  min-height: calc(100vh - 84px);

  // 第二、三行：el-col 固定 450px，子卡片 height:100% 撑满对齐
  .chart-row-2 > .el-col,
  .chart-row-3 > .el-col {
    display: flex;
    flex-direction: column;
    height: 450px;
    margin-bottom: 20px;

    // 子卡片统一撑满
    > .data-trend-card,
    > .order-status-card,
    > .pending-tasks-card,
    > .latest-activities-card,
    > .hot-products-card,
    > .user-growth-card {
      height: 100%;
    }
  }
}

@media (max-width: 1024px) {
  .dashboard-container {
    padding: 12px;

    .chart-row-2 > .el-col,
    .chart-row-3 > .el-col {
      display: block;
      height: auto;
    }
  }
}
</style>
