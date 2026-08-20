<p align="center">
  <h1 align="center">宠迹 · 宠物档案管理系统</h1>
  <p align="center">
    宠物档案管理 · 宠物社区 · 积分商城 综合服务平台
  </p>
  <p align="center">
    <img alt="Java" src="https://img.shields.io/badge/Java-1.8-orange">
    <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-2.5.15-green">
    <img alt="RuoYi" src="https://img.shields.io/badge/RuoYi-3.9.1-brightgreen">
    <img alt="Vue" src="https://img.shields.io/badge/Vue-2.x%20%7C%203.x-42b883">
    <img alt="UniApp" src="https://img.shields.io/badge/UniApp-latest-blue">
    <img alt="License" src="https://img.shields.io/github/license/caimingyang1999/pet-pro">
  </p>
</p>

---

## 项目简介

**宠迹** 是一套面向宠物主人的综合服务平台，围绕「宠物档案管理 + 宠物社区 + 积分商城」三大核心场景，提供从宠物信息录入、动态分享到积分兑换的完整闭环。系统由三端协同构成：

- **小程序端（用户端）**：面向 C 端用户，提供首页动态、积分商城、宠物档案、个人中心等能力。
- **后台管理端（管理员端）**：面向运营人员，提供动态审核、商品管理、宠物列表管理、用户积分操作等能力。
- **后台服务端（API）**：为两端提供统一的 RESTful API 接口，基于 RuoYi-Vue 框架扩展实现。

---

## 系统架构

```
宠迹系统
├── 小程序端 (pettrace-miniapp)        ─── 用户端
│   ├── 首页  ·  动态资讯
│   ├── 商城  ·  积分兑换
│   ├── 爱宠  ·  宠物档案管理
│   └── 我的  ·  个人中心
├── 后台管理端 (ruoyi-ui)               ─── 管理员端
│   ├── 动态管理  ·  审核 / 删除
│   ├── 商城管理  ·  商品 / 分类 / 订单
│   ├── 宠物列表  ·  全站宠物管理
│   └── 用户管理  ·  积分操作
└── 后台服务端 (ruoyi-admin / ruoyi-system / ruoyi-ai)
    └── 统一 RESTful API · Spring Security 鉴权
```

---

## 技术栈

### 小程序端（pettrace-miniapp）

| 类别     | 技术          | 版本        |
| -------- | ------------- | ----------- |
| 框架     | UniApp        | 最新        |
| 前端框架 | Vue 3         | 3.x         |
| 构建工具 | Vite          | 最新        |
| UI 库    | uView Plus    | 3.x（Vue3）|
| 状态管理 | Pinia         | 2.x         |

### 后台服务端（基于 RuoYi-Vue 扩展）

| 类别      | 技术                          |
| --------- | ----------------------------- |
| 基础框架   | RuoYi-Vue                     |
| 后端框架   | Spring Boot                   |
| ORM        | MyBatis-Plus                  |
| 权限认证   | Spring Security + JWT         |
| 数据库     | MySQL                         |

### 后台管理端（ruoyi-ui）

| 类别      | 技术             |
| --------- | ---------------- |
| 前端框架   | Vue 2            |
| UI 库      | Element UI       |
| 基础框架   | RuoYi-Vue（前端）|

---

## 项目结构

```
pet-pro/
├── ruoyi-admin/             # 后端启动模块 + Controller 层
│   └── src/main/java/com/ruoyi/web/controller/
│       ├── pet/             #   宠物模块 Controller
│       ├── post/            #   动态模块 Controller
│       └── shop/           #   商城模块 Controller
├── ruoyi-system/            # 业务核心模块（实体、Mapper、Service）
│   └── src/main/java/com/ruoyi/system/
│       ├── domain/          #   实体类
│       ├── mapper/          #   Mapper 接口
│       └── service/         #   Service 接口与实现
├── ruoyi-ai/                # AI 对话模块（扩展）
├── ruoyi-framework/         # RuoYi 核心框架（不可修改）
├── ruoyi-common/            # RuoYi 通用模块（不可修改）
├── ruoyi-quartz/            # 定时任务模块
├── ruoyi-generator/         # 代码生成器模块
├── ruoyi-ui/                # 后台管理端（Vue 2 + Element UI）
├── pettrace-miniapp/        # 用户端小程序（UniApp + Vue 3）
├── sql/                     # 数据库脚本
├── doc/                     # 接口文档、开发手册
├── bin/                     # 脚本工具
└── pom.xml                  # Maven 父 POM
```

---

## 核心业务模块

| 模块         | 说明                                              |
| ------------ | ------------------------------------------------- |
| 宠物档案管理 | 宠物信息、疫苗记录的全生命周期管理                |
| 动态社区     | 动态发布、点赞、评论，审核通过后首页展示          |
| 积分商城     | 商品分类、积分兑换、订单流转、库存扣减            |
| 用户体系     | JWT 鉴权、积分变动日志、收货地址管理              |
| 后台运营     | 动态审核、商品上下架、订单管理、积分操作          |

