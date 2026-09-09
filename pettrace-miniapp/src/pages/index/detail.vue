<template>
  <view class="detail-page">
    <!-- 动态详情卡片 -->
    <view v-if="postDetail" class="post-section">
      <!-- 头部 -->
      <view class="post-header">
        <image
          class="avatar"
          :src="postAvatarUrl"
          mode="aspectFill"
        />
        <view class="user-info">
          <text class="nickname">{{ postDetail.userName || '匿名用户' }}</text>
          <text class="time">{{ formatTime(postDetail.createTime) }}</text>
        </view>
        <!-- 自己的动态可删除 -->
        <view v-if="isOwner" class="more-btn" @click="handleDeletePost">
          <u-icon name="trash" color="#999" size="18" />
        </view>
      </view>

      <!-- 正文 -->
      <view class="post-body">
        <text class="content-text">{{ postDetail.content }}</text>

        <view v-if="postImages.length" class="image-grid" :class="'grid-' + gridClass">
          <image
            v-for="(img, idx) in postImages"
            :key="idx"
            class="grid-image"
            :src="img"
            mode="aspectFill"
            @click="previewImage(idx)"
          />
        </view>
      </view>

      <!-- 点赞/评论数据 -->
      <view class="post-stats">
        <text class="stat-item" @click="showLikes">
          <u-icon name="heart" color="#FF8C42" size="14" />
          {{ postDetail.likeCount || 0 }} 赞
        </text>
        <text class="stat-item">{{ postDetail.commentCount || 0 }} 评论</text>
        <text class="stat-item">{{ postDetail.viewCount || 0 }} 浏览</text>
      </view>

      <!-- 操作栏 -->
      <view class="post-actions">
        <view class="action" @click="handleLikePost">
          <u-icon
            :name="postDetail.isLike ? 'heart-fill' : 'heart'"
            :color="postDetail.isLike ? '#FF8C42' : '#576B95'"
            size="22"
          />
          <text :class="{ liked: postDetail.isLike }">赞</text>
        </view>
        <view class="action" @click="focusComment">
          <u-icon name="chat" color="#576B95" size="22" />
          <text>评论</text>
        </view>
      </view>
    </view>

    <!-- 评论列表 -->
    <view class="comment-section">
      <view class="comment-header">
        <text class="comment-title">评论 {{ commentTotal > 0 ? '(' + commentTotal + ')' : '' }}</text>
      </view>

      <view v-if="comments.length" class="comment-list">
        <view v-for="item in comments" :key="item.id" class="comment-item">
          <!-- 一级评论 -->
          <view class="comment-row">
            <image class="comment-avatar" :src="fullImageUrl(item.userAvatar) || '/static/default-avatar.png'" mode="aspectFill" />
            <view class="comment-body">
              <view class="comment-meta">
                <text class="comment-name">{{ item.userName || '匿名' }}</text>
                <text class="comment-time">{{ formatTime(item.createTime) }}</text>
              </view>
              <text class="comment-content">{{ item.content }}</text>
              <view class="comment-reply-btn" @click="replyComment(item)">
                <u-icon name="chat" color="#576B95" size="12" />
                <text>回复</text>
              </view>
            </view>
          </view>

          <!-- 子评论 -->
          <view v-if="item.children && item.children.length" class="sub-comments">
            <view v-for="child in item.children" :key="child.id" class="comment-row sub-row">
              <image class="comment-avatar sub-avatar" :src="fullImageUrl(child.userAvatar) || '/static/default-avatar.png'" mode="aspectFill" />
              <view class="comment-body">
                <view class="comment-meta">
                  <text class="comment-name">{{ child.userName || '匿名' }}</text>
                  <text v-if="child.parentId" class="reply-to">
                    回复 <text class="reply-target">{{ child.replyUserName || item.userName || '用户' }}</text>
                  </text>
                  <text class="comment-time">{{ formatTime(child.createTime) }}</text>
                </view>
                <text class="comment-content">{{ child.content }}</text>
                <view class="comment-reply-btn" @click="replyComment(item, child)">
                  <u-icon name="chat" color="#576B95" size="12" />
                  <text>回复</text>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>

      <view v-if="!commentLoading && !comments.length" class="no-comments">
        <text class="no-comment-text">暂无评论，来说点什么吧</text>
      </view>

      <view v-if="commentLoadStatus === 'loadmore'" class="load-more-btn" @click="loadMoreComments">
        加载更多评论
      </view>
      <view v-else-if="commentLoadStatus === 'nomore' && comments.length > 0" class="load-more-text">
        - 没有更多评论了 -
      </view>
    </view>

    <!-- 底部输入栏 -->
    <view class="comment-input-bar">
      <view class="input-wrap">
        <input
          ref="commentInputRef"
          v-model="commentText"
          class="comment-input"
          :placeholder="replyTarget ? '回复 ' + replyTarget.userName + '...' : '写评论...'"
          placeholder-class="input-placeholder"
          confirm-type="send"
          @confirm="submitComment"
        />
        <view v-if="replyTarget" class="cancel-reply" @click="cancelReply">
          <u-icon name="close-circle" color="#ccc" size="16" />
        </view>
      </view>
      <view class="send-btn" @click="submitComment">
        <text>发送</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { getPostDetail, getComments, addComment, likePost, deletePost } from '@/api/post.js';
