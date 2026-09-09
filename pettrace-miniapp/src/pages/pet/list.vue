<template>
  <view class="pet-page">
    <!-- ========== 顶部渐变头部 ========== -->
    <view class="header-section">
      <view class="header-decor paw-1">🐾</view>
      <view class="header-decor paw-2">🐾</view>

      <!-- 自定义导航栏 -->
      <view class="custom-nav" :style="{ paddingTop: statusBarHeight + 'px' }">
        <view class="nav-content">
          <view class="nav-back" @click="goBack">
            <Icon name="chevron_left" :size="22" color="#fff" />
          </view>
          <text class="nav-title">我的宠物</text>
          <view class="nav-add" @click="goAdd">
            <Icon name="plus" :size="20" color="#fff" />
          </view>
        </view>
      </view>

      <view class="header-main">
        <view class="header-info">
          <text class="header-title">我的爱宠 🐾</text>
          <text class="header-sub" v-if="petList.length">共 {{ petList.length }} 只毛孩子陪伴着你</text>
          <text class="header-sub" v-else>从第一只毛孩子开始记录吧</text>
        </view>
        <view class="header-count" v-if="petList.length">
          <text class="count-num">{{ petList.length }}</text>
          <text class="count-unit">位家人</text>
        </view>
      </view>
    </view>

    <!-- 加载骨架屏 -->
    <view v-if="loading && !petList.length" class="pet-list">
      <LoadingState mode="skeleton" type="pet" :count="3" />
    </view>

    <!-- 宠物卡片列表 + 添加卡片 -->
    <view class="pet-list" v-else-if="petList.length">
      <view
        class="pet-card"
        :class="petClass(pet)"
        v-for="pet in petList"
        :key="pet.id"
        @click="goDetail(pet.id)"
      >
        <!-- 头像 + 类型角标 -->
        <view class="avatar-wrap">
          <image
            class="pet-avatar"
            :src="fullImageUrl(pet.avatar) || '/static/default-pet.png'"
            mode="aspectFill"
          />
          <view class="type-badge">
            <text>{{ petEmoji(pet) }}</text>
          </view>
        </view>

        <!-- 信息区 -->
        <view class="pet-info">
          <view class="name-row">
            <text class="pet-name">{{ pet.name }}</text>
            <text v-if="pet.sterilization === '1'" class="sterilization-tag">已绝育</text>
          </view>
          <view class="breed-row">
            <text class="pet-breed">{{ pet.breed || '未知品种' }}</text>
            <text class="gender-text" :class="pet.gender === '1' ? 'male' : 'female'">
              {{ pet.gender === '1' ? '♂ 弟弟' : '♀ 妹妹' }}
            </text>
          </view>
          <view class="meta-row">
            <text v-if="getAge(pet.birthday)" class="meta-item">
              <text class="meta-emoji">🎂</text>
              {{ getAge(pet.birthday) }}
            </text>
            <text v-if="pet.weight" class="meta-item">
              <text class="meta-emoji">⚖️</text>
              {{ pet.weight }}kg
            </text>
            <text v-if="pet.color" class="meta-item color-item">{{ pet.color }}</text>
          </view>
          <view v-if="pet.vaccineList && pet.vaccineList.length" class="vaccine-hint">
            <text class="vaccine-emoji">💉</text>
            <text class="vaccine-text">已接种 {{ pet.vaccineList.length }} 针疫苗</text>
          </view>
        </view>

        <!-- 更多操作 -->
        <view class="more-btn" @click.stop="handleMore(pet)">
          <Icon name="more" :size="20" color="#8A8D9A" />
        </view>
      </view>

      <!-- 虚线添加卡片 -->
      <view class="add-card" @click="goAdd">
        <view class="add-icon">
          <Icon name="plus" :size="26" color="#FF8C42" />
        </view>
        <text class="add-text">添加新宠物</text>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-wrap" v-else>
      <view class="empty-art">
        <text class="empty-emoji">🐕‍🦺</text>
      </view>
      <text class="empty-title">还没有宠物档案</text>
      <text class="empty-desc">添加你的第一位毛孩子吧~</text>
      <view class="empty-btn pet-press" @click="goAdd">
        <Icon name="plus" :size="18" color="#fff" />
        <text class="empty-btn-text">添加第一位毛孩子</text>
      </view>
    </view>

    <!-- 底部安全区 -->
    <view class="safe-bottom" />
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import LoadingState from '@/components/LoadingState.vue';
import Icon from '@/components/Icon.vue';
import { usePetStore } from '@/store/pet.js';
import { deletePet } from '@/api/pet.js';
import { showToast, showConfirm, fullImageUrl } from '@/utils/index.js';

