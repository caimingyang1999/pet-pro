<template>
  <view class="health-page">
    <!-- 概览 -->
    <view class="overview-card">
      <view class="overview-item">
        <text class="overview-value">{{ petList.length }}</text>
        <text class="overview-label">爱宠</text>
      </view>
      <view class="overview-divider" />
      <view class="overview-item">
        <text class="overview-value">{{ vaccineTotal }}</text>
        <text class="overview-label">疫苗记录</text>
      </view>
      <view class="overview-divider" />
      <view class="overview-item">
        <text class="overview-value" :class="{ warn: reminders.length > 0 }">{{ reminders.length }}</text>
        <text class="overview-label">待办提醒</text>
      </view>
    </view>

    <!-- 加载中 -->
    <view class="loading-wrap" v-if="loading">
      <view class="loading-icon">
        <Icon name="medal" :size="26" color="#FF8C42" />
      </view>
      <text class="loading-text">正在加载健康记录...</text>
    </view>

    <template v-else>
      <!-- 无宠物 -->
      <view class="empty-wrap" v-if="!petList.length">
        <view class="empty-art">
          <Icon name="pet" :size="44" color="#FFB07A" />
        </view>
        <text class="empty-title">还没有爱宠档案</text>
        <text class="empty-desc">添加爱宠后，这里会自动汇总疫苗与体重记录</text>
        <view class="empty-btn pet-press" @click="goPetList">
          <text class="empty-btn-text">去添加爱宠</text>
        </view>
      </view>

      <template v-else>
        <!-- 健康提醒 -->
        <view class="section-head">
          <text class="section-title">健康提醒</text>
          <text class="section-sub">疫苗到期前 30 天开始提示</text>
        </view>

        <view class="reminder-list" v-if="reminders.length">
          <view
            class="reminder-item"
            :class="'level-' + item.level"
            v-for="(item, idx) in reminders"
            :key="idx"
          >
            <view class="reminder-icon">
              <Icon name="vaccine" :size="16" color="#fff" />
            </view>
            <view class="reminder-info">
              <text class="reminder-title">{{ item.petName }} · {{ item.vaccineName }}</text>
              <text class="reminder-sub">下次接种：{{ item.nextDate }}</text>
            </view>
            <view class="reminder-tag">
              <text class="tag-text">{{ item.tagText }}</text>
            </view>
          </view>
        </view>

        <view class="reminder-empty" v-else>
          <Icon name="check" :size="15" color="#4CAF7D" />
          <text class="reminder-empty-text">近期没有到期的疫苗，继续保持~</text>
        </view>

        <!-- 爱宠健康档案 -->
        <view class="section-head">
          <text class="section-title">爱宠档案</text>
        </view>

        <view class="pet-list">
          <view class="pet-card" v-for="pet in petList" :key="pet.id">
            <view class="pet-main" @click="goPetDetail(pet.id)">
              <image
                v-if="pet.avatar"
                class="pet-avatar"
                :src="fullImageUrl(pet.avatar)"
                mode="aspectFill"
              />
              <view v-else class="pet-avatar pet-avatar--empty">
                <Icon name="pet" :size="22" color="#FF8C42" />
              </view>

              <view class="pet-info">
                <text class="pet-name">{{ pet.name || '未命名' }}</text>
                <view class="pet-meta">
                  <text class="meta-text">疫苗 {{ (pet.vaccineList || []).length }} 条</text>
                  <text class="meta-dot">·</text>
                  <text class="meta-text">
                    {{ weightMap[pet.id] ? '体重 ' + weightMap[pet.id] + 'kg' : '暂无体重' }}
                  </text>
                </view>
              </view>

              <Icon name="chevron_right" :size="16" color="#C0C2CE" />
            </view>

            <view class="pet-actions">
              <view class="action-btn pet-press" @click="goWeight(pet.id)">
                <Icon name="weight" :size="15" color="#3D9BE9" />
                <text class="action-text">体重记录</text>
              </view>
              <view class="action-btn pet-press" @click="goAlbum(pet.id)">
                <Icon name="images" :size="15" color="#4CAF7D" />
                <text class="action-text">成长相册</text>
              </view>
              <view class="action-btn pet-press" @click="goPetDetail(pet.id)">
                <Icon name="file_text" :size="15" color="#E8A33D" />
                <text class="action-text">宠物档案</text>
              </view>
            </view>
          </view>
        </view>
      </template>
    </template>

    <view class="safe-bottom" />
  </view>
</template>

<script setup>
import Icon from '@/components/Icon.vue';
import { ref, computed } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { getPetList } from '@/api/pet.js';
import { getWeightLogList } from '@/api/health.js';
import { fullImageUrl } from '@/utils/index.js';