import { formatTime, showToast, showLoading, hideLoading, fullImageUrl } from '@/utils/index.js';
import { useUserStore } from '@/store/user.js';

const userStore = useUserStore();

const postId = ref(0);
const postDetail = ref(null);
const comments = ref([]);
const commentTotal = ref(0);
const commentLoading = ref(false);
const commentLoadStatus = ref('loadmore');
const commentPageParams = ref({ pageNum: 1, pageSize: 10 });

const commentText = ref('');
const replyTarget = ref(null);
const commentInputRef = ref(null);

// 是否是自己的动态
const isOwner = computed(() => {
  if (!postDetail.value) return false;
  const uInfo = userStore.userInfo;
  if (!uInfo) return false;
  return uInfo.userId === postDetail.value.userId || uInfo.id === postDetail.value.userId;
});

// 解析图片（补全URL）
const postImages = computed(() => {
  const val = postDetail.value?.images;
  if (!val) return [];
  let arr = val;
  if (!Array.isArray(val)) {
    try {
      arr = JSON.parse(val);
    } catch {
      return [];
    }
  }
  if (!Array.isArray(arr)) return [];
  return arr.map(fullImageUrl);
});

// 动态发布者头像
const postAvatarUrl = computed(() => {
  return fullImageUrl(postDetail.value?.userAvatar) || '/static/default-avatar.png';
});

const gridClass = computed(() => {
  const len = postImages.value.length;
  if (len === 1) return 'one';
  if (len === 2 || len === 4) return 'two';
  return 'multi';
});

onLoad((options) => {
  postId.value = Number(options.id);
  fetchPostDetail();
  fetchComments(true);
});

// 获取动态详情
const fetchPostDetail = async () => {
  try {
    const res = await getPostDetail(postId.value);
    const data = res.data || res;
    postDetail.value = data;
    // 评论总数以后端动态详情里的 comment_count 为准（含回复）
    if (data?.commentCount != null) {
      commentTotal.value = data.commentCount;
    }
  } catch (err) {
    console.error('[详情页] 获取动态详情失败:', err?.code || err?.msg || err);
    showToast('获取详情失败');
  }
};

// 获取评论列表
const fetchComments = async (isRefresh = false) => {
  if (isRefresh) commentPageParams.value.pageNum = 1;
  commentLoading.value = true;
  commentLoadStatus.value = 'loading';
  try {
    const res = await getComments(postId.value, commentPageParams.value);
    const list = res.rows || [];
    // 详情接口未返回时先用评论分页的总数兜底，避免标题长时间显示 0
    if (commentTotal.value === 0) {
      commentTotal.value = res.total || 0;
    }
    if (isRefresh) {
      comments.value = list;
    } else {
      comments.value = [...comments.value, ...list];
    }
    commentLoadStatus.value = list.length < commentPageParams.value.pageSize ? 'nomore' : 'loadmore';
  } catch (err) {
    console.error('[详情页] 获取评论失败:', err?.code || err?.msg || err);
    commentLoadStatus.value = 'loadmore';
  } finally {
    commentLoading.value = false;
  }
};

// 加载更多评论
const loadMoreComments = () => {
  if (commentLoadStatus.value === 'nomore') return;
  commentPageParams.value.pageNum++;
  fetchComments();
};

