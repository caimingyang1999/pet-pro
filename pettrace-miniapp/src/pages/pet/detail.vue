<template>
  <view class="page-container" v-if="pet">
    <!-- 宠物信息头部 -->
    <view class="header-card">
      <image
        class="header-avatar"
        :src="fullImageUrl(pet.avatar) || '/static/default-pet.png'"
        mode="aspectFill"
      />
      <view class="header-info">
        <view class="name-row">
          <text class="name">{{ pet.name }}</text>
          <text class="gender-icon">{{ pet.gender === '1' ? '♂' : '♀' }}</text>
        </view>
        <text class="breed">{{ pet.breed || '未知品种' }}</text>
        <view class="header-tags">
          <text v-if="pet.weight" class="tag">⚖ {{ pet.weight }}kg</text>
          <text v-if="pet.color" class="tag">{{ pet.color }}</text>
          <text v-if="pet.sterilization === '1'" class="tag sterilized">已绝育</text>
          <text v-if="pet.sterilization === '0'" class="tag not-sterilized">未绝育</text>
        </view>
      </view>
    </view>

    <!-- 详细信息 -->
    <view class="info-card">
      <view class="section-title">基本信息</view>
      <view class="info-grid">
        <view class="info-item">
          <text class="info-label">品种</text>
          <text class="info-value">{{ pet.breed || '未记录' }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">性别</text>
          <text class="info-value">{{ pet.gender === '1' ? '公 ♂' : '母 ♀' }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">毛色</text>
          <text class="info-value">{{ pet.color || '未记录' }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">体重</text>
          <text class="info-value">{{ pet.weight ? pet.weight + ' kg' : '未记录' }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">出生日期</text>
          <text class="info-value">{{ pet.birthday || '未记录' }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">绝育状态</text>
          <text class="info-value">{{ pet.sterilization === '1' ? '已绝育' : '未绝育' }}</text>
        </view>
      </view>
    </view>

    <!-- 疫苗记录 -->
    <view class="info-card" v-if="pet.vaccineList && pet.vaccineList.length">
      <view class="section-title">
        疫苗记录
        <text class="vaccine-count">共 {{ pet.vaccineList.length }} 条</text>
      </view>
      <view class="vaccine-list">
        <view class="vaccine-item" v-for="v in pet.vaccineList" :key="v.id">
          <view class="vaccine-dot" />
          <view class="vaccine-content">
            <text class="vaccine-name">{{ v.vaccineName }}</text>
            <view class="vaccine-dates">
              <text class="vaccine-date">
                接种：{{ v.inoculationDate || '-' }}
              </text>
              <text class="vaccine-next" v-if="v.nextDate">
                下次：{{ v.nextDate }}
              </text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 备注 -->
    <view class="info-card" v-if="pet.remark">
      <view class="section-title">备注</view>
      <text class="remark-text">{{ pet.remark }}</text>
    </view>

    <!-- 操作按钮 -->
    <view class="action-bar">
      <u-button type="primary" shape="circle" text="编辑信息" @click="goEdit" customStyle="height: 88rpx; font-size: 30rpx;" />
    </view>

    <!-- 底部安全区 -->
    <view class="safe-bottom" />
  </view>

  <!-- 加载中 -->
  <view class="loading-wrap" v-else-if="loading">
    <u-loading-icon />
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { getPetDetail } from '@/api/pet.js';
import { fullImageUrl } from '@/utils/index.js';

const pet = ref(null);
const petId = ref('');
const loading = ref(false);

/** 获取宠物详情 */
const fetchDetail = async (id) => {
  loading.value = true;
  try {
    const res = await getPetDetail(id);
    pet.value = res.data;
  } catch (err) {
    // request.js 已统一处理 toast
  } finally {
    loading.value = false;
  }
};

/** 跳转编辑 */
const goEdit = () => {
  uni.navigateTo({
    url: `/pages/pet/edit?id=${petId.value}`,
  });
};

onMounted(() => {
  const pages = getCurrentPages();
  const current = pages[pages.length - 1];
  const { id } = current.options || current.$route?.query || {};
  if (id) {
    petId.value = id;
    fetchDetail(id);
  }
});
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background-color: $pet-bg;
  padding: 20rpx 30rpx;
}

/* ========== 头部卡片 ========== */
.header-card {
  @include pet-card;
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;

  .header-avatar {
    width: 140rpx;
    height: 140rpx;
    border-radius: 24rpx;
    margin-right: 28rpx;
    background-color: $pet-bg;
  }

  .header-info {
    flex: 1;

    .name-row {
      display: flex;
      align-items: center;
      margin-bottom: 6rpx;

      .name {
        font-size: 38rpx;
        font-weight: 700;
        color: $pet-text-main;
        margin-right: 12rpx;
      }

      .gender-icon {
        font-size: 28rpx;
        font-weight: bold;
        color: $pet-primary;
      }
    }

    .breed {
      font-size: 28rpx;
      color: $pet-text-secondary;
      display: block;
      margin-bottom: 14rpx;
    }

    .header-tags {
      display: flex;
      flex-wrap: wrap;
      gap: 12rpx;

      .tag {
        font-size: 22rpx;
        padding: 4rpx 14rpx;
        border-radius: 8rpx;
        background-color: $pet-bg;
        color: $pet-text-regular;

        &.sterilized {
          background-color: #E8F5E9;
          color: #4CAF50;
        }

        &.not-sterilized {
          background-color: #FFF3E0;
          color: #FF9800;
        }
      }
    }
  }
}

/* ========== 信息卡片 ========== */
.info-card {
  @include pet-card;
  margin-bottom: 20rpx;

  .section-title {
    font-size: 30rpx;
    font-weight: 600;
    color: $pet-text-main;
    margin-bottom: 20rpx;
    display: flex;
    align-items: center;

    .vaccine-count {
      font-size: 24rpx;
      font-weight: 400;
      color: $pet-text-secondary;
      margin-left: 16rpx;
    }
  }
}

.info-grid {
  display: flex;
  flex-wrap: wrap;

  .info-item {
    width: 50%;
    padding: 16rpx 0;

    .info-label {
      display: block;
      font-size: 24rpx;
      color: $pet-text-secondary;
      margin-bottom: 6rpx;
    }

    .info-value {
      font-size: 28rpx;
      color: $pet-text-main;
      font-weight: 500;
    }
  }
}

/* ========== 疫苗列表 ========== */
.vaccine-list {
  .vaccine-item {
    display: flex;
    padding: 16rpx 0;

    &:not(:last-child) {
      border-bottom: 1rpx solid $pet-border-lighter;
    }

    .vaccine-dot {
      width: 12rpx;
      height: 12rpx;
      border-radius: 50%;
      background-color: $pet-primary;
      margin-top: 10rpx;
      margin-right: 16rpx;
      flex-shrink: 0;
    }

    .vaccine-content {
      flex: 1;

      .vaccine-name {
        font-size: 28rpx;
        color: $pet-text-main;
        font-weight: 500;
        display: block;
        margin-bottom: 6rpx;
      }

      .vaccine-dates {
        display: flex;
        gap: 24rpx;

        .vaccine-date,
        .vaccine-next {
          font-size: 24rpx;
          color: $pet-text-secondary;
        }

        .vaccine-next {
          color: $pet-primary;
        }
      }
    }
  }
}

/* ========== 备注 ========== */
.remark-text {
  font-size: 28rpx;
  color: $pet-text-regular;
  line-height: 1.6;
}

/* ========== 操作按钮 ========== */
.action-bar {
  margin-top: 30rpx;
  padding: 0 10rpx;
}

/* ========== 底部安全区 ========== */
.safe-bottom {
  height: 40rpx;
}

/* ========== 加载中 ========== */
.loading-wrap {
  display: flex;
  justify-content: center;
  padding-top: 200rpx;
}
</style>