const petList = ref([]);
/** petId -> 最新体重 */
const weightMap = ref({});
const loading = ref(true);

const vaccineTotal = computed(() =>
  petList.value.reduce((sum, pet) => sum + ((pet.vaccineList || []).length), 0)
);

/** 把 yyyy-MM-dd 解析成时间戳（按本地零点） */
const parseDay = (val) => {
  if (!val) return null;
  const s = String(val).slice(0, 10);
  const parts = s.split('-').map(Number);
  if (parts.length !== 3 || parts.some((n) => isNaN(n))) return null;
  return new Date(parts[0], parts[1] - 1, parts[2]).getTime();
};

/**
 * 疫苗到期提醒：nextDate 距今 ≤ 30 天即进入提醒列表
 * level：0-已逾期 1-7 天内 2-30 天内
 */
const reminders = computed(() => {
  const list = [];
  const now = new Date();
  const todayTs = new Date(now.getFullYear(), now.getMonth(), now.getDate()).getTime();

  petList.value.forEach((pet) => {
    (pet.vaccineList || []).forEach((v) => {
      const ts = parseDay(v.nextDate);
      if (ts === null) return;
      const days = Math.round((ts - todayTs) / (24 * 3600 * 1000));
      if (days > 30) return;

      let level = 2;
      let tagText = `${days} 天后`;
      if (days < 0) {
        level = 0;
        tagText = `已逾期 ${Math.abs(days)} 天`;
      } else if (days === 0) {
        level = 0;
        tagText = '今天到期';
      } else if (days <= 7) {
        level = 1;
      }

      list.push({
        petName: pet.name || '未命名',
        vaccineName: v.vaccineName || '疫苗',
        nextDate: String(v.nextDate).slice(0, 10),
        days,
        level,
        tagText,
      });
    });
  });

  // 逾期的排最前，其次按剩余天数升序
  return list.sort((a, b) => a.days - b.days);
});

/** 并发拉取每只宠物的最新体重（失败静默降级，不影响页面） */
const loadWeights = async () => {
  const pets = petList.value;
  if (!pets.length) return;
  const results = await Promise.allSettled(pets.map((pet) => getWeightLogList(pet.id)));
  const map = {};
  results.forEach((r, idx) => {
    if (r.status !== 'fulfilled') return;
    const list = r.value?.data || [];
    if (list.length) {
      map[pets[idx].id] = list[list.length - 1].weight;
    }
  });
  weightMap.value = map;
};

const loadData = async () => {
  loading.value = true;
  try {
    const res = await getPetList();
    petList.value = res.data || [];
    await loadWeights();
  } catch (e) {
    console.error('[健康记录] 加载失败:', e?.code || e?.msg || e);
    petList.value = [];
  } finally {
    loading.value = false;
  }
};

const goPetDetail = (petId) => uni.navigateTo({ url: `/pages/pet/detail?id=${petId}` });
const goWeight = (petId) => uni.navigateTo({ url: `/pages/pet/weight?petId=${petId}` });
const goAlbum = (petId) => uni.navigateTo({ url: `/pages/pet/album?petId=${petId}` });
const goPetList = () => uni.switchTab({ url: '/pages/pet/list' });

onShow(() => {
  loadData();
});
</script>

<style lang="scss" scoped>
.health-page {
  min-height: 100vh;
  background-color: $bg-page;
}

/* ===== 概览卡片 ===== */
.overview-card {
  display: flex;
  align-items: center;
  margin: 24rpx;
  padding: 34rpx 0;
  border-radius: $radius-lg;
  background: $gradient-primary;
  box-shadow: $shadow-primary;

  .overview-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;

    .overview-value {
      font-size: 48rpx;
      font-weight: $font-weight-bold;
      color: #fff;
      line-height: 1.15;

      &.warn {
        color: #FFF0A8;
      }
    }

    .overview-label {
      margin-top: 8rpx;
      font-size: $font-xs;
      color: rgba(255, 255, 255, 0.82);
    }
  }

  .overview-divider {
    width: 1rpx;
    height: 56rpx;
    background: rgba(255, 255, 255, 0.24);
  }
}

/* ===== 加载 ===== */
.loading-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 140rpx 0;

  .loading-icon {
    width: 80rpx;
    height: 80rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    animation: pet-breath 1.6s ease-in-out infinite;
  }

  .loading-text {
    margin-top: 22rpx;
    font-size: $font-sm;
    color: $text-hint;
  }
}

