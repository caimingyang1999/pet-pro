<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="提示词" prop="word">
        <el-input
          v-model="queryParams.word"
          placeholder="请输入提示词内容"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="宠物类型" prop="petType">
        <el-select v-model="queryParams.petType" placeholder="请选择宠物类型" clearable style="width: 160px">
          <el-option
            v-for="item in petTypeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="分类" prop="category">
        <el-select v-model="queryParams.category" placeholder="请选择分类" clearable style="width: 160px">
          <el-option
            v-for="item in categoryOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 140px">
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
          v-hasPermi="['admin']"
        >新增</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 推荐词列表表格 -->
    <el-table v-loading="loading" :data="suggestWordList">
      <el-table-column label="序号" type="index" width="55" align="center" />
      <el-table-column label="提示词内容" align="left"  prop="word" min-width="220" :show-overflow-tooltip="true" />
      <el-table-column label="宠物类型" align="center" width="100">
        <template slot-scope="scope">
          <el-tag :type="petTypeTagType(scope.row.petType)" effect="plain">
            {{ getPetTypeLabel(scope.row.petType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="分类" align="center" width="100">
        <template slot-scope="scope">
          <el-tag :type="categoryTagType(scope.row.category)">
            {{ getCategoryLabel(scope.row.category) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="权重" align="center" prop="weight" width="80" />
      <el-table-column label="状态" align="center" width="100">
        <template slot-scope="scope">
          <el-switch
            v-model="scope.row.status"
            active-value="1"
            inactive-value="0"
            @change="handleStatusChange(scope.row)"
          ></el-switch>
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
            v-hasPermi="['admin']"
          >编辑</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['admin']"
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

    <!-- 新增/编辑推荐词对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="720px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="提示词内容"  prop="word">
              <el-input
                v-model="form.word"
                type="textarea"
                :rows="3"
                placeholder="请输入提示词内容"
                maxlength="100"
                show-word-limit
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="宠物类型" prop="petType">
              <el-select v-model="form.petType" placeholder="请选择宠物类型" style="width: 100%">
                <el-option
                  v-for="item in petTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" prop="category">
              <el-select v-model="form.category" placeholder="请选择分类" style="width: 100%">
                <el-option
                  v-for="item in categoryOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="权重" prop="weight">
              <el-input-number v-model="form.weight" controls-position="right" :min="0" :max="9999" :precision="0" placeholder="数字越大越靠前" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-switch v-model="form.status" active-value="1" inactive-value="0" active-text="启用" inactive-text="停用" />
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
  getSuggestWordList,
  getSuggestWordDetail,
  addSuggestWord,
  updateSuggestWord,
  deleteSuggestWord
} from "@/api/system/pet/suggestWord";

export default {
  name: "PetSuggestWord",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 推荐词表格数据
      suggestWordList: [],
      // 宠物类型选项
      petTypeOptions: [
        { value: "general", label: "通用" },
        { value: "cat", label: "猫" },
        { value: "dog", label: "狗" },
        { value: "rabbit", label: "兔子" },
        { value: "bird", label: "鸟" },
        { value: "fish", label: "鱼" },
        { value: "other", label: "其他" }
      ],
      // 分类选项
      categoryOptions: [
        { value: "general", label: "通用" },
        { value: "care", label: "护理" },
        { value: "diet", label: "饮食" },
        { value: "medical", label: "医疗" },
        { value: "behavior", label: "行为" },
        { value: "training", label: "训练" }
      ],
      // 状态选项
      statusOptions: [
        { value: "1", label: "启用" },
        { value: "0", label: "停用" }
      ],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        word: undefined,
        petType: undefined,
        category: undefined,
        status: undefined
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        word: [
          { required: true, message: "提示词内容不能为空", trigger: "blur" }
        ],
        petType: [
          { required: true, message: "宠物类型不能为空", trigger: "change" }
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询推荐词列表 */
    getList() {
      this.loading = true;
      getSuggestWordList(this.queryParams).then(response => {
        this.suggestWordList = response.rows;
        this.total = response.total;
        this.loading = false;
      }).catch(() => {
        this.loading = false;
      });
    },
    /** 宠物类型对应文案 */
    getPetTypeLabel(value) {
      const item = this.petTypeOptions.find(o => o.value === value);
      return item ? item.label : (value || "-");
    },
    /** 分类对应文案 */
    getCategoryLabel(value) {
      const item = this.categoryOptions.find(o => o.value === value);
      return item ? item.label : (value || "-");
    },
    /** 宠物类型对应标签样式 */
    petTypeTagType(value) {
      const map = { general: "info", cat: "success", dog: "warning", rabbit: "", bird: "success", fish: "info", other: "info" };
      return map[value] || "info";
    },
    /** 分类对应标签样式 */
    categoryTagType(value) {
      const map = { general: "info", care: "", diet: "success", medical: "danger", behavior: "warning", training: "" };
      return map[value] || "info";
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
        word: undefined,
        petType: undefined,
        category: "general",
        weight: 100,
        status: "1"
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
      this.title = "新增推荐词";
    },
    /** 编辑按钮操作 */
    handleUpdate(row) {
      this.reset();
      getSuggestWordDetail(row.id).then(response => {
        this.form = { ...response.data };
        if (!this.form.category) {
          this.form.category = "general";
        }
        if (this.form.weight === undefined || this.form.weight === null) {
          this.form.weight = 100;
        }
        if (this.form.status === undefined || this.form.status === null) {
          this.form.status = "1";
        }
        this.open = true;
        this.title = "编辑推荐词";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (!valid) {
          return;
        }
        const data = { ...this.form };
        if (data.id != undefined) {
          updateSuggestWord(data).then(() => {
            this.$modal.msgSuccess("修改成功");
            this.open = false;
            this.getList();
          });
        } else {
          addSuggestWord(data).then(() => {
            this.$modal.msgSuccess("新增成功");
            this.open = false;
            this.getList();
          });
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      this.$modal.confirm('是否确认删除推荐词"' + row.word + '"？').then(function() {
        return deleteSuggestWord(row.id);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 推荐词状态切换 */
    handleStatusChange(row) {
      const text = row.status === "1" ? "启用" : "停用";
      this.$modal.confirm('确认要' + text + '推荐词"' + row.word + '"吗？').then(() => {
        return updateSuggestWord({
          id: row.id,
          word: row.word,
          petType: row.petType,
          category: row.category,
          weight: row.weight,
          status: row.status
        });
      }).then(() => {
        this.$modal.msgSuccess(text + "成功");
      }).catch(() => {
        // 恢复原状态
        row.status = row.status === "1" ? "0" : "1";
      });
    }
  }
};
</script>
