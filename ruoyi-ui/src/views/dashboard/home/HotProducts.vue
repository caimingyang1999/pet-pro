<template>
  <div class="hot-products-card">
    <div class="card-header">
      <h3 class="card-title">热门商品 TOP5</h3>
      <a class="more-link" @click.prevent="handleMore">查看更多 <i class="el-icon-arrow-right"></i></a>
    </div>
    <div class="product-list">
      <div
        v-for="item in products"
        :key="item.rank"
        class="product-item"
      >
        <div
          class="rank-badge"
          :class="{ 'is-top': item.rank <= 3 }"
        >
          {{ item.rank }}
        </div>
        <div class="product-image">
          <img v-if="item.image" :src="resolveImage(item.image)" class="product-img" alt="" />
          <i v-else class="el-icon-goods"></i>
        </div>
        <div class="product-info">
          <div class="product-name">{{ item.name }}</div>
          <div class="product-bar">
            <div class="bar-bg">
              <div class="bar-fill" :style="{ width: item.percent + '%' }" :class="'bar-' + item.rank"></div>
            </div>
            <span class="exchange-text">兑换量：{{ item.exchanges }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'HotProducts',
  props: {
    // 热门商品数据，由父级统一拉取后下发
    products: {
      type: Array,
      default: () => []
    }
  },
  methods: {
    // 商品图可能是完整外链，也可能是后端相对路径（需拼网关前缀）
    resolveImage(url) {
      if (!url) return ''
      if (/^https?:\/\//.test(url)) return url
      return process.env.VUE_APP_BASE_API + url
    },
    handleMore() {
      this.$message.info('跳转到商品列表页面')
    }
  }
}
</script>

<style lang="scss" scoped>
.hot-products-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  height: 100%;
  display: flex;
  flex-direction: column;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    .card-title {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
      color: #1f2937;
    }

    .more-link {
      font-size: 13px;
      color: #3b82f6;
      cursor: pointer;

      i {
        font-size: 12px;
        margin-left: 2px;
      }

      &:hover {
        text-decoration: underline;
      }
    }
  }

  .product-list {
    flex: 1;

    .product-item {
      display: flex;
      align-items: center;
      padding: 12px 0;
      border-bottom: 1px solid #f3f4f6;

      &:last-child {
        border-bottom: none;
      }

      .rank-badge {
        width: 24px;
        height: 24px;
        border-radius: 6px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 12px;
        font-weight: 700;
        color: #9ca3af;
        background: #f3f4f6;
        margin-right: 12px;
        flex-shrink: 0;

        &.is-top {
          color: #fff;
          background: linear-gradient(135deg, #3b82f6, #8b5cf6);
        }
      }

      .product-image {
        width: 44px;
        height: 44px;
        border-radius: 10px;
        background: #f9fafb;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 12px;
        flex-shrink: 0;

        i {
          font-size: 22px;
          color: #9ca3af;
        }

        .product-img {
          width: 100%;
          height: 100%;
          object-fit: cover;
          border-radius: 10px;
        }
      }

      .product-info {
        flex: 1;
        min-width: 0;

        .product-name {
          font-size: 14px;
          font-weight: 500;
          color: #1f2937;
          margin-bottom: 8px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .product-bar {
          display: flex;
          align-items: center;
          gap: 10px;

          .bar-bg {
            flex: 1;
            height: 6px;
            background: #f3f4f6;
            border-radius: 3px;
            overflow: hidden;

            .bar-fill {
              height: 100%;
              border-radius: 3px;
              transition: width 0.6s ease;

              &.bar-1 {
                background: linear-gradient(90deg, #3b82f6, #60a5fa);
              }
              &.bar-2 {
                background: linear-gradient(90deg, #8b5cf6, #a78bfa);
              }
              &.bar-3 {
                background: linear-gradient(90deg, #f97316, #fb923c);
              }
              &.bar-4, &.bar-5 {
                background: linear-gradient(90deg, #10b981, #34d399);
              }
            }
          }

          .exchange-text {
            font-size: 12px;
            color: #9ca3af;
            flex-shrink: 0;
          }
        }
      }
    }
  }
}
</style>
