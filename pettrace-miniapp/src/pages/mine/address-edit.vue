<template>
  <view class="page-container">
    <!-- 编辑模式加载 -->
    <LoadingState v-if="loading" mode="skeleton" type="post" :count="2" />

    <template v-else>
      <view class="form-card">
        <!-- 收件人 -->
        <view class="form-row" :class="{ 'form-error': errors.receiverName }">
          <text class="form-label required">收件人</text>
          <input
            class="form-input"
            v-model="form.receiverName"
            placeholder="请输入收货人姓名"
            maxlength="20"
          />
        </view>
        <text class="error-text" v-if="errors.receiverName">{{ errors.receiverName }}</text>

        <!-- 联系电话 -->
        <view class="form-row" :class="{ 'form-error': errors.receiverPhone }">
          <text class="form-label required">联系电话</text>
          <input
            class="form-input"
            v-model="form.receiverPhone"
            type="number"
            maxlength="11"
            placeholder="请输入手机号码"
          />
        </view>
        <text class="error-text" v-if="errors.receiverPhone">{{ errors.receiverPhone }}</text>

        <!-- 省 / 市 / 区 一行三列 -->
        <view class="form-row region-row">
          <view class="region-item">
            <text class="form-label">省份</text>
            <input
              class="form-input"
              v-model="form.province"
              placeholder="如：浙江省"
              maxlength="20"
            />
          </view>
          <view class="region-item">
            <text class="form-label">城市</text>
            <input
              class="form-input"
              v-model="form.city"
              placeholder="如：杭州市"
              maxlength="20"
            />
          </view>
          <view class="region-item">
            <text class="form-label">区县</text>
            <input
              class="form-input"
              v-model="form.district"
              placeholder="如：西湖区"
              maxlength="20"
            />
          </view>
        </view>

        <!-- 详细地址 -->
        <view class="form-row form-row-vertical" :class="{ 'form-error': errors.detailAddress }">
          <text class="form-label required">详细地址</text>
          <textarea
            class="form-textarea"
            v-model="form.detailAddress"
            placeholder="街道、小区、楼栋、门牌号等"
            :maxlength="100"
            auto-height
          />
        </view>
        <text class="error-text" v-if="errors.detailAddress">{{ errors.detailAddress }}</text>

        <!-- 设为默认 -->
        <view class="form-row default-row">
          <view class="default-left">
            <text class="form-label">设为默认地址</text>
            <text class="default-tip">下单时优先使用该地址</text>
          </view>
          <switch
            :checked="form.isDefault"
            color="#FF8C42"
            @change="onDefaultChange"
          />
        </view>
      </view>

      <view class="bottom-bar">
        <view class="save-btn" :class="{ disabled: saving }" @click="handleSave">
          <text>{{ saving ? '保存中...' : '保存' }}</text>
        </view>
      </view>
    </template>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import LoadingState from '@/components/LoadingState.vue';
import { getAddressList, addAddress, updateAddress } from '@/api/user.js';
import { showToast, showLoading, hideLoading } from '@/utils/index.js';

const addressId = ref('');
const loading = ref(false);
const saving = ref(false);

const form = reactive({
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  isDefault: false,
});

const errors = reactive({
  receiverName: '',
  receiverPhone: '',
  detailAddress: '',
});

onLoad(async (query) => {
  addressId.value = query?.id || '';
  if (addressId.value) {
    uni.setNavigationBarTitle({ title: '编辑收货地址' });
    await loadAddress(addressId.value);
  } else {
    uni.setNavigationBarTitle({ title: '添加收货地址' });
  }
});

const loadAddress = async (id) => {
  loading.value = true;
  try {
    const res = await getAddressList();
    const list = res.data || [];
    const target = list.find((item) => String(item.id) === String(id));
    if (!target) {
      showToast('地址不存在');
      setTimeout(() => uni.navigateBack(), 1200);
      return;
    }
    form.receiverName = target.receiverName || '';
    form.receiverPhone = target.receiverPhone || '';
    form.province = target.province || '';
    form.city = target.city || '';
    form.district = target.district || '';
    form.detailAddress = target.detailAddress || '';
    form.isDefault = String(target.isDefault) === '1';
  } catch (err) {
    console.error('[地址编辑] 加载失败:', err?.msg);
  } finally {
    loading.value = false;
  }
};