### 积分规则

| 行为              | 积分变动 | 限制          |
| ----------------- | -------- | ------------- |
| 每日签到          | +5       | 每天一次      |
| 发布动态（审核通过）| +10      | -             |
| 动态被点赞        | +1       | 每日上限 20   |
| 动态被评论        | +2       | 每日上限 20   |
| 完善宠物信息      | +20      | 首次完善      |
| 兑换商品           | -        | 扣减对应积分  |

### 订单状态流转

```
待发货 → 已发货 → 已完成
   ↓
已取消
```

---

## 快速开始

### 1. 环境要求

- JDK 1.8+
- Maven 3.6+
- MySQL 5.7+ / 8.0
- Node.js 16+（前端构建）
- 微信开发者工具（小程序调试）

### 2. 初始化数据库

```bash
# 在 sql/ 目录下找到数据库脚本
# 1) 创建数据库 ruoyi-pettrace（utf8mb4）
# 2) 依次执行基础脚本与业务脚本
```

### 3. 启动后端服务

```bash
# 修改 ruoyi-admin/src/main/resources/application-druid.yml 中的数据库连接
# 修改 application.yml 中的 Redis / 文件路径等配置

mvn clean install -DskipTests
cd ruoyi-admin
mvn spring-boot:run
# 默认端口 8080，上下文路径 /api/v1
```

### 4. 启动后台管理端

```bash
cd ruoyi-ui
npm install
npm run dev
# 默认访问 http://localhost:80
```

### 5. 启动小程序端

```bash
cd pettrace-miniapp
npm install
npm run dev:mp-weixin
# 使用微信开发者工具打开 pettrace-miniapp/dist/dev/mp-weixin
```

---

## 接口规范

| 规范          | 说明                          |
| ------------- | ----------------------------- |
| Base URL      | `/api/v1`                     |
| Content-Type  | `application/json`            |
| 认证方式      | `Authorization: Bearer <JWT>` |
| 字符编码      | UTF-8                         |
| 统一响应格式  | `{ code, msg, data }`         |

分页参数：

| 参数      | 类型 | 必填 | 说明                      |
| --------- | ---- | ---- | ------------------------- |
| pageNum   | int  | 是   | 页码，从 1 开始           |
| pageSize  | int  | 是   | 每页条数，最大 100        |

> 完整接口文档见 [doc/API接口文档.md](doc/API接口文档.md)。

---

## 开发规范

### 命名规范

| 类型           | 规范             | 示例                     |
| -------------- | ---------------- | ------------------------ |
| Java 类        | UpperCamelCase   | `PetInfoController`      |
| Java 方法/变量 | lowerCamelCase   | `getPetList()`           |
| Vue 组件文件   | UpperCamelCase   | `PostCard.vue`           |
| Vue 组合式函数 | use + UpperCamelCase | `useUserStore()`      |
| 数据库表       | snake_case       | `pet_info`               |
| 数据库字段     | snake_case       | `user_id`                |
| API 路径       | kebab-case       | `/api/v1/pet-info`       |

### 数据库规范

- 所有表必须有主键 `id BIGINT AUTO_INCREMENT`
- 所有表必须包含 `create_time`、`update_time` 字段
- 逻辑删除字段统一使用 `del_flag CHAR(1) DEFAULT '0'`
- 字符集统一使用 `utf8mb4`
- 外键关联字段必须建立索引

### Git 提交规范

| 规范         | 说明                               |
| ------------ | ---------------------------------- |
| 分支命名     | `feature/模块-功能`、`bugfix/描述` |
| Commit 格式  | `<type>: <subject>`                |
| Type 类型    | `feat`、`fix`、`docs`、`refactor`、`chore` 等 |
| 示例         | `feat(pet): 添加疫苗记录功能`      |

---

## 核心数据表

| 表名              | 说明           |
| ----------------- | -------------- |
| `pet_info`        | 宠物信息       |
| `pet_vaccine`     | 疫苗记录       |
| `pet_post`        | 动态           |
| `post_comment`    | 动态评论       |
| `post_like`       | 动态点赞       |
| `shop_category`   | 商品分类       |
| `shop_product`    | 商品           |
| `shop_order`      | 兑换订单       |
| `user_address`    | 收货地址       |
| `user_points_log` | 积分记录       |

---

## 许可证

本项目基于 RuoYi-Vue 框架二次开发，遵循 MIT 许阅证。详见 [LICENSE](LICENSE)。

---

## 致谢

- [RuoYi-Vue](https://gitee.com/y_project/RuoYi-Vue) - 后端基础框架
- [UniApp](https://uniapp.dcloud.net.cn/) - 跨平台开发框架
- [uView Plus](https://uiadmin.net/uview-plus/) - Vue3 组件库
- [Element UI](https://element.eleme.io/) - 后台 UI 库

---

> **提示**：本项目仍在持续开发中，如需二次开发，请先阅读 [AGENTS.md](AGENTS.md) 与 [开发文档.md](开发文档.md) 中的约束与规范。
