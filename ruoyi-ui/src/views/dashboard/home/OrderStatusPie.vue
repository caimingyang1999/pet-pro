<template>
  <div class="order-status-card">
    <div class="card-header">
      <h3 class="card-title">订单状态分布</h3>
    </div>
    <div class="chart-area">
      <div class="chart-wrap" ref="chartRef"></div>
      <div class="center-info">
        <div class="center-value">{{ orderData.total.toLocaleString() }}</div>
        <div class="center-label">{{ orderData.totalLabel }}</div>
      </div>
    </div>
    <div class="legend-list">
      <div
        v-for="item in orderData.legend"
        :key="item.name"
        class="legend-item"
      >
        <div class="legend-left">
          <span class="legend-dot" :style="{ background: item.color }"></span>
          <span class="legend-name">{{ item.name }}</span>
        </div>
        <div class="legend-right">
          <span class="legend-value">{{ item.value }}</span>
          <span class="legend-percent">({{ item.percent }}%)</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import resize from '../mixins/resize'
import { orderStatusData } from './mock'

export default {
  name: 'OrderStatusPie',
  mixins: [resize],
  data() {
    return {
      chart: null,
      orderData: orderStatusData
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
      const data = this.orderData.legend.map(item => ({
        name: item.name,
        value: item.value,
        itemStyle: { color: item.color }
      }))

      this.chart.setOption({
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} ({d}%)'
        },
        series: [{
          type: 'pie',
          radius: ['58%', '78%'],
          center: ['50%', '50%'],
          avoidLabelOverlap: false,
          label: { show: false },
          labelLine: { show: false },
          emphasis: {
            scale: true,
            scaleSize: 6,
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.2)'
            }
          },
          itemStyle: {
            borderColor: '#fff',
            borderWidth: 3,
            borderRadius: 4
          },
          data
        }]
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.order-status-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  height: 100%;

  .card-header {
    margin-bottom: 16px;

    .card-title {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
      color: #1f2937;
    }
  }

  .chart-area {
    position: relative;
    height: 200px;
    display: flex;
    align-items: center;
    justify-content: center;

    .chart-wrap {
      width: 200px;
      height: 200px;
    }

    .center-info {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      text-align: center;
      pointer-events: none;

      .center-value {
        font-size: 24px;
        font-weight: 700;
        color: #1f2937;
        line-height: 1.2;
      }

      .center-label {
        font-size: 12px;
        color: #9ca3af;
        margin-top: 4px;
      }
    }
  }

  .legend-list {
    margin-top: 12px;

    .legend-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8px 0;
      border-bottom: 1px solid #f3f4f6;

      &:last-child {
        border-bottom: none;
      }

      .legend-left {
        display: flex;
        align-items: center;
        gap: 8px;

        .legend-dot {
          width: 10px;
          height: 10px;
          border-radius: 3px;
        }

        .legend-name {
          font-size: 13px;
          color: #374151;
        }
      }

      .legend-right {
        font-size: 12px;

        .legend-value {
          color: #1f2937;
          font-weight: 600;
        }

        .legend-percent {
          color: #9ca3af;
          margin-left: 4px;
        }
      }
    }
  }
}
</style>