// 点赞
const handleLikePost = async () => {
  try {
    const res = await likePost(postId.value);
    // 后端返回 { code: 200, liked: true/false }
    if (postDetail.value) {
      const nowLiked = res.liked !== undefined ? res.liked : !postDetail.value.isLike;
      postDetail.value.isLike = nowLiked;
      postDetail.value.likeCount = (postDetail.value.likeCount || 0) + (nowLiked ? 1 : -1);
      if (postDetail.value.likeCount < 0) postDetail.value.likeCount = 0;
    }
  } catch (err) {
    console.error('[详情页] 点赞失败:', err?.code || err?.msg || err);
    showToast(err?.msg || '操作失败');
  }
};

// 删除动态
const handleDeletePost = () => {
  uni.showModal({
    title: '提示',
    content: '确定删除这条动态吗？',
    confirmColor: '#FF4D4F',
    success: async (res) => {
      if (res.confirm) {
        try {
          await deletePost(postId.value);
          showToast('已删除', 'success');
          setTimeout(() => {
            // navigateBack 优先，失败则跳转首页
            uni.navigateBack({
              fail: () => {
                uni.switchTab({ url: '/pages/index/index' });
              },
            });
          }, 800);
        } catch (err) {
          console.error('[详情页] 删除动态失败:', err?.code || err?.msg || err);
          showToast(err?.msg || '删除失败');
        }
      }
    },
  });
};

// 回复评论
const replyComment = (parent, child) => {
  replyTarget.value = child || parent;
  nextTick(() => {
    focusComment();
  });
};

// 取消回复
const cancelReply = () => {
  replyTarget.value = null;
  commentText.value = '';
};

// 聚焦评论输入框
const focusComment = () => {
  // 小程序中无法直接聚焦 input，这里让用户手动点击
};

// 发表评论
const submitComment = async () => {
  const text = commentText.value.trim();
  if (!text) {
    showToast('请输入评论内容');
    return;
  }
  const token = uni.getStorageSync('token');
  if (!token) {
    showToast('请先登录');
    return;
  }
  try {
    showLoading('评论中...');
    const data = { content: text };
    if (replyTarget.value) {
      data.parentId = replyTarget.value.id;
    }
    await addComment(postId.value, data);
    showToast('评论成功', 'success');
    commentText.value = '';
    replyTarget.value = null;
    // 先刷新评论，再刷动态详情（顺序执行避免竞态）
    await fetchComments(true);
    await fetchPostDetail();
  } catch (err) {
    console.error('[详情页] 评论失败:', err?.code || err?.msg || err);
    showToast(err?.msg || '评论失败');
  } finally {
    hideLoading();
  }
};

// 预览图片
const previewImage = (idx) => {
  if (!postImages.value.length) return;
  uni.previewImage({
    urls: postImages.value,
    current: postImages.value[idx] || postImages.value[0],
  });
};

const showLikes = () => {
  showToast('点赞列表开发中');
};
</script>

<style lang="scss" scoped>
.detail-page {
  min-height: 100vh;
  background-color: #EDEDED;
  padding-bottom: 120rpx;
}

/* 动态卡片 */
.post-section {
  background-color: #fff;
  padding: 24rpx 20rpx;
  margin-bottom: 16rpx;
}

.post-header {
  display: flex;
  align-items: flex-start;

  .avatar {
    width: 80rpx;
    height: 80rpx;
    border-radius: 8rpx;
    margin-right: 16rpx;
    flex-shrink: 0;
    background-color: #F0F0F0;
  }

  .user-info {
    flex: 1;

    .nickname {
      font-size: 30rpx;
      color: #576B95;
      font-weight: 500;
      display: block;
    }

    .time {
      font-size: 22rpx;
      color: #B0B0B0;
      margin-top: 2rpx;
      display: block;
    }
  }

  .more-btn {
    padding: 8rpx;
    flex-shrink: 0;

    &:active { opacity: 0.6; }
  }
}

.post-body {
  margin-top: 14rpx;

  .content-text {
    font-size: 32rpx;
    color: #1A1A1A;
    line-height: 1.6;
    word-break: break-all;
  }

  .image-grid {
    margin-top: 14rpx;
    display: grid;
    gap: 6rpx;
    border-radius: 8rpx;
    overflow: hidden;
  }

  .grid-one {
    grid-template-columns: 1fr;
    max-width: 360rpx;
    .grid-image { width: 360rpx; height: 360rpx; }
  }

  .grid-two {
    grid-template-columns: repeat(2, 1fr);
    max-width: 420rpx;
    .grid-image { width: 207rpx; height: 207rpx; }
  }

  .grid-multi {
    grid-template-columns: repeat(3, 1fr);
    max-width: 630rpx;
    .grid-image { width: 208rpx; height: 208rpx; }
  }

  .grid-image {
    display: block;
    background-color: #F0F0F0;
  }
}

