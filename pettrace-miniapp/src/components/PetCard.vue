<template>
  <view class="pet-card" :class="petClass" @click="handleClick">
    <!-- 头像 + 类型角标 -->
    <view class="avatar-wrap">
      <image
        class="pet-avatar"
        :src="avatarSrc"
        mode="aspectFill"
      />
      <view class="type-badge">
        <text>{{ petEmoji }}</text>
      </view>
    </view>

    <!-- 信息 -->
    <view class="pet-info">
      <view class="name-row">
        <text class="name">{{ pet.name }}</text>
        <text v-if="pet.sterilization === '1'" class="sterilization-tag">已绝育</text>
      </view>
      <view class="breed-row">
        <text class="breed">{{ pet.breed || '未知品种' }}</text>
        <text class="gender-text" :class="pet.gender === '1' ? 'male' : 'female'">
          {{ pet.gender === '1' ? '♂ 弟弟' : '♀ 妹妹' }}
        </text>
      </view>
      <view class="detail-row">
        <view v-if="pet.weight" class="detail-item">
          <Icon class="detail-emoji" name="weight" :size="12" color="#8A8D9A" />
          <text>{{ pet.weight }}kg</text>
        </view>
        <text v-if="pet.color" class="detail-item color-chip">{{ pet.color }}</text>
        <view v-if="ageText" class="detail-item">
          <Icon class="detail-emoji" name="cake" :size="12" color="#8A8D9A" />
          <text>{{ ageText }}</text>
        </view>
      </view>
      <view v-if="pet.vaccineList && pet.vaccineList.length" class="vaccine-hint">
        <Icon class="vaccine-emoji" name="vaccine" :size="12" color="#4CAF7D" />
        <text class="vaccine-text">{{ pet.vaccineList.length }} 条疫苗记录</text>
      </view>
    </view>

    <view class="arrow">
      <Icon name="chevron_right" :size="20" color="#B0B2BE" />
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue';
import { fullImageUrl } from '@/utils/index.js';
import Icon from '@/components/Icon.vue';

const props = defineProps({
  pet: {
    type: Object,
    default: () => ({}),
  },
});

const emit = defineEmits(['click']);

const avatarSrc = computed(() => fullImageUrl(props.pet.avatar) || '/static/default-pet.png');

const petEmoji = computed(() => {
  const text = `${props.pet.breed || ''}${props.pet.name || ''}`;
  if (/猫|布偶|英短|美短|暹罗|橘/.test(text)) return '🐱';
  if (/狗|犬|柯基|金毛|泰迪|拉布拉多|边牧|柴犬/.test(text)) return '🐶';
  if (/兔/.test(text)) return '🐰';
  if (/仓鼠|鼠/.test(text)) return '🐹';
  return '🐾';
});

const petClass = computed(() => {
  const text = `${props.pet.breed || ''}${props.pet.name || ''}`;
  if (/猫|布偶|英短|美短|暹罗|橘/.test(text)) return 'card-cat';
  if (/狗|犬|柯基|金毛|泰迪|拉布拉多|边牧|柴犬/.test(text)) return 'card-dog';
  return 'card-other';
});

const ageText = computed(() => formatBirthday(props.pet.birthday));

/** 格式化生日：显示年龄 */
const formatBirthday = (birthday) => {
  if (!birthday) return '';
  const birth = new Date(birthday);
  if (isNaN(birth.getTime())) return '';
  const now = new Date();
  let ageYears = now.getFullYear() - birth.getFullYear();
  const monthDiff = now.getMonth() - birth.getMonth();
  if (monthDiff < 0 || (monthDiff === 0 && now.getDate() < birth.getDate())) {
    ageYears--;
  }
  if (ageYears > 0) return ageYears + '岁';
  let ageMonths = (now.getFullYear() - birth.getFullYear()) * 12 + (now.getMonth() - birth.getMonth());
  if (now.getDate() < birth.getDate()) ageMonths--;
  return ageMonths <= 0 ? '刚出生' : ageMonths + '个月';
};

const handleClick = () => {
  emit('click', props.pet.id);
};
</script>

<style lang="scss" scoped>
.pet-card {
  position: relative;
  display: flex;
  align-items: center;
  padding: 28rpx 24rpx;
  border-radius: $radius-lg;
  box-shadow: $shadow-card;
  margin-bottom: 24rpx;
  animation: pet-slideIn 0.42s ease both;
  transition: transform 0.3s ease;

  &:active {
    transform: scale(0.975);
  }

  &.card-cat {
    background: linear-gradient(135deg, #FFF3E9 0%, #FFE3E9 100%);
  }

  &.card-dog {
    background: linear-gradient(135deg, #E9F3FF 0%, #ECE7FF 100%);
  }

  &.card-other {
    background: linear-gradient(135deg, #EBF7EC 0%, #FFF7DD 100%);
  }

  .avatar-wrap {
    position: relative;
    flex-shrink: 0;
    margin-right: 24rpx;

    .pet-avatar {
      width: 120rpx;
      height: 120rpx;
      border-radius: 50%;
      background: #fff;
      border: 4rpx solid rgba(255, 255, 255, 0.92);
      box-shadow: $shadow-sm;
    }

    .type-badge {
      position: absolute;
      right: -8rpx;
      bottom: -4rpx;
      width: 48rpx;
      height: 48rpx;
      border-radius: 50%;
      background: #fff;
      border: 2rpx solid #fff;
      box-shadow: $shadow-sm;
      display: flex;
      align-items: center;
      justify-content: center;

      text {
        font-size: 28rpx;
        line-height: 1;
      }
    }
  }

  .pet-info {
    flex: 1;
    min-width: 0;

    .name-row {
      display: flex;
      align-items: center;

      .name {
        font-size: $font-xl;
        color: $text-primary;
        font-weight: $font-weight-bold;
        @include pet-ellipsis;
        max-width: 210rpx;
      }

      .sterilization-tag {
        margin-left: 10rpx;
        font-size: $font-xs;
        color: $accent-green;
        background: rgba(255, 255, 255, 0.78);
        padding: 3rpx 14rpx;
        border-radius: $radius-round;
        flex-shrink: 0;
      }
    }

    .breed-row {
      display: flex;
      align-items: center;
      gap: 10rpx;
      margin-top: 10rpx;

      .breed {
        font-size: $font-xs;
        color: $primary-dark;
        background: rgba(255, 255, 255, 0.78);
        padding: 4rpx 16rpx;
        border-radius: $radius-round;
        @include pet-ellipsis;
        max-width: 190rpx;
      }

      .gender-text {
        font-size: $font-xs;
        font-weight: $font-weight-medium;

        &.male { color: $accent-blue; }
        &.female { color: $accent-pink; }
      }
    }

    .detail-row {
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      gap: 16rpx;
      margin-top: 10rpx;

      .detail-item {
        display: inline-flex;
        align-items: center;
        gap: 6rpx;
        font-size: $font-xs;
        color: $text-secondary;

        .detail-emoji {
          flex-shrink: 0;
        }
      }

      .color-chip {
        padding: 2rpx 14rpx;
        background: rgba(255, 255, 255, 0.72);
        border-radius: $radius-round;
      }
    }

    .vaccine-hint {
      display: flex;
      align-items: center;
      margin-top: 10rpx;

      .vaccine-emoji {
        font-size: 20rpx;
        margin-right: 6rpx;
      }

      .vaccine-text {
        font-size: $font-xs;
        color: $accent-green;
        font-weight: $font-weight-medium;
      }
    }
  }

  .arrow {
    margin-left: 10rpx;
    flex-shrink: 0;
  }
}
</style>
