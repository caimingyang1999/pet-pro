<template>
  <view class="weight-page">
    <!-- 宠物选择（从首页快捷工具进入时没有 petId，需要先选宠物） -->
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
    <view v-if="!petLoading && !petList.length" class="empty-wrap">
      <view class="empty-art">
        <Icon name="pet" :size="44" color="#FFB07A" />
      </view>
      <text class="empty-title">还没有爱宠档案</text>
      <text class="empty-desc">先添加一只爱宠，就可以开始记录体重啦</text>
      <view class="empty-btn pet-press" @click="goPetList">
        <text class="empty-btn-text">去添加爱宠</text>
      </view>
    </view>

    <template v-else>
      <!-- 趋势卡片 -->
      <view class="chart-card">
        <view class="chart-head">
          <text class="chart-title">体重趋势</text>
          <text class="chart-range" v-if="weightList.length >= 2">
            {{ formatDate(weightList[0].recordDate) }} ~ {{ formatDate(weightList[weightList.length - 1].recordDate) }}
          </text>
        </view>

        <view class="chart-summary">
          <view class="summary-item">
            <text class="summary-value">{{ latestWeight }}</text>
            <text class="summary-label">当前体重(kg)</text>
          </view>
          <view class="summary-divider" />
          <view class="summary-item">
            <text class="summary-value" :class="trendClass">{{ trendText }}</text>
            <text class="summary-label">较上次</text>
          </view>
          <view class="summary-divider" />
          <view class="summary-item">
            <text class="summary-value">{{ weightList.length }}</text>
            <text class="summary-label">记录次数</text>
          </view>
        </view>

        <!-- 折线图 -->
        <view class="chart-body" v-if="weightList.length >= 2">
          <canvas
            canvas-id="weightChart"
            class="weight-canvas"
            :style="{ width: chartW + 'px', height: chartH + 'px' }"
          />
        </view>
        <view class="chart-empty" v-else>
          <text class="chart-empty-text">再记录 1 次，就能看到体重变化曲线了</text>
        </view>
      </view>

      <!-- 记录列表 -->
      <view class="section-head">
        <text class="section-title">记录明细</text>
        <text class="section-count" v-if="weightList.length">{{ weightList.length }} 条</text>
      </view>

      <view class="log-list" v-if="weightList.length">
        <view class="log-item" v-for="item in sortedList" :key="item.id">
          <view class="log-icon">
            <Icon name="weight" :size="16" color="#3D9BE9" />
          </view>
          <view class="log-info">
            <text class="log-weight">{{ item.weight }} kg</text>
            <text class="log-sub">{{ formatDate(item.recordDate) }}<text v-if="item.remark"> · {{ item.remark }}</text></text>
          </view>
          <view class="log-del pet-press" @click="handleDelete(item.id)">
            <Icon name="trash" :size="16" color="#C0C4CE" />
          </view>
        </view>
      </view>

      <view class="empty-log" v-else>
        <text class="empty-log-text">还没有体重记录，点击右下角按钮开始记录</text>
      </view>

      <view class="safe-bottom" />
    </template>

    <!-- 添加按钮 -->
    <view class="fab-btn pet-press" v-if="petList.length" @click="openForm">
      <Icon name="plus" :size="28" color="#fff" />
    </view>

    <!-- 添加面板 -->
    <view class="form-mask" v-if="formVisible" @click="closeForm" />
    <view class="form-panel" :class="{ show: formVisible }">
      <view class="panel-head">
        <text class="panel-title">记录体重</text>
        <view class="panel-close pet-press" @click="closeForm">
          <Icon name="close" :size="16" color="#8A8D9A" />
        </view>
      </view>

      <view class="form-row">
        <text class="form-label">体重(kg)</text>
        <input
          v-model="form.weight"
          class="form-input"
          type="digit"
          placeholder="如 4.5"
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

      <view class="form-row">
        <text class="form-label">备注</text>
        <input
          v-model="form.remark"
          class="form-input"
          type="text"
          maxlength="50"
          placeholder="选填，如：空腹称重"
          placeholder-class="input-placeholder"
        />
      </view>

      <view class="submit-btn pet-press" :class="{ loading: submitting }" @click="handleSubmit">
        <text>{{ submitting ? '保存中...' : '保存记录' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import Icon from '@/components/Icon.vue';
import { ref, computed, nextTick } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { getPetList } from '@/api/pet.js';
import { addWeightLog, getWeightLogList, deleteWeightLog } from '@/api/health.js';
import { showToast, showConfirm, formatDateTime } from '@/utils/index.js';

const petList = ref([]);
const currentPetId = ref('');
const weightList = ref([]);
const petLoading = ref(true);

const formVisible = ref(false);
const submitting = ref(false);
const form = ref({ weight: '', recordDate: '', remark: '' });

/* ==================== 画布尺寸 ==================== */
const chartH = 180;
const chartW = ref(300);
try {
  const info = uni.getSystemInfoSync();
  // 页面左右各 24rpx，卡片内左右各 32rpx
  const side = uni.upx2px(24) * 2 + uni.upx2px(32) * 2;
  chartW.value = Math.max(200, Math.round((info.windowWidth || 375) - side));
} catch (e) {}

const today = computed(() => {
  const d = new Date();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${d.getFullYear()}-${m}-${day}`;
});

/* ==================== 展示数据 ==================== */
const sortedList = computed(() => [...weightList.value].reverse());

const latestWeight = computed(() => {
  const list = weightList.value;
  if (!list.length) return '--';
  return list[list.length - 1].weight;
});

const diff = computed(() => {
  const list = weightList.value;
  if (list.length < 2) return null;
  const last = Number(list[list.length - 1].weight);
  const prev = Number(list[list.length - 2].weight);
  if (isNaN(last) || isNaN(prev)) return null;
  return Number((last - prev).toFixed(2));
});

const trendText = computed(() => {
  if (diff.value === null) return '--';
  if (diff.value === 0) return '持平';
  return `${diff.value > 0 ? '+' : ''}${diff.value}`;
});

const trendClass = computed(() => {
  if (diff.value === null || diff.value === 0) return '';
  return diff.value > 0 ? 'up' : 'down';
});

const formatDate = (val) => {
  if (!val) return '';
  const s = String(val);
  // 后端返回 yyyy-MM-dd HH:mm:ss 或 yyyy-MM-dd
  return s.length > 10 ? s.slice(0, 10) : s;
};

/* ==================== 数据加载 ==================== */
const loadPetList = async () => {
  petLoading.value = true;
  try {
    const res = await getPetList();
    petList.value = res.data || [];
    if (!currentPetId.value && petList.value.length) {
      currentPetId.value = petList.value[0].id;
    }
  } catch (e) {
    console.error('[体重记录] 获取宠物列表失败:', e?.code || e?.msg || e);
    petList.value = [];
  } finally {
    petLoading.value = false;
  }
};

const loadWeightList = async () => {
  if (!currentPetId.value) {
    weightList.value = [];
    return;
  }
  try {
    const res = await getWeightLogList(currentPetId.value);
    weightList.value = res.data || [];
  } catch (e) {
    console.error('[体重记录] 获取记录失败:', e?.code || e?.msg || e);
    weightList.value = [];
  }
  nextTick(() => drawChart());
};

const switchPet = (petId) => {
  if (String(currentPetId.value) === String(petId)) return;
  currentPetId.value = petId;
  loadWeightList();
};

/* ==================== 折线图（原生 canvas 旧版 API） ==================== */
const drawChart = () => {
  const list = weightList.value;
  if (list.length < 2 || !chartW.value) return;

  const ctx = uni.createCanvasContext('weightChart');
  const W = chartW.value;
  const H = chartH;
  const padL = 42;
  const padR = 12;
  const padT = 16;
  const padB = 24;
  const innerW = W - padL - padR;
  const innerH = H - padT - padB;

  const values = list.map((i) => Number(i.weight));
  let min = Math.min(...values);
  let max = Math.max(...values);
  if (max === min) {
    // 全部相同时人为撑开区间，避免折线贴边
    max = max + 1;
    min = Math.max(0, min - 1);
  }
  const range = max - min;

  const stepX = innerW / (list.length - 1);
  const pt = (i) => ({
    x: padL + stepX * i,
    y: padT + innerH - ((values[i] - min) / range) * innerH,
  });

  // 网格线 + Y 轴刻度
  ctx.setStrokeStyle('#F0F1F5');
  ctx.setLineWidth(1);
  ctx.setFontSize(10);
  ctx.setFillStyle('#A0A0A0');
  const gridCount = 4;
  for (let g = 0; g <= gridCount; g++) {
    const y = padT + (innerH / gridCount) * g;
    ctx.beginPath();
    ctx.moveTo(padL, y);
    ctx.lineTo(W - padR, y);
    ctx.stroke();
    const label = (max - (range / gridCount) * g).toFixed(1);
    ctx.fillText(label, 6, y + 3);
  }

  // 折线
  ctx.setStrokeStyle('#3D9BE9');
  ctx.setLineWidth(2);
  ctx.beginPath();
  list.forEach((_, i) => {
    const p = pt(i);
    if (i === 0) ctx.moveTo(p.x, p.y);
    else ctx.lineTo(p.x, p.y);
  });
  ctx.stroke();

  // 数据点
  list.forEach((_, i) => {
    const p = pt(i);
    ctx.beginPath();
    ctx.setFillStyle('#FFFFFF');
    ctx.arc(p.x, p.y, 4, 0, Math.PI * 2);
    ctx.fill();
    ctx.setStrokeStyle('#3D9BE9');
    ctx.setLineWidth(2);
    ctx.stroke();
  });

  ctx.draw();
};

/* ==================== 新增 / 删除 ==================== */
const openForm = () => {
  if (!petList.value.length) {
    showToast('请先添加爱宠');
    return;
  }
  form.value = { weight: '', recordDate: today.value, remark: '' };
  formVisible.value = true;
};

const closeForm = () => {
  formVisible.value = false;
};

const onDateChange = (e) => {
  form.value.recordDate = e.detail.value;
};

const handleSubmit = async () => {
  if (submitting.value) return;
  const weight = String(form.value.weight || '').trim();
  if (!weight) {
    showToast('请填写体重');
    return;
  }
  const num = Number(weight);
  if (isNaN(num) || num <= 0 || num > 200) {
    showToast('请填写正确的体重（0~200kg）');
    return;
  }

  submitting.value = true;
  try {
    await addWeightLog({
      petId: currentPetId.value,
      weight: num,
      recordDate: form.value.recordDate,
      remark: String(form.value.remark || '').trim(),
    });
    showToast('记录成功', 'success');
    formVisible.value = false;
    await loadWeightList();
  } catch (e) {
    showToast(e?.msg || '保存失败，请重试');
  } finally {
    submitting.value = false;
  }
};

const handleDelete = async (id) => {
  const ok = await showConfirm('确定删除这条体重记录吗？');
  if (!ok) return;
  try {
    await deleteWeightLog(id);
    showToast('已删除', 'success');
    await loadWeightList();
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
  await loadWeightList();
});
</script>

<style lang="scss" scoped>
.weight-page {
  min-height: 100vh;
  background-color: $bg-page;
  padding-bottom: 40rpx;
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

/* ===== 趋势卡片 ===== */
.chart-card {
  margin: 24rpx;
  padding: 32rpx;
  border-radius: $radius-lg;
  background-color: #fff;
  box-shadow: $shadow-card;

  .chart-head {
    display: flex;
    align-items: baseline;
    justify-content: space-between;

    .chart-title {
      font-size: $font-md;
      font-weight: $font-weight-bold;
      color: $text-primary;
    }

    .chart-range {
      font-size: $font-xs;
      color: $text-hint;
    }
  }

  .chart-summary {
    display: flex;
    align-items: center;
    margin-top: 26rpx;
    padding: 22rpx 0;
    border-radius: $radius-md;
    background-color: $bg-page;

    .summary-item {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;

      .summary-value {
        font-size: 36rpx;
        font-weight: $font-weight-bold;
        color: $text-primary;
        line-height: 1.2;

        &.up {
          color: #E8607F;
        }

        &.down {
          color: #4CAF7D;
        }
      }

      .summary-label {
        margin-top: 6rpx;
        font-size: $font-xs;
        color: $text-hint;
      }
    }

    .summary-divider {
      width: 1rpx;
      height: 44rpx;
      background-color: $bg-input;
    }
  }

  .chart-body {
    margin-top: 24rpx;
    display: flex;
    justify-content: center;
  }

  .weight-canvas {
    display: block;
  }

  .chart-empty {
    margin-top: 24rpx;
    padding: 40rpx 0;
    text-align: center;

    .chart-empty-text {
      font-size: $font-sm;
      color: $text-hint;
    }
  }
}

/* ===== 区块标题 ===== */
.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 8rpx 32rpx 16rpx;

  .section-title {
    font-size: $font-md;
    font-weight: $font-weight-bold;
    color: $text-primary;
  }

  .section-count {
    font-size: $font-xs;
    color: $text-hint;
  }
}

/* ===== 记录列表 ===== */
.log-list {
  margin: 0 24rpx;
  border-radius: $radius-lg;
  background-color: #fff;
  box-shadow: $shadow-card;
  overflow: hidden;
}

.log-item {
  display: flex;
  align-items: center;
  padding: 26rpx 28rpx;
  border-bottom: 1rpx solid $bg-input;

  &:last-child {
    border-bottom: none;
  }

  .log-icon {
    width: 64rpx;
    height: 64rpx;
    border-radius: 20rpx;
    background-color: #E5F4FF;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 22rpx;
    flex-shrink: 0;
  }

  .log-info {
    flex: 1;
    min-width: 0;

    .log-weight {
      display: block;
      font-size: $font-md;
      font-weight: $font-weight-bold;
      color: $text-primary;
    }

    .log-sub {
      display: block;
      margin-top: 6rpx;
      font-size: $font-xs;
      color: $text-hint;
    }
  }

  .log-del {
    width: 56rpx;
    height: 56rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }
}

.empty-log {
  margin: 0 24rpx;
  padding: 60rpx 0;
  border-radius: $radius-lg;
  background-color: #fff;
  text-align: center;

  .empty-log-text {
    font-size: $font-sm;
    color: $text-hint;
  }
}

/* ===== 空状态 ===== */
.empty-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 160rpx 72rpx 0;

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

/* ===== 添加面板 ===== */
.form-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.42);
  z-index: 400;
}

.form-panel {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 401;
  background: #fff;
  border-radius: $radius-xl $radius-xl 0 0;
  padding: 32rpx 36rpx calc(env(safe-area-inset-bottom) + 40rpx);
  transform: translateY(110%);
  transition: transform 0.28s ease;

  &.show {
    transform: translateY(0);
  }

  .panel-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 28rpx;

    .panel-title {
      font-size: $font-lg;
      font-weight: $font-weight-bold;
      color: $text-primary;
    }

    .panel-close {
      width: 56rpx;
      height: 56rpx;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

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

  .submit-btn {
    margin-top: 44rpx;
    height: 96rpx;
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
}

.input-placeholder {
  color: $text-placeholder;
  font-size: $font-md;
}

.safe-bottom {
  height: calc(env(safe-area-inset-bottom) + 180rpx);
}
</style>