.post-stats {
  display: flex;
  align-items: center;
  gap: 24rpx;
  margin-top: 20rpx;
  padding-bottom: 16rpx;
  border-bottom: 1rpx solid #F0F0F0;

  .stat-item {
    font-size: 24rpx;
    color: #888;
    display: flex;
    align-items: center;
    gap: 4rpx;
  }
}

.post-actions {
  display: flex;
  align-items: center;
  gap: 40rpx;
  padding-top: 12rpx;

  .action {
    display: flex;
    align-items: center;
    gap: 6rpx;
    padding: 8rpx 16rpx;
    border-radius: 6rpx;

    &:active { background: #F5F5F5; }

    text {
      font-size: 26rpx;
      color: #576B95;

      &.liked { color: #FF8C42; }
    }
  }
}

/* 评论区 */
.comment-section {
  background-color: #fff;
  padding: 20rpx;
}

.comment-header {
  padding-bottom: 16rpx;
  border-bottom: 1rpx solid #F0F0F0;

  .comment-title {
    font-size: 30rpx;
    font-weight: 600;
    color: #1A1A1A;
  }
}

.comment-list {
  margin-top: 10rpx;
}

.comment-item {
  padding: 16rpx 0;
  border-bottom: 1rpx solid #F5F5F5;
}

.comment-row {
  display: flex;
  align-items: flex-start;
}

.sub-row {
  margin-left: 84rpx;
  margin-top: 12rpx;
  padding: 12rpx;
  background: #F7F7F7;
  border-radius: 8rpx;
}

.comment-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 8rpx;
  margin-right: 12rpx;
  flex-shrink: 0;
  background-color: #F0F0F0;
}

.sub-avatar {
  width: 52rpx;
  height: 52rpx;
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8rpx;
  margin-bottom: 4rpx;

  .comment-name {
    font-size: 26rpx;
    color: #576B95;
  }

  .reply-to {
    font-size: 22rpx;
    color: #B0B0B0;

    .reply-target {
      color: #576B95;
      margin: 0 2rpx;
    }
  }

  .comment-time {
    font-size: 22rpx;
    color: #B0B0B0;
  }
}

.comment-content {
  font-size: 28rpx;
  color: #1A1A1A;
  line-height: 1.5;
  word-break: break-all;
}

.comment-reply-btn {
  display: inline-flex;
  align-items: center;
  gap: 4rpx;
  margin-top: 6rpx;
  font-size: 22rpx;
  color: #576B95;

  &:active { opacity: 0.6; }
}

.sub-comments {
  // sub comments styling
}

.no-comments {
  text-align: center;
  padding: 60rpx 0;

  .no-comment-text {
    font-size: 26rpx;
    color: #B0B0B0;
  }
}

.load-more-btn {
  text-align: center;
  padding: 24rpx 0;
  font-size: 26rpx;
  color: #576B95;

  &:active { opacity: 0.6; }
}

.load-more-text {
  text-align: center;
  padding: 24rpx 0;
  font-size: 24rpx;
  color: #ccc;
}

/* 底部输入栏 */
.comment-input-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  padding: 12rpx 16rpx;
  padding-bottom: calc(12rpx + env(safe-area-inset-bottom));
  background-color: #F7F7F7;
  border-top: 1rpx solid #E0E0E0;
  z-index: 100;

  .input-wrap {
    flex: 1;
    display: flex;
    align-items: center;
    height: 68rpx;
    background-color: #fff;
    border-radius: 8rpx;
    padding: 0 16rpx;
    border: 1rpx solid #E0E0E0;
  }

  .comment-input {
    flex: 1;
    font-size: 28rpx;
    color: #333;
    height: 100%;
  }

  .input-placeholder {
    color: #B0B0B0;
    font-size: 28rpx;
  }

  .cancel-reply {
    padding: 4rpx;
    margin-left: 8rpx;

    &:active { opacity: 0.6; }
  }

  .send-btn {
    margin-left: 16rpx;
    padding: 8rpx 24rpx;
    background-color: #FF8C42;
    color: #fff;
    border-radius: 8rpx;
    font-size: 28rpx;
    font-weight: 500;
    flex-shrink: 0;

    &:active {
      opacity: 0.8;
    }
  }
}
</style>
