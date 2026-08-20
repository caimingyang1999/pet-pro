<template>
  <div class="app-container">
    <el-row :gutter="16">
      <!-- 左侧分类树 -->
      <el-col :span="8">
        <el-card class="tree-card" shadow="never">
          <div slot="header" class="panel-header">
            <span class="panel-title">商品分类</span>
            <el-button
              type="primary"
              plain
              icon="el-icon-plus"
              size="mini"
              @click="handleAddTop"
              v-hasPermi="['system:pet:category:add']"
            >新增顶级分类</el-button>
          </div>
          <div v-loading="loading">
            <el-tree
              v-if="categoryList.length"
              ref="tree"
              :data="categoryList"
              node-key="id"
              :props="defaultProps"
              :default-expanded-keys="expandedKeys"
              :expand-on-click-node="false"
              highlight-current
              @node-click="handleNodeClick"
            >
              <span class="custom-tree-node" slot-scope="{ node, data }">
                <span class="node-label">
                  <el-image
                    v-if="data.icon"
                    class="node-icon"
                    :src="data.icon"
                    fit="cover"
                  >
                    <div slot="error" class="node-icon-fallback">
                      <i class="el-icon-folder"></i>
                    </div>
                  </el-image>
                  <i v-else class="el-icon-folder node-icon-default"></i>
                  <span class="node-name">{{ data.categoryName }}</span>
                </span>
                <span class="node-actions">
                  <el-button
                    type="text"
                    size="mini"
                    icon="el-icon-plus"
                    title="新增子分类"
                    @click.stop="handleAdd(data)"
                    v-hasPermi="['system:pet:category:add']"
                  ></el-button>
                  <el-button
                    type="text"
                    size="mini"
                    icon="el-icon-edit"
                    title="编辑"
                    @click.stop="handleEdit(data)"
                    v-hasPermi="['system:pet:category:edit']"
                  ></el-button>
                  <el-button
                    type="text"
                    size="mini"
                    icon="el-icon-delete"
                    title="删除"
                    @click.stop="handleDelete(data)"
                    v-hasPermi="['system:pet:category:remove']"
                  ></el-button>
                </span>
              </span>
            </el-tree>
            <el-empty v-else description="暂无分类数据" :image-size="80" />
          </div>
        </el-card>
      </el-col>

      <!-- 右侧详情/编辑区 -->
      <el-col :span="16">
        <el-card class="detail-card" shadow="never">
          <!-- 顶部标题与操作 -->
          <div slot="header" class="panel-header">
            <span class="panel-title">{{ detailTitle }}</span>
            <div v-if="mode === 'view' && current.id" class="header-actions">
              <el-button
                type="primary"
                plain
                icon="el-icon-plus"
                size="mini"
                @click="handleAdd(current)"
                v-hasPermi="['system:pet:category:add']"
              >新增子分类</el-button>
              <el-button
                type="success"
                plain
                icon="el-icon-edit"
                size="mini"
                @click="handleEdit(current)"
                v-hasPermi="['system:pet:category:edit']"
              >编辑</el-button>
            </div>
          </div>

          <!-- 查看模式 -->
          <div v-if="mode === 'view'" class="detail-view">
            <template v-if="current.id">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="分类名称">{{ current.categoryName }}</el-descriptions-item>
                <el-descriptions-item label="上级分类">{{ current.parentName || (current.parentId ? '主分类' : '顶级分类') }}</el-descriptions-item>
                <el-descriptions-item label="排序">{{ current.sortOrder }}</el-descriptions-item>
                <el-descriptions-item label="状态">
                  <dict-tag :options="dict.type.sys_normal_disable" :value="current.status" />
                </el-descriptions-item>
                <el-descriptions-item label="分类图标" :span="2">
                  <el-image
                    v-if="current.icon"
                    class="detail-icon"
                    :src="current.icon"
                    fit="cover"
                    :preview-src-list="[current.icon]"
                  >
                    <div slot="error" class="detail-icon-fallback">
                      <i class="el-icon-picture-outline"></i>
                    </div>
                  </el-image>
                  <span v-else class="text-placeholder">暂无图标</span>
                </el-descriptions-item>
                <el-descriptions-item label="创建时间">{{ parseTime(current.createTime) }}</el-descriptions-item>
                <el-descriptions-item label="更新时间">{{ parseTime(current.updateTime) }}</el-descriptions-item>
              </el-descriptions>
            </template>
            <el-empty v-else description="请选择左侧分类查看详情" :image-size="100" />
          </div>

          <!-- 新增/编辑表单 -->
          <div v-else class="form-view">
            <el-form ref="form" :model="form" :rules="rules" label-width="100px">
              <el-form-item label="上级分类">
                <el-input :value="form.parentName || '顶级分类'" disabled />
              </el-form-item>
              <el-form-item label="分类名称" prop="categoryName">
                <el-input v-model="form.categoryName" placeholder="请输入分类名称" maxlength="30" show-word-limit />
              </el-form-item>
              <el-form-item label="分类图标">
                <image-upload v-model="form.icon" :limit="1" :file-size="2" />
              </el-form-item>
              <el-form-item label="显示排序" prop="sortOrder">
                <el-input-number v-model="form.sortOrder" controls-position="right" :min="0" />
              </el-form-item>
              <el-form-item label="状态" prop="status">
                <el-radio-group v-model="form.status">
                  <el-radio
                    v-for="dict in dict.type.sys_normal_disable"
                    :key="dict.value"
                    :label="dict.value"
                  >{{ dict.label }}</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-form>
            <div class="form-footer">
              <el-button type="primary" :loading="submitting" @click="submitForm">保 存</el-button>
              <el-button @click="cancel">取 消</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getCategoryList, addCategory, updateCategory, deleteCategory } from "@/api/system/pet/category";

