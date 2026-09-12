<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="宠物名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入宠物名称"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="所属用户" prop="userKeyword">
        <el-input
          v-model="queryParams.userKeyword"
          placeholder="昵称/账号/手机号"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="品种" prop="breed">
        <el-select v-model="queryParams.breed" placeholder="请选择品种" clearable filterable style="width: 180px">
          <el-option
            v-for="item in breedOptions"
            :key="item"
            :label="item"
            :value="item"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="宠物类型" prop="petType">
        <el-select v-model="queryParams.petType" placeholder="请选择" clearable style="width: 140px">
          <el-option
            v-for="item in petTypeOptions"
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

    <el-row :gutter="10" class="mb8">
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="petList">
      <el-table-column label="序号" type="index" width="55" align="center" />
      <el-table-column label="宠物头像" align="center" width="80">
        <template slot-scope="scope">
          <el-image
            v-if="scope.row.avatar"
            class="pet-avatar"
            :src="formatImageUrl(scope.row.avatar)"
            :preview-src-list="[formatImageUrl(scope.row.avatar)]"
            fit="cover"
            :z-index="9999"
          >
            <div slot="error" class="avatar-fallback">
              <i class="el-icon-picture-outline"></i>
            </div>
          </el-image>
          <div v-else class="avatar-fallback">
            <i class="el-icon-picture-outline"></i>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="宠物名称" align="left" prop="name" min-width="120" :show-overflow-tooltip="true" />
      <el-table-column label="品种" align="center" prop="breed" width="100" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <span>{{ scope.row.breed || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="宠物类型" align="center" width="100">
        <template slot-scope="scope">
          <el-tag size="mini" effect="plain">{{ petTypeLabel(scope.row.petType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="性别" align="center" width="70">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.gender === '1'" type="primary" size="mini">公</el-tag>
          <el-tag v-else-if="scope.row.gender === '0'" type="danger" size="mini" effect="plain">母</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="年龄" align="center" width="100">
        <template slot-scope="scope">
          <span>{{ calculateAge(scope.row.birthday) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="体重" align="center" prop="weight" width="80">
        <template slot-scope="scope">
          <span v-if="scope.row.weight">{{ scope.row.weight }}kg</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="所属用户" align="center" prop="userName" width="100" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <span>{{ scope.row.userName || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="疫苗记录数" align="center" prop="vaccineCount" width="100">
        <template slot-scope="scope">
          <span>{{ scope.row.vaccineCount || 0 }}</span>
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
            icon="el-icon-view"
            @click="handleView(scope.row)"
            v-hasPermi="['system:pet:petinfo:query']"
          >详情</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:pet:petinfo:remove']"
          >删除</el-button>
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

    <el-dialog title="宠物详情" :visible.sync="detailOpen" width="700px" append-to-body>
      <div v-loading="detailLoading" class="detail-container">
        <div class="detail-avatar-section">
          <el-image
            v-if="petDetail.avatar"
            class="detail-avatar"
            :src="formatImageUrl(petDetail.avatar)"
            :preview-src-list="[formatImageUrl(petDetail.avatar)]"
            fit="cover"
            :z-index="9999"
          >
            <div slot="error" class="detail-avatar-fallback">
              <i class="el-icon-picture-outline"></i>
            </div>
          </el-image>
          <div v-else class="detail-avatar-fallback">
            <i class="el-icon-picture-outline"></i>
          </div>
          <div class="detail-name">{{ petDetail.name }}</div>
        </div>

        <el-descriptions :column="2" border class="detail-descriptions">
          <el-descriptions-item label="品种">{{ petDetail.breed || '-' }}</el-descriptions-item>
          <el-descriptions-item label="宠物类型">{{ petTypeLabel(petDetail.petType) }}</el-descriptions-item>
          <el-descriptions-item label="性别">
            <el-tag v-if="petDetail.gender === '1'" type="primary" size="small">公</el-tag>
            <el-tag v-else-if="petDetail.gender === '0'" type="danger" size="small" effect="plain">母</el-tag>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="出生日期">{{ petDetail.birthday || '-' }}</el-descriptions-item>
          <el-descriptions-item label="年龄">{{ calculateAge(petDetail.birthday) }}</el-descriptions-item>
          <el-descriptions-item label="体重">
            <span v-if="petDetail.weight">{{ petDetail.weight }}kg</span>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="毛色">{{ petDetail.color || '-' }}</el-descriptions-item>
          <el-descriptions-item label="绝育状态">
            <el-tag v-if="petDetail.sterilization === '1'" type="success" size="small">已绝育</el-tag>
            <el-tag v-else-if="petDetail.sterilization === '0'" type="info" size="small">未绝育</el-tag>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="所属用户">{{ petDetail.userName || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div class="detail-section">
          <div class="section-title">疫苗记录</div>
          <el-table :data="petDetail.vaccineList || []" size="small" border>
            <el-table-column label="序号" type="index" width="50" align="center" />
            <el-table-column label="疫苗名称" prop="vaccineName" align="center" />
            <el-table-column label="接种日期" prop="inoculationDate" align="center" width="120" />
            <el-table-column label="下次接种日期" prop="nextDate" align="center" width="130" />
          </el-table>
          <el-empty v-if="!petDetail.vaccineList || petDetail.vaccineList.length === 0" description="暂无疫苗记录" :image-size="60" />
        </div>

        <div class="detail-section">
          <div class="section-title">备注信息</div>
          <div class="remark-content">{{ petDetail.remark || '暂无备注' }}</div>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getPetList, getPetDetail, deletePet, getBreedOptions } from "@/api/system/pet/petinfo";

export default {
  name: "PetInfo",
  data() {
    return {
      loading: true,
      detailLoading: false,
      showSearch: true,
      total: 0,
      petList: [],
      breedOptions: [],
      // 宠物类型选项（与 pet_info.pet_type 取值一致）
      petTypeOptions: [
        { value: "cat", label: "猫" },
        { value: "dog", label: "狗" },
        { value: "other", label: "其他" }
      ],
      detailOpen: false,
      petDetail: {},
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        name: undefined,
        userKeyword: undefined,
        breed: undefined,
        petType: undefined
      }
    };
  },
  created() {
    this.getList();
    this.loadBreedOptions();
  },
  methods: {
    getList() {
      this.loading = true;
      const params = {
        pageNum: this.queryParams.pageNum,
        pageSize: this.queryParams.pageSize,
        name: this.queryParams.name,
        userKeyword: this.queryParams.userKeyword,
        breed: this.queryParams.breed,
        petType: this.queryParams.petType
      };
      getPetList(params).then(response => {
        this.petList = response.rows || [];
        this.total = response.total || 0;
        this.loading = false;
      }).catch(() => {
        this.loading = false;
      });
    },
    loadBreedOptions() {
      getBreedOptions().then(response => {
        this.breedOptions = response.data || [];
      }).catch(() => {});
    },
    /** 宠物类型文案（空值按"其他"展示，与后端 NULL 视为其他一致） */
    petTypeLabel(petType) {
      const map = { cat: "猫", dog: "狗", other: "其他" };
      return map[petType] || "其他";
    },
    /**
     * 拼接图片地址：
     * 历史数据存的是带旧局域网 IP 的绝对地址（http://192.168.1.3:8080/profile/...），
     * 新数据存 /profile/... 相对路径，两种都要按 /profile 截断后用当前接口前缀重新拼接，
     * 否则头像会指向不可访问的地址而显示为裂图。
     */
    formatImageUrl(url) {
      if (!url) return "";
      if (/^(data:|blob:)/i.test(url)) return url;
      const idx = url.indexOf("/profile");
      if (idx !== -1) {
        return process.env.VUE_APP_BASE_API + url.substring(idx);
      }
      if (/^https?:\/\//i.test(url)) return url;
      return process.env.VUE_APP_BASE_API + (url.startsWith("/") ? url : "/" + url);
    },
    calculateAge(birthday) {
      if (!birthday) return '-';
      const birthDate = new Date(birthday);
      if (isNaN(birthDate.getTime())) return '-';
      const now = new Date();
      let years = now.getFullYear() - birthDate.getFullYear();
      let months = now.getMonth() - birthDate.getMonth();
      if (months < 0) {
        years--;
        months += 12;
      }
      if (years <= 0 && months <= 0) {
        const days = Math.floor((now - birthDate) / (1000 * 60 * 60 * 24));
        if (days < 0) return '-';
        return days + '天';
      }
      if (years <= 0) {
        return months + '个月';
      }
      if (months === 0) {
        return years + '岁';
      }
      return years + '岁' + months + '个月';
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleView(row) {
      this.detailLoading = true;
      this.detailOpen = true;
      this.petDetail = {};
      getPetDetail(row.id).then(response => {
        this.petDetail = response.data || response.rows || {};
        this.detailLoading = false;
      }).catch(() => {
        this.detailLoading = false;
      });
    },
    handleDelete(row) {
      this.$modal.confirm('是否确认删除宠物"' + row.name + '"？删除后将同时删除关联的疫苗记录等数据，此操作不可恢复。').then(() => {
        return deletePet(row.id);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    }
  }
};
</script>

<style lang="scss" scoped>
.pet-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  cursor: pointer;
}

.avatar-fallback {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #c0c4cc;
  font-size: 20px;
  margin: 0 auto;
}

.detail-container {
  padding: 10px 0;
}

.detail-avatar-section {
  text-align: center;
  margin-bottom: 20px;

  .detail-avatar {
    width: 120px;
    height: 120px;
    border-radius: 50%;
    margin: 0 auto 10px;
  }

  .detail-avatar-fallback {
    width: 120px;
    height: 120px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f5f7fa;
    color: #c0c4cc;
    font-size: 48px;
    margin: 0 auto 10px;
  }

  .detail-name {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
  }
}

.detail-descriptions {
  margin-bottom: 20px;
}

.detail-section {
  margin-bottom: 20px;

  .section-title {
    font-size: 14px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 10px;
    padding-left: 8px;
    border-left: 3px solid #409eff;
  }

  .remark-content {
    padding: 12px;
    background: #f5f7fa;
    border-radius: 4px;
    color: #606266;
    font-size: 13px;
    line-height: 1.6;
    min-height: 60px;
  }
}
</style>
