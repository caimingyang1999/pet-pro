<template>
  <view class="page-container">
    <!-- 顶部渐变头部 -->
    <view class="header-section">
      <view class="header-bg" />
      <view class="header-decor decor-1" />
      <view class="header-decor decor-2" />

      <!-- 自定义导航栏 -->
      <view class="custom-nav" :style="{ paddingTop: statusBarHeight + 'px' }">
        <view class="nav-content">
          <view class="nav-back" @click="goBack">
            <Icon name="chevron_left" :size="22" color="#fff" />
          </view>
          <text class="nav-bar-title">我的宠物</text>
        </view>
      </view>

      <view class="header-content">
        <view class="header-left">
          <text class="header-title">我的宠物</text>
          <view class="header-count-wrap" v-if="petList.length">
            <view class="count-icon">
              <Icon name="paw" :size="12" color="#fff" />
            </view>
            <text class="header-count">共 {{ petList.length }} 只小可爱</text>
          </view>
          <text class="header-subtitle" v-else>添加你的第一只宠物吧</text>
        </view>
        <view class="header-right" @click="goAdd">
          <view class="add-btn">
            <Icon name="plus" :size="16" color="#fff" />
            <text class="add-text">添加</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 加载骨架屏 -->
    <view v-if="loading && !petList.length" class="pet-list">
      <LoadingState mode="skeleton" type="pet" :count="3" />
    </view>

    <!-- 宠物卡片列表 -->
    <view class="pet-list" v-else-if="petList.length">
      <view
        class="pet-item"
        v-for="pet in petList"
        :key="pet.id"
        @click="goDetail(pet.id)"
      >
        <!-- 宠物图片 -->
        <view class="pet-avatar-wrap">
          <image
            class="pet-avatar"
            :src="fullImageUrl(pet.avatar) || '/static/tabbar/pet.png'"
            mode="aspectFill"
          />
          <view class="gender-badge" :class="pet.gender === '1' ? 'male' : 'female'">
            <text>{{ pet.gender === '1' ? '♂' : '♀' }}</text>
          </view>
        </view>

        <!-- 宠物信息 -->
        <view class="pet-info">
          <view class="pet-name-row">
            <text class="pet-name">{{ pet.name }}</text>
            <text v-if="pet.sterilization === '1'" class="sterilization-tag">已绝育</text>
          </view>
          <text class="pet-breed">{{ pet.breed || '未知品种' }}</text>
          <view class="pet-tags">
            <text v-if="pet.weight" class="pet-tag">
              <Icon name="weight" :size="10" color="#FF7E3D" /> {{ pet.weight }}kg
            </text>
            <text v-if="pet.color" class="pet-tag">{{ pet.color }}</text>
            <text v-if="getAge(pet.birthday)" class="pet-tag">{{ getAge(pet.birthday) }}</text>
          </view>
          <view v-if="pet.vaccineList && pet.vaccineList.length" class="vaccine-hint">
            <Icon name="vaccine" :size="12" color="#67C23A" />
            <text class="vaccine-text">{{ pet.vaccineList.length }} 条疫苗记录</text>
          </view>
        </view>

        <!-- 右侧操作 -->
        <view class="pet-actions" @click.stop>
          <view class="action-btn edit" @click="goEdit(pet.id)">
            <Icon name="edit" :size="14" color="#4A90D9" />
          </view>
          <view class="action-btn delete" @click="handleDelete(pet)">
            <Icon name="trash" :size="14" color="#F56C6C" />
          </view>
          <view class="arrow-icon">
            <Icon name="chevron_right" :size="18" color="#C0C4CC" />
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-section" v-else>
      <view class="empty-img-wrap">
        <Icon name="pet" :size="80" color="#FFD4A8" />
      </view>
      <text class="empty-title">还没有添加宠物哦</text>
      <text class="empty-desc">点击下方按钮，创建你的宠物档案</text>
      <view class="empty-btn" @click="goAdd">
        <Icon name="plus" :size="16" color="#fff" />
        <text>添加第一只宠物</text>
      </view>
      <text class="empty-tip">或使用右侧按钮快速添加</text>
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

const handleDelete = async (pet) => {
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
};

onMounted(() => fetchList());
onShow(() => fetchList());
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background-color: #F5F6FA;
}

