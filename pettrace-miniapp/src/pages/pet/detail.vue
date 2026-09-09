<template>
  <view class="page-container" v-if="pet">
    <!-- ========== 顶部档案卡（渐变头像区） ========== -->
    <view class="hero-card" :class="petClass">
      <text class="hero-deco deco-1">🐾</text>
      <text class="hero-deco deco-2">🐾</text>

      <view class="avatar-wrap">
        <image
          class="hero-avatar"
          :src="fullImageUrl(pet.avatar) || '/static/default-pet.png'"
          mode="aspectFill"
        />
        <view class="type-badge">
          <text>{{ petEmoji }}</text>
        </view>
      </view>

      <view class="hero-info">
        <view class="name-line">
          <text class="name">{{ pet.name }}</text>
          <text class="gender-icon" :class="pet.gender === '1' ? 'male' : 'female'">
            {{ pet.gender === '1' ? '♂' : '♀' }}
          </text>
        </view>
        <view class="breed-chip">{{ pet.breed || '未知品种' }}</view>
        <view class="hero-tags">
          <text v-if="pet.sterilization === '1'" class="hero-tag ok">已绝育</text>
          <text v-else class="hero-tag warn">未绝育</text>
          <text v-if="ageText" class="hero-tag plain">{{ ageText }}</text>
        </view>
      </view>
    </view>

    <!-- ========== 基本信息 ========== -->
    <view class="info-card">
      <view class="section-title">
        <view class="title-icon">📋</view>
        <text>基本信息</text>
      </view>
      <view class="info-grid">
        <view class="info-item">
          <text class="info-label">品种</text>
          <text class="info-value">{{ pet.breed || '未记录' }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">性别</text>
          <text class="info-value">{{ pet.gender === '1' ? '弟弟 ♂' : '妹妹 ♀' }}</text>
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

    <!-- ========== 疫苗记录（时间线） ========== -->
    <view class="info-card" v-if="pet.vaccineList && pet.vaccineList.length">
      <view class="section-title">
        <view class="title-icon">💉</view>
        <text>疫苗记录</text>
        <text class="vaccine-count">共 {{ pet.vaccineList.length }} 针</text>
      </view>
      <view class="vaccine-timeline">
        <view
          class="vaccine-item"
          v-for="(v, idx) in pet.vaccineList"
          :key="v.id"
        >
          <view class="timeline-left">
            <view class="timeline-dot" :class="{ done: idx === 0 }">
              <text v-if="idx === 0">✓</text>
            </view>
            <view class="timeline-line" v-if="idx < pet.vaccineList.length - 1" />
          </view>
          <view class="vaccine-content">
            <view class="vaccine-head">
              <text class="vaccine-name">{{ v.vaccineName }}</text>
              <text class="vaccine-order" v-if="idx === 0">最近接种</text>
            </view>
            <view class="vaccine-dates">
              <view class="date-chip">
                <text class="date-label">接种</text>
                <text class="date-value">{{ v.inoculationDate || '-' }}</text>
              </view>
              <view class="date-chip next" v-if="v.nextDate">
                <text class="date-label">下次</text>
                <text class="date-value">{{ v.nextDate }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- ========== 备注 ========== -->
    <view class="info-card" v-if="pet.remark">
      <view class="section-title">
        <view class="title-icon">💌</view>
        <text>小备注</text>
      </view>
      <view class="remark-box">
        <text class="remark-text">{{ pet.remark }}</text>
      </view>
    </view>

    <!-- ========== 底部操作 ========== -->
    <view class="action-bar">
      <view class="edit-btn pet-press" @click="goEdit">
        <Icon name="edit" :size="18" color="#fff" />
        <text class="edit-text">编辑档案</text>
      </view>
    </view>

    <!-- 底部安全区 -->
    <view class="safe-bottom" />
  </view>

  <!-- 加载中 -->
  <view class="loading-wrap" v-else-if="loading">
    <view class="loading-paw">🐾</view>
    <text class="loading-text">正在打开档案...</text>
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { getPetDetail } from '@/api/pet.js';
import { fullImageUrl } from '@/utils/index.js';
import Icon from '@/components/Icon.vue';

const pet = ref(null);
const petId = ref('');
const loading = ref(false);

const petEmoji = computed(() => {
  const text = `${pet.value?.breed || ''}${pet.value?.name || ''}`;
  if (/猫|布偶|英短|美短|暹罗|橘/.test(text)) return '🐱';
  if (/狗|犬|柯基|金毛|泰迪|拉布拉多|边牧|柴犬/.test(text)) return '🐶';
  if (/兔/.test(text)) return '🐰';
  if (/仓鼠|鼠/.test(text)) return '🐹';
  return '🐾';
});

const petClass = computed(() => {
  const text = `${pet.value?.breed || ''}${pet.value?.name || ''}`;
  if (/猫|布偶|英短|美短|暹罗|橘/.test(text)) return 'cat';
  if (/狗|犬|柯基|金毛|泰迪|拉布拉多|边牧|柴犬/.test(text)) return 'dog';
  return 'other';
});

const ageText = computed(() => formatBirthday(pet.value?.birthday));

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
  return ageMonths <= 0 ? '' : ageMonths + '个月';
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
  background-color: $bg-page;
  padding: 24rpx 24rpx 0;
}

/* ========== 顶部档案卡 ========== */
.hero-card {
  position: relative;
  display: flex;
  align-items: center;
  overflow: hidden;
  padding: 48rpx 36rpx;
  margin-bottom: 24rpx;
  border-radius: $radius-xl;
  box-shadow: $shadow-card;

  &.cat {
    background: linear-gradient(135deg, #FFE9D6 0%, #FFD3DE 100%);
  }

  &.dog {
    background: linear-gradient(135deg, #DDEBFF 0%, #E5DFFF 100%);
  }

  &.other {
    background: linear-gradient(135deg, #E0F2E1 0%, #FFF3CE 100%);
  }

  .hero-deco {
    position: absolute;
    font-size: 150rpx;
    opacity: 0.1;

    &.deco-1 {
      right: -28rpx;
      bottom: -20rpx;
      transform: rotate(-18deg);
    }

    &.deco-2 {
      top: -36rpx;
      right: 120rpx;
      font-size: 90rpx;
      transform: rotate(24deg);
    }
  }

  .avatar-wrap {
    position: relative;
    flex-shrink: 0;
    margin-right: 32rpx;

    .hero-avatar {
      width: 190rpx;
      height: 190rpx;
      border-radius: 50%;
      background: #fff;
      border: 6rpx solid rgba(255, 255, 255, 0.92);
      box-shadow: $shadow-md;
    }

    .type-badge {
      position: absolute;
      right: -2rpx;
      bottom: 2rpx;
      width: 64rpx;
      height: 64rpx;
      border-radius: 50%;
      background: #fff;
      border: 3rpx solid #fff;
      box-shadow: $shadow-sm;
      display: flex;
      align-items: center;
      justify-content: center;

      text {
        font-size: 36rpx;
      }
    }
  }

  .hero-info {
    flex: 1;
    min-width: 0;
    position: relative;
    z-index: 1;

    .name-line {
      display: flex;
      align-items: center;
      margin-bottom: 14rpx;

      .name {
        font-size: 48rpx;
        font-weight: $font-weight-bold;
        color: $text-primary;
        @include pet-ellipsis;
        max-width: 260rpx;
      }

      .gender-icon {
        margin-left: 14rpx;
        font-size: 36rpx;
        font-weight: $font-weight-bold;

        &.male { color: $accent-blue; }
        &.female { color: $accent-pink; }
      }
    }

    .breed-chip {
      display: inline-block;
      align-self: flex-start;
      font-size: $font-sm;
      color: $primary-dark;
      background: rgba(255, 255, 255, 0.88);
      padding: 8rpx 24rpx;
      border-radius: $radius-round;
      box-shadow: $shadow-sm;
    }

    .hero-tags {
      display: flex;
      flex-wrap: wrap;
      gap: 10rpx;
      margin-top: 18rpx;

      .hero-tag {
        font-size: $font-xs;
        padding: 4rpx 16rpx;
        border-radius: $radius-round;
        background: rgba(255, 255, 255, 0.72);
        color: $text-secondary;

        &.ok {
          color: $accent-green;
        }

        &.warn {
          color: $warning;
        }

        &.plain {
          color: $primary-dark;
        }
      }
    }
  }
}

/* ========== 分组信息卡片 ========== */
.info-card {
  background: $bg-card;
  border-radius: $radius-lg;
  padding: 30rpx 32rpx;
  margin-bottom: 24rpx;
  box-shadow: $shadow-card;
  animation: pet-fadeInUp 0.4s ease both;

  .section-title {
    display: flex;
    align-items: center;
    font-size: $font-lg;
    font-weight: $font-weight-bold;
    color: $text-primary;
    margin-bottom: 24rpx;

    .title-icon {
      width: 52rpx;
      height: 52rpx;
      margin-right: 14rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 16rpx;
      background: $primary-lighter;
      font-size: 28rpx;
    }

    .vaccine-count {
      margin-left: auto;
      font-size: $font-xs;
      font-weight: $font-weight-regular;
      color: $text-hint;
      background: $bg-input;
      padding: 4rpx 16rpx;
      border-radius: $radius-round;
    }
  }
}

.info-grid {
  display: flex;
  flex-wrap: wrap;

  .info-item {
    width: 50%;
    padding: 14rpx 0;

    .info-label {
      display: block;
      font-size: $font-xs;
      color: $text-hint;
      margin-bottom: 8rpx;
    }

    .info-value {
      font-size: $font-md;
      color: $text-primary;
      font-weight: $font-weight-medium;
    }
  }
}

/* ========== 疫苗时间线 ========== */
.vaccine-timeline {
  .vaccine-item {
    display: flex;

    .timeline-left {
      display: flex;
      flex-direction: column;
      align-items: center;
      width: 44rpx;
      flex-shrink: 0;

      .timeline-dot {
        width: 36rpx;
        height: 36rpx;
        border-radius: 50%;
        background: $bg-input;
        border: 4rpx solid $primary-lighter;
        box-sizing: border-box;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;

        text {
          font-size: 20rpx;
          color: #fff;
          font-weight: $font-weight-bold;
        }

        &.done {
          background: $gradient-primary;
          border-color: transparent;
        }
      }

      .timeline-line {
        width: 3rpx;
        flex: 1;
        margin: 6rpx 0;
        background: linear-gradient(180deg, $primary-lighter, #F0E3FF);
      }
    }

    .vaccine-content {
      flex: 1;
      min-width: 0;
      padding: 0 20rpx 30rpx;

      .vaccine-head {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 12rpx;

        .vaccine-name {
          font-size: $font-md;
          color: $text-primary;
          font-weight: $font-weight-bold;
        }

        .vaccine-order {
          font-size: $font-xs;
          color: $primary;
          background: $primary-lighter;
          padding: 4rpx 16rpx;
          border-radius: $radius-round;
        }
      }

      .vaccine-dates {
        display: flex;
        flex-wrap: wrap;
        gap: 12rpx;

        .date-chip {
          display: flex;
          flex-direction: column;
          background: $bg-input;
          padding: 12rpx 20rpx;
          border-radius: $radius-sm;

          .date-label {
            font-size: $font-xs;
            color: $text-hint;
            margin-bottom: 4rpx;
          }

          .date-value {
            font-size: $font-sm;
            color: $text-secondary;
            font-weight: $font-weight-medium;
          }

          &.next {
            background: $primary-lighter;

            .date-value {
              color: $primary-dark;
            }
          }
        }
      }
    }
  }
}

/* ========== 备注 ========== */
.remark-box {
  background: $bg-input;
  border-radius: $radius-md;
  padding: 24rpx 26rpx;

  .remark-text {
    font-size: $font-md;
    color: $text-secondary;
    line-height: 1.75;
  }
}

/* ========== 操作按钮 ========== */
.action-bar {
  margin-top: 10rpx;
  padding: 0 4rpx 20rpx;

  .edit-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12rpx;
    height: 96rpx;
    border-radius: $radius-round;
    background: $gradient-primary;
    box-shadow: $shadow-primary;

    .edit-text {
      font-size: $font-lg;
      color: #fff;
      font-weight: $font-weight-bold;
    }
  }
}

/* ========== 底部安全区 ========== */
.safe-bottom {
  height: calc(env(safe-area-inset-bottom) + 24rpx);
}

/* ========== 加载中 ========== */
.loading-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 300rpx;

  .loading-paw {
    font-size: 90rpx;
    animation: pet-spin 1.2s linear infinite;
  }

  .loading-text {
    margin-top: 24rpx;
    font-size: $font-sm;
    color: $text-hint;
  }
}
</style>
