<template>
  <view class="page-container">
    <!-- 加载骨架 -->
    <LoadingState v-if="loading && !addressList.length" mode="skeleton" type="list" :count="3" />

    <template v-else>
      <!-- 地址列表 -->
      <view class="address-list">
        <view
          class="address-card"
          v-for="addr in addressList"
          :key="addr.id"
          @click="onCardClick(addr)"
        >
          <view class="card-main">
            <view class="top">
              <text class="name">{{ addr.receiverName }}</text>
              <text class="phone">{{ maskPhone(addr.receiverPhone) }}</text>
              <view class="tag-default" v-if="isDefault(addr)">
                <text>默认</text>
              </view>
              <view v-if="selectMode" class="radio-box" :class="{ checked: isSelected(addr) }">
                <text class="radio-dot" v-if="isSelected(addr)"></text>
              </view>
            </view>
            <text class="detail">{{ fullAddress(addr) }}</text>
          </view>

          <view class="actions" @click.stop>
            <view
              v-if="!isDefault(addr)"
              class="action-btn"
              @click="handleSetDefault(addr)"
            >
              <Icon name="check" color="#FF8C42" size="16" />
              <text class="action-text">设为默认</text>
            </view>
            <view class="action-btn" @click="goEdit(addr)">
              <Icon name="edit" color="#999" size="16" />
              <text class="action-text">编辑</text>
            </view>
            <view class="action-btn danger" @click="handleDelete(addr)">
              <Icon name="trash" color="#F56C6C" size="16" />
              <text class="action-text">删除</text>
            </view>
          </view>
        </view>
      </view>

      <EmptyState v-if="!addressList.length" text="还没有收货地址，点击下方按钮添加" />
    </template>

    <view class="bottom-bar">
      <view class="add-btn" @click="goEdit()">
        <Icon name="plus" color="#fff" size="20" />
        <text class="add-text">{{ selectMode ? '添加地址' : '添加新地址' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import Icon from '@/components/Icon.vue';
import { ref, getCurrentInstance } from 'vue';
import { onShow, onLoad } from '@dcloudio/uni-app';
import EmptyState from '@/components/EmptyState.vue';
import LoadingState from '@/components/LoadingState.vue';
import { getAddressList, updateAddress, deleteAddress } from '@/api/user.js';
import { showToast, showConfirm } from '@/utils/index.js';

const addressList = ref([]);
const loading = ref(false);
const selectMode = ref(false);
const selectedId = ref('');

const pageProxy = getCurrentInstance()?.proxy;

onLoad((query) => {
  selectMode.value = query?.select === '1';
  if (selectMode.value) {
    uni.setNavigationBarTitle({ title: '选择收货地址' });
  }
});

onShow(() => {
  fetchList();
});

const isDefault = (addr) => String(addr.isDefault) === '1';
const isSelected = (addr) => String(selectedId.value) === String(addr.id);

const fullAddress = (addr) => {
  return [addr.province, addr.city, addr.district, addr.detailAddress]
    .filter(Boolean)
    .join('');
};

// 手机号脱敏展示
const maskPhone = (phone) => {
  if (!phone) return '';
  const str = String(phone);
  return str.length === 11 ? `${str.slice(0, 3)}****${str.slice(7)}` : str;
};

const fetchList = async () => {
  loading.value = true;
  try {
    const res = await getAddressList();
    const list = res.data || [];
    // 默认地址排最前（接口已排序，这里兜底）
    addressList.value = list.sort((a, b) => {
      const an = isDefault(a) ? 0 : 1;
      const bn = isDefault(b) ? 0 : 1;
      return an - bn;
    });
    // 选择模式下默认选中默认地址（或第一个）
    if (selectMode.value && !selectedId.value && list.length) {
      const defaultAddr = list.find((a) => isDefault(a));
      selectedId.value = defaultAddr?.id ?? list[0].id;
    }
  } catch (err) {
    console.error('[地址列表] 加载失败:', err?.msg);
  } finally {
    loading.value = false;
  }
};

// 点击卡片：选择模式下回传地址，普通模式进入编辑
const onCardClick = (addr) => {
  if (!selectMode.value) {
    goEdit(addr);
    return;
  }
  selectedId.value = addr.id;
  const eventChannel = pageProxy?.getOpenerEventChannel?.();
  if (eventChannel && eventChannel.emit) {
    eventChannel.emit('selectAddress', addr);
    uni.navigateBack();
  }
};

const goEdit = (addr) => {
  const url = addr?.id
    ? `/pages/mine/address-edit?id=${addr.id}${selectMode.value ? '&select=1' : ''}`
    : `/pages/mine/address-edit${selectMode.value ? '?select=1' : ''}`;
  uni.navigateTo({ url });
};

const handleSetDefault = async (addr) => {
  const confirm = await showConfirm('将该地址设为默认收货地址？');
  if (!confirm) return;
  try {
    await updateAddress(addr.id, { isDefault: '1' });
    showToast('已设为默认地址', 'success');
    fetchList();
  } catch (err) {
    showToast(err?.msg || '设置失败');
  }
};

const handleDelete = async (addr) => {
  const confirm = await showConfirm('确认删除该地址？');
  if (!confirm) return;
  try {
    await deleteAddress(addr.id);
    showToast('删除成功', 'success');
    fetchList();
  } catch (err) {
    showToast(err?.msg || '删除失败');
  }
};
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  padding: 20rpx 20rpx 160rpx;
  background-color: $pet-bg;
}

.address-list {
  .address-card {
    @include pet-card;
    margin-bottom: 20rpx;

    .card-main {
      .top {
        display: flex;
        align-items: center;
        margin-bottom: 12rpx;

        .name {
          font-size: 30rpx;
          color: $pet-text-main;
          font-weight: 600;
          margin-right: 16rpx;
        }

        .phone {
          font-size: 26rpx;
          color: $pet-text-secondary;
          margin-right: 16rpx;
        }

        .tag-default {
          padding: 4rpx 14rpx;
          border-radius: 8rpx;
          background: $pet-primary-light;

          text {
            font-size: 22rpx;
            color: $pet-primary;
            font-weight: 500;
          }
        }

        .radio-box {
          margin-left: auto;
          width: 36rpx;
          height: 36rpx;
          border-radius: 50%;
          border: 2rpx solid $pet-border;
          background: #fff;
          @include pet-flex-center;

          &.checked {
            border-color: $pet-primary;
            background: $pet-primary;
          }

          .radio-dot {
            width: 12rpx;
            height: 12rpx;
            border-radius: 50%;
            background: #fff;
          }
        }
      }

      .detail {
        display: block;
        font-size: 26rpx;
        color: $pet-text-regular;
        line-height: 1.5;
      }
    }

    .actions {
      display: flex;
      justify-content: flex-end;
      align-items: center;
      gap: 32rpx;
      margin-top: 20rpx;
      padding-top: 16rpx;
      border-top: 1rpx solid $pet-border-lighter;

      .action-btn {
        display: flex;
        align-items: center;

        .action-text {
          font-size: 24rpx;
          color: $pet-text-secondary;
          margin-left: 6rpx;
        }

        &.danger {
          .action-text {
            color: $pet-danger;
          }
        }
      }
    }
  }
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 30rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  background-color: #fff;
  border-top: 1rpx solid $pet-border-lighter;

  .add-btn {
    height: 88rpx;
    border-radius: 44rpx;
    background: linear-gradient(135deg, #FF8C42, #FFA940);
    @include pet-flex-center;
    box-shadow: 0 6rpx 20rpx rgba(255, 140, 66, 0.35);

    &:active {
      opacity: 0.9;
      transform: scale(0.98);
    }

    .add-text {
      font-size: 30rpx;
      color: #fff;
      font-weight: 600;
      margin-left: 10rpx;
    }
  }
}
</style>
