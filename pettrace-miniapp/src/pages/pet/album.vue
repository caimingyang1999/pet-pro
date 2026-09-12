<template>
  <view class="album-page">
    <!-- 宠物切换 -->
    <scroll-view v-if="petList.length > 1" scroll-x class="pet-strip">
      <view class="pet-strip-inner">
        <view
          class="pet-chip"
          :class="{ active: String(currentPetId) === String(pet.id) }"
          v-for="pet in petList"
          :key="pet.id"
          @click="switchPet(pet.id)"
        >
          <text class="pet-chip-text">{{ pet.name || '未命名' }}</text>
        </view>
      </view>
    </scroll-view>

    <!-- 无宠物 -->
    <view v-if="!loading && !petList.length" class="empty-wrap">
      <view class="empty-art">
        <Icon name="pet" :size="44" color="#FFB07A" />
      </view>
      <text class="empty-title">还没有爱宠档案</text>
      <text class="empty-desc">先添加一只爱宠，就可以开始记录成长相册啦</text>
      <view class="empty-btn pet-press" @click="goPetList">
        <text class="empty-btn-text">去添加爱宠</text>
      </view>
    </view>

    <template v-else>
      <!-- 隐私说明：仅自己可见 -->
      <view class="privacy-tip">
        <Icon name="locked" :size="13" color="#8A8D9A" />
        <text class="privacy-text">成长相册仅你自己可见，不会对外公开</text>
      </view>

      <!-- 记录列表 -->
      <view class="album-list" v-if="albumList.length">
        <view
          class="album-card"
          v-for="item in albumList"
          :key="item.id"
          @click="goEdit(item.id)"
        >
          <view class="album-cover">
            <image
              v-if="coverOf(item)"
              class="cover-img"
              :src="coverOf(item)"
              mode="aspectFill"
            />
            <view v-else class="cover-empty">
              <Icon name="image" :size="28" color="#FFB07A" />
            </view>
            <view class="cover-badge" v-if="imageCountOf(item) > 1">
              <Icon name="images" :size="11" color="#fff" />
              <text class="badge-text">{{ imageCountOf(item) }}</text>
            </view>
          </view>

          <view class="album-info">
            <text class="album-title pet-line2">{{ item.title || '未命名记录' }}</text>
            <text class="album-desc pet-line2" v-if="item.content">{{ item.content }}</text>
            <view class="album-foot">
              <text class="album-date">{{ formatDate(item.recordDate || item.createTime) }}</text>
              <view class="album-del pet-press" @click.stop="handleDelete(item.id)">
                <Icon name="trash" :size="15" color="#C0C4CE" />
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view class="empty-wrap" v-else-if="!loading">
        <view class="empty-art">
          <Icon name="images" :size="44" color="#FFB07A" />
        </view>
        <text class="empty-title">还没有成长记录</text>
        <text class="empty-desc">记录下毛孩子的每个第一次，日后回看会很感动</text>
        <view class="empty-btn pet-press" @click="goEdit()">
          <text class="empty-btn-text">添加第一条记录</text>
        </view>
      </view>

      <view class="safe-bottom" />
    </template>

    <!-- 添加按钮 -->
    <view class="fab-btn pet-press" v-if="petList.length" @click="goEdit()">
      <Icon name="plus" :size="28" color="#fff" />
    </view>
  </view>
</template>

<script setup>
import Icon from '@/components/Icon.vue';
import { ref } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { getPetList } from '@/api/pet.js';
import { getAlbumList, deleteAlbum } from '@/api/health.js';
import { showToast, showConfirm, fullImageUrl } from '@/utils/index.js';

const petList = ref([]);
const currentPetId = ref('');
const albumList = ref([]);
const loading = ref(true);

/** 解析 images 字段（JSON 数组字符串或数组） */
const parseImages = (item) => {
  const raw = item?.images;
  if (!raw) return [];
  if (Array.isArray(raw)) return raw;
  try {
    const arr = JSON.parse(raw);
    return Array.isArray(arr) ? arr : [];
  } catch (e) {
    return [];
  }
};

const coverOf = (item) => {
  const list = parseImages(item);
  return list.length ? fullImageUrl(list[0]) : '';
};

const imageCountOf = (item) => parseImages(item).length;

const formatDate = (val) => {
  if (!val) return '';
  const s = String(val);
  return s.length > 10 ? s.slice(0, 10) : s;
};

const loadPetList = async () => {
  try {
    const res = await getPetList();
    petList.value = res.data || [];
    if (!currentPetId.value && petList.value.length) {
      currentPetId.value = petList.value[0].id;
    }
  } catch (e) {
    console.error('[成长相册] 获取宠物列表失败:', e?.code || e?.msg || e);
    petList.value = [];
  }
};

