<template>
  <div class="data-trend-card">
    <div class="card-header">
      <h3 class="card-title">数据趋势</h3>
      <el-select v-model="range" size="mini" class="range-select">
        <el-option label="近7天" value="7" />
        <el-option label="近14天" value="14" />
        <el-option label="近30天" value="30" />
      </el-select>
    </div>
    <div class="chart-body" ref="chartRef"></div>
    <div class="chart-legend">
      <div
        v-for="item in trend.series"
        :key="item.name"
        class="legend-item"
        :class="{ active: activeSeries.includes(item.name) }"
        @click="toggleSeries(item.name)"
      >
        <span class="legend-dot" :style="{ background: item.color }"></span>
        <span class="legend-text">{{ item.name }}</span>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import resize from '../mixins/resize'

export default {
  name: 'DataTrendChart',
  mixins: [resize],
  props: {
    // 趋势数据，由父级统一拉取后下发
    trend: {
      type: Object,
      default: () => ({ dates: [], series: [] })
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
      range: String(this.days),
      activeSeries: []
    }
  },
  watch: {
    // 父级数据到位后刷新可选图例，进而触发重绘
    trend(val) {
      this.activeSeries = (val && val.series ? val.series : []).map(s => s.name)
    },
    // 同步父级天数变化，避免下拉与实际数据窗口不一致
    days(val) {
      if (String(val) !== this.range) {
        this.range = String(val)
      }
    },
    range() {
      this.$emit('range-change', Number(this.range))
    },
    activeSeries: {
      deep: true,
      handler() {
        this.renderChart()
      }
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
    toggleSeries(name) {
      const idx = this.activeSeries.indexOf(name)
      if (idx > -1) {
        if (this.activeSeries.length > 1) {
          this.activeSeries.splice(idx, 1)
        }
      } else {
        this.activeSeries.push(name)
      }
    },
    renderChart() {
      // 数据可能早于图表初始化到达，此时跳过绘制
      if (!this.chart) {
        return
      }
      const trend = this.trend || { dates: [], series: [] }
      const dates = trend.dates || []
      const series = (trend.series || [])
        .filter(s => this.activeSeries.includes(s.name))
        .map(s => ({
          name: s.name,
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 6,
          data: s.data,
          itemStyle: { color: s.color },
          lineStyle: { width: 2.5, color: s.color }
        }))

      this.chart.setOption({
        tooltip: {
          trigger: 'axis',
          backgroundColor: 'rgba(255,255,255,0.95)',
          borderColor: '#e5e7eb',
          borderWidth: 1,
          textStyle: { color: '#374151', fontSize: 12 },
          axisPointer: { type: 'line', lineStyle: { color: '#e5e7eb' } }
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
        series
      }, true)
    }
  }
}
</script>

<style lang="scss" scoped>
.data-trend-card {
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
    min-height: 260px;
  }

  .chart-legend {
    display: flex;
    justify-content: center;
    gap: 24px;
    margin-top: 8px;

    .legend-item {
      display: flex;
      align-items: center;
      gap: 6px;
      cursor: pointer;
      font-size: 12px;
      color: #9ca3af;
      transition: color 0.2s;

      .legend-dot {
        width: 10px;
        height: 10px;
        border-radius: 50%;
      }

      &.active {
        color: #374151;
        font-weight: 500;
      }

      &:hover {
        color: #374151;
      }
    }
  }
}
</style>
