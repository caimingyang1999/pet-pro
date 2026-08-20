<template>
  <view class="page-container">
    <!-- 宠物头像 -->
    <view class="avatar-section">
      <view class="avatar-wrap" @click="handleChooseAvatar">
        <image
          class="avatar-img"
          :src="fullImageUrl(form.avatar) || '/static/default-pet.png'"
          mode="aspectFill"
        />
        <view class="avatar-mask">
          <template v-if="avatarUploading">
            <u-loading-icon color="#fff" size="20" />
            <text class="avatar-tip">上传中</text>
          </template>
          <template v-else>
            <u-icon name="camera" color="#fff" size="24" />
            <text class="avatar-tip">{{ form.avatar ? '更换头像' : '添加头像' }}</text>
          </template>
        </view>
      </view>
    </view>

    <!-- 基本信息表单 -->
    <view class="form-card">
      <!-- 宠物名称 -->
      <view class="form-row" :class="{ 'form-error': errors.name }">
        <text class="form-label required">宠物名称</text>
        <input
          class="form-input"
          v-model="form.name"
          placeholder="给你的宝贝起个名字"
          maxlength="20"
        />
      </view>
      <text class="error-text" v-if="errors.name">{{ errors.name }}</text>

      <!-- 品种 -->
      <view class="form-row" :class="{ 'form-error': errors.breed }">
        <text class="form-label required">品种</text>
        <input
          class="form-input"
          v-model="form.breed"
          placeholder="如：金毛、英短、柯基"
          maxlength="30"
        />
      </view>
      <text class="error-text" v-if="errors.breed">{{ errors.breed }}</text>

      <!-- 性别 -->
      <view class="form-row">
        <text class="form-label">性别</text>
        <view class="radio-group">
          <view
            class="radio-option"
            :class="{ active: form.gender === '1' }"
            @click="form.gender = '1'"
          >
            <text class="radio-icon" :class="{ checked: form.gender === '1' }">
              {{ form.gender === '1' ? '✓' : '' }}
            </text>
            <text class="radio-text">公 ♂</text>
          </view>
          <view
            class="radio-option"
            :class="{ active: form.gender === '0' }"
            @click="form.gender = '0'"
          >
            <text class="radio-icon" :class="{ checked: form.gender === '0' }">
              {{ form.gender === '0' ? '✓' : '' }}
            </text>
            <text class="radio-text">母 ♀</text>
          </view>
        </view>
      </view>

      <!-- 毛色 -->
      <view class="form-row">
        <text class="form-label">毛色</text>
        <input
          class="form-input"
          v-model="form.color"
          placeholder="如：金色、黑白、虎斑"
          maxlength="20"
        />
      </view>

      <!-- 体重 -->
      <view class="form-row">
        <text class="form-label">体重(kg)</text>
        <input
          class="form-input"
          v-model="form.weight"
          placeholder="请输入体重"
          type="digit"
        />
      </view>

      <!-- 出生日期 -->
      <picker mode="date" :value="form.birthday" @change="onBirthdayChange">
        <view class="form-row">
          <text class="form-label">出生日期</text>
          <view class="form-input form-picker">
            <text :class="{ placeholder: !form.birthday }">
              {{ form.birthday || '请选择出生日期' }}
            </text>
            <u-icon name="arrow-right" color="#C0C4CC" size="16" />
          </view>
        </view>
      </picker>

      <!-- 绝育状态 -->
      <view class="form-row">
        <text class="form-label">绝育状态</text>
        <view class="radio-group">
          <view
            class="radio-option"
            :class="{ active: form.sterilization === '0' }"
            @click="form.sterilization = '0'"
          >
            <text class="radio-icon" :class="{ checked: form.sterilization === '0' }">
              {{ form.sterilization === '0' ? '✓' : '' }}
            </text>
            <text class="radio-text">未绝育</text>
          </view>
          <view
            class="radio-option"
            :class="{ active: form.sterilization === '1' }"
            @click="form.sterilization = '1'"
          >
            <text class="radio-icon" :class="{ checked: form.sterilization === '1' }">
              {{ form.sterilization === '1' ? '✓' : '' }}
            </text>
            <text class="radio-text">已绝育</text>
          </view>
        </view>
      </view>

      <!-- 备注 -->
      <view class="form-row form-row-vertical">
        <text class="form-label">备注</text>
        <textarea
          class="form-textarea"
          v-model="form.remark"
          placeholder="记录宠物的性格、习惯、小故事…"
          maxlength="200"
        />
        <text class="textarea-count">{{ (form.remark || '').length }}/200</text>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="submit-bar">
      <view
        class="submit-btn submit-btn-primary"
        :class="{ disabled: submitting }"
        @click="handleSubmit"
      >
        <text v-if="!submitting">{{ isEdit ? '保存修改' : '添加宠物' }}</text>
        <text v-else>提交中...</text>
      </view>
      <view
        v-if="isEdit"
        class="submit-btn submit-btn-danger"
        :class="{ disabled: deleting }"
        @click="handleDelete"
      >
        <text v-if="!deleting">删除宠物</text>
        <text v-else>删除中...</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { addPet, updatePet, deletePet, getPetDetail } from '@/api/pet.js';
