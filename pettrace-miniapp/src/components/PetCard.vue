<template>
  <view class="pet-card" @click="handleClick">
    <view class="pet-avatar-wrap">
      <image
        class="pet-avatar"
        :src="fullImageUrl(pet.avatar) || '/static/default-pet.png'"
        mode="aspectFill"
      />
      <!-- 性别角标 -->
      <view class="gender-badge" :class="pet.gender === '1' ? 'male' : 'female'">
        <text>{{ pet.gender === '1' ? '♂' : '♀' }}</text>
      </view>
    </view>
    <view class="pet-info">
      <view class="name-row">
        <text class="name">{{ pet.name }}</text>
        <text v-if="pet.sterilization === '1'" class="sterilization-tag">已绝育</text>
      </view>
      <text class="breed">{{ pet.breed || '未知品种' }}</text>
      <view class="detail-row">
        <text v-if="pet.weight" class="detail-item">⚖ {{ pet.weight }}kg</text>
        <text v-if="pet.color" class="detail-item">{{ pet.color }}</text>
        <text v-if="pet.birthday" class="detail-item">{{ formatBirthday(pet.birthday) }}</text>
      </view>
      <!-- 疫苗提示 -->
      <view v-if="pet.vaccineList && pet.vaccineList.length" class="vaccine-hint">
        <text class="vaccine-icon">💉</text>
        <text class="vaccine-text">{{ pet.vaccineList.length }} 条疫苗记录</text>
      </view>
    </view>
    <view class="arrow-icon">
      <u-icon name="arrow-right" color="#C0C4CC" size="16" />
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue';
import { fullImageUrl } from '@/utils/index.js';

const props = defineProps({
  pet: {
    type: Object,
    default: () => ({}),
  },
});

const emit = defineEmits(['click']);

const handleClick = () => {
  emit('click', props.pet.id);
};

/** 格式化生日：显示年龄或日期 */
const formatBirthday = (birthday) => {
  if (!birthday) return '';
  const birth = new Date(birthday);
  if (isNaN(birth.getTime())) return birthday;
  const now = new Date();
  let ageYears = now.getFullYear() - birth.getFullYear();
  const monthDiff = now.getMonth() - birth.getMonth();
  if (monthDiff < 0 || (monthDiff === 0 && now.getDate() < birth.getDate())) {
    ageYears--;
  }
  if (ageYears > 0) return ageYears + '岁';
  // 小于 1 岁，按月计算
  let ageMonths = (now.getFullYear() - birth.getFullYear()) * 12 + (now.getMonth() - birth.getMonth());
  if (now.getDate() < birth.getDate()) ageMonths--;
  return ageMonths <= 0 ? '刚出生' : ageMonths + '个月';
};
</script>

<style lang="scss" scoped>
.pet-card {
  @include pet-card;
  display: flex;
  align-items: center;
  position: relative;
  margin-bottom: 20rpx;

  .pet-avatar-wrap {
    position: relative;
    margin-right: 24rpx;

    .pet-avatar {
      width: 130rpx;
      height: 130rpx;
      border-radius: 20rpx;
      background-color: $pet-bg;
    }

    .gender-badge {
      position: absolute;
      bottom: -4rpx;
      right: -4rpx;
      width: 36rpx;
      height: 36rpx;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 22rpx;
      font-weight: bold;
      color: #fff;

      &.male {
        background-color: #4A90D9;
      }
      &.female {
        background-color: #E8738A;
      }
    }
  }

  .pet-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-width: 0;

    .name-row {
      display: flex;
      align-items: center;
      margin-bottom: 6rpx;

      .name {
        font-size: 32rpx;
        color: $pet-text-main;
        font-weight: 700;
        margin-right: 12rpx;
      }

      .sterilization-tag {
        font-size: 20rpx;
        color: $pet-primary;
        background-color: $pet-primary-light;
        padding: 2rpx 10rpx;
        border-radius: 6rpx;
      }
    }

    .breed {
      font-size: 26rpx;
      color: $pet-text-secondary;
      margin-bottom: 10rpx;
      @include pet-ellipsis;
    }

    .detail-row {
      display: flex;
      flex-wrap: wrap;
      gap: 16rpx;
      margin-bottom: 8rpx;

      .detail-item {
        font-size: 22rpx;
        color: $pet-text-secondary;
        background-color: $pet-bg;
        padding: 2rpx 12rpx;
        border-radius: 6rpx;
      }
    }

    .vaccine-hint {
      display: flex;
      align-items: center;

      .vaccine-icon {
        font-size: 22rpx;
        margin-right: 6rpx;
      }

      .vaccine-text {
        font-size: 22rpx;
        color: $pet-success;
      }
    }
  }

  .arrow-icon {
    margin-left: 12rpx;
    flex-shrink: 0;
  }
}
</style>
