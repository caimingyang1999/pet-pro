<template>
  <el-row :gutter="20" class="stats-cards">
    <el-col
      v-for="item in stats"
      :key="item.key"
      :xs="24"
      :sm="12"
      :lg="6"
      class="stat-col"
    >
      <div class="stat-card">
        <div class="stat-icon-wrap" :style="{ background: item.bgGradient }">
          <component :is="getIcon(item.iconType)" :style="{ color: item.color }" />
        </div>
        <div class="stat-info">
          <div class="stat-title">{{ item.title }}</div>
          <div class="stat-value">
            <count-to :start-val="0" :end-val="item.value" :duration="1500" :decimals="0" />
          </div>
          <div class="stat-growth">
            <span class="growth-label">较昨日</span>
            <span :class="item.growth >= 0 ? 'growth-up' : 'growth-down'">
              <i :class="item.growth >= 0 ? 'el-icon-top' : 'el-icon-bottom'"></i> {{ Math.abs(item.growth) }}%
            </span>
          </div>
        </div>
      </div>
    </el-col>
  </el-row>
</template>

<script>
import CountTo from 'vue-count-to'

export default {
  name: 'StatsCards',
  components: { CountTo },
  props: {
    // 统计卡片数据，由父级统一拉取后下发
    stats: {
      type: Array,
      default: () => []
    }
  },
  methods: {
    getIcon(type) {
      const iconMap = {
        order: { render: (h) => h('i', { class: 'el-icon-s-order', style: 'font-size:24px' }) },
        product: { render: (h) => h('i', { class: 'el-icon-goods', style: 'font-size:24px' }) },
        activity: { render: (h) => h('i', { class: 'el-icon-chat-dot-round', style: 'font-size:24px' }) },
        pet: { render: (h) => h('i', { class: 'el-icon-s-custom', style: 'font-size:24px' }) }
      }
      return iconMap[type] || iconMap.order
    }
  }
}
</script>

<style lang="scss" scoped>
.stats-cards {
  margin-bottom: 20px;

  .stat-col {
    display: flex;
    flex-direction: column;
    margin-bottom: 0;
  }

  .stat-card {
    display: flex;
    align-items: center;
    padding: 24px;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
    transition: box-shadow 0.3s;
    height: 100%;

    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    }

    .stat-icon-wrap {
      width: 56px;
      height: 56px;
      border-radius: 14px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 18px;
      flex-shrink: 0;
    }

    .stat-info {
      flex: 1;
      min-width: 0;

      .stat-title {
        font-size: 13px;
        color: #6b7280;
        margin-bottom: 6px;
      }

      .stat-value {
        font-size: 26px;
        font-weight: 700;
        color: #1f2937;
        line-height: 1.2;
        margin-bottom: 6px;
      }

      .stat-growth {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 12px;

        .growth-label {
          color: #9ca3af;
        }

        .growth-up {
          color: #10b981;
          font-weight: 500;
          display: flex;
          align-items: center;

          i {
            font-size: 12px;
            margin-right: 2px;
          }
        }

        .growth-down {
          color: #ef4444;
          font-weight: 500;
          display: flex;
          align-items: center;

          i {
            font-size: 12px;
            margin-right: 2px;
          }
        }
      }
    }
  }
}
</style>
