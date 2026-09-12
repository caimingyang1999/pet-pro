<template>
  <view class="album-edit-page">
    <!-- 图片区 -->
    <view class="form-card">
      <view class="card-title">
        <text class="title-text">照片</text>
        <text class="title-tip">{{ imageList.length }}/9</text>
      </view>

      <view class="image-grid">
        <view class="image-item" v-for="(img, idx) in imageList" :key="idx">
          <image class="grid-img" :src="img.src" mode="aspectFill" @click="previewImage(idx)" />
          <view class="img-delete" @click.stop="removeImage(idx)">
            <Icon name="close" :size="12" color="#fff" />
          </view>
          <view class="img-loading" v-if="img.uploading">
            <view class="loading-ring" />
          </view>
        </view>

        <view
          class="image-item add-item"
          v-if="imageList.length < 9"
          @click="chooseImage"
        >
          <Icon name="camera" :size="26" color="#FF8C42" />
          <text class="add-text">添加照片</text>
        </view>
      </view>
    </view>

    <!-- 文字区 -->
    <view class="form-card">
      <view class="form-row">
        <text class="form-label">标题</text>
        <input
          v-model="form.title"
          class="form-input"
          type="text"
          maxlength="30"
          placeholder="如：第一次学会握手"
          placeholder-class="input-placeholder"
        />
      </view>

      <view class="form-row">
        <text class="form-label">记录日期</text>
        <picker mode="date" :value="form.recordDate" :end="today" @change="onDateChange">
          <view class="picker-value">
            <text>{{ form.recordDate }}</text>
            <Icon name="chevron_right" :size="14" color="#C0C4CE" />
          </view>
        </picker>
      </view>

      <view class="form-textarea">
        <textarea
          v-model="form.content"
          class="textarea-input"
          maxlength="200"
          placeholder="写点什么吧，比如它当时的样子、你的心情…"
          placeholder-class="input-placeholder"
        />
        <text class="textarea-count">{{ (form.content || '').length }}/200</text>
      </view>
    </view>

    <!-- 隐私提示 -->
    <view class="privacy-tip">
      <Icon name="locked" :size="13" color="#8A8D9A" />
      <text class="privacy-text">这条记录仅你自己可见，不会对外公开</text>
    </view>

    <!-- 操作按钮 -->
    <view class="action-wrap">
      <view class="submit-btn pet-press" :class="{ loading: submitting }" @click="handleSubmit">
        <text>{{ submitting ? '保存中...' : '保存记录' }}</text>
      </view>
      <view class="delete-btn pet-press" v-if="recordId" @click="handleDelete">
        <text>删除这条记录</text>
      </view>
    </view>

    <view class="safe-bottom" />
  </view>
</template>

<script setup>
import Icon from '@/components/Icon.vue';
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { addAlbum, getAlbumDetail, updateAlbum, deleteAlbum } from '@/api/health.js';
import { showToast, showConfirm, showLoading, hideLoading, fullImageUrl } from '@/utils/index.js';
import { SERVER_BASE, pickUploadedPath } from '@/api/request.js';

const UPLOAD_URL = `${SERVER_BASE}/common/upload`;

const petId = ref('');
const recordId = ref('');
const submitting = ref(false);

/** 图片项：{ src-展示用地址, path-服务端相对路径, uploading-是否上传中 } */
const imageList = ref([]);

const form = ref({
  title: '',
  content: '',
  recordDate: '',
});

