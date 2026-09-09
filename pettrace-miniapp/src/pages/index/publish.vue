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

    <!-- 媒体类型切换 -->
    <view class="media-tab-bar">
      <view
        class="media-tab"
        :class="{ active: mediaType === 'image' }"
        @click="switchMediaType('image')"
      >
        <text>图片</text>
      </view>
      <view
        class="media-tab"
        :class="{ active: mediaType === 'video' }"
        @click="switchMediaType('video')"
      >
        <text>视频/实况</text>
      </view>
    </view>

    <!-- 图片区 -->
    <view v-if="mediaType === 'image'" class="image-section">
      <view class="image-list">
        <view v-for="(img, idx) in localImages" :key="idx" class="image-item">
          <image class="preview-img" :src="img" mode="aspectFill" />
          <view class="img-delete" @click="removeImage(idx)">
            <text class="delete-icon">✕</text>
          </view>
        </view>
        <!-- 添加图片按钮 -->
        <view v-if="localImages.length < 9 && !videoUrl" class="add-image-btn" @click="chooseImage">
          <text class="plus-icon">+</text>
        </view>
      </view>
      <text class="image-tips">可上传 9 张图片（选择后自动上传）</text>
    </view>

    <!-- 视频区 -->
    <view v-else class="video-section">
      <view v-if="videoUrl" class="video-preview">
        <video
          :src="localVideoPath"
          :poster="videoCover"
          class="video-player"
          controls
          show-center-play-btn
          object-fit="cover"
        />
        <view class="video-delete" @click="removeVideo">
          <text class="delete-icon">✕</text>
        </view>
        <text class="video-info">时长: {{ videoDuration }}秒 · 大小: {{ videoSize }}</text>
      </view>
      <view v-else class="add-video-btn" @click="chooseVideo">
        <text class="plus-icon">+</text>
        <text class="add-video-text">点击选择视频或实况照片</text>
      </view>
      <text class="video-tips">支持视频和 iOS 实况照片（选择后自动上传，最大 60 秒）</text>
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
import { pickUploadedPath } from '@/api/request.js';

const form = ref({
  content: '',
  images: '',
  videoUrl: '',
  videoCover: '',
});

const mediaType = ref('image'); // 'image' | 'video'
const localImages = ref([]);
const uploadedUrls = ref([]);
const localVideoPath = ref('');
const videoUrl = ref('');
const videoCover = ref('');
const videoDuration = ref(0);
const videoSize = ref('');
const submitting = ref(false);

const canPublish = computed(() => {
  return form.value.content.trim().length > 0 && !submitting.value;
});

const switchMediaType = (type) => {
  mediaType.value = type;
};

// ================= 图片相关 =================

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
      uploadImages(res.tempFilePaths);
    },
  });
};

const UPLOAD_URL = `${SERVER_BASE}/common/upload`;

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
            } catch { reject(new Error('解析失败')); }
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
  if (failCount > 0) showToast(`${failCount} 张图片上传失败`);
};

const removeImage = (idx) => {
  localImages.value.splice(idx, 1);
  uploadedUrls.value.splice(idx, 1);
};

// ================= 视频相关 =================

const chooseVideo = () => {
  uni.chooseVideo({
    sourceType: ['album', 'camera'],
    maxDuration: 60,
    compressed: true,
    camera: 'back',
    success: (res) => {
      localVideoPath.value = res.tempFilePath;
      videoDuration.value = Math.round(res.duration);
      // 格式化大小
      const sizeKB = res.size / 1024;
      videoSize.value = sizeKB > 1024 ? (sizeKB / 1024).toFixed(1) + ' MB' : sizeKB.toFixed(0) + ' KB';
      // 自动上传视频
      uploadVideo(res.tempFilePath, res.thumbTempFilePath);
    },
    fail: (err) => {
      if (err.errMsg?.includes('cancel')) {
        // 用户取消，不提示
      } else {
        showToast('选择视频失败');
      }
    },
  });
};

