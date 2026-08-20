<template>
  <el-image-viewer
    v-if="visible"
    :url-list="urlList"
    :initial-index="currentIndex"
    :on-close="handleClose"
    :on-switch="handleSwitch"
    append-to-body
  />
</template>

<script>
/**
 * 图片预览组件
 *
 * @description 基于 el-image-viewer 封装的图片预览组件，支持切换上下张、缩放等功能
 *
 * @example
 * // 基础用法
 * <image-preview
 *   v-model="previewVisible"
 *   :images="imageList"
 *   :current-index="0"
 * />
 *
 * // 直接触发预览
 * <image-preview
 *   ref="imagePreviewRef"
 *   :images="imageList"
 * />
 * // 通过 ref 调用
 * this.$refs.imagePreviewRef.open(index)
 *
 * @property {Boolean} value - 是否显示预览（v-model 绑定）
 * @property {Array} images - 图片 URL 数组
 * @property {Number} currentIndex - 当前预览的图片索引，默认 0
 *
 * @event input - 预览显隐变化时触发，用于 v-model 绑定
 * @event close - 关闭预览时触发
 * @event switch - 切换图片时触发，回调参数为 (index, url)
 */
export default {
  name: 'ImagePreview',
  props: {
    value: {
      type: Boolean,
      default: false
    },
    images: {
      type: Array,
      required: true,
      default: () => []
    },
    currentIndex: {
      type: Number,
      default: 0
    }
  },
  data() {
    return {
      visible: this.value,
      urlList: this.resolveImageUrls(this.images)
    }
  },
  watch: {
    value: {
      handler(val) {
        this.visible = val
      },
      immediate: true
    },
    images: {
      handler(val) {
        this.urlList = this.resolveImageUrls(val)
      },
      immediate: true,
      deep: true
    }
  },
  methods: {
    resolveImageUrls(imgs) {
      if (!imgs || !Array.isArray(imgs)) return []
      return imgs.map(url => {
        if (!url) return ''
        if (/^https?:\/\//.test(url)) return url
        if (url.startsWith('data:') || url.startsWith('blob:')) return url
        return process.env.VUE_APP_BASE_API + url
      })
    },
    handleClose() {
      this.visible = false
      this.$emit('input', false)
      this.$emit('close')
    },
    handleSwitch(index, url) {
      this.$emit('switch', index, url)
    },
    open(index) {
      this.visible = true
      this.$emit('input', true)
      if (typeof index === 'number') {
        this.$nextTick(() => {
          const viewer = document.querySelector('.el-image-viewer__img')
          if (viewer && this.$refs && this.$children[0]) {
            this.$children[0].currentIndex = index
          }
        })
      }
    },
    close() {
      this.handleClose()
    }
  }
}
</script>

<style lang="scss" scoped>
</style>