import { showToast, showConfirm, showLoading, hideLoading, fullImageUrl } from '@/utils/index.js';
import { SERVER_BASE } from '@/api/request.js';

// RuoYi 通用上传接口
const UPLOAD_URL = `${SERVER_BASE}/common/upload`;

const form = ref({
  name: '',
  avatar: '',
  breed: '',
  gender: '1',
  weight: '',
  color: '',
  birthday: '',
  sterilization: '0',
  remark: '',
});

const errors = ref({ name: '', breed: '' });

const submitting = ref(false);
const deleting = ref(false);
const isEdit = ref(false);
const petId = ref('');
const avatarUploading = ref(false);

/** 出生日期选择 */
const onBirthdayChange = (e) => {
  form.value.birthday = e.detail.value;
};

/** 选择/更换头像 */
const handleChooseAvatar = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      const tempPath = res.tempFilePaths[0];
      form.value.avatar = tempPath;
      uploadAvatar(tempPath);
    },
  });
};

/** 上传头像到服务器 */
const uploadAvatar = (filePath) => {
  avatarUploading.value = true;
  const token = uni.getStorageSync('token') || '';
  uni.uploadFile({
    url: UPLOAD_URL,
    filePath,
    name: 'file',
    header: {
      Authorization: `Bearer ${token}`,
    },
    success: (uploadRes) => {
      try {
        const data = JSON.parse(uploadRes.data);
        if (data.code === 200) {
          const url = data.url || data.data?.url || data.fileName || '';
          if (url) {
            form.value.avatar = url;
          } else {
            showToast('服务器未返回图片地址');
          }
        } else {
          showToast(data.msg || '头像上传失败');
        }
      } catch {
        showToast('头像上传失败');
      }
    },
    fail: () => {
      showToast('头像上传失败，请重试');
    },
    complete: () => {
      avatarUploading.value = false;
    },
  });
};

/** 表单校验 */
const validate = () => {
  errors.value = { name: '', breed: '' };
  if (!form.value.name.trim()) {
    errors.value.name = '请输入宠物名称';
    showToast('请输入宠物名称');
    return false;
  }
  if (!form.value.breed.trim()) {
    errors.value.breed = '请输入品种';
    showToast('请输入品种');
    return false;
  }
  return true;
};

/** 提交表单 */
const handleSubmit = async () => {
  if (submitting.value) return; // 防重复提交
  if (!validate()) return;

  submitting.value = true;
  showLoading('提交中...');
  try {
    const payload = {
      name: form.value.name.trim(),
      breed: form.value.breed.trim(),
      gender: form.value.gender,
      sterilization: form.value.sterilization,
      avatar: form.value.avatar || undefined,
      weight: form.value.weight ? Number(form.value.weight) : undefined,
      color: form.value.color || undefined,
      birthday: form.value.birthday || undefined,
      remark: form.value.remark || undefined,
    };

    const res = isEdit.value
      ? await updatePet(petId.value, payload)
      : await addPet(payload);

    // request.js 已包装成 data.code 风格，这里再兜一层保证
    const code = res?.code;
    if (code === 200 || code === 0 || code === undefined) {
      const msg = isEdit.value ? '修改成功' : '添加成功';
      showToast(msg, 'success');
      setTimeout(() => {
        uni.navigateBack();
      }, 1500);
    } else {
      showToast(res?.msg || '提交失败');
    }
  } catch (err) {
    // request.js 已统一 toast，这里兜底避免静默失败
    const msg = (err && (err.message || err.msg)) || '提交失败，请重试';
    showToast(msg);
  } finally {
    hideLoading();
    submitting.value = false;
  }
};

/** 删除宠物 */
const handleDelete = async () => {
  if (deleting.value) return;
  const confirmed = await showConfirm('确认删除该宠物吗？删除后无法恢复。');
  if (!confirmed) return;
  deleting.value = true;
  showLoading('删除中...');
  try {
    const res = await deletePet(petId.value);
    const code = res?.code;
    if (code === 200 || code === 0 || code === undefined) {
      showToast('删除成功', 'success');
      setTimeout(() => {
        uni.navigateBack();
      }, 1500);
    } else {
      showToast(res?.msg || '删除失败');
    }
  } catch (err) {
    const msg = (err && (err.message || err.msg)) || '删除失败，请重试';
    showToast(msg);
  } finally {
    hideLoading();
    deleting.value = false;
  }
};

