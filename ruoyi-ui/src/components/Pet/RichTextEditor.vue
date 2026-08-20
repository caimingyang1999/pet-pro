<template>
  <div class="rich-text-editor">
    <el-upload
      v-if="uploadMode === 'url'"
      :action="uploadUrl"
      :before-upload="handleBeforeUpload"
      :on-success="handleUploadSuccess"
      :on-error="handleUploadError"
      :headers="headers"
      name="file"
      :show-file-list="false"
      ref="upload"
      style="display: none"
    />
    <div
      ref="editor"
      class="editor-container"
      :style="editorStyles"
    />
  </div>
</template>

<script>
import Quill from 'quill'
import 'quill/dist/quill.core.css'
import 'quill/dist/quill.snow.css'
import { getToken } from '@/utils/auth'

/**
 * 富文本编辑器组件
 *
 * @description 基于 Quill 封装的富文本编辑器，支持图片上传、v-model 双向绑定
 *
 * @example
 * // 基础用法
 * <rich-text-editor v-model="content" />
 *
 * // 自定义配置
 * <rich-text-editor
 *   v-model="form.description"
 *   :height="300"
 *   :file-size="10"
 *   upload-mode="url"
 * />
 *
 * @property {String} value - 编辑器内容（v-model 绑定）
 * @property {Number} height - 编辑器高度（px），不设置则自适应
 * @property {Number} minHeight - 编辑器最小高度（px），默认 200
 * @property {Boolean} readOnly - 是否只读，默认 false
 * @property {Number} fileSize - 图片上传大小限制（MB），默认 5
 * @property {String} uploadMode - 图片上传模式：url（上传到服务器）/ base64，默认 url
 * @property {String} placeholder - 编辑器占位符，默认"请输入内容"
 * @property {Array} toolbar - 自定义工具栏配置，不设置则使用默认配置
 *
 * @event input - 内容变化时触发，用于 v-model 绑定
 * @event change - 内容变化时触发，回调参数为 { html, text, delta, source }
 */
const DEFAULT_TOOLBAR = [
  ['bold', 'italic', 'underline', 'strike'],
  ['blockquote', 'code-block'],
  [{ list: 'ordered' }, { list: 'bullet' }],
  [{ indent: '-1' }, { indent: '+1' }],
  [{ size: ['small', false, 'large', 'huge'] }],
  [{ header: [1, 2, 3, 4, 5, 6, false] }],
  [{ color: [] }, { background: [] }],
  [{ align: [] }],
  ['clean'],
  ['link', 'image', 'video']
]

export default {
  name: 'RichTextEditor',
  props: {
    value: {
      type: String,
      default: ''
    },
    height: {
      type: Number,
      default: null
    },
    minHeight: {
      type: Number,
      default: 200
    },
    readOnly: {
      type: Boolean,
      default: false
    },
    fileSize: {
      type: Number,
      default: 5
    },
    uploadMode: {
      type: String,
      default: 'url',
      validator: val => ['url', 'base64'].includes(val)
    },
    placeholder: {
      type: String,
      default: '请输入内容'
    },
    toolbar: {
      type: Array,
      default: null
    }
  },
  data() {
    return {
      quill: null,
      currentValue: '',
      uploadUrl: process.env.VUE_APP_BASE_API + '/common/upload',
      headers: {
        Authorization: 'Bearer ' + getToken()
      }
    }
  },
  computed: {
    editorStyles() {
      const style = {}
      if (this.height) {
        style.height = this.height + 'px'
      }
      if (this.minHeight) {
        style.minHeight = this.minHeight + 'px'
      }
      return style
    },
    toolbarConfig() {
      return this.toolbar || DEFAULT_TOOLBAR
    }
  },
  watch: {
    value: {
      handler(val) {
        if (val !== this.currentValue) {
          this.currentValue = val == null ? '' : val
          if (this.quill) {
            this.quill.clipboard.dangerouslyPasteHTML(this.currentValue)
          }
        }
      },
      immediate: true
    }
  },
  mounted() {
    this.initEditor()
  },
  beforeDestroy() {
    if (this.quill) {
      this.quill = null
    }
  },
  methods: {
    initEditor() {
      const editorEl = this.$refs.editor
      this.quill = new Quill(editorEl, {
        theme: 'snow',
        modules: {
          toolbar: {
            container: this.toolbarConfig,
            handlers: this.uploadMode === 'url'
              ? { image: this.handleImageButton }
              : undefined
          }
        },
        placeholder: this.placeholder,
        readOnly: this.readOnly
      })

      this.quill.clipboard.dangerouslyPasteHTML(this.currentValue)

      this.quill.on('text-change', (delta, oldDelta, source) => {
        if (source === 'user') {
          const html = this.quill.root.innerHTML
          const text = this.quill.getText()
          this.currentValue = html
          this.$emit('input', html)
          this.$emit('change', { html, text, delta, source })
        }
      })
    },
    handleImageButton(value) {
      if (value) {
        this.$refs.upload.$children[0].$refs.input.click()
      } else {
        this.quill.format('image', false)
      }
    },
    handleBeforeUpload(file) {
      const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/svg+xml']
      const isValidType = allowedTypes.includes(file.type)
      if (!isValidType) {
        this.$message.error('图片格式不正确，请上传 jpg/png/gif/svg 格式图片')
        return false
      }
      if (this.fileSize) {
        const isValidSize = file.size / 1024 / 1024 < this.fileSize
        if (!isValidSize) {
          this.$message.error(`上传图片大小不能超过 ${this.fileSize} MB！`)
          return false
        }
      }
      return true
    },
    handleUploadSuccess(res) {
      if (res.code === 200) {
        const quill = this.quill
        const range = quill.getSelection(true)
        const imageUrl = process.env.VUE_APP_BASE_API + res.fileName
        quill.insertEmbed(range.index, 'image', imageUrl, Quill.sources.USER)
        quill.setSelection(range.index + 1, Quill.sources.SILENT)
      } else {
        this.$message.error(res.msg || '图片上传失败')
      }
    },
    handleUploadError() {
      this.$message.error('图片上传失败，请重试')
    },
    focus() {
      if (this.quill) {
        this.quill.focus()
      }
    },
    getHtml() {
      return this.quill ? this.quill.root.innerHTML : ''
    },
    getText() {
      return this.quill ? this.quill.getText() : ''
    },
    setContents(html) {
      if (this.quill) {
        this.quill.clipboard.dangerouslyPasteHTML(html || '')
      }
    },
    clear() {
      if (this.quill) {
        this.quill.setText('')
        this.$emit('input', '')
        this.$emit('change', { html: '', text: '', delta: null, source: 'user' })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.rich-text-editor {
  width: 100%;

  .editor-container {
    width: 100%;
    background: #fff;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    overflow: hidden;
    transition: border-color 0.2s;

    &:focus-within {
      border-color: #409eff;
    }

    ::v-deep .ql-toolbar {
      border-top: none;
      border-left: none;
      border-right: none;
      background: #f5f7fa;
      position: sticky;
      top: 0;
      z-index: 1;
    }

    ::v-deep .ql-container {
      border: none;
      font-family: inherit;
      font-size: 14px;
      line-height: 1.6;
    }

    ::v-deep .ql-editor {
      min-height: 200px;
      word-break: break-all;

      p {
        margin-bottom: 0.5em;
      }

      img {
        max-width: 100%;
        height: auto;
        border-radius: 4px;
        margin: 8px 0;
      }
    }

    ::v-deep .ql-editor.ql-blank::before {
      font-size: 14px;
      color: #c0c4cc;
      font-style: normal;
    }
  }
}
</style>