const today = computed(() => {
  const d = new Date();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${d.getFullYear()}-${m}-${day}`;
});

/* ==================== 图片 ==================== */
const chooseImage = () => {
  const remain = 9 - imageList.value.length;
  uni.chooseImage({
    count: remain,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      const paths = res.tempFilePaths || [];
      const start = imageList.value.length;
      paths.forEach((p) => imageList.value.push({ src: p, path: '', uploading: true }));
      await uploadImages(paths, start);
    },
  });
};

const uploadImages = async (files, startIndex) => {
  const token = uni.getStorageSync('token') || '';
  let failCount = 0;
  for (let i = 0; i < files.length; i++) {
    const target = imageList.value[startIndex + i];
    if (!target) continue;
    try {
      const url = await new Promise((resolve, reject) => {
        uni.uploadFile({
          url: UPLOAD_URL,
          filePath: files[i],
          name: 'file',
          header: { Authorization: `Bearer ${token}` },
          success: (uploadRes) => {
            try {
              const data = JSON.parse(uploadRes.data);
              if (data.code === 200) {
                const imgUrl = pickUploadedPath(data);
                if (imgUrl) resolve(imgUrl);
                else reject(new Error('服务器未返回图片地址'));
              } else {
                reject(new Error(data.msg || '上传失败'));
              }
            } catch (e) {
              reject(new Error('解析失败'));
            }
          },
          fail: reject,
        });
      });
      target.path = url;
      target.src = url.startsWith('http') ? url : fullImageUrl(url);
    } catch (e) {
      console.error('[成长相册] 图片上传失败:', e);
      failCount++;
      // 上传失败的直接从列表移除，避免提交时缺图
      const idx = imageList.value.indexOf(target);
      if (idx !== -1) imageList.value.splice(idx, 1);
    } finally {
      if (target) target.uploading = false;
    }
  }
  if (failCount > 0) showToast(`${failCount} 张图片上传失败，请重试`);
};

const removeImage = (idx) => {
  imageList.value.splice(idx, 1);
};

const previewImage = (idx) => {
  const urls = imageList.value.map((i) => i.src);
  if (!urls.length) return;
  uni.previewImage({ urls, current: urls[idx] || urls[0] });
};

const onDateChange = (e) => {
  form.value.recordDate = e.detail.value;
};

/* ==================== 加载详情 ==================== */
const loadDetail = async () => {
  showLoading('加载中...');
  try {
    const res = await getAlbumDetail(recordId.value);
    const data = res.data || {};
    form.value.title = data.title || '';
    form.value.content = data.content || '';
    form.value.recordDate = data.recordDate
      ? String(data.recordDate).slice(0, 10)
      : today.value;

    let imgs = data.images;
    if (typeof imgs === 'string') {
      try { imgs = JSON.parse(imgs); } catch (e) { imgs = []; }
    }
    if (Array.isArray(imgs)) {
      imageList.value = imgs.map((p) => ({
        src: p.startsWith('http') ? p : fullImageUrl(p),
        path: p,
        uploading: false,
      }));
    }
  } catch (e) {
    showToast(e?.msg || '记录不存在');
    setTimeout(() => uni.navigateBack(), 800);
  } finally {
    hideLoading();
  }
};

/* ==================== 保存 / 删除 ==================== */
const handleSubmit = async () => {
  if (submitting.value) return;

  if (!imageList.value.length && !String(form.value.title || '').trim()) {
    showToast('请至少添加一张照片或填写标题');
    return;
  }
  if (imageList.value.some((i) => i.uploading)) {
    showToast('图片还在上传中，请稍候');
    return;
  }

  const images = imageList.value.map((i) => i.path).filter(Boolean);
  const payload = {
    petId: petId.value,
    title: String(form.value.title || '').trim(),
    content: String(form.value.content || '').trim(),
    images: JSON.stringify(images),
    recordDate: form.value.recordDate,
  };

  submitting.value = true;
  try {
    if (recordId.value) {
      await updateAlbum(recordId.value, payload);
    } else {
      await addAlbum(payload);
    }
    showToast('保存成功', 'success');
    setTimeout(() => uni.navigateBack(), 700);
  } catch (e) {
    showToast(e?.msg || '保存失败，请重试');
  } finally {
    submitting.value = false;
  }
};

const handleDelete = async () => {
  const ok = await showConfirm('确定删除这条成长记录吗？删除后不可恢复');
  if (!ok) return;
  try {
    await deleteAlbum(recordId.value);
    showToast('已删除', 'success');
    setTimeout(() => uni.navigateBack(), 700);
  } catch (e) {
    showToast(e?.msg || '删除失败');
  }
};

onLoad((options = {}) => {
  petId.value = options.petId || '';
  recordId.value = options.id || '';
  uni.setNavigationBarTitle({ title: recordId.value ? '编辑记录' : '添加记录' });

  if (recordId.value) {
    loadDetail();
  } else {
    form.value.recordDate = today.value;
  }
});
</script>

<style lang="scss" scoped>
.album-edit-page {
  min-height: 100vh;
  background-color: $bg-page;
  padding: 24rpx;
  box-sizing: border-box;
}

.form-card {
  padding: 28rpx;
  border-radius: $radius-lg;
  background-color: #fff;
  box-shadow: $shadow-card;
  margin-bottom: 24rpx;

  .card-title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 22rpx;

    .title-text {
      font-size: $font-md;
      font-weight: $font-weight-bold;
      color: $text-primary;
    }

    .title-tip {
      font-size: $font-xs;
      color: $text-hint;
    }
  }
}

/* ===== 图片网格 ===== */
.image-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 18rpx;
}

.image-item {
  position: relative;
  width: 186rpx;
  height: 186rpx;
  border-radius: $radius-md;
  overflow: hidden;
  background-color: $bg-input;

  .grid-img {
    width: 100%;
    height: 100%;
  }

  .img-delete {
    position: absolute;
    right: 0;
    top: 0;
    width: 44rpx;
    height: 44rpx;
    border-radius: 0 0 0 16rpx;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .img-loading {
    position: absolute;
    inset: 0;
    background: rgba(255, 255, 255, 0.65);
    display: flex;
    align-items: center;
    justify-content: center;

    .loading-ring {
      width: 32rpx;
      height: 32rpx;
      border-radius: 50%;
      border: 4rpx solid rgba(255, 140, 66, 0.28);
      border-top-color: $primary;
      animation: pet-spin 0.8s linear infinite;
    }
  }
}

.add-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  border: 2rpx dashed rgba(255, 140, 66, 0.4);
  background-color: rgba(255, 232, 214, 0.35);

  .add-text {
    font-size: $font-xs;
    color: $primary;
  }
}

/* ===== 表单行 ===== */
.form-row {
  display: flex;
  align-items: center;
  min-height: 100rpx;
  border-bottom: 1rpx solid $bg-input;

  .form-label {
    width: 170rpx;
    font-size: $font-md;
    color: $text-secondary;
    flex-shrink: 0;
  }

  .form-input {
    flex: 1;
    font-size: $font-md;
    color: $text-primary;
    height: 100rpx;
  }

  .picker-value {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;

    text {
      font-size: $font-md;
      color: $text-primary;
    }
  }
}

.form-textarea {
  position: relative;
  padding-top: 24rpx;

  .textarea-input {
    width: 100%;
    height: 220rpx;
    font-size: $font-md;
    color: $text-primary;
    line-height: 1.7;
  }

  .textarea-count {
    position: absolute;
    right: 0;
    bottom: 0;
    font-size: $font-xs;
    color: $text-placeholder;
  }
}

.input-placeholder {
  color: $text-placeholder;
  font-size: $font-md;
}

/* ===== 隐私提示 ===== */
.privacy-tip {
  display: flex;
  align-items: center;
  gap: 10rpx;
  padding: 20rpx 24rpx;
  border-radius: $radius-md;
  background: $primary-lighter;

  .privacy-text {
    flex: 1;
    font-size: $font-xs;
    color: $primary-dark;
    line-height: 1.6;
  }
}

/* ===== 操作 ===== */
.action-wrap {
  margin-top: 48rpx;

  .submit-btn {
    height: 100rpx;
    border-radius: $radius-round;
    background: $gradient-primary;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: $shadow-primary;

    text {
      font-size: $font-lg;
      color: #fff;
      font-weight: $font-weight-bold;
    }

    &.loading {
      opacity: 0.85;
    }
  }

  .delete-btn {
    margin-top: 24rpx;
    height: 96rpx;
    border-radius: $radius-round;
    background: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: $shadow-sm;

    text {
      font-size: $font-md;
      color: $danger;
      font-weight: $font-weight-medium;
    }
  }
}

.safe-bottom {
  height: calc(env(safe-area-inset-bottom) + 60rpx);
}
</style>
