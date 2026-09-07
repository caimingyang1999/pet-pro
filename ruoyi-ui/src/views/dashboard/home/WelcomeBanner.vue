<template>
  <div class="welcome-banner">
    <div class="welcome-left">
      <h2 class="welcome-title">
        欢迎回来，{{ nickName || name }}
        <span class="welcome-emoji">👋</span>
      </h2>
      <p class="welcome-desc">这里是宠迹宠物档案管理系统的数据总览</p>
    </div>
    <div class="welcome-right">
      <div class="current-date">
        <i class="el-icon-date"></i>
        <span>{{ currentDate }}</span>
        <span class="weekday">{{ weekday }}</span>
      </div>
      <el-button type="primary" icon="el-icon-download" class="export-btn" @click="handleExport">
        数据导出
      </el-button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'WelcomeBanner',
  computed: {
    nickName() {
      return this.$store.state.user.nickName
    },
    name() {
      return this.$store.state.user.name
    },
    currentDate() {
      const now = new Date()
      const y = now.getFullYear()
      const m = String(now.getMonth() + 1).padStart(2, '0')
      const d = String(now.getDate()).padStart(2, '0')
      return `${y}-${m}-${d}`
    },
    weekday() {
      const days = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
      return days[new Date().getDay()]
    }
  },
  methods: {
    handleExport() {
      this.$message.success('数据导出功能开发中...')
    }
  }
}
</script>

<style lang="scss" scoped>
.welcome-banner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 28px;
  background: #fff;
  border-radius: 12px;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);

  .welcome-left {
    .welcome-title {
      margin: 0 0 6px;
      font-size: 22px;
      font-weight: 600;
      color: #1f2937;
      line-height: 1.4;

      .welcome-emoji {
        margin-left: 6px;
      }
    }

    .welcome-desc {
      margin: 0;
      font-size: 14px;
      color: #6b7280;
    }
  }

  .welcome-right {
    display: flex;
    align-items: center;
    gap: 20px;

    .current-date {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 14px;
      color: #6b7280;

      i {
        color: #3b82f6;
        font-size: 16px;
      }

      .weekday {
        color: #9ca3af;
      }
    }

    .export-btn {
      border-radius: 8px;
      padding: 10px 20px;
      font-weight: 500;
    }
  }
}
</style>