const uploadVideo = async (filePath, thumbPath) => {
  const token = uni.getStorageSync('token');
  showLoading('上传视频中...');
  try {
    // 上传视频
    const videoRes = await new Promise((resolve, reject) => {
      uni.uploadFile({
        url: UPLOAD_URL,
        filePath: filePath,
        name: 'file',
        header: { Authorization: `Bearer ${token}` },
        success: (uploadRes) => {
          try {
            const data = JSON.parse(uploadRes.data);
            if (data.code === 200) {
              const url = pickUploadedPath(data);
              if (url) resolve(url);
              else reject(new Error('服务器未返回地址'));
            } else {
              reject(new Error(data.msg || '上传失败'));
            }
          } catch { reject(new Error('解析失败')); }
        },
        fail: reject,
      });
    });
    videoUrl.value = videoRes;
    form.value.videoUrl = videoRes;

    // 如果有缩略图，也上传一下作为封面
    if (thumbPath) {
      try {
        const coverRes = await new Promise((resolve, reject) => {
          uni.uploadFile({
            url: UPLOAD_URL,
            filePath: thumbPath,
            name: 'file',
            header: { Authorization: `Bearer ${token}` },
            success: (uploadRes) => {
              try {
                const data = JSON.parse(uploadRes.data);
                if (data.code === 200) {
                  const url = pickUploadedPath(data);
                  if (url) resolve(url);
                  else reject(new Error('服务器未返回地址'));
                } else { reject(new Error(data.msg || '上传失败')); }
              } catch { reject(new Error('解析失败')); }
            },
            fail: reject,
          });
        });
        videoCover.value = coverRes;
        form.value.videoCover = coverRes;
      } catch (e) {
        console.warn('上传视频封面失败:', e);
        // 封面上传失败不影响主流程
      }
    }
  } catch (err) {
    showToast('视频上传失败');
    localVideoPath.value = '';
    videoUrl.value = '';
  } finally {
    hideLoading();
  }
};

const removeVideo = () => {
  localVideoPath.value = '';
  videoUrl.value = '';
  videoCover.value = '';
  videoDuration.value = 0;
  videoSize.value = '';
  form.value.videoUrl = '';
  form.value.videoCover = '';
};

// ================= 提交发布 =================

const handleSubmit = async () => {
  if (!form.value.content.trim()) {
    showToast('请输入动态内容');
    return;
  }
  if (mediaType.value === 'image' && uploadedUrls.value.length === 0) {
    showToast('请至少上传 1 张图片，或切换到视频模式');
    return;
  }
  if (mediaType.value === 'video' && !videoUrl.value) {
    showToast('请等待视频上传完成，或移除后重新选择');
    return;
  }

  submitting.value = true;
  try {
    form.value.images = mediaType.value === 'image' ? JSON.stringify(uploadedUrls.value) : '';
    form.value.videoUrl = mediaType.value === 'video' ? videoUrl.value : '';
    form.value.videoCover = mediaType.value === 'video' ? videoCover.value : '';
    await addPost({ ...form.value });
    showToast('发布成功', 'success');
    setTimeout(() => uni.navigateBack(), 800);
  } catch (err) {
    if (err?.msg) showToast(err.msg);
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

.media-tab-bar {
  display: flex;
  padding: 16rpx 20rpx 0;
  gap: 24rpx;
  border-bottom: 1rpx solid #F0F0F0;

  .media-tab {
    font-size: 28rpx;
    color: #999;
    padding-bottom: 16rpx;
    position: relative;

    &.active {
      color: #FF7E3D;
      font-weight: 700;

      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 36rpx;
        height: 4rpx;
        background: linear-gradient(90deg, #FF8C42, #FFB07C);
        border-radius: 2rpx;
      }
    }
  }
}

.image-section, .video-section {
  padding: 20rpx;
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

.img-delete, .video-delete {
  position: absolute;
  top: -8rpx;
  right: -8rpx;
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border-radius: 50%;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.2);
}

.delete-icon {
  font-size: 24rpx;
  color: #FF4D4F;
  font-weight: 700;
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

  &:active { background-color: #F0F0F0; }

  .plus-icon {
    font-size: 48rpx;
    color: #ccc;
  }
}

.add-video-btn {
  width: 100%;
  height: 400rpx;
  border: 2rpx dashed #E0E0E0;
  border-radius: 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #FAFAFA;
  gap: 16rpx;

  &:active { background-color: #F0F0F0; }

  .plus-icon {
    font-size: 80rpx;
    color: #ccc;
  }

  .add-video-text {
    font-size: 26rpx;
    color: #999;
  }
}

.video-preview {
  position: relative;
  width: 100%;

  .video-player {
    width: 100%;
    height: 400rpx;
    border-radius: 16rpx;
    background-color: #000;
  }

  .video-delete {
    top: -12rpx;
    right: -12rpx;
  }

  .video-info {
    display: block;
    font-size: 24rpx;
    color: #999;
    margin-top: 12rpx;
  }
}

.image-tips, .video-tips {
  display: block;
  font-size: 24rpx;
  color: #B0B0B0;
  margin-top: 16rpx;
}

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

  &:active { transform: scale(0.98); }

  &.disabled {
    background: #E0E0E0;
    color: #999;
    box-shadow: none;
    pointer-events: none;
  }
}
</style>
