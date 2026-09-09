<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="110px">
      <el-form-item label="关键字" prop="keyword">
        <el-input
          v-model="queryParams.keyword"
          placeholder="用户名/昵称/手机号"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="用户账号" prop="userName">
        <el-input
          v-model="queryParams.userName"
          placeholder="请输入用户账号"
          clearable
          style="width: 160px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="用户昵称" prop="nickName">
        <el-input
          v-model="queryParams.nickName"
          placeholder="请输入用户昵称"
          clearable
          style="width: 160px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="手机号" prop="phonenumber">
        <el-input
          v-model="queryParams.phonenumber"
          placeholder="请输入手机号"
          clearable
          style="width: 160px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="userList">
      <el-table-column label="序号" type="index" width="60" align="center" />
      <el-table-column label="用户头像" align="center" width="80">
        <template slot-scope="scope">
          <el-image
            v-if="scope.row.user && scope.row.user.avatar"
            class="user-avatar"
            :src="scope.row.user.avatar"
            fit="cover"
            :preview-src-list="[scope.row.user.avatar]"
          >
            <div slot="error" class="avatar-placeholder">
              <i class="el-icon-user"></i>
            </div>
          </el-image>
          <div v-else class="avatar-placeholder">
            <i class="el-icon-user"></i>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="用户昵称" align="center" min-width="120" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <span>{{ scope.row.user ? scope.row.user.nickName : '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="手机号" align="center" width="130">
        <template slot-scope="scope">
          <span>{{ scope.row.user ? scope.row.user.phonenumber : '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="当前积分" align="center" prop="points" width="100">
        <template slot-scope="scope">
          <span class="points-text">{{ scope.row.points || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="宠物数量" align="center" prop="petCount" width="100" />
      <el-table-column label="动态数量" align="center" prop="postCount" width="100" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handlePointsOperate(scope.row)"
            v-hasPermi="['system:pet:points:operate']"
          >积分操作</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-document"
            @click="handlePointsDetail(scope.row)"
            v-hasPermi="['system:pet:points:detail']"
          >积分明细</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <el-dialog :title="pointsDialog.title" :visible.sync="pointsDialog.open" width="500px" append-to-body>
      <el-form ref="pointsForm" :model="pointsForm" :rules="pointsRules" label-width="100px">
        <el-form-item label="操作类型" prop="operateType">
          <el-radio-group v-model="pointsForm.operateType">
            <el-radio label="add" border>
              <span class="operate-add">增加积分</span>
            </el-radio>
            <el-radio label="subtract" border>
              <span class="operate-subtract">扣减积分</span>
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="积分数量" prop="points">
          <el-input-number
            v-model="pointsForm.points"
            :min="1"
            :precision="0"
            controls-position="right"
            style="width: 100%"
            placeholder="请输入积分数量"
          />
        </el-form-item>
        <el-form-item label="操作原因" prop="reason">
          <el-input
            v-model="pointsForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入操作原因"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="当前积分">
          <span class="current-points">{{ pointsForm.currentPoints || 0 }}</span>
        </el-form-item>
        <el-form-item label="操作后积分">
          <span :class="previewPointsClass">{{ previewPoints }}</span>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitPointsForm">确 定</el-button>
        <el-button @click="cancelPointsForm">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="积分明细" :visible.sync="detailDialog.open" width="70%" top="5vh" append-to-body>
      <el-table v-loading="detailDialog.loading" :data="recordList" border style="width: 100%;">
        <el-table-column label="序号" type="index" width="60" align="center" />
        <el-table-column label="变动类型" align="center" prop="changeType" min-width="110">
          <template slot-scope="scope">
            <el-tag :type="getTypeTagType(scope.row.changeType)" size="small">
              {{ getTypeLabel(scope.row.changeType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="变动数量" align="center" prop="pointsChange" width="100">
          <template slot-scope="scope">
            <span :class="scope.row.pointsChange > 0 ? 'points-add' : 'points-subtract'">
              {{ scope.row.pointsChange > 0 ? '+' : '' }}{{ scope.row.pointsChange }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="变动后余额" align="center" prop="pointsBalance" width="110" />
        <el-table-column label="关联业务ID" align="center" prop="relateId" width="120" />
        <el-table-column label="备注" align="center" prop="remark" min-width="160" :show-overflow-tooltip="true">
          <template slot-scope="scope">
            <span>{{ scope.row.remark || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="时间" align="center" prop="createTime" width="160">
          <template slot-scope="scope">
            <span>{{ parseTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-container">
        <pagination
          v-show="detailDialog.total > 0"
          :total="detailDialog.total"
          :page.sync="detailQueryParams.pageNum"
          :limit.sync="detailQueryParams.pageSize"
          @pagination="getRecordList"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getUserList, operatePoints, getPointsRecordList } from "@/api/system/pet/points";
import { isHttp, isEmpty } from "@/utils/validate";
import defAva from '@/assets/images/profile.jpg';

export default {
  name: "PetPoints",
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      userList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        keyword: undefined,
        userName: undefined,
        nickName: undefined,
        phonenumber: undefined
      },
      pointsDialog: {
        open: false,
        title: ""
      },
      pointsForm: {
        userId: undefined,
        operateType: "add",
        points: 1,
        reason: "",
        currentPoints: 0
      },
      pointsRules: {
        operateType: [
          { required: true, message: "请选择操作类型", trigger: "change" }
        ],
        points: [
          { required: true, message: "请输入积分数量", trigger: "blur" },
          { type: "number", min: 1, message: "积分数量必须大于0", trigger: "blur" }
        ],
        reason: [
          { required: true, message: "请输入操作原因", trigger: "blur" },
          { min: 2, max: 200, message: "操作原因长度必须介于 2 和 200 之间", trigger: "blur" }
        ]
      },
      detailDialog: {
        open: false,
        loading: false,
        total: 0
      },
      detailQueryParams: {
        pageNum: 1,
        pageSize: 10
      },
      recordList: [],
      currentUserId: undefined,
      typeOptions: [
        { value: "sign_in", label: "签到", tagType: "success" },
        { value: "post", label: "发布动态", tagType: "primary" },
        { value: "pet", label: "完善宠物信息", tagType: "primary" },
        { value: "register", label: "注册奖励", tagType: "success" },
        { value: "exchange", label: "兑换商品", tagType: "warning" },
        { value: "admin", label: "管理员操作", tagType: "info" }
      ]
    };
  },
  computed: {
    previewPoints() {
      const current = Number(this.pointsForm.currentPoints) || 0;
      const points = Number(this.pointsForm.points) || 0;
      if (this.pointsForm.operateType === "add") {
        return current + points;
      } else {
        return current - points;
      }
    },
    previewPointsClass() {
      if (this.pointsForm.operateType === "add") {
        return "preview-add";
      } else {
        return "preview-subtract";
      }
    }
  },
  created() {
    this.getList();
  },
  methods: {
    getList() {
      this.loading = true;
      getUserList(this.queryParams).then(response => {
        const rows = response.rows || [];
        rows.forEach(item => {
          if (item.user) {
            let avatar = item.user.avatar || "";
            if (!isHttp(avatar)) {
              avatar = isEmpty(avatar) ? defAva : process.env.VUE_APP_BASE_API + avatar;
            }
            item.user.avatar = avatar;
          }
        });
        this.userList = rows;
        this.total = response.total || 0;
        this.loading = false;
      }).catch(() => {
        this.loading = false;
      });
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handlePointsOperate(row) {
      const nickName = row.user ? row.user.nickName : '';
      const phone = row.user ? row.user.phonenumber : '';
      this.pointsForm = {
        userId: row.user ? row.user.userId : undefined,
        operateType: "add",
        points: 1,
        reason: "",
        currentPoints: row.points || 0
      };
      this.pointsDialog.title = "积分操作 - " + (nickName || phone || '未知用户');
      this.pointsDialog.open = true;
      this.$nextTick(() => {
        this.$refs.pointsForm.clearValidate();
      });
    },
    cancelPointsForm() {
      this.pointsDialog.open = false;
      this.resetPointsForm();
    },
    resetPointsForm() {
      this.pointsForm = {
        userId: undefined,
        operateType: "add",
        points: 1,
        reason: "",
        currentPoints: 0
      };
      this.resetForm("pointsForm");
    },
    submitPointsForm() {
      this.$refs.pointsForm.validate(valid => {
        if (!valid) return;
        const data = {
          pointsChange: this.pointsForm.operateType === "add" ? this.pointsForm.points : -this.pointsForm.points,
          remark: this.pointsForm.reason
        };
        const actionText = this.pointsForm.operateType === "add" ? "增加" : "扣减";
        this.$modal.confirm(`确认要${actionText} ${this.pointsForm.points} 积分吗？`).then(() => {
          return operatePoints(this.pointsForm.userId, data);
        }).then(() => {
          this.$modal.msgSuccess("积分操作成功");
          this.pointsDialog.open = false;
          this.getList();
        }).catch(() => {});
      });
    },
    handlePointsDetail(row) {
      this.currentUserId = row.user ? row.user.userId : undefined;
      this.detailQueryParams = {
        pageNum: 1,
        pageSize: 10
      };
      this.recordList = [];
      this.detailDialog.total = 0;
      this.detailDialog.open = true;
      this.getRecordList();
    },
    getRecordList() {
      if (!this.currentUserId) return;
      this.detailDialog.loading = true;
      getPointsRecordList(this.currentUserId, this.detailQueryParams).then(response => {
        this.recordList = response.rows || [];
        this.detailDialog.total = response.total || 0;
        this.detailDialog.loading = false;
      }).catch(() => {
        this.detailDialog.loading = false;
      });
    },
    getTypeLabel(type) {
      const item = this.typeOptions.find(t => t.value === type);
      return item ? item.label : type;
    },
    getTypeTagType(type) {
      const item = this.typeOptions.find(t => t.value === type);
      return item ? item.tagType : "info";
    }
  }
};
</script>

<style lang="scss" scoped>
.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  display: inline-block;
  vertical-align: middle;
}

.avatar-placeholder {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #f0f2f5;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  color: #c0c4cc;
  font-size: 20px;
}

.points-text {
  font-weight: 600;
  color: #e6a23c;
}

.operate-add {
  color: #67c23a;
  font-weight: 500;
}

.operate-subtract {
  color: #f56c6c;
  font-weight: 500;
}

.current-points {
  font-size: 16px;
  font-weight: 600;
  color: #606266;
}

.preview-add {
  font-size: 18px;
  font-weight: bold;
  color: #67c23a;
}

.preview-subtract {
  font-size: 18px;
  font-weight: bold;
  color: #f56c6c;
}

.points-add {
  color: #67c23a;
  font-weight: 500;
}

.points-subtract {
  color: #f56c6c;
  font-weight: 500;
}

.pagination-container {
  margin-top: 15px;
  text-align: right;
}
</style>