/* ===== 区块标题 ===== */
.section-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin: 12rpx 32rpx 18rpx;

  .section-title {
    font-size: $font-md;
    font-weight: $font-weight-bold;
    color: $text-primary;
  }

  .section-sub {
    font-size: $font-xs;
    color: $text-hint;
  }
}

/* ===== 提醒列表 ===== */
.reminder-list {
  margin: 0 24rpx 32rpx;
  border-radius: $radius-lg;
  background-color: #fff;
  box-shadow: $shadow-card;
  overflow: hidden;
}

.reminder-item {
  display: flex;
  align-items: center;
  padding: 26rpx 28rpx;
  border-bottom: 1rpx solid $bg-input;

  &:last-child {
    border-bottom: none;
  }

  .reminder-icon {
    width: 64rpx;
    height: 64rpx;
    border-radius: 20rpx;
    background: linear-gradient(135deg, #7BC67E 0%, #4CAF7D 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 22rpx;
    flex-shrink: 0;
  }

  .reminder-info {
    flex: 1;
    min-width: 0;

    .reminder-title {
      display: block;
      font-size: $font-md;
      font-weight: $font-weight-medium;
      color: $text-primary;
      @include pet-ellipsis;
    }

    .reminder-sub {
      display: block;
      margin-top: 6rpx;
      font-size: $font-xs;
      color: $text-hint;
    }
  }

  .reminder-tag {
    flex-shrink: 0;
    padding: 8rpx 20rpx;
    border-radius: $radius-round;
    background: #E5F4FF;

    .tag-text {
      font-size: $font-xs;
      color: #2E7BC4;
      font-weight: $font-weight-medium;
    }
  }

  &.level-1 {
    .reminder-tag {
      background: #FFF6DF;

      .tag-text {
        color: #B57C13;
      }
    }
  }

  &.level-0 {
    .reminder-icon {
      background: linear-gradient(135deg, #FF9090 0%, #E86060 100%);
    }

    .reminder-tag {
      background: #FFEBEE;

      .tag-text {
        color: #C0392B;
      }
    }
  }
}

.reminder-empty {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin: 0 24rpx 32rpx;
  padding: 28rpx;
  border-radius: $radius-lg;
  background-color: #fff;
  box-shadow: $shadow-card;

  .reminder-empty-text {
    font-size: $font-sm;
    color: $text-secondary;
  }
}

/* ===== 爱宠档案 ===== */
.pet-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
  padding: 0 24rpx;
}

.pet-card {
  border-radius: $radius-lg;
  background-color: #fff;
  box-shadow: $shadow-card;
  overflow: hidden;

  .pet-main {
    display: flex;
    align-items: center;
    padding: 26rpx 28rpx;

    .pet-avatar {
      width: 96rpx;
      height: 96rpx;
      border-radius: 50%;
      margin-right: 22rpx;
      flex-shrink: 0;
      background-color: $bg-input;

      &--empty {
        display: flex;
        align-items: center;
        justify-content: center;
        background: $gradient-card;
      }
    }

    .pet-info {
      flex: 1;
      min-width: 0;

      .pet-name {
        display: block;
        font-size: $font-md;
        font-weight: $font-weight-bold;
        color: $text-primary;
        @include pet-ellipsis;
      }

      .pet-meta {
        display: flex;
        align-items: center;
        gap: 10rpx;
        margin-top: 8rpx;

        .meta-text {
          font-size: $font-xs;
          color: $text-hint;
        }

        .meta-dot {
          font-size: $font-xs;
          color: $text-placeholder;
        }
      }
    }
  }

  .pet-actions {
    display: flex;
    border-top: 1rpx solid $bg-input;

    .action-btn {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8rpx;
      padding: 22rpx 0;
      border-right: 1rpx solid $bg-input;

      &:last-child {
        border-right: none;
      }

      .action-text {
        font-size: $font-xs;
        color: $text-secondary;
      }
    }
  }
}

/* ===== 空状态 ===== */
.empty-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 72rpx 0;

  .empty-art {
    width: 180rpx;
    height: 180rpx;
    border-radius: 50%;
    background: $gradient-card;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: $shadow-md;
  }

  .empty-title {
    margin-top: 36rpx;
    font-size: $font-lg;
    font-weight: $font-weight-bold;
    color: $text-primary;
  }

  .empty-desc {
    margin-top: 14rpx;
    font-size: $font-sm;
    color: $text-hint;
    text-align: center;
    line-height: 1.6;
  }

  .empty-btn {
    margin-top: 44rpx;
    padding: 20rpx 56rpx;
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

.safe-bottom {
  height: calc(env(safe-area-inset-bottom) + 60rpx);
}
</style>
