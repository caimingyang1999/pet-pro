<template>
  <div class="latest-activities-card">
    <div class="card-header">
      <h3 class="card-title">最新动态</h3>
      <a class="more-link" @click.prevent="handleMore">查看更多 <i class="el-icon-arrow-right"></i></a>
    </div>
    <div class="activity-list">
      <div
        v-for="item in activitiesData"
        :key="item.id"
        class="activity-item"
      >
        <div class="avatar" :style="{ background: getAvatarBg(item.user) }">
          {{ item.user.charAt(0) }}
        </div>
        <div class="activity-content">
          <div class="content-top">
            <span class="username">{{ item.user }}</span>
            <el-tag
              :type="getStatusTagType(item.statusType)"
              size="mini"
              effect="plain"
              class="status-tag"
            >
              {{ item.status }}
            </el-tag>
          </div>
          <p class="content-text">{{ item.content }}</p>
          <span class="content-time">{{ item.time }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { latestActivitiesData } from './mock'

export default {
  name: 'LatestActivities',
  data() {
    return {
      activitiesData: latestActivitiesData,
      avatarColors: ['#3b82f6', '#10b981', '#8b5cf6', '#f97316', '#ef4444', '#ec4899']
    }
  },
  methods: {
    getAvatarBg(name) {
      let hash = 0
      for (let i = 0; i < name.length; i++) {
        hash = name.charCodeAt(i) + ((hash << 5) - hash)
      }
      const index = Math.abs(hash) % this.avatarColors.length
      return this.avatarColors[index]
    },
    getStatusTagType(type) {
      const map = {
        pending: 'warning',
        success: 'success',
        reject: 'danger'
      }
      return map[type] || 'info'
    },
    handleMore() {
      this.$message.info('跳转到动态列表页面')
    }
  }
}
</script>

<style lang="scss" scoped>
.latest-activities-card {
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

    .more-link {
      font-size: 13px;
      color: #3b82f6;
      cursor: pointer;

      i {
        font-size: 12px;
        margin-left: 2px;
      }

      &:hover {
        text-decoration: underline;
      }
    }
  }

  .activity-list {
    flex: 1;
    overflow-y: auto;

    .activity-item {
      display: flex;
      padding: 12px 0;
      border-bottom: 1px solid #f3f4f6;

      &:last-child {
        border-bottom: none;
      }

      .avatar {
        width: 40px;
        height: 40px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
        font-size: 16px;
        font-weight: 600;
        margin-right: 12px;
        flex-shrink: 0;
      }

      .activity-content {
        flex: 1;
        min-width: 0;

        .content-top {
          display: flex;
          align-items: center;
          justify-content: space-between;
          margin-bottom: 4px;

          .username {
            font-size: 14px;
            font-weight: 500;
            color: #1f2937;
          }

          .status-tag {
            font-size: 11px;
            padding: 0 8px;
            height: 20px;
            line-height: 18px;
          }
        }

        .content-text {
          margin: 0 0 4px;
          font-size: 13px;
          color: #6b7280;
          line-height: 1.5;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .content-time {
          font-size: 12px;
          color: #9ca3af;
        }
      }
    }
  }
}
</style>