export default {
  name: "PetCategory",
  dicts: ['sys_normal_disable'],
  data() {
    return {
      // 遮罩层
      loading: true,
      // 提交中
      submitting: false,
      // 分类树数据
      categoryList: [],
      // 默认展开的节点（第一级）
      expandedKeys: [],
      // 树属性配置
      defaultProps: {
        children: "children",
        label: "categoryName"
      },
      // 右侧模式：view-查看 add-新增 edit-编辑
      mode: "view",
      // 当前选中节点
      current: {},
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        categoryName: [
          { required: true, message: "分类名称不能为空", trigger: "blur" }
        ],
        sortOrder: [
          { required: true, message: "显示排序不能为空", trigger: "blur" }
        ],
        status: [
          { required: true, message: "请选择状态", trigger: "change" }
        ]
      }
    };
  },
  computed: {
    detailTitle() {
      if (this.mode === "add") {
        return "新增分类";
      } else if (this.mode === "edit") {
        return "编辑分类";
      }
      return "分类详情";
    }
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询分类树 */
    getList() {
      this.loading = true;
      getCategoryList().then(response => {
        this.categoryList = response.data || [];
        // 默认展开第一级
        this.expandedKeys = this.categoryList.map(item => item.id);
        this.loading = false;
      }).catch(() => {
        this.loading = false;
      });
    },
    /** 节点点击事件 */
    handleNodeClick(data) {
      this.current = data;
      this.mode = "view";
    },
    /** 新增顶级分类 */
    handleAddTop() {
      this.reset();
      this.form.parentId = 0;
      this.form.parentName = "顶级分类";
      this.mode = "add";
    },
    /** 新增子分类 */
    handleAdd(data) {
      this.reset();
      this.form.parentId = data.id;
      this.form.parentName = data.categoryName;
      this.mode = "add";
    },
    /** 编辑分类 */
    handleEdit(data) {
      this.reset();
      this.form = {
        id: data.id,
        parentId: data.parentId || 0,
        parentName: data.parentName || (data.parentId ? "主分类" : "顶级分类"),
        categoryName: data.categoryName,
        icon: data.icon,
        sortOrder: data.sortOrder,
        status: data.status || "0"
      };
      this.mode = "edit";
    },
    /** 表单重置 */
    reset() {
      this.form = {
        id: undefined,
        parentId: 0,
        parentName: "顶级分类",
        categoryName: undefined,
        icon: undefined,
        sortOrder: 0,
        status: "0"
      };
      this.resetForm("form");
    },
    /** 取消按钮 */
    cancel() {
      this.mode = "view";
      this.reset();
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (!valid) {
          return;
        }
        this.submitting = true;
        const data = { ...this.form };
        // 移除仅用于展示的字段
        delete data.parentName;
        const action = data.id ? updateCategory(data) : addCategory(data);
        action.then(() => {
          this.$modal.msgSuccess(data.id ? "修改成功" : "新增成功");
          this.submitting = false;
          this.mode = "view";
          this.getList();
        }).catch(() => {
          this.submitting = false;
        });
      });
    },
    /** 删除按钮操作 */
    handleDelete(data) {
      // 检查是否有子节点
      if (data.children && data.children.length > 0) {
        this.$modal.msgWarning("该分类存在子分类，无法删除，请先删除子分类");
        return;
      }
      this.$modal.confirm('是否确认删除分类"' + data.categoryName + '"？').then(function() {
        return deleteCategory(data.id);
      }).then(() => {
        // 删除的是当前选中项时清空右侧
        if (this.current.id === data.id) {
          this.current = {};
          this.mode = "view";
        }
        this.$modal.msgSuccess("删除成功");
        this.getList();
      }).catch(() => {});
    }
  }
};
</script>

<style lang="scss" scoped>
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;

  .panel-title {
    font-size: 15px;
    font-weight: 600;
    color: #303133;

    &::before {
      content: "";
      display: inline-block;
      width: 3px;
      height: 14px;
      background: #409eff;
      margin-right: 8px;
      vertical-align: middle;
    }
  }
}

.tree-card,
.detail-card {
  min-height: calc(100vh - 160px);
}

.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-right: 8px;
  font-size: 14px;
  overflow: hidden;

  .node-label {
    display: flex;
    align-items: center;
    overflow: hidden;

    .node-icon {
      width: 20px;
      height: 20px;
      border-radius: 4px;
      margin-right: 6px;
      flex-shrink: 0;

      .node-icon-fallback {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #f0f0f0;
        color: #c0c4cc;
        font-size: 14px;
        border-radius: 4px;
      }
    }

    .node-icon-default {
      color: #e6a23c;
      margin-right: 6px;
      font-size: 18px;
    }

    .node-name {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .node-actions {
    display: none;
    flex-shrink: 0;
    margin-left: 8px;

    .el-button {
      margin-left: 2px;
      padding: 4px;
    }
  }
}

.custom-tree-node:hover .node-actions {
  display: inline-flex;
}

.detail-view {
  .detail-icon {
    width: 80px;
    height: 80px;
    border-radius: 6px;
    cursor: pointer;

    .detail-icon-fallback {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f5f7fa;
      color: #c0c4cc;
      font-size: 28px;
      border-radius: 6px;
    }
  }
}

.form-view {
  .form-footer {
    text-align: center;
    margin-top: 20px;
  }
}

.text-placeholder {
  color: #c0c4cc;
  font-size: 13px;
}
</style>