/* ========== 顶部渐变头部 ========== */
.header-section {
  position: relative;
  padding: 0 30rpx 30rpx;
  overflow: hidden;

  /* ========== 自定义导航栏 ========== */
  .custom-nav {
    position: relative;
    z-index: 2;

    .nav-content {
      position: relative;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 16rpx 10rpx;
    }

    .nav-back {
      position: absolute;
      left: 16rpx;
      top: 50%;
      transform: translateY(-50%);
      width: 56rpx;
      height: 56rpx;
      display: flex;
      align-items: center;
      justify-content: center;

      &:active {
        opacity: 0.6;
      }
    }

    .nav-bar-title {
      font-size: 34rpx;
      font-weight: 700;
      color: #fff;
    }
  }

  .header-bg {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 320rpx;
    background: linear-gradient(135deg, #FF934F 0%, #FF7E3D 60%, #FFC89E 100%);
    border-radius: 0 0 40rpx 40rpx;
  }

  .header-decor {
    position: absolute;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.15);
  }

  .decor-1 {
    width: 260rpx;
    height: 260rpx;
    top: -80rpx;
    right: -60rpx;
  }

  .decor-2 {
    width: 140rpx;
    height: 140rpx;
    top: 180rpx;
    right: 120rpx;
    opacity: 0.6;
  }

  .header-content {
    position: relative;
    z-index: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 40rpx 10rpx 36rpx;
  }

  .header-left {
    .header-title {
      display: block;
      font-size: 40rpx;
      font-weight: 700;
      color: #fff;
      margin-bottom: 12rpx;
    }

    .header-count-wrap {
      display: flex;
      align-items: center;
      background-color: rgba(255, 255, 255, 0.25);
      padding: 8rpx 20rpx;
      border-radius: 30rpx;

      .count-icon {
        margin-right: 8rpx;
      }
    }

    .header-count {
      font-size: 24rpx;
      color: #fff;
    }

    .header-subtitle {
      font-size: 26rpx;
      color: rgba(255, 255, 255, 0.85);
    }
  }

  .header-right {
    .add-btn {
      display: flex;
      align-items: center;
      background-color: rgba(255, 255, 255, 0.25);
      border: 2rpx solid rgba(255, 255, 255, 0.4);
      border-radius: 40rpx;
      padding: 16rpx 28rpx;
      backdrop-filter: blur(10rpx);

      .add-text {
        font-size: 26rpx;
        color: #fff;
        margin-left: 8rpx;
        font-weight: 500;
      }
    }
  }
}

/* ========== 宠物列表 ========== */
.pet-list {
  padding: 0 24rpx;
}

.pet-item {
  display: flex;
  align-items: center;
  background-color: #fff;
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

  &:active {
    transform: scale(0.98);
  }
}

.pet-avatar-wrap {
  position: relative;
  margin-right: 24rpx;

  .pet-avatar {
    width: 140rpx;
    height: 140rpx;
    border-radius: 24rpx;
    background-color: #F5F5F5;
  }

  .gender-badge {
    position: absolute;
    bottom: -8rpx;
    right: -8rpx;
    width: 40rpx;
    height: 40rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 22rpx;
    font-weight: bold;
    color: #fff;

    &.male {
      background: linear-gradient(135deg, #4A90D9, #6DB3F2);
    }

    &.female {
      background: linear-gradient(135deg, #E8738A, #F4A4B4);
    }
  }
}

.pet-info {
  flex: 1;
  min-width: 0;

  .pet-name-row {
    display: flex;
    align-items: center;
    margin-bottom: 6rpx;
  }

  .pet-name {
    font-size: 32rpx;
    color: #303133;
    font-weight: 700;
    margin-right: 12rpx;
  }

  .sterilization-tag {
    font-size: 20rpx;
    color: #FF7E3D;
    background-color: #FFF0E6;
    padding: 2rpx 12rpx;
    border-radius: 6rpx;
  }

  .pet-breed {
    font-size: 26rpx;
    color: #909399;
    margin-bottom: 12rpx;
    display: block;
  }

  .pet-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 12rpx;
    margin-bottom: 8rpx;
  }

  .pet-tag {
    display: inline-flex;
    align-items: center;
    font-size: 20rpx;
    color: #606266;
    background-color: #F5F7FA;
    padding: 4rpx 14rpx;
    border-radius: 8rpx;
  }

  .vaccine-hint {
    display: flex;
    align-items: center;
    margin-top: 4rpx;

    .vaccine-text {
      font-size: 22rpx;
      color: #67C23A;
      margin-left: 6rpx;
    }
  }
}

.pet-actions {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
  margin-left: 12rpx;

  .action-btn {
    width: 48rpx;
    height: 48rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #F5F7FA;

    &.edit:active {
      background-color: #E6F2FF;
    }

    &.delete:active {
      background-color: #FFF0F0;
    }
  }

  .arrow-icon {
    margin-top: 4rpx;
  }
}

/* ========== 空状态 ========== */
.empty-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100rpx 60rpx 0;

  .empty-img-wrap {
    width: 160rpx;
    height: 160rpx;
    background: linear-gradient(135deg, #FFF0E6, #FFE4CC);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 32rpx;
  }

  .empty-title {
    font-size: 32rpx;
    color: #303133;
    font-weight: 600;
    margin-bottom: 12rpx;
  }

  .empty-desc {
    font-size: 26rpx;
    color: #909399;
    margin-bottom: 40rpx;
  }

  .empty-btn {
    display: flex;
    align-items: center;
    background: linear-gradient(135deg, #FF934F, #FF7E3D);
    color: #fff;
    font-size: 30rpx;
    font-weight: 500;
    padding: 24rpx 64rpx;
    border-radius: 44rpx;
    box-shadow: 0 8rpx 20rpx rgba(255, 126, 61, 0.3);

    text {
      margin-left: 8rpx;
    }

    &:active {
      transform: scale(0.96);
    }
  }

  .empty-tip {
    font-size: 24rpx;
    color: #C0C4CC;
    margin-top: 20rpx;
  }
}

/* ========== 底部安全区 ========== */
.safe-bottom {
  height: calc(env(safe-area-inset-bottom) + 40rpx);
}
</style>
