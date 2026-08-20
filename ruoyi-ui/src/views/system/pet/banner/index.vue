<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="标题" prop="title">
        <el-input
          v-model="queryParams.title"
          placeholder="请输入标题"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 160px">
          <el-option
            v-for="item in statusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['banner:list']"
        >新增</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 轮播图列表表格 -->
    <el-table v-loading="loading" :data="bannerList">
      <el-table-column label="序号" type="index" width="55" align="center" />
      <el-table-column label="轮播图片" align="center" width="120">
        <template slot-scope="scope">
          <el-image
            v-if="scope.row.imageUrl"
            class="banner-thumbnail"
            :src="formatImageUrl(scope.row.imageUrl)"
            :preview-src-list="[formatImageUrl(scope.row.imageUrl)]"
            fit="cover"
            :z-index="9999"
          >
            <div slot="error" class="image-fallback">
              <i class="el-icon-picture-outline"></i>
            </div>
          </el-image>
          <div v-else class="image-fallback">
            <i class="el-icon-picture-outline"></i>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="标题" align="left" prop="title" min-width="160" :show-overflow-tooltip="true" />
      <el-table-column label="跳转类型" align="center" width="110">
        <template slot-scope="scope">
          <el-tag :type="jumpTypeTagType(scope.row.jumpType)">
            {{ getJumpTypeLabel(scope.row.jumpType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="跳转目标" align="center" prop="jumpTarget" width="140" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <span>{{ scope.row.jumpType === 'none' ? '-' : (scope.row.jumpTarget || '-') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="排序号" align="center" prop="sortOrder" width="80" />
      <el-table-column label="状态" align="center" width="90">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === '1' ? 'success' : 'info'">
            {{ scope.row.status === '1' ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="展示时间" align="center" width="220">
        <template slot-scope="scope">
          <span>{{ formatTimeRange(scope.row) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="160">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['banner:list']"
          >编辑</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['banner:list']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 新增/编辑轮播图对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="780px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入轮播图标题" maxlength="50" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="轮播图片" prop="imageUrl">
              <image-upload :value="form.imageUrl" @input="onImageUrlInput" :limit="1" :file-size="5" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="跳转类型" prop="jumpType">
              <el-select v-model="form.jumpType" placeholder="请选择跳转类型" style="width: 100%">
                <el-option
                  v-for="item in jumpTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="form.jumpType !== 'none'">
            <el-form-item label="跳转目标" prop="jumpTarget">
              <el-input v-model="form.jumpTarget" :placeholder="jumpTargetPlaceholder" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序号" prop="sortOrder">
              <el-input-number v-model="form.sortOrder" controls-position="right" :min="0" :precision="0" placeholder="留空自动取最大值+1" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-switch v-model="form.status" active-value="1" inactive-value="0" active-text="启用" inactive-text="停用" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                v-model="form.startTime"
                type="datetime"
                value-format="yyyy-MM-dd HH:mm:ss"
                placeholder="选择展示开始时间"
                style="width: 100%"
                :picker-options="startTimeOptions"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker
                v-model="form.endTime"
                type="datetime"
                value-format="yyyy-MM-dd HH:mm:ss"
                placeholder="选择展示结束时间"
                style="width: 100%"
                :picker-options="endTimeOptions"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" maxlength="200" show-word-limit />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  getBannerList,
  getBannerDetail,
  addBanner,
  updateBanner,
  deleteBanner
} from "@/api/system/pet/banner";

export default {
  name: "PetBanner",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 轮播图表格数据
      bannerList: [],
      // 状态选项
      statusOptions: [
        { value: "1", label: "启用" },
        { value: "0", label: "停用" }
      ],
      // 跳转类型选项
      jumpTypeOptions: [
        { value: "none", label: "不跳转" },
        { value: "post", label: "动态详情" },
        { value: "product", label: "商品详情" },
        { value: "url", label: "外部链接" },
        { value: "miniapp", label: "小程序页面" }
      ],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: undefined,
        status: undefined
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        title: [
          { required: true, message: "标题不能为空", trigger: "blur" }
        ],
        imageUrl: [
          { required: true, message: "图片地址不能为空", trigger: "change" }
        ],
        jumpType: [
          { required: true, message: "跳转类型不能为空", trigger: "change" }
        ]
      }
    };
  },
  computed: {
    // 跳转目标输入框占位符
    jumpTargetPlaceholder() {
      const map = {
        post: "请输入动态ID",
        product: "请输入商品ID",
        url: "请输入外部链接地址（含http(s)://）",
        miniapp: "请输入小程序页面路径"
      };
      return map[this.form.jumpType] || "请输入跳转目标";
    },
    // 开始时间不可早于已选结束时间
    startTimeOptions() {
      return {
        disabledDate: (time) => {
          if (this.form.endTime) {
            return time.getTime() > new Date(this.form.endTime).getTime();
          }
          return false;
        }
      };
    },
    // 结束时间不可早于已选开始时间
    endTimeOptions() {
      return {
        disabledDate: (time) => {
          if (this.form.startTime) {
            return time.getTime() < new Date(this.form.startTime).getTime() - 8.64e7;
          }
          return false;
        }
      };
    }
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询轮播图列表 */
    getList() {
      this.loading = true;
      getBannerList(this.queryParams).then(response => {
        this.bannerList = response.rows;
        this.total = response.total;
        this.loading = false;
      }).catch(() => {
        this.loading = false;
      });
    },
    /** 跳转类型对应文案 */
    getJumpTypeLabel(value) {
      const item = this.jumpTypeOptions.find(o => o.value === value);
      return item ? item.label : (value || "-");
    },
    /** 跳转类型对应标签样式 */
    jumpTypeTagType(value) {
      const map = { none: "info", post: "", product: "success", url: "warning", miniapp: "danger" };
      return map[value] || "info";
    },
    /** 展示时间范围文案 */
    formatTimeRange(row) {
      if (!row.startTime && !row.endTime) return "长期";
      const start = row.startTime || "不限";
      const end = row.endTime || "不限";
      return start + " ~ " + end;
    },
    /** 拼接图片地址（处理后端返回的相对路径） */
    formatImageUrl(url) {
      if (!url) return "";
      if (url.startsWith("data:") || url.startsWith("blob:")) return url;
      if (url.startsWith("http://") || url.startsWith("https://")) {
        const idx = url.indexOf("/profile");
        if (idx !== -1) {
          return process.env.VUE_APP_BASE_API + url.substring(idx);
        }
        return url;
      }
      return process.env.VUE_APP_BASE_API + (url.startsWith("/") ? url : "/" + url);
    },
    /** 图片上传组件值变化 */
    onImageUrlInput(val) {
      this.form.imageUrl = val;
      this.$refs.form && this.$refs.form.validateField("imageUrl");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 表单重置 */
    reset() {
      this.form = {
        id: undefined,
        title: undefined,
        imageUrl: undefined,
        jumpType: "none",
        jumpTarget: undefined,
        sortOrder: undefined,
        status: "1",
        startTime: null,
        endTime: null,
        remark: undefined
      };
      this.resetForm("form");
    },
    /** 取消按钮 */
    cancel() {
      this.open = false;
      this.reset();
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "新增轮播图";
    },
    /** 编辑按钮操作 */
    handleUpdate(row) {
      this.reset();
      getBannerDetail(row.id).then(response => {
        this.form = { ...response.data };
        if (!this.form.jumpType) {
          this.form.jumpType = "none";
        }
        if (this.form.status === undefined || this.form.status === null) {
          this.form.status = "1";
        }
        this.open = true;
        this.title = "编辑轮播图";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (!valid) {
          return;
        }
        const data = { ...this.form };
        // 不跳转时清空跳转目标
        if (data.jumpType === "none") {
          data.jumpTarget = null;
        }
        if (data.id != undefined) {
          updateBanner(data).then(() => {
            this.$modal.msgSuccess("修改成功");
            this.open = false;
            this.getList();
          });
        } else {
          addBanner(data).then(() => {
            this.$modal.msgSuccess("新增成功");
            this.open = false;
            this.getList();
          });
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      this.$modal.confirm('是否确认删除轮播图"' + row.title + '"？').then(function() {
        return deleteBanner(row.id);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    }
  }
};
</script>

<style lang="scss" scoped>
.banner-thumbnail {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  cursor: pointer;

  .image-fallback {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f5f7fa;
    color: #c0c4cc;
    font-size: 20px;
    border-radius: 4px;
  }
}

.image-fallback {
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #c0c4cc;
  font-size: 20px;
  border-radius: 4px;
  margin: 0 auto;
}
</style>