const onDefaultChange = (e) => {
  form.isDefault = e.detail.value;
};

const validate = () => {
  errors.receiverName = form.receiverName.trim() ? '' : '请输入收件人姓名';
  errors.receiverPhone = /^1[3-9]\d{9}$/.test(form.receiverPhone.trim())
    ? ''
    : '请输入正确的11位手机号';
  errors.detailAddress = form.detailAddress.trim() ? '' : '请输入详细地址';
  return !errors.receiverName && !errors.receiverPhone && !errors.detailAddress;
};

const handleSave = async () => {
  if (saving.value) return;
  if (!validate()) return;

  const payload = {
    receiverName: form.receiverName.trim(),
    receiverPhone: form.receiverPhone.trim(),
    province: form.province.trim(),
    city: form.city.trim(),
    district: form.district.trim(),
    detailAddress: form.detailAddress.trim(),
    isDefault: form.isDefault ? '1' : '0',
  };

  saving.value = true;
  showLoading('保存中...');
  try {
    if (addressId.value) {
      await updateAddress(addressId.value, payload);
    } else {
      await addAddress(payload);
    }
    showToast(addressId.value ? '修改成功' : '添加成功', 'success');
    setTimeout(() => uni.navigateBack(), 800);
  } catch (err) {
    showToast(err?.msg || '保存失败');
  } finally {
    saving.value = false;
    hideLoading();
  }
};
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  padding: 20rpx 20rpx 160rpx;
  background-color: $pet-bg;
}

.form-card {
  @include pet-card;
}

.form-row {
  display: flex;
  align-items: center;
  min-height: 96rpx;
  border-bottom: 1rpx solid $pet-border-lighter;
  padding: 20rpx 0;

  &:last-child {
    border-bottom: none;
  }

  &.form-error {
    .form-label,
    .form-input {
      color: $pet-danger;
    }
  }

  .form-label {
    width: 160rpx;
    flex-shrink: 0;
    font-size: 28rpx;
    color: $pet-text-main;
    font-weight: 500;

    &.required::before {
      content: '*';
      color: $pet-danger;
      margin-right: 6rpx;
    }
  }

  .form-input {
    flex: 1;
    font-size: 28rpx;
    color: $pet-text-main;
  }

  &.form-row-vertical {
    flex-direction: column;
    align-items: flex-start;

    .form-label {
      width: auto;
      margin-bottom: 16rpx;
    }

    .form-textarea {
      width: 100%;
      min-height: 100rpx;
      font-size: 28rpx;
      color: $pet-text-main;
      line-height: 1.5;
    }
  }
}

.region-row {
  display: flex;

  .region-item {
    flex: 1;
    display: flex;
    flex-direction: column;

    & + .region-item {
      margin-left: 20rpx;
    }

    .form-label {
      width: auto;
      font-size: 24rpx;
      color: $pet-text-secondary;
      margin-bottom: 8rpx;
    }

    .form-input {
      width: 100%;
      font-size: 26rpx;
    }
  }
}

.default-row {
  .default-left {
    flex: 1;
    display: flex;
    flex-direction: column;

    .form-label {
      width: auto;
      margin-bottom: 6rpx;
    }

    .default-tip {
      font-size: 22rpx;
      color: $pet-text-placeholder;
    }
  }
}

.error-text {
  display: block;
  font-size: 22rpx;
  color: $pet-danger;
  padding: 8rpx 0 4rpx;
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

  .save-btn {
    height: 88rpx;
    border-radius: 44rpx;
    background: linear-gradient(135deg, #FF8C42, #FFA940);
    @include pet-flex-center;
    box-shadow: 0 6rpx 20rpx rgba(255, 140, 66, 0.35);

    text {
      font-size: 30rpx;
      color: #fff;
      font-weight: 600;
    }

    &:active {
      opacity: 0.9;
      transform: scale(0.98);
    }

    &.disabled {
      background: #E0E0E0;
      box-shadow: none;
    }
  }
}
</style>
