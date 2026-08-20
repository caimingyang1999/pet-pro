<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="动态状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择动态状态" clearable style="width: 180px">
          <el-option
            v-for="item in statusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="发布时间" prop="dateRange">
        <el-date-picker
          v-model="dateRange"
          style="width: 240px"
          value-format="yyyy-MM-dd"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        ></el-date-picker>
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
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:pet:post:remove']"
        >删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 动态列表表格 -->
    <el-table v-loading="loading" :data="postList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="45" align="center" />
      <el-table-column label="序号" type="index" width="50" align="center" />
      <el-table-column label="发布用户" align="left" min-width="150">
        <template slot-scope="scope">
          <div class="user-cell">
            <!-- <el-image
              class="user-avatar"
              :src="scope.row.userAvatar"
              fit="cover"
            >
              <div slot="error" class="avatar-fallback">
                <i class="el-icon-user-solid"></i>
              </div>
            </el-image> -->
            <span class="user-nickname">{{ scope.row.userName }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="动态内容" align="left" prop="content" min-width="200" show-overflow-tooltip>
        <template slot-scope="scope">
          <div class="post-content">{{ scope.row.content }}</div>
        </template>
      </el-table-column>
      <el-table-column label="关联宠物" align="center" prop="petName" min-width="90">
        <template slot-scope="scope">
          <span>{{ scope.row.petName || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="图片" align="center" width="80">
        <template slot-scope="scope">
          <div class="post-images" v-if="parseImages(scope.row.images).length">
            <el-image
              class="post-thumb"
              :src="parseImages(scope.row.images)[0]"
              fit="cover"
              :preview-src-list="parseImages(scope.row.images)"
            >
              <div slot="error" class="image-fallback">
                <i class="el-icon-picture-outline"></i>
              </div>
            </el-image>
            <span class="image-count" v-if="parseImages(scope.row.images).length > 1">
              {{ parseImages(scope.row.images).length }}
            </span>
          </div>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="点赞" align="center" prop="likeCount" width="60" />
      <el-table-column label="评论" align="center" prop="commentCount" width="60" />
      <el-table-column label="浏览" align="center" prop="viewCount" width="60" />
      <el-table-column label="状态" align="center" width="80">
        <template slot-scope="scope">
          <el-tag :type="getStatusType(scope.row.status)" size="mini">
            {{ getStatusLabel(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发布时间" align="center" prop="createTime" min-width="140">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="160">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleDetail(scope.row)"
            v-hasPermi="['system:pet:post:query']"
          >详情</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleAudit(scope.row)"
            v-hasPermi="['system:pet:post:audit']"
          >审核</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:pet:post:remove']"
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

    <!-- 动态详情/审核弹窗 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogOpen" width="700px" append-to-body>
      <div v-loading="detailLoading" class="post-detail">
        <!-- 用户信息 -->
        <div class="detail-header">
          <el-image
            class="detail-avatar"
            :src="detail.userAvatar"
            fit="cover"
          >
            <div slot="error" class="avatar-fallback">
              <i class="el-icon-user-solid"></i>
            </div>
          </el-image>
          <div class="detail-user-info">
            <div class="detail-nickname">{{ detail.userName }}</div>
            <div class="detail-meta">
              <span>关联宠物：{{ detail.petName || '-' }}</span>
              <span class="detail-time">发布时间：{{ parseTime(detail.createTime) }}</span>
            </div>
          </div>
        </div>

        <!-- 动态内容 -->
        <div class="detail-content">
          <div class="detail-section-title">动态内容</div>
          <div class="detail-text">{{ detail.content }}</div>
        </div>

        <!-- 图片九宫格 -->
        <div class="detail-content" v-if="parseImages(detail.images).length">
          <div class="detail-section-title">动态图片（{{ parseImages(detail.images).length }}张）</div>
          <div class="image-grid">
            <el-image
              v-for="(url, index) in parseImages(detail.images)"
              :key="index"
              class="grid-image"
              :src="url"
              :preview-src-list="parseImages(detail.images)"
              fit="cover"
            >
              <div slot="error" class="image-fallback">
                <i class="el-icon-picture-outline"></i>
              </div>
            </el-image>
          </div>
        </div>

        <!-- 互动数据 -->
        <div class="detail-content">
          <div class="detail-section-title">互动数据</div>
          <div class="detail-stats">
            <span>点赞：{{ detail.likeCount || 0 }}</span>
            <span>评论：{{ detail.commentCount || 0 }}</span>
            <span>浏览：{{ detail.viewCount || 0 }}</span>
          </div>
        </div>

        <!-- 拒绝原因（仅审核拒绝时显示） -->
        <div class="detail-content" v-if="auditMode && auditForm.status === '2'">
          <el-form ref="auditForm" :model="auditForm" label-width="80px">
            <el-form-item label="拒绝原因" prop="remark">
              <el-input
                v-model="auditForm.remark"
                type="textarea"
                :rows="3"
                placeholder="请输入拒绝原因（非必填）"
                maxlength="200"
                show-word-limit
              />
            </el-form-item>
          </el-form>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <template v-if="auditMode">
          <el-button
            type="success"
            @click="submitAudit('1')"
            :loading="auditSubmitting"
          >审核通过</el-button>
          <el-button
            type="danger"
            @click="submitAudit('2')"
            :loading="auditSubmitting"
          >审核拒绝</el-button>
        </template>
        <el-button @click="dialogOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  getPostList,
  getPostDetail,
  auditPost,
  deletePost
} from "@/api/system/pet/post";

export default {
  name: "PetPost",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 详情遮罩层
      detailLoading: false,
      // 选中数组
      ids: [],
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 动态表格数据
      postList: [],
      // 日期范围
      dateRange: [],
      // 状态选项
      statusOptions: [
        { value: "0", label: "待审核" },
        { value: "1", label: "已通过" },
        { value: "2", label: "已拒绝" }
      ],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        status: undefined,
        beginTime: undefined,
        endTime: undefined
      },
      // 弹窗是否显示
      dialogOpen: false,
      // 弹窗标题
      dialogTitle: "",
      // 是否为审核模式
      auditMode: false,
      // 审核提交中
      auditSubmitting: false,
      // 动态详情
      detail: {},
      // 审核表单
      auditForm: {
        status: "",
        remark: ""
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询动态列表 */
    getList() {
      this.loading = true;
      if (this.dateRange && this.dateRange.length === 2) {
        this.queryParams.beginTime = this.dateRange[0];
        this.queryParams.endTime = this.dateRange[1];
      } else {
        this.queryParams.beginTime = undefined;
        this.queryParams.endTime = undefined;
      }
      getPostList(this.queryParams).then(response => {
        this.postList = response.rows;
        this.total = response.total;
        this.loading = false;
      }).catch(() => {
        this.loading = false;
      });
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.dateRange = [];
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id);
      this.multiple = !selection.length;
    },
    /** 查看详情 */
    handleDetail(row) {
      this.auditMode = false;
      this.dialogTitle = "动态详情";
      this.loadDetail(row.id);
    },
    /** 审核按钮 */
    handleAudit(row) {
      this.auditMode = true;
      this.dialogTitle = "动态审核";
      this.auditForm = { status: "", remark: "" };
      this.loadDetail(row.id);
    },
    /** 加载详情数据 */
    loadDetail(postId) {
      this.detailLoading = true;
      this.dialogOpen = true;
      getPostDetail(postId).then(response => {
        this.detail = response.data;
        this.detailLoading = false;
      }).catch(() => {
        this.detailLoading = false;
      });
    },
    /** 提交审核 */
    submitAudit(status) {
      this.auditForm.status = status;
      this.auditSubmitting = true;
      const data = {
        status: status,
        remark: status === "2" ? this.auditForm.remark : undefined
      };
      auditPost(this.detail.id, data).then(() => {
        this.$modal.msgSuccess(status === "1" ? "审核通过成功" : "审核拒绝成功");
        this.auditSubmitting = false;
        this.dialogOpen = false;
        this.getList();
      }).catch(() => {
        this.auditSubmitting = false;
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const postIds = row.id || this.ids;
      this.$modal.confirm('是否确认删除该动态？').then(function() {
        return deletePost(postIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 解析图片 JSON 字符串为数组 */
    parseImages(images) {
      if (!images) return [];
      try {
        return JSON.parse(images);
      } catch (e) {
        return [];
      }
    },
    /** 获取图片数量 */
    getImageCount(images) {
      return this.parseImages(images).length;
    },
    /** 获取状态标签类型 */
    getStatusType(status) {
      const map = {
        "0": "warning",
        "1": "success",
        "2": "danger"
      };
      return map[status] || "info";
    },
    /** 获取状态文本 */
    getStatusLabel(status) {
      const map = {
        "0": "待审核",
        "1": "已通过",
        "2": "已拒绝"
      };
      return map[status] || "未知";
    }
  }
};
</script>

<style lang="scss" scoped>
.user-cell {
  display: flex;
  align-items: center;

  .user-avatar {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    margin-right: 8px;
    flex-shrink: 0;

    .avatar-fallback {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f0f0f0;
      color: #c0c4cc;
      font-size: 20px;
      border-radius: 50%;
    }
  }

  .user-nickname {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.post-content {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.5;
}

.post-images {
  position: relative;
  display: inline-block;

  .post-thumb {
    width: 50px;
    height: 50px;
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
    }
  }

  .image-count {
    position: absolute;
    top: -6px;
    right: -6px;
    background: #f56c6c;
    color: #fff;
    font-size: 11px;
    line-height: 1;
    padding: 2px 5px;
    border-radius: 8px;
    min-width: 14px;
    text-align: center;
  }
}

.post-detail {
  .detail-header {
    display: flex;
    align-items: center;
    padding-bottom: 16px;
    border-bottom: 1px solid #ebeef5;
    margin-bottom: 16px;

    .detail-avatar {
      width: 56px;
      height: 56px;
      border-radius: 50%;
      margin-right: 12px;
      flex-shrink: 0;

      .avatar-fallback {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #f0f0f0;
        color: #c0c4cc;
        font-size: 28px;
        border-radius: 50%;
      }
    }

    .detail-user-info {
      flex: 1;

      .detail-nickname {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
        margin-bottom: 6px;
      }

      .detail-meta {
        font-size: 13px;
        color: #909399;

        span {
          margin-right: 16px;
        }

        .detail-time {
          margin-right: 0;
        }
      }
    }
  }

  .detail-content {
    margin-bottom: 20px;

    .detail-section-title {
      font-size: 14px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 10px;

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

    .detail-text {
      font-size: 14px;
      color: #606266;
      line-height: 1.6;
      background: #f5f7fa;
      padding: 12px;
      border-radius: 4px;
      white-space: pre-wrap;
      word-break: break-all;
    }

    .image-grid {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;

      .grid-image {
        width: 120px;
        height: 120px;
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
          font-size: 28px;
        }
      }
    }

    .detail-stats {
      font-size: 14px;
      color: #606266;

      span {
        margin-right: 24px;
      }
    }
  }
}
</style>