onMounted(() => {
  const pages = getCurrentPages();
  const current = pages[pages.length - 1];
  const { id } = current.options || current.$route?.query || {};
  if (id) {
    isEdit.value = true;
    petId.value = id;
    uni.setNavigationBarTitle({ title: '编辑宠物' });
    getPetDetail(id).then((res) => {
      const data = res.data || {};
      form.value = {
        name: data.name || '',
        avatar: data.avatar || '',
        breed: data.breed || '',
        gender: String(data.gender ?? '1'),
        weight: data.weight != null ? String(data.weight) : '',
        color: data.color || '',
        birthday: data.birthday || '',
        sterilization: String(data.sterilization ?? '0'),
        remark: data.remark || '',
      };
    }).catch(() => {});
  } else {
    uni.setNavigationBarTitle({ title: '添加宠物' });
  }
});
</script>

<style lang="scss" scoped>
.page-container {
  padding: 0 30rpx 60rpx;
  min-height: 100vh;
  background-color: $pet-bg;
}

/* ========== 头像区 ========== */
.avatar-section {
  display: flex;
  justify-content: center;
  padding: 40rpx 0 30rpx;

  .avatar-wrap {
    position: relative;
    width: 160rpx;
    height: 160rpx;
    border-radius: 50%;
    overflow: hidden;

    .avatar-img {
      width: 100%;
      height: 100%;
      border-radius: 50%;
    }

    .avatar-mask {
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      height: 50rpx;
      background: rgba(0, 0, 0, 0.4);
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 6rpx;

      .avatar-tip {
        font-size: 20rpx;
        color: #fff;
      }
    }
  }
}

/* ========== 表单卡片 ========== */
.form-card {
  background-color: $pet-bg-white;
  border-radius: 16rpx;
  padding: 12rpx 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.form-row {
  display: flex;
  align-items: center;
  min-height: 96rpx;
  padding: 18rpx 0;
  border-bottom: 1rpx solid $pet-border-lighter;

  &:last-child {
    border-bottom: none;
  }

  &.form-error {
    border-bottom-color: $pet-danger;
  }

  &.form-row-vertical {
    flex-direction: column;
    align-items: flex-start;
    gap: 12rpx;

    .form-label {
      margin-bottom: 0;
    }
  }
}

.form-label {
  width: 160rpx;
  font-size: 28rpx;
  color: $pet-text-main;
  flex-shrink: 0;

  &.required::after {
    content: '*';
    color: $pet-danger;
    margin-left: 4rpx;
  }
}

.form-input {
  flex: 1;
  font-size: 28rpx;
  color: $pet-text-main;
  height: 60rpx;
  line-height: 60rpx;

  &.form-picker {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 60rpx;
    line-height: 60rpx;

    .placeholder {
      color: $pet-text-placeholder;
    }
  }
}

.form-textarea {
  width: 100%;
  min-height: 160rpx;
  font-size: 28rpx;
  color: $pet-text-main;
  line-height: 1.6;
}

.textarea-count {
  align-self: flex-end;
  font-size: 22rpx;
  color: $pet-text-placeholder;
}

.error-text {
  display: block;
  font-size: 22rpx;
  color: $pet-danger;
  padding: 8rpx 0 0 160rpx;
  margin-bottom: 8rpx;
}

/* ========== 单选组（自定义） ========== */
.radio-group {
  display: flex;
  gap: 24rpx;
}

.radio-option {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 10rpx 20rpx;
  border-radius: 8rpx;
  border: 2rpx solid $pet-border-lighter;
  background-color: $pet-bg;

  &.active {
    border-color: $pet-primary;
    background-color: $pet-primary-light;
  }

  .radio-icon {
    width: 32rpx;
    height: 32rpx;
    border-radius: 50%;
    border: 2rpx solid $pet-border;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20rpx;
    color: transparent;

    &.checked {
      border-color: $pet-primary;
      background-color: $pet-primary;
      color: #fff;
    }
  }

  .radio-text {
    font-size: 28rpx;
    color: $pet-text-regular;
  }

  &.active .radio-text {
    color: $pet-primary;
    font-weight: 500;
  }
}

/* ========== 按钮 ========== */
.submit-bar {
  margin-top: 50rpx;
  margin-bottom: 40rpx;
  padding: 0 20rpx;
}

.submit-btn {
  height: 88rpx;
  line-height: 88rpx;
  text-align: center;
  border-radius: 44rpx;
  font-size: 30rpx;
  color: #fff;
  font-weight: 500;

  &:active {
    opacity: 0.85;
  }

  &.disabled {
    opacity: 0.6;
    pointer-events: none;
  }

  &.submit-btn-primary {
    background: linear-gradient(135deg, #FF8C42 0%, #FF6B1A 100%);
    box-shadow: 0 8rpx 20rpx rgba(255, 140, 66, 0.3);
  }

  &.submit-btn-danger {
    margin-top: 24rpx;
    background: #FF4D4F;
    box-shadow: 0 8rpx 20rpx rgba(255, 77, 79, 0.25);
  }
}
</style>
