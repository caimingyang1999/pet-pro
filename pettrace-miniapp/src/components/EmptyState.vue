<template>
  <view class="empty-state">
    <u-empty :text="text" :mode="mode">
      <template #bottom>
        <slot name="action">
          <u-button v-if="showButton" type="primary" size="small" :text="buttonText" @click="handleClick" />
        </slot>
      </template>
    </u-empty>
  </view>
</template>

<script setup>
const props = defineProps({
  text: {
    type: String,
    default: '暂无数据',
  },
  mode: {
    type: String,
    default: 'data',
  },
  showButton: {
    type: Boolean,
    default: false,
  },
  buttonText: {
    type: String,
    default: '去逛逛',
  },
});

const emit = defineEmits(['click']);

const handleClick = () => {
  emit('click');
};
</script>

<style lang="scss" scoped>
.empty-state {
  @include pet-flex-center;
  flex-direction: column;
  padding: 80rpx 40rpx;
}
</style>
