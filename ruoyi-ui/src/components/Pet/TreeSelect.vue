<template>
  <el-popover
    v-model="popoverVisible"
    :width="popoverWidth"
    placement="bottom-start"
    trigger="click"
    popper-class="pet-tree-select__popover"
    @show="handlePopoverShow"
    @hide="handlePopoverHide"
  >
    <div class="pet-tree-select__search">
      <el-input
        v-model="filterText"
        placeholder="输入关键字过滤"
        size="small"
        clearable
        prefix-icon="el-icon-search"
        @click.native.stop
      />
    </div>
    <el-tree
      ref="tree"
      class="pet-tree-select__tree"
      :data="treeData"
      :props="treeProps"
      :node-key="nodeKey"
      :current-node-key="currentValue"
      :expand-on-click-node="false"
      :filter-node-method="filterNode"
      :highlight-current="true"
      :default-expanded-keys="defaultExpandedKeys"
      @node-click="handleNodeClick"
      @node-expand="handleNodeExpand"
      @node-collapse="handleNodeCollapse"
    />
    <div
      slot="reference"
      class="pet-tree-select__trigger"
      :class="{ 'is-active': popoverVisible, 'is-placeholder': !selectedLabel }"
    >
      <span class="pet-tree-select__label" v-if="selectedLabel">{{ selectedLabel }}</span>
      <span class="pet-tree-select__placeholder" v-else>{{ placeholder }}</span>
      <i
        v-if="clearable && selectedLabel"
        class="el-icon-circle-close pet-tree-select__clear"
        @click.stop="handleClear"
      />
      <i
        class="el-icon-arrow-down pet-tree-select__arrow"
        :class="{ 'is-reverse': popoverVisible }"
      />
    </div>
  </el-popover>
</template>

<script>
/**
 * 树形下拉选择器组件
 *
 * @description 基于 el-popover + el-tree 封装的树形下拉选择器，支持单选、清空、搜索过滤
 *
 * @example
 * // 基础用法
 * <tree-select
 *   v-model="selectedId"
 *   :tree-data="treeData"
 *   placeholder="请选择分类"
 * />
 *
 * // 自定义配置
 * <tree-select
 *   v-model="form.categoryId"
 *   :tree-data="categoryTree"
 *   :props="{ label: 'name', children: 'children' }"
 *   node-key="id"
 *   :clearable="true"
 *   :expand-all="true"
 * />
 *
 * @property {[String, Number, Array]} value - 当前选中的值（v-model 绑定）
 * @property {Array} treeData - 树结构数据
 * @property {String} placeholder - 占位符文本，默认"请选择"
 * @property {Object} props - 树节点属性配置，参考 el-tree props
 * @property {String} nodeKey - 节点唯一标识的 key，默认'id'
 * @property {Boolean} clearable - 是否可清空选择，默认 true
 * @property {Boolean} expandAll - 是否默认展开所有节点，默认 true
 * @property {Array} defaultExpandedKeys - 默认展开的节点 key 数组
 * @property {String} emptyText - 无数据时显示文本
 *
 * @event input - 值变化时触发，用于 v-model 绑定
 * @event change - 选中值变化时触发，回调参数为 (value, node)
 * @event node-click - 节点点击时触发
 */
const DEFAULT_PROPS = {
  label: 'label',
  children: 'children',
  value: 'value'
}