const loadAlbumList = async () => {
  if (!currentPetId.value) {
    albumList.value = [];
    loading.value = false;
    return;
  }
  loading.value = true;
  try {
    const res = await getAlbumList(currentPetId.value);
    albumList.value = res.data || [];
  } catch (e) {
    console.error('[成长相册] 获取列表失败:', e?.code || e?.msg || e);
    albumList.value = [];
  } finally {
    loading.value = false;
  }
};

const switchPet = (petId) => {
  if (String(currentPetId.value) === String(petId)) return;
  currentPetId.value = petId;
  loadAlbumList();
};

const goEdit = (id) => {
  const query = [`petId=${currentPetId.value}`];
  if (id) query.push(`id=${id}`);
  uni.navigateTo({ url: `/pages/pet/album-edit?${query.join('&')}` });
};

const handleDelete = async (id) => {
  const ok = await showConfirm('确定删除这条成长记录吗？删除后不可恢复');
  if (!ok) return;
  try {
    await deleteAlbum(id);
    showToast('已删除', 'success');
    await loadAlbumList();
  } catch (e) {
    showToast(e?.msg || '删除失败');
  }
};

const goPetList = () => uni.switchTab({ url: '/pages/pet/list' });

onLoad(async (options = {}) => {
  await loadPetList();
  if (options.petId) {
    currentPetId.value = options.petId;
  }
  await loadAlbumList();
});

// 从编辑页返回时刷新列表
onShow(() => {
  if (currentPetId.value) loadAlbumList();
});
</script>

<style lang="scss" scoped>
.album-page {
  min-height: 100vh;
  background-color: $bg-page;
}

/* ===== 宠物切换 ===== */
.pet-strip {
  white-space: nowrap;
  padding: 20rpx 0 4rpx;

  .pet-strip-inner {
    display: inline-flex;
    gap: 16rpx;
    padding: 0 24rpx;
  }

  .pet-chip {
    padding: 12rpx 32rpx;
    border-radius: $radius-round;
    background-color: #fff;
    box-shadow: $shadow-sm;
    transition: all 0.25s ease;

    .pet-chip-text {
      font-size: $font-sm;
      color: $text-secondary;
    }

    &.active {
      background: $gradient-primary;
      box-shadow: $shadow-primary;

      .pet-chip-text {
        color: #fff;
        font-weight: $font-weight-bold;
      }
    }
  }
}

/* ===== 隐私说明 ===== */
.privacy-tip {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin: 24rpx 24rpx 0;
  padding: 18rpx 24rpx;
  border-radius: $radius-md;
  background: $primary-lighter;

  .privacy-text {
    flex: 1;
    font-size: $font-xs;
    color: $primary-dark;
    line-height: 1.6;
  }
}

/* ===== 相册列表 ===== */
.album-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
  padding: 24rpx;
}

.album-card {
  display: flex;
  gap: 22rpx;
  padding: 24rpx;
  border-radius: $radius-lg;
  background-color: #fff;
  box-shadow: $shadow-card;
  animation: pet-fadeInUp 0.36s ease both;

  .album-cover {
    position: relative;
    width: 190rpx;
    height: 190rpx;
    border-radius: $radius-md;
    overflow: hidden;
    flex-shrink: 0;
    background-color: $bg-input;

    .cover-img {
      width: 100%;
      height: 100%;
    }

    .cover-empty {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      background: $gradient-card;
    }

    .cover-badge {
      position: absolute;
      right: 10rpx;
      bottom: 10rpx;
      display: flex;
      align-items: center;
      gap: 4rpx;
      padding: 4rpx 12rpx;
      border-radius: $radius-round;
      background: rgba(0, 0, 0, 0.45);

      .badge-text {
        font-size: $font-xs;
        color: #fff;
      }
    }
  }

  .album-info {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;

    .album-title {
      font-size: $font-md;
      font-weight: $font-weight-bold;
      color: $text-primary;
      line-height: 1.45;
    }

    .album-desc {
      margin-top: 10rpx;
      font-size: $font-xs;
      color: $text-secondary;
      line-height: 1.6;
    }

    .album-foot {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-top: auto;
      padding-top: 16rpx;

      .album-date {
        font-size: $font-xs;
        color: $text-hint;
      }

      .album-del {
        width: 52rpx;
        height: 52rpx;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }
  }
}

/* ===== 空状态 ===== */
.empty-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 140rpx 72rpx 0;

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

/* ===== 悬浮添加按钮 ===== */
.fab-btn {
  position: fixed;
  right: 40rpx;
  bottom: calc(env(safe-area-inset-bottom) + 60rpx);
  width: 108rpx;
  height: 108rpx;
  background: $gradient-primary;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: $shadow-primary;
  z-index: 300;
  animation: pet-pulse 2.4s ease-out infinite;
}

.safe-bottom {
  height: calc(env(safe-area-inset-bottom) + 180rpx);
}
</style>
