<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="订单编号" prop="orderNo">
        <el-input
          v-model="queryParams.orderNo"
          placeholder="请输入订单编号"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="订单状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择订单状态" clearable style="width: 160px">
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
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['system:pet:order:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 订单列表表格 -->
    <el-table v-loading="loading" :data="orderList">
      <el-table-column label="序号" type="index" width="55" align="center" />
      <el-table-column label="订单编号" align="center" width="200">
        <template slot-scope="scope">
          <div class="order-no-cell">
            <el-tooltip class="item" effect="dark" content="点击查看详情" placement="top">
              <span class="order-no-link" @click="handleDetail(scope.row)">{{ scope.row.orderNo }}</span>
            </el-tooltip>
            <el-tooltip class="item" effect="dark" content="复制订单编号" placement="top">
              <i
                class="el-icon-copy-document order-no-copy"
                v-clipboard="scope.row.orderNo"
                v-clipboard:success="onCopySuccess"
                v-clipboard:error="onCopyError"
              ></i>
            </el-tooltip>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="用户信息" align="center" prop="userName" width="120" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <span>{{ scope.row.userName || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="商品信息" align="left" min-width="200">
        <template slot-scope="scope">
          <div class="product-info">
            <el-image
              v-if="getFirstImageUrl(scope.row.productImage)"
              class="product-thumbnail"
              :src="getFirstImageUrl(scope.row.productImage)"
              :preview-src-list="[getFirstImageUrl(scope.row.productImage)]"
              fit="cover"
            >
              <div slot="error" class="image-fallback">
                <i class="el-icon-picture-outline"></i>
              </div>
            </el-image>
            <div v-else class="image-fallback">
              <i class="el-icon-picture-outline"></i>
            </div>
            <span class="product-name">{{ scope.row.productName }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="兑换数量" align="center" prop="quantity" width="80" />
      <el-table-column label="消耗积分" align="center" prop="totalPoints" width="90" />
      <el-table-column label="订单状态" align="center" width="100">
        <template slot-scope="scope">
          <el-tag :type="getStatusType(scope.row.status)">{{ getStatusLabel(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="快递信息" align="left" width="180" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <span v-if="scope.row.expressCompany">
            {{ scope.row.expressCompany }} / {{ scope.row.expressNo }}
          </span>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column label="下单时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200" fixed="right">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleDetail(scope.row)"
            v-hasPermi="['system:pet:order:query']"
          >详情</el-button>
          <el-button
            v-if="scope.row.status === '0'"
            size="mini"
            type="text"
            icon="el-icon-s-promotion"
            @click="handleShip(scope.row)"
            v-hasPermi="['system:pet:order:ship']"
          >发货</el-button>
          <el-button
            v-if="scope.row.status === '1'"
            size="mini"
            type="text"
            icon="el-icon-circle-check"
            @click="handleComplete(scope.row)"
            v-hasPermi="['system:pet:order:complete']"
          >完成</el-button>
          <el-button
            v-if="scope.row.status === '0' || scope.row.status === '1'"
            size="mini"
            type="text"
            icon="el-icon-close"
            @click="handleCancel(scope.row)"
            v-hasPermi="['system:pet:order:cancel']"
          >取消</el-button>
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

    <!-- 发货对话框 -->
    <el-dialog :title="shipTitle" :visible.sync="shipOpen" width="500px" append-to-body>
      <el-form ref="shipForm" :model="shipForm" :rules="shipRules" label-width="100px">
        <el-form-item label="订单编号">
          <span>{{ shipForm.orderNo }}</span>
        </el-form-item>
        <el-form-item label="快递公司" prop="expressCompany">
          <el-select v-model="shipForm.expressCompany" placeholder="请选择快递公司" style="width: 100%">
            <el-option
              v-for="item in expressCompanyOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="快递单号" prop="expressNo">
          <el-input v-model="shipForm.expressNo" placeholder="请输入快递单号" maxlength="50" show-word-limit />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitShip">确认发货</el-button>
        <el-button @click="cancelShip">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 订单详情对话框 -->
    <el-dialog title="订单详情" :visible.sync="detailOpen" width="700px" append-to-body>
      <div v-loading="detailLoading" class="order-detail">
        <!-- 基本信息 -->
        <el-descriptions title="基本信息" :column="2" border size="medium">
          <el-descriptions-item label="订单编号">{{ detailData.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getStatusType(detailData.status)">{{ getStatusLabel(detailData.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ parseTime(detailData.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ parseTime(detailData.updateTime) }}</el-descriptions-item>
          <el-descriptions-item label="兑换数量">{{ detailData.quantity }}</el-descriptions-item>
          <el-descriptions-item label="消耗积分">{{ detailData.totalPoints }}</el-descriptions-item>
        </el-descriptions>

        <!-- 用户信息 -->
        <el-descriptions title="用户信息" :column="2" border size="medium" class="mt20">
          <el-descriptions-item label="用户头像">
            <el-avatar v-if="detailData.user && detailData.user.avatar" :size="36" :src="formatImageUrl(detailData.user.avatar)"></el-avatar>
            <span v-else class="text-muted">-</span>
          </el-descriptions-item>
          <el-descriptions-item label="用户昵称">{{ detailData.user ? detailData.user.nickName : '-' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ detailData.user ? detailData.user.phonenumber : '-' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 商品信息 -->
        <el-descriptions title="商品信息" :column="1" border size="medium" class="mt20">
          <el-descriptions-item label="商品图片">
            <el-image
              v-if="getFirstImageUrl(detailData.productImage)"
              class="detail-product-image"
              :src="getFirstImageUrl(detailData.productImage)"
              :preview-src-list="[getFirstImageUrl(detailData.productImage)]"
              fit="cover"
            />
            <span v-else class="text-muted">暂无图片</span>
          </el-descriptions-item>
          <el-descriptions-item label="商品名称">{{ detailData.productName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="积分单价">{{ detailData.pointsPrice }} 积分</el-descriptions-item>
        </el-descriptions>

        <!-- 收货地址 -->
        <el-descriptions title="收货地址" :column="2" border size="medium" class="mt20">
          <el-descriptions-item label="收件人">{{ detailData.address ? detailData.address.receiverName : '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ detailData.address ? detailData.address.receiverPhone : '-' }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ formatAddress(detailData) }}</el-descriptions-item>
        </el-descriptions>

        <!-- 快递信息 -->
        <el-descriptions title="快递信息" :column="2" border size="medium" class="mt20">
          <el-descriptions-item label="快递公司">{{ detailData.expressCompany || '-' }}</el-descriptions-item>
          <el-descriptions-item label="快递单号">{{ detailData.expressNo || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getOrderList, getOrderDetail, updateOrderStatus } from "@/api/system/pet/order";

export default {
  name: "PetOrder",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 订单表格数据
      orderList: [],
      // 订单状态选项（0-待发货 1-已发货 2-已完成 3-已取消）
      statusOptions: [
        { value: "0", label: "待发货", type: "info" },
        { value: "1", label: "已发货", type: "warning" },
        { value: "2", label: "已完成", type: "success" },
        { value: "3", label: "已取消", type: "danger" }
      ],
      // 快递公司选项
      expressCompanyOptions: [
        { value: "顺丰速运", label: "顺丰速运" },
        { value: "圆通速递", label: "圆通速递" },
        { value: "中通快递", label: "中通快递" },
        { value: "韵达快递", label: "韵达快递" },
        { value: "申通快递", label: "申通快递" },
        { value: "京东物流", label: "京东物流" },
        { value: "百世快递", label: "百世快递" },
        { value: "邮政EMS", label: "邮政EMS" }
      ],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        orderNo: undefined,
        status: undefined
      },
      // 发货对话框
      shipOpen: false,
      shipTitle: "",
      shipForm: {
        id: undefined,
        orderNo: undefined,
        expressCompany: undefined,
        expressNo: undefined
      },
      // 发货表单校验
      shipRules: {
        expressCompany: [
          { required: true, message: "请选择快递公司", trigger: "change" }
        ],
        expressNo: [
          { required: true, message: "快递单号不能为空", trigger: "blur" },
          { min: 6, max: 50, message: "快递单号长度必须介于 6 和 50 之间", trigger: "blur" }
        ]
      },
      // 详情对话框
      detailOpen: false,
      detailLoading: false,
      detailData: {}
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询订单列表 */
    getList() {
      this.loading = true;
      getOrderList(this.queryParams).then(response => {
        this.orderList = response.rows;
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
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 格式化收货地址 */
    formatAddress(row) {
      const address = row && row.address ? row.address : row;
      if (!address) return "-";
      const region = [address.province, address.city, address.district].filter(Boolean).join("");
      const detail = address.detailAddress || address.address || "";
      return (region + detail) || "-";
    },
    /** 拼接图片完整地址（处理后端返回的相对路径及旧地址） */
    formatImageUrl(url) {
      if (!url) return "";
      if (url.startsWith("data:") || url.startsWith("blob:")) {
        return url;
      }
      if (url.startsWith("http://") || url.startsWith("https://")) {
        // 数据库中可能存了 localhost / 旧 IP 的完整地址，替换为当前后端前缀
        const profileIdx = url.indexOf("/profile");
        if (profileIdx !== -1) {
          return process.env.VUE_APP_BASE_API + url.substring(profileIdx);
        }
        // 外部图片（如微信头像）直接返回
        return url;
      }
      // 相对路径：通过 dev-api 代理访问后端静态资源
      return process.env.VUE_APP_BASE_API + (url.startsWith("/") ? url : "/" + url);
    },
    /** 解析商品图片（兼容 JSON 数组字符串与逗号分隔两种格式） */
    parseImages(images) {
      if (!images) return [];
      if (Array.isArray(images)) return images;
      try {
        const parsed = JSON.parse(images);
        if (Array.isArray(parsed)) return parsed;
      } catch (e) {
        // 兼容逗号分隔的旧数据
      }
      return images.split(",").filter(Boolean);
    },
    /** 获取第一张商品图片的完整地址 */
    getFirstImageUrl(images) {
      const list = this.parseImages(images);
      return this.formatImageUrl(list[0]);
    },
    /** 获取状态标签文本 */
    getStatusLabel(status) {
      const item = this.statusOptions.find(s => s.value === String(status));
      return item ? item.label : "未知";
    },
    /** 获取状态标签类型 */
    getStatusType(status) {
      const item = this.statusOptions.find(s => s.value === String(status));
      return item ? item.type : "info";
    },
    /** 复制成功回调 */
    onCopySuccess() {
      this.$modal.msgSuccess("订单编号已复制");
    },
    /** 复制失败回调 */
    onCopyError() {
      this.$modal.msgError("复制失败，请手动复制");
    },
    /** 发货按钮操作 */
    handleShip(row) {
      this.resetShipForm();
      this.shipForm.id = row.id;
      this.shipForm.orderNo = row.orderNo;
      this.shipOpen = true;
      this.shipTitle = "订单发货";
    },
    /** 重置发货表单 */
    resetShipForm() {
      this.shipForm = {
        id: undefined,
        orderNo: undefined,
        expressCompany: undefined,
        expressNo: undefined
      };
      if (this.$refs.shipForm) {
        this.$refs.shipForm.resetFields();
      }
    },
    /** 取消发货 */
    cancelShip() {
      this.shipOpen = false;
      this.resetShipForm();
    },
    /** 确认发货 */
    submitShip() {
      this.$refs.shipForm.validate(valid => {
        if (!valid) {
          return;
        }
        const data = {
          status: "1",
          expressCompany: this.shipForm.expressCompany,
          expressNo: this.shipForm.expressNo
        };
        updateOrderStatus(this.shipForm.id, data).then(() => {
          this.$modal.msgSuccess("发货成功");
          this.shipOpen = false;
          this.getList();
        });
      });
    },
    /** 完成订单 */
    handleComplete(row) {
      this.$modal.confirm('是否确认完成订单"' + row.orderNo + '"？').then(() => {
        return updateOrderStatus(row.id, { status: "2" });
      }).then(() => {
        this.$modal.msgSuccess("订单已完成");
        this.getList();
      }).catch(() => {});
    },
    /** 取消订单 */
    handleCancel(row) {
      this.$modal.confirm('是否确认取消订单"' + row.orderNo + '"？取消后不可恢复。').then(() => {
        return updateOrderStatus(row.id, { status: "3" });
      }).then(() => {
        this.$modal.msgSuccess("订单已取消");
        this.getList();
      }).catch(() => {});
    },
    /** 订单详情 */
    handleDetail(row) {
      this.detailOpen = true;
      this.detailLoading = true;
      this.detailData = {};
      getOrderDetail(row.id).then(response => {
        this.detailData = response.data;
        this.detailLoading = false;
      }).catch(() => {
        this.detailLoading = false;
      });
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('/api/v1/admin/orders/export', {
        orderNo: this.queryParams.orderNo,
        status: this.queryParams.status
      }, `order_${new Date().getTime()}.xlsx`);
    }
  }
};
</script>

<style lang="scss" scoped>
.order-no-cell {
  display: inline-flex;
  align-items: center;

  .order-no-link {
    color: #409eff;
    cursor: pointer;

    &:hover {
      text-decoration: underline;
    }
  }
}

.order-no-copy {
  color: #409eff;
  cursor: pointer;
  margin-left: 6px;

  &:hover {
    opacity: 0.75;
  }
}

.product-info {
  display: flex;
  align-items: center;

  .product-thumbnail {
    width: 40px;
    height: 40px;
    border-radius: 4px;
    flex-shrink: 0;
    margin-right: 8px;

    .image-fallback {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f5f7fa;
      color: #c0c4cc;
      font-size: 16px;
      border-radius: 4px;
    }
  }

  .product-name {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.image-fallback {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #c0c4cc;
  font-size: 16px;
  border-radius: 4px;
  flex-shrink: 0;
}

.text-muted {
  color: #c0c4cc;
}

.mt20 {
  margin-top: 20px;
}

.order-detail {
  .detail-product-image {
    width: 80px;
    height: 80px;
    border-radius: 4px;
  }

}
</style>
