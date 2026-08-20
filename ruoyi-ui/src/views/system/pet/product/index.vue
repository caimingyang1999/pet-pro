<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="商品分类" prop="categoryId">
        <treeselect
          v-model="queryParams.categoryId"
          :options="categoryOptions"
          :normalizer="normalizer"
          placeholder="请选择商品分类"
          class="treeselect-width"
          :clearable="true"
        />
      </el-form-item>
      <el-form-item label="商品名称" prop="keyword">
        <el-input
          v-model="queryParams.keyword"
          placeholder="请输入商品名称"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="商品状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择商品状态" clearable style="width: 160px">
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
          v-hasPermi="['system:pet:product:add']"
        >新增</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 商品列表表格 -->
    <el-table v-loading="loading" :data="productList">
      <el-table-column label="序号" type="index" width="55" align="center" />
      <el-table-column label="商品图片" align="center" width="100">
        <template slot-scope="scope">
          <el-image
            v-if="getFirstImage(scope.row.productImages)"
            class="product-thumbnail"
            :src="getFirstImage(scope.row.productImages)"
            :preview-src-list="getImageList(scope.row.productImages)"
            fit="cover"
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
      <el-table-column label="商品名称" align="left" prop="productName" min-width="160" :show-overflow-tooltip="true" />
      <el-table-column label="所属分类" align="center" prop="categoryName" width="120" :show-overflow-tooltip="true" />
      <el-table-column label="积分价格" align="center" prop="pointsPrice" width="100">
        <template slot-scope="scope">
          <span>{{ scope.row.pointsPrice }}</span>
        </template>
      </el-table-column>
      <el-table-column label="库存数量" align="center" prop="stock" width="90" />
      <el-table-column label="总兑换数" align="center" prop="totalExchange" width="90" />
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
            v-hasPermi="['system:pet:product:edit']"
          >编辑</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:pet:product:remove']"
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

    <!-- 新增/编辑商品对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="800px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="商品分类" prop="categoryId">
              <treeselect
                v-model="form.categoryId"
                :options="categoryOptions"
                :normalizer="normalizer"
                placeholder="请选择商品分类"
                class="treeselect-form-width"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品名称" prop="productName">
              <el-input v-model="form.productName" placeholder="请输入商品名称" maxlength="50" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="积分价格" prop="pointsPrice">
              <el-input-number v-model="form.pointsPrice" controls-position="right" :min="1" :precision="0" placeholder="请输入积分价格" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="库存数量" prop="stock">
              <el-input-number v-model="form.stock" controls-position="right" :min="0" :precision="0" placeholder="请输入库存数量" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="商品状态" prop="status">
              <el-switch
                v-model="form.status"
                active-value="1"
                inactive-value="0"
                active-text="上架"
                inactive-text="下架"
              ></el-switch>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="商品图片" prop="productImages">
              <image-upload v-model="form.productImages" :limit="6" :file-size="5" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="商品描述" prop="description">
              <editor v-model="form.description" :min-height="200" />
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
  getProductList,
  getProductDetail,
  addProduct,
  updateProduct,
  deleteProduct,
  updateProductStatus
} from "@/api/system/pet/product";
import { getCategoryList } from "@/api/system/pet/category";
import Treeselect from "@riophae/vue-treeselect";
import "@riophae/vue-treeselect/dist/vue-treeselect.css";

export default {
  name: "PetProduct",
  components: { Treeselect },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 商品表格数据
      productList: [],
      // 分类树选项
      categoryOptions: [],
      // 商品状态选项
      statusOptions: [
        { value: "1", label: "上架" },
        { value: "0", label: "下架" }
      ],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        categoryId: undefined,
        keyword: undefined,
        status: undefined
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        categoryId: [
          { required: true, message: "商品分类不能为空", trigger: "select" }
        ],
        productName: [
          { required: true, message: "商品名称不能为空", trigger: "blur" },
          { min: 2, max: 50, message: "商品名称长度必须介于 2 和 50 之间", trigger: "blur" }
        ],
        pointsPrice: [
          { required: true, message: "积分价格不能为空", trigger: "blur" },
          { type: "number", min: 1, message: "积分价格必须大于0", trigger: "blur" }
        ],
        stock: [
          { required: true, message: "库存数量不能为空", trigger: "blur" },
          { type: "number", min: 0, message: "库存数量不能小于0", trigger: "blur" }
        ],
        status: [
          { required: true, message: "请选择商品状态", trigger: "change" }
        ]
      }
    };
  },
  created() {
    this.getList();
    this.getCategoryTree();
  },
  methods: {
    /** 查询商品列表 */
    getList() {
      this.loading = true;
      getProductList(this.queryParams).then(response => {
        this.productList = response.rows;
        this.total = response.total;
        this.loading = false;
      }).catch(() => {
        this.loading = false;
      });
    },
    /** 查询分类树 */
    getCategoryTree() {
      getCategoryList().then(response => {
        this.categoryOptions = response.data || [];
      });
    },
    /** treeselect 转换方法 */
    normalizer(node) {
      if (node.children && !node.children.length) {
        delete node.children;
      }
      return {
        id: node.id,
        label: node.categoryName,
        children: node.children
      };
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
    /** 获取第一张图片 */
    getFirstImage(images) {
      const list = this.parseImages(images);
      return this.formatImageUrl(list[0]);
    },
    /** 获取图片列表 */
    getImageList(images) {
      return this.parseImages(images).map(url => this.formatImageUrl(url));
    },
    /** 拼接 baseUrl（处理后端返回的相对路径） */
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
        // 外部图片直接返回
        return url;
      }
      // 相对路径：通过 dev-api 代理访问后端静态资源
      return process.env.VUE_APP_BASE_API + (url.startsWith("/") ? url : "/" + url);
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
        categoryId: undefined,
        productName: undefined,
        productImages: undefined,
        description: undefined,
        pointsPrice: 1,
        stock: 0,
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
      this.title = "新增商品";
    },
    /** 编辑按钮操作 */
    handleUpdate(row) {
      this.reset();
      getProductDetail(row.id).then(response => {
        const detail = response.data;
        this.form = {
          ...detail,
          // image-upload 组件内部使用逗号分隔字符串
          productImages: this.parseImages(detail.productImages).join(",")
        };
        this.open = true;
        this.title = "编辑商品";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (!valid) {
          return;
        }
        const data = { ...this.form };
        // 接口文档约定 productImages 以 JSON 数组字符串传输
        data.productImages = JSON.stringify(this.parseImages(data.productImages));
        if (data.id != undefined) {
          updateProduct(data).then(() => {
            this.$modal.msgSuccess("修改成功");
            this.open = false;
            this.getList();
          });
        } else {
          addProduct(data).then(() => {
            this.$modal.msgSuccess("新增成功");
            this.open = false;
            this.getList();
          });
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      this.$modal.confirm('是否确认删除商品"' + row.productName + '"？').then(function() {
        return deleteProduct(row.id);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 商品状态切换 */
    handleStatusChange(row) {
      const text = row.status === "1" ? "上架" : "下架";
      this.$modal.confirm('确认要' + text + '商品"' + row.productName + '"吗？').then(() => {
        return updateProductStatus(row.id, row.status);
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

<style lang="scss" scoped>
.treeselect-width {
  width: 220px;
}

.treeselect-form-width {
  width: 100%;
}

.product-thumbnail {
  width: 60px;
  height: 60px;
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
  width: 60px;
  height: 60px;
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