export default {
  name: 'TreeSelect',
  props: {
    value: {
      type: [String, Number, Array],
      default: null
    },
    treeData: {
      type: Array,
      required: true,
      default: () => []
    },
    placeholder: {
      type: String,
      default: '请选择'
    },
    props: {
      type: Object,
      default: () => ({})
    },
    nodeKey: {
      type: String,
      default: 'id'
    },
    clearable: {
      type: Boolean,
      default: true
    },
    expandAll: {
      type: Boolean,
      default: true
    },
    defaultExpandedKeys: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      popoverVisible: false,
      filterText: '',
      currentValue: this.value,
      selectedNode: null,
      expandedKeys: []
    }
  },
  computed: {
    mergedProps() {
      return { ...DEFAULT_PROPS, ...this.props }
    },
    treeProps() {
      return {
        label: this.mergedProps.label,
        children: this.mergedProps.children,
        value: this.mergedProps.value
      }
    },
    popoverWidth() {
      return 260
    },
    selectedLabel() {
      if (!this.selectedNode) return ''
      const labelKey = this.mergedProps.label
      return this.selectedNode[labelKey] || ''
    }
  },
  watch: {
    value: {
      handler(val) {
        this.currentValue = val
        this.syncSelectedNode()
      },
      immediate: true
    },
    treeData: {
      handler() {
        this.syncSelectedNode()
        this.initExpandedKeys()
      },
      immediate: true,
      deep: true
    },
    filterText(val) {
      this.$refs.tree && this.$refs.tree.filter(val)
    }
  },
  mounted() {
    this.initExpandedKeys()
    this.$nextTick(() => {
      this.syncSelectedNode()
    })
  },
  methods: {
    initExpandedKeys() {
      if (this.expandAll) {
        this.expandedKeys = this.getAllNodeKeys(this.treeData)
      } else if (this.defaultExpandedKeys.length > 0) {
        this.expandedKeys = [...this.defaultExpandedKeys]
      } else {
        this.expandedKeys = []
      }
    },
    getAllNodeKeys(nodes) {
      const keys = []
      if (!Array.isArray(nodes)) return keys
      const traverse = (list) => {
        list.forEach(node => {
          keys.push(node[this.nodeKey])
          const children = node[this.mergedProps.children]
          if (children && children.length) {
            traverse(children)
          }
        })
      }
      traverse(nodes)
      return keys
    },
    syncSelectedNode() {
      if (this.currentValue == null) {
        this.selectedNode = null
        return
      }
      this.$nextTick(() => {
        const tree = this.$refs.tree
        if (!tree) return
        const node = tree.getNode(this.currentValue)
        if (node) {
          this.selectedNode = node.data
          tree.setCurrentKey(this.currentValue)
        } else {
          this.selectedNode = null
        }
      })
    },
    handleNodeClick(data, node) {
      const value = data[this.nodeKey]
      this.currentValue = value
      this.selectedNode = data
      this.$emit('input', value)
      this.$emit('change', value, data)
      this.$emit('node-click', data, node)
      this.popoverVisible = false
    },
    handleClear() {
      this.currentValue = null
      this.selectedNode = null
      this.filterText = ''
      this.$emit('input', null)
      this.$emit('change', null, null)
      this.$emit('clear')
    },
    handlePopoverShow() {
      this.$nextTick(() => {
        this.initExpandedKeys()
        if (this.currentValue) {
          this.$refs.tree && this.$refs.tree.setCurrentKey(this.currentValue)
        }
      })
    },
    handlePopoverHide() {
      this.filterText = ''
    },
    handleNodeExpand(data) {
      const key = data[this.nodeKey]
      if (!this.expandedKeys.includes(key)) {
        this.expandedKeys.push(key)
      }
    },
    handleNodeCollapse(data) {
      const key = data[this.nodeKey]
      this.expandedKeys = this.expandedKeys.filter(k => k !== key)
    },
    filterNode(value, data) {
      if (!value) return true
      const labelKey = this.mergedProps.label
      const label = (data[labelKey] || '').toLowerCase()
      return label.indexOf(value.toLowerCase()) !== -1
    },
    focus() {
      this.popoverVisible = true
    },
    blur() {
      this.popoverVisible = false
    }
  }
}
</script>

<style lang="scss" scoped>
.pet-tree-select {
  &__trigger {
    display: flex;
    align-items: center;
    width: 100%;
    min-height: 34px;
    padding: 0 30px 0 12px;
    background: #fff;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    cursor: pointer;
    transition: border-color 0.2s;
    box-sizing: border-box;
    font-size: 14px;
    color: #606266;

    &:hover {
      border-color: #c0c4cc;
    }

    &.is-active {
      border-color: #409eff;

      .pet-tree-select__arrow {
        transform: rotate(180deg);
      }
    }

    &.is-placeholder {
      color: #c0c4cc;
    }
  }

  &__label {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__placeholder {
    flex: 1;
  }

  &__clear {
    position: absolute;
    right: 28px;
    top: 50%;
    transform: translateY(-50%);
    display: none;
    color: #c0c4cc;
    font-size: 14px;

    &:hover {
      color: #909399;
    }
  }

  &__trigger:hover .pet-tree-select__clear {
    display: inline-block;
  }

  &__arrow {
    position: absolute;
    right: 10px;
    top: 50%;
    transform: translateY(-50%);
    font-size: 12px;
    color: #c0c4cc;
    transition: transform 0.2s;

    &.is-reverse {
      transform: translateY(-50%) rotate(180deg);
    }
  }

  &__search {
    padding: 8px 10px 4px;
    border-bottom: 1px solid #f0f0f0;
    margin-bottom: 4px;

    ::v-deep .el-input {
      width: 100%;
    }
  }

  &__tree {
    max-height: 300px;
    overflow-y: auto;
    padding: 4px 0;

    ::v-deep .el-tree-node__content {
      height: 34px;
      line-height: 34px;
    }

    ::v-deep .el-tree-node.is-current > .el-tree-node__content {
      background-color: #ecf5ff;
      color: #409eff;
    }
  }
}
</style>

<style lang="scss">
.pet-tree-select__popover {
  padding: 0 !important;
  overflow: hidden;
}
</style>