const petStore = usePetStore();
const petList = ref([]);
const loading = ref(false);

const statusBarHeight = ref(20);
// #ifdef MP-WEIXIN
try {
  const sysInfo = uni.getSystemInfoSync();
  statusBarHeight.value = sysInfo.statusBarHeight || 20;
} catch (e) {}
// #endif

const fetchList = async () => {
  loading.value = true;
  try {
    await petStore.fetchPetList();
    petList.value = petStore.petList;
  } catch (err) {
    // request.js 已统一处理 toast
  } finally {
    loading.value = false;
  }
};

const goBack = () => uni.navigateBack({ delta: 1 });
const goDetail = (id) => uni.navigateTo({ url: `/pages/pet/detail?id=${id}` });
const goAdd = () => uni.navigateTo({ url: '/pages/pet/edit' });
const goEdit = (id) => uni.navigateTo({ url: `/pages/pet/edit?id=${id}` });

/** 根据品种推断动物 emoji（后端未下发类型字段时按品种关键字兜底） */
const petEmoji = (pet) => {
  const text = `${pet.breed || ''}${pet.name || ''}`;
  if (/猫|布偶|英短|美短|暹罗|橘/.test(text)) return '🐱';
  if (/狗|犬|柯基|金毛|泰迪|拉布拉多|边牧|柴犬/.test(text)) return '🐶';
  if (/兔/.test(text)) return '🐰';
  if (/仓鼠|鼠/.test(text)) return '🐹';
  return '🐾';
};

/** 按宠物类别返回柔和渐变卡片类型 */
const petClass = (pet) => {
  const text = `${pet.breed || ''}${pet.name || ''}`;
  if (/猫|布偶|英短|美短|暹罗|橘/.test(text)) return 'card-cat';
  if (/狗|犬|柯基|金毛|泰迪|拉布拉多|边牧|柴犬/.test(text)) return 'card-dog';
  return 'card-other';
};

const getAge = (birthday) => {
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

const handleMore = (pet) => {
  uni.showActionSheet({
    itemList: ['编辑档案', '删除宠物'],
    itemColor: '#2D2D2D',
    success: async (res) => {
      if (res.tapIndex === 0) {
        goEdit(pet.id);
      } else if (res.tapIndex === 1) {
        const confirmed = await showConfirm(`确定要删除「${pet.name}」吗？删除后无法恢复。`);
        if (!confirmed) return;
        try {
          await deletePet(pet.id);
          showToast('删除成功', 'success');
          petList.value = petList.value.filter((p) => p.id !== pet.id);
          petStore.setPetList(petList.value);
        } catch (err) {
          // request.js 已统一处理 toast
        }
      }
    },
  });
};

onMounted(() => fetchList());
onShow(() => fetchList());
</script>

<style lang="scss" scoped>
.pet-page {
  min-height: 100vh;
  background-color: $bg-page;
}

/* ========== 顶部渐变头部 ========== */
.header-section {
  position: relative;
  overflow: hidden;
  background: linear-gradient(160deg, #FFB26B 0%, #FF8C42 58%, #F06E2D 100%);
  border-radius: 0 0 56rpx 56rpx;
  padding-bottom: 34rpx;

  .header-decor {
    position: absolute;
    opacity: 0.13;
    font-size: 110rpx;
  }

  .paw-1 {
    top: 120rpx;
    right: -16rpx;
    transform: rotate(20deg);
  }

  .paw-2 {
    bottom: 8rpx;
    left: -10rpx;
    font-size: 90rpx;
    transform: rotate(-12deg);
  }
}

.custom-nav {
  position: relative;
  z-index: 2;

  .nav-content {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    height: 88rpx;
    padding: 4rpx 24rpx;
  }

  .nav-back {
    position: absolute;
    left: 12rpx;
    width: 64rpx;
    height: 64rpx;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.2);
    display: flex;
    align-items: center;
    justify-content: center;

    &:active {
      opacity: 0.65;
    }
  }

  .nav-title {
    font-size: $font-xl;
    font-weight: $font-weight-bold;
    color: $text-white;
  }

  .nav-add {
    position: absolute;
    right: 12rpx;
    width: 64rpx;
    height: 64rpx;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.2);
    display: flex;
    align-items: center;
    justify-content: center;

    &:active {
      transform: scale(0.9);
    }
  }
}

