<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="标题" prop="title">
        <el-input
          v-model="queryParams.title"
          placeholder="请输入标题关键词"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="分类" prop="category">
        <el-select v-model="queryParams.category" placeholder="请选择分类" clearable style="width: 160px">
          <el-option
            v-for="dict in dict.type.article_category"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="适用宠物" prop="petType">
        <el-select v-model="queryParams.petType" placeholder="请选择" clearable style="width: 140px">
          <el-option
            v-for="dict in dict.type.pet_types"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
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
          v-hasPermi="['article:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-magic-stick"
          size="mini"
          @click="openAiDialog"
          v-hasPermi="['article:add']"
        >AI 生成</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 文章列表表格 -->
    <el-table v-loading="loading" :data="articleList">
      <el-table-column label="序号" type="index" width="55" align="center" />
      <el-table-column label="封面" align="center" width="100">
        <template slot-scope="scope">
          <el-image
            v-if="scope.row.coverImage"
            class="article-thumbnail"
            :src="formatImageUrl(scope.row.coverImage)"
            :preview-src-list="[formatImageUrl(scope.row.coverImage)]"
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
      <el-table-column label="标题" align="left" prop="title" min-width="200" :show-overflow-tooltip="true" />
      <el-table-column label="分类" align="center" width="90">
        <template slot-scope="scope">
          <dict-tag v-if="scope.row.category" :options="dict.type.article_category" :value="scope.row.category" />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="适用宠物" align="center" width="100">
        <template slot-scope="scope">
          <dict-tag v-if="scope.row.petType" :options="dict.type.pet_types" :value="scope.row.petType" />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="来源" align="center" prop="source" width="130" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <span>{{ scope.row.source || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="浏览量" align="center" prop="viewCount" width="80" />
      <el-table-column label="排序号" align="center" prop="sortOrder" width="80" />
      <el-table-column label="状态" align="center" width="100">
        <template slot-scope="scope">
          <el-switch
            v-if="hasEditPermi"
            v-model="scope.row.status"
            active-value="1"
            inactive-value="0"
            @change="handleStatusChange(scope.row)"
          />
          <el-tag v-else :type="scope.row.status === '1' ? 'success' : 'info'">
            {{ scope.row.status === '1' ? '已发布' : '草稿' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="140">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['article:edit']"
          >编辑</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['article:remove']"
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

    <!-- 新增/编辑文章对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="880px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="文章标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入文章标题" maxlength="100" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" prop="category">
              <el-select v-model="form.category" placeholder="请选择分类" clearable filterable style="width: 100%">
                <el-option
                  v-for="dict in dict.type.article_category"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="适用宠物" prop="petType">
              <el-select v-model="form.petType" placeholder="请选择" clearable style="width: 100%">
                <el-option
                  v-for="dict in dict.type.pet_types"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="摘要" prop="summary">
              <el-input
                v-model="form.summary"
                type="textarea"
                :rows="2"
                placeholder="列表页展示的摘要，建议 40~80 字"
                maxlength="255"
                show-word-limit
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="封面图" prop="coverImage">
              <image-upload :value="form.coverImage" @input="onCoverInput" :limit="1" :file-size="5" />
              <div class="form-tip">建议尺寸 750×420，未上传时小程序列表按纯文字样式展示</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="来源" prop="source">
              <el-input v-model="form.source" placeholder="如：宠迹编辑部" maxlength="100" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="标签" prop="tags">
              <el-input v-model="form.tags" placeholder="多个标签用英文逗号分隔" maxlength="200" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序号" prop="sortOrder">
              <el-input-number
                v-model="form.sortOrder"
                controls-position="right"
                :min="0"
                :precision="0"
                style="width: 100%"
              />
              <div class="form-tip">数字越小越靠前，默认 0</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio label="1">已发布</el-radio>
                <el-radio label="0">草稿</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="正文" prop="content">
              <editor v-model="form.content" :min-height="320" />
              <div class="form-tip">正文中的图片请使用编辑器自带的上传按钮，保存时会自动处理为小程序可识别的地址</div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- AI 生成文章对话框 -->
    <el-dialog :title="aiTitle" :visible.sync="aiOpen" width="560px" append-to-body :close-on-click-modal="false">
      <el-form ref="aiForm" :model="aiForm" label-width="92px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="适用宠物" prop="petType">
              <el-select v-model="aiForm.petType" placeholder="请选择" clearable style="width: 100%">
                <el-option
                  v-for="dict in dict.type.pet_types"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="主题分类" prop="category">
              <el-select v-model="aiForm.category" placeholder="请选择" clearable filterable style="width: 100%">
                <el-option
                  v-for="dict in dict.type.article_category"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="指定选题" prop="topic">
              <el-input
                v-model="aiForm.topic"
                placeholder="留空则由 AI 自拟一个最实用的话题"
                maxlength="60"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="生成配图" prop="withImage">
              <el-switch v-model="aiForm.withImage" active-text="生成" inactive-text="不生成" />
              <span class="form-tip" style="margin-left: 8px">
                {{ aiStatus.aiImageEnabled ? 'AI 出图已启用' : 'AI 出图未配置，将回退到内置图库' }}
              </span>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="内嵌配图数" prop="inlineCount">
              <el-input-number
                v-model="aiForm.inlineCount"
                controls-position="right"
                :min="1"
                :max="3"
                :disabled="!aiForm.withImage"
                style="width: 140px"
              />
              <span class="form-tip" style="margin-left: 8px">正文内嵌插图数量（1~3 张）</span>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" :loading="aiLoading" @click="submitAi">
          {{ aiLoading ? 'AI 生成中（约 20~40 秒）' : '开始生成' }}
        </el-button>
        <el-button @click="cancelAi" :disabled="aiLoading">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  getArticleList,
  getArticleDetail,
  addArticle,
  updateArticle,
  changeArticleStatus,
  deleteArticle,
  generateArticle,
  getArticleGenerateStatus
} from "@/api/system/pet/article";
import { checkPermi } from "@/utils/permission";

/** 后台接口前缀（开发环境 /dev-api，生产环境 /prod-api） */
const API_PREFIX = process.env.VUE_APP_BASE_API || "";

/** 匹配 <img> 标签 src 属性中的图片地址 */
const IMG_SRC_RE = /(<img[^>]+src=["'])([^"']+)(["'])/gi;

/**
 * 把图片地址转成后台可预览的完整地址，供富文本编辑器显示。
 * 数据库里统一存 /profile/... 这种与域名无关的相对路径。
 */
function toPreviewSrc(src) {
  if (/^(data:|blob:)/i.test(src)) return src;
  const idx = src.indexOf("/profile");
  return idx === -1 ? src : API_PREFIX + src.substring(idx);
}

/**
 * 把图片地址还原成与域名无关的 /profile/... 形式再入库。
 * 编辑器插入图片时会带上 /dev-api 或 /prod-api 前缀，
 * 若原样保存，小程序端 rich-text 渲染时会拼成错误地址导致图片打不开。
 */
function toStoreSrc(src) {
  if (/^(data:|blob:)/i.test(src)) return src;
  const idx = src.indexOf("/profile");
  return idx === -1 ? src : src.substring(idx);
}

export default {
  name: "PetArticle",
  // 适用宠物、文章分类均走数据字典（sys_dict_type: pet_types / article_category），后台可维护、不用改代码
  dicts: ["pet_types", "article_category"],
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 文章表格数据
      articleList: [],
      // 状态选项
      statusOptions: [
        { value: "1", label: "已发布" },
        { value: "0", label: "草稿" }
      ],
      // 分类选项已改为数据字典驱动，见 dicts:["article_category"]
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // AI 生成对话框是否显示
      aiOpen: false,
      // AI 生成中
      aiLoading: false,
      // AI 生成对话框标题
      aiTitle: "AI 生成文章",
      // AI 生成能力探测结果
      aiStatus: {
        available: false,
        aiImageEnabled: false
      },
      // AI 生成表单参数
      aiForm: {},
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: undefined,
        category: undefined,
        petType: undefined,
        status: undefined
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        title: [
          { required: true, message: "文章标题不能为空", trigger: "blur" },
          { max: 100, message: "文章标题不能超过 100 个字", trigger: "blur" }
        ],
        content: [
          { validator: this.validateContent, trigger: "change" }
        ]
      }
    };
  },
  computed: {
    /** 是否具备编辑权限（用于列表页状态开关） */
    hasEditPermi() {
      return checkPermi(["article:edit"]);
    }
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询文章列表 */
    getList() {
      this.loading = true;
      getArticleList(this.queryParams).then(response => {
        this.articleList = response.rows;
        this.total = response.total;
        this.loading = false;
      }).catch(() => {
        this.loading = false;
      });
    },
    /** 正文校验：正式发布时必须填写正文，草稿允许留空 */
    validateContent(rule, value, callback) {
      if (this.form.status === "1" && !this.stripHtml(value)) {
        callback(new Error("正式发布前请填写正文内容"));
      } else {
        callback();
      }
    },
    /** 去除富文本标签后取纯文本，用于判断正文是否为空 */
    stripHtml(html) {
      return String(html || "")
        .replace(/<[^>]+>/g, "")
        .replace(/&nbsp;/gi, " ")
        .trim();
    },
    /** 拼接图片地址（处理后端返回的相对路径） */
    formatImageUrl(url) {
      if (!url) return "";
      if (/^(data:|blob:)/i.test(url)) return url;
      if (/^https?:\/\//i.test(url)) {
        const idx = url.indexOf("/profile");
        return idx !== -1 ? API_PREFIX + url.substring(idx) : url;
      }
      return API_PREFIX + (url.startsWith("/") ? url : "/" + url);
    },
    /** 封面图上传组件值变化 */
    onCoverInput(val) {
      this.form.coverImage = val;
      this.$refs.form && this.$refs.form.validateField("coverImage");
    },
    /** 列表页状态开关：切换草稿/已发布 */
    handleStatusChange(row) {
      const status = row.status;
      const text = status === "1" ? "发布" : "转为草稿";
      this.$modal.confirm('是否确认将文章"' + row.title + '"' + text + '？').then(() => {
        return changeArticleStatus(row.id, status);
      }).then(() => {
        this.$modal.msgSuccess(text + "成功");
      }).catch(() => {
        // 用户取消或接口失败时回滚开关显示
        row.status = status === "1" ? "0" : "1";
      });
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
        summary: undefined,
        coverImage: undefined,
        content: undefined,
        category: undefined,
        petType: "general",
        tags: undefined,
        source: "宠迹编辑部",
        sortOrder: 0,
        status: "1"
      };
      this.resetForm("form");
    },
    /** 取消按钮 */
    cancel() {
      this.open = false;
      this.reset();
    },
    /** AI 生成对话框：打开前探测能力并初始化表单 */
    openAiDialog() {
      this.aiForm = {
        petType: undefined,
        category: undefined,
        topic: undefined,
        withImage: true,
        inlineCount: 1
      };
      this.aiOpen = true;
      this.aiTitle = "AI 生成文章";
      // 能力探测（失败不影响生成，仅用于提示文案）
      getArticleGenerateStatus().then(res => {
        const data = res.data || {};
        this.aiStatus = {
          available: data.available !== false,
          aiImageEnabled: !!data.aiImageEnabled
        };
      }).catch(() => {
        this.aiStatus = { available: false, aiImageEnabled: false };
      });
    },
    /** 取消 AI 生成对话框 */
    cancelAi() {
      this.aiOpen = false;
      this.aiForm = {};
    },
    /** 提交 AI 生成：调用后端生成接口，成功后直接打开草稿供审核 */
    submitAi() {
      this.aiLoading = true;
      const params = { ...this.aiForm };
      generateArticle(params).then(res => {
        this.aiLoading = false;
        this.aiOpen = false;
        const article = res.data || {};
        this.$modal.msgSuccess("已生成草稿，请在下方审核内容后发布");
        this.openReview(article);
      }).catch(() => {
        this.aiLoading = false;
      });
    },
    /** 用生成的文章数据打开编辑对话框（已是草稿），供人工审核/发布 */
    openReview(data) {
      const d = { ...data };
      // 正文图片地址补上预览前缀，避免编辑时显示为裂图
      d.content = String(d.content || "").replace(
        IMG_SRC_RE,
        (match, prefix, src, suffix) => prefix + toPreviewSrc(src) + suffix
      );
      if (d.status === undefined || d.status === null) {
        d.status = "0";
      }
      if (d.sortOrder === undefined || d.sortOrder === null) {
        d.sortOrder = 0;
      }
      this.form = d;
      this.open = true;
      this.title = "AI 生成草稿（请审核后发布）";
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "新增文章";
    },
    /** 编辑按钮操作 */
    handleUpdate(row) {
      this.reset();
      getArticleDetail(row.id).then(response => {
        const data = { ...response.data };
        // 补充后台可预览的图片前缀，避免编辑时正文图片显示为裂图
        data.content = String(data.content || "").replace(
          IMG_SRC_RE,
          (match, prefix, src, suffix) => prefix + toPreviewSrc(src) + suffix
        );
        this.form = data;
        if (this.form.status === undefined || this.form.status === null) {
          this.form.status = "1";
        }
        if (this.form.sortOrder === undefined || this.form.sortOrder === null) {
          this.form.sortOrder = 0;
        }
        this.open = true;
        this.title = "编辑文章";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (!valid) {
          return;
        }
        const data = { ...this.form };
        // 去掉图片地址中的接口前缀，保证小程序端能正确拼接出图片地址
        data.content = String(data.content || "").replace(
          IMG_SRC_RE,
          (match, prefix, src, suffix) => prefix + toStoreSrc(src) + suffix
        );
        if (data.id != undefined) {
          updateArticle(data).then(() => {
            this.$modal.msgSuccess("修改成功");
            this.open = false;
            this.getList();
          });
        } else {
          addArticle(data).then(() => {
            this.$modal.msgSuccess("新增成功");
            this.open = false;
            this.getList();
          });
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      this.$modal.confirm('是否确认删除文章"' + row.title + '"？').then(function() {
        return deleteArticle(row.id);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    }
  }
};
</script>

<style lang="scss" scoped>
.article-thumbnail {
  width: 72px;
  height: 48px;
  border-radius: 4px;
  cursor: pointer;

  .image-fallback {
    width: 100%;
    height: 100%;
  }
}

.image-fallback {
  width: 72px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #c0c4cc;
  font-size: 18px;
  border-radius: 4px;
  margin: 0 auto;
}

.form-tip {
  line-height: 1.6;
  font-size: 12px;
  color: #909399;
}
</style>
