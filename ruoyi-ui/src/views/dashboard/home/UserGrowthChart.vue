<template>
  <div class="user-growth-card">
    <div class="card-header">
      <h3 class="card-title">用户增长趋势</h3>
      <el-select v-model="range" size="mini" class="range-select">
        <el-option label="近7天" value="7" />
        <el-option label="近14天" value="14" />
        <el-option label="近30天" value="30" />
      </el-select>
    </div>
    <div class="chart-body" ref="chartRef"></div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import resize from '../mixins/resize'

export default {
  name: 'UserGrowthChart',
  mixins: [resize],
  props: {
    // 用户增长数据，由父级统一拉取后下发
    userGrowth: {
      type: Object,
      default: () => ({ dates: [], data: [] })
    },
    // 当前统计天数（7/14/30），用于同步时间范围下拉
    days: {
      type: Number,
      default: 7
    }
  },
  data() {
    return {
      chart: null,
      range: String(this.days)
    }
  },
  watch: {
    // 父级数据到位后重绘
    userGrowth() {
      this.renderChart()
    },
    // 同步父级天数变化，避免下拉与实际数据窗口不一致
    days(val) {
      if (String(val) !== this.range) {
        this.range = String(val)
      }
    },
    range() {
      this.$emit('range-change', Number(this.range))
    }
  },
  mounted() {
    this.$nextTick(() => {
      this.initChart()
    })
  },
  beforeDestroy() {
    if (this.chart) {
      this.chart.dispose()
      this.chart = null
    }
  },
  methods: {
    initChart() {
      this.chart = echarts.init(this.$refs.chartRef)
      this.renderChart()
    },
    renderChart() {
      // 数据可能早于图表初始化到达，此时跳过绘制
      if (!this.chart) {
        return
      }
      const growth = this.userGrowth || { dates: [], data: [] }
      const dates = growth.dates || []
      const data = growth.data || []

      this.chart.setOption({
        tooltip: {
          trigger: 'axis',
          backgroundColor: 'rgba(255,255,255,0.95)',
          borderColor: '#e5e7eb',
          borderWidth: 1,
          textStyle: { color: '#374151', fontSize: 12 },
          formatter: (params) => {
            const p = params[0]
            return `<div style="font-weight:500;margin-bottom:4px">${p.axisValue}</div>新增用户：<b>${p.value}</b>`
          }
        },
        grid: {
          left: 40,
          right: 20,
          top: 20,
          bottom: 30,
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: dates,
          axisLine: { lineStyle: { color: '#e5e7eb' } },
          axisTick: { show: false },
          axisLabel: { color: '#9ca3af', fontSize: 12 }
        },
        yAxis: {
          type: 'value',
          splitLine: { lineStyle: { color: '#f3f4f6', type: 'dashed' } },
          axisLine: { show: false },
          axisTick: { show: false },
          axisLabel: { color: '#9ca3af', fontSize: 12 }
        },
        series: [{
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 8,
          data: data,
          itemStyle: { color: '#3b82f6' },
          lineStyle: { width: 3, color: '#3b82f6' },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(59, 130, 246, 0.35)' },
              { offset: 1, color: 'rgba(59, 130, 246, 0.02)' }
            ])
          }
        }]
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.user-growth-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  height: 100%;
  display: flex;
  flex-direction: column;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    .card-title {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
      color: #1f2937;
    }

    .range-select {
      width: 100px;
    }
  }

  .chart-body {
    flex: 1;
    min-height: 240px;
  }
}
</style>
