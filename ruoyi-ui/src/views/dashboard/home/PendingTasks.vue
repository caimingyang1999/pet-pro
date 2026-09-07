<template>
  <div class="pending-tasks-card">
    <div class="card-header">
      <h3 class="card-title">待处理事项</h3>
    </div>
    <div class="task-list">
      <div
        v-for="item in tasksData"
        :key="item.id"
        class="task-item"
        @click="handleClick(item)"
      >
        <div class="task-icon" :style="{ background: getIconBg(item.color) }">
          <i :class="getIcon(item.iconType)" :style="{ color: item.color }"></i>
        </div>
        <div class="task-info">
          <span class="task-title">{{ item.title }}</span>
        </div>
        <div class="task-count" :style="{ color: item.color }">
          {{ item.count }}
        </div>
        <i class="el-icon-arrow-right task-arrow"></i>
      </div>
    </div>
  </div>
</template>

<script>
import { pendingTasksData } from './mock'

export default {
  name: 'PendingTasks',
  data() {
    return {
      tasksData: pendingTasksData
    }
  },
  methods: {
    getIcon(type) {
      const iconMap = {
        review: 'el-icon-view',
        ship: 'el-icon-s-promotion',
        warning: 'el-icon-warning-outline',
        complaint: 'el-icon-warning'
      }
      return iconMap[type] || 'el-icon-bell'
    },
    getIconBg(color) {
      const bgMap = {
        '#3b82f6': '#eff6ff',
        '#10b981': '#ecfdf5',
        '#f97316': '#fff7ed',
        '#ef4444': '#fef2f2'
      }
      return bgMap[color] || '#f3f4f6'
    },
    handleClick(item) {
      this.$message.info(`跳转到「${item.title}」页面`)
    }
  }
}
</script>

<style lang="scss" scoped>
.pending-tasks-card {
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

  .task-list {
    .task-item {
      display: flex;
      align-items: center;
      padding: 14px 12px;
      border-radius: 10px;
      cursor: pointer;
      transition: background 0.2s;

      &:hover {
        background: #f9fafb;
      }

      &:hover .task-arrow {
        opacity: 1;
        transform: translateX(0);
      }

      .task-icon {
        width: 40px;
        height: 40px;
        border-radius: 10px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 14px;
        flex-shrink: 0;

        i {
          font-size: 18px;
        }
      }

      .task-info {
        flex: 1;

        .task-title {
          font-size: 14px;
          color: #374151;
        }
      }

      .task-count {
        font-size: 18px;
        font-weight: 700;
        margin-right: 8px;
      }

      .task-arrow {
        font-size: 14px;
        color: #d1d5db;
        opacity: 0.5;
        transition: all 0.2s;
        transform: translateX(-3px);
      }
    }
  }
}
</style>