.header-main {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 40rpx 8rpx;

  .header-info {
    .header-title {
      display: block;
      font-size: 44rpx;
      font-weight: $font-weight-bold;
      color: #fff;
      line-height: 1.4;
      text-shadow: 0 4rpx 12rpx rgba(190, 70, 18, 0.22);
    }

    .header-sub {
      display: block;
      margin-top: 8rpx;
      font-size: $font-sm;
      color: rgba(255, 255, 255, 0.88);
    }
  }

  .header-count {
    display: flex;
    flex-direction: column;
    align-items: center;
    width: 110rpx;
    height: 110rpx;
    justify-content: center;
    background: rgba(255, 255, 255, 0.24);
    border: 2rpx dashed rgba(255, 255, 255, 0.6);
    border-radius: 50%;

    .count-num {
      font-size: 40rpx;
      font-weight: $font-weight-bold;
      color: #fff;
      line-height: 1;
    }

    .count-unit {
      margin-top: 4rpx;
      font-size: $font-xs;
      color: rgba(255, 255, 255, 0.9);
    }
  }
}

/* ========== 宠物列表 ========== */
.pet-list {
  padding: 28rpx 24rpx 0;
}

.pet-card {
  position: relative;
  display: flex;
  align-items: center;
  padding: 28rpx 26rpx;
  margin-bottom: 24rpx;
  border-radius: $radius-lg;
  box-shadow: $shadow-card;
  animation: pet-slideIn 0.42s ease both;
  transition: transform 0.3s ease, box-shadow 0.3s ease;

  &:active {
    transform: scale(0.975);
  }

  /* 猫：浅橙 → 浅粉 */
  &.card-cat {
    background: linear-gradient(135deg, #FFF3E9 0%, #FFE3E9 100%);
  }

  /* 狗：浅蓝 → 浅紫 */
  &.card-dog {
    background: linear-gradient(135deg, #E9F3FF 0%, #ECE7FF 100%);
  }

  /* 其他：浅绿 → 浅黄 */
  &.card-other {
    background: linear-gradient(135deg, #EBF7EC 0%, #FFF7DD 100%);
  }

  .avatar-wrap {
    position: relative;
    flex-shrink: 0;
    margin-right: 26rpx;

    .pet-avatar {
      width: 130rpx;
      height: 130rpx;
      border-radius: 50%;
      background-color: #fff;
      border: 4rpx solid rgba(255, 255, 255, 0.9);
      box-shadow: $shadow-sm;
    }

    .type-badge {
      position: absolute;
      right: -6rpx;
      bottom: -4rpx;
      width: 52rpx;
      height: 52rpx;
      border-radius: 50%;
      background: #fff;
      border: 2rpx solid #fff;
      box-shadow: $shadow-sm;
      display: flex;
      align-items: center;
      justify-content: center;

      text {
        font-size: 30rpx;
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

      .pet-name {
        font-size: $font-xl;
        color: $text-primary;
        font-weight: $font-weight-bold;
        @include pet-ellipsis;
        max-width: 220rpx;
      }

      .sterilization-tag {
        margin-left: 12rpx;
        font-size: $font-xs;
        color: $accent-green;
        background: rgba(255, 255, 255, 0.75);
        padding: 4rpx 16rpx;
        border-radius: $radius-round;
        flex-shrink: 0;
      }
    }

    .breed-row {
      display: flex;
      align-items: center;
      gap: 12rpx;
      margin-top: 12rpx;

      .pet-breed {
        font-size: $font-sm;
        color: $primary-dark;
        background: rgba(255, 255, 255, 0.78);
        padding: 4rpx 18rpx;
        border-radius: $radius-round;
        @include pet-ellipsis;
        max-width: 200rpx;
      }

      .gender-text {
        font-size: $font-xs;
        font-weight: $font-weight-medium;

        &.male { color: $accent-blue; }
        &.female { color: $accent-pink; }
      }
    }

    .meta-row {
      display: flex;
      align-items: center;
      gap: 20rpx;
      margin-top: 12rpx;

      .meta-item {
        display: inline-flex;
        align-items: center;
        font-size: $font-xs;
        color: $text-secondary;

        .meta-emoji {
          margin-right: 4rpx;
          font-size: 22rpx;
        }
      }

      .color-item {
        padding: 2rpx 14rpx;
        background: rgba(255, 255, 255, 0.7);
        border-radius: $radius-round;
      }
    }

    .vaccine-hint {
      display: flex;
      align-items: center;
      margin-top: 12rpx;

      .vaccine-emoji {
        font-size: 22rpx;
        margin-right: 6rpx;
      }

      .vaccine-text {
        font-size: $font-xs;
        color: $accent-green;
        font-weight: $font-weight-medium;
      }
    }
  }

  .more-btn {
    position: absolute;
    top: 16rpx;
    right: 12rpx;
    width: 56rpx;
    height: 56rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;

    &:active {
      background: rgba(255, 255, 255, 0.72);
      transform: scale(0.9);
    }
  }
}

/* ===== 添加宠物卡片 ===== */
.add-card {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14rpx;
  padding: 34rpx 0;
  margin-bottom: 24rpx;
  border: 3rpx dashed rgba(255, 140, 66, 0.5);
  border-radius: $radius-lg;
  background: rgba(255, 255, 255, 0.7);
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.97) rotate(-0.6deg);
    background: #fff;
  }

  .add-icon {
    width: 56rpx;
    height: 56rpx;
    border-radius: 50%;
    background: $primary-lighter;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .add-text {
    font-size: $font-md;
    color: $primary;
    font-weight: $font-weight-medium;
  }
}

/* ========== 空状态 ========== */
.empty-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 60rpx 0;

  .empty-art {
    width: 200rpx;
    height: 200rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    background: $gradient-card;
    box-shadow: $shadow-md;
    animation: pet-float 3s ease-in-out infinite;

    .empty-emoji {
      font-size: 100rpx;
    }
  }

  .empty-title {
    margin-top: 44rpx;
    font-size: $font-xl;
    color: $text-primary;
    font-weight: $font-weight-bold;
  }

  .empty-desc {
    margin-top: 14rpx;
    font-size: $font-sm;
    color: $text-hint;
  }

  .empty-btn {
    display: flex;
    align-items: center;
    gap: 10rpx;
    margin-top: 44rpx;
    padding: 22rpx 60rpx;
    border-radius: $radius-round;
    background: $gradient-primary;
    box-shadow: $shadow-primary;

    .empty-btn-text {
      font-size: $font-md;
      color: #fff;
      font-weight: $font-weight-medium;
    }
  }
}

/* ========== 底部安全区 ========== */
.safe-bottom {
  height: calc(env(safe-area-inset-bottom) + 40rpx);
}
</style>
