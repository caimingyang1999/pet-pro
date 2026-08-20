<template>
  <view class="publish-page">
    <!-- 内容区 -->
    <view class="content-wrap">
      <textarea
        v-model="form.content"
        class="content-textarea"
        placeholder="分享你和爱宠的点滴..."
        placeholder-class="textarea-placeholder"
        :maxlength="500"
        :auto-height="true"
        :focus="true"
      />
      <text class="char-count">{{ form.content.length }}/500</text>
    </view>

    <!-- 图片区 -->
    <view class="image-section">
      <view class="image-list">
        <view v-for="(img, idx) in localImages" :key="idx" class="image-item">
          <image class="preview-img" :src="img" mode="aspectFill" />
          <view class="img-delete" @click="removeImage(idx)">
            <u-icon name="close-circle-fill" color="#FF4D4F" size="20" />
          </view>
        </view>
        <!-- 添加图片按钮 -->
        <view v-if="localImages.length < 9" class="add-image-btn" @click="chooseImage">
          <u-icon name="plus" color="#ccc" size="36" />
        </view>
      </view>
      <text class="image-tips">可上传9张图片（选择后自动上传）</text>
    </view>

    <!-- 底部按钮 -->
    <view class="bottom-bar">
      <view class="publish-btn" :class="{ disabled: !canPublish }" @click="handleSubmit">
        <text v-if="!submitting">发表</text>
        <text v-else>发布中...</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { addPost } from '@/api/post.js';
import { showToast, showLoading, hideLoading } from '@/utils/index.js';
import { BASE_URL, SERVER_BASE } from '@/api/request.js';

const form = ref({
  content: '',
  images: '',
});

const localImages = ref([]);
const uploadedUrls = ref([]);
const submitting = ref(false);

const canPublish = computed(() => {
  return form.value.content.trim().length > 0 && !submitting.value;
});

// 选择图片
const chooseImage = () => {
  const remain = 9 - localImages.value.length;
  if (remain <= 0) {
    showToast('最多上传9张图片');
    return;
  }
  uni.chooseImage({
    count: remain,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      localImages.value = [...localImages.value, ...res.tempFilePaths];
      // 自动上传图片
      uploadImages(res.tempFilePaths);
    },
  });
};

// RuoYi 通用上传接口（不在 /api/v1 路径下）
const UPLOAD_URL = `${SERVER_BASE}/common/upload`;

// 上传图片到服务器
const uploadImages = async (files) => {
  const token = uni.getStorageSync('token');
  let failCount = 0;
  for (let i = 0; i < files.length; i++) {
    try {
      const res = await new Promise((resolve, reject) => {
        uni.uploadFile({
          url: UPLOAD_URL,
          filePath: files[i],
          name: 'file',
          header: {
            Authorization: `Bearer ${token}`,
          },
          success: (uploadRes) => {
            try {
              const data = JSON.parse(uploadRes.data);
              // RuoYi AjaxResult 格式: { code, msg, url, fileName }
              if (data.code === 200) {
                // 优先使用相对路径(fileName)，避免完整 URL 中包含 localhost / 旧 IP 导致真机无法访问
                const imgUrl = data.fileName || data.url || '';
                if (imgUrl) {
                  resolve(imgUrl);
                } else {
                  reject(new Error('服务器未返回图片地址'));
                }
              } else {
                reject(new Error(data.msg || '上传失败'));
              }
            } catch {
              reject(new Error('解析失败'));
            }
          },
          fail: reject,
        });
      });
      uploadedUrls.value.push(res);
    } catch (err) {
      failCount++;
      console.error('上传图片失败:', err);
    }
  }
  if (failCount > 0) {
    showToast(`${failCount} 张图片上传失败`);
  }
};

// 删除图片
const removeImage = (idx) => {
  localImages.value.splice(idx, 1);
  uploadedUrls.value.splice(idx, 1);
};

// 提交发布
const handleSubmit = async () => {
  if (!form.value.content.trim()) {
    showToast('请输入动态内容');
    return;
  }
  submitting.value = true;
  try {
    // 构造 images 为 JSON 数组字符串
    form.value.images = JSON.stringify(uploadedUrls.value);
    await addPost(form.value);
    showToast('发布成功', 'success');
    setTimeout(() => {
      uni.navigateBack();
    }, 800);
  } catch (err) {
    if (err?.msg) {
      showToast(err.msg);
    }
  } finally {
    submitting.value = false;
  }
};
</script>

<style lang="scss" scoped>
.publish-page {
  min-height: 100vh;
  background-color: #fff;
  display: flex;
  flex-direction: column;
}

/* 内容区 */
.content-wrap {
  padding: 24rpx 20rpx;
  border-bottom: 1rpx solid #F0F0F0;

  .content-textarea {
    width: 100%;
    min-height: 200rpx;
    font-size: 32rpx;
    color: #1A1A1A;
    line-height: 1.6;
  }

  .textarea-placeholder {
    color: #B0B0B0;
    font-size: 32rpx;
  }

  .char-count {
    display: block;
    text-align: right;
    font-size: 24rpx;
    color: #B0B0B0;
    margin-top: 8rpx;
  }
}

/* 图片区 */
.image-section {
  padding: 20rpx;
  border-bottom: 1rpx solid #F0F0F0;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.image-item {
  position: relative;
  width: 210rpx;
  height: 210rpx;
}

.preview-img {
  width: 100%;
  height: 100%;
  border-radius: 8rpx;
  background-color: #F0F0F0;
}

.img-delete {
  position: absolute;
  top: -8rpx;
  right: -8rpx;
  width: 36rpx;
  height: 36rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border-radius: 50%;
}

.add-image-btn {
  width: 210rpx;
  height: 210rpx;
  border: 2rpx dashed #E0E0E0;
  border-radius: 8rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #FAFAFA;

  &:active {
    background-color: #F0F0F0;
  }
}

.image-tips {
  display: block;
  font-size: 24rpx;
  color: #B0B0B0;
  margin-top: 16rpx;
}

/* 底部按钮 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 24rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  background-color: #fff;
  border-top: 1rpx solid #F0F0F0;
}

.publish-btn {
  width: 100%;
  height: 80rpx;
  background: linear-gradient(135deg, #FF8C42 0%, #FF6B35 100%);
  color: #fff;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30rpx;
  font-weight: 600;
  box-shadow: 0 4rpx 16rpx rgba(255, 107, 53, 0.25);

  &:active {
    transform: scale(0.98);
  }

  &.disabled {
    background: #E0E0E0;
    color: #999;
    box-shadow: none;
    pointer-events: none;
  }
}
</style>
