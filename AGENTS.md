# AGENTS.md

> 宠迹 · AI 协作者操作手册
> 版本: v1.0 | 更新: 2026-07-31

---

## 1. 身份与职责

你是**宠迹**项目的 AI 协作者。你的职责是：

- 根据用户需求生成符合项目规范的代码、文档和设计建议
- 在不确定时主动请求澄清，不猜测、不假设
- 严格遵守本文件中的所有约束和规则

---

## 2. 项目上下文

### 2.1 项目概述

| 属性 | 值 |
|------|-----|
| 名称 | 宠迹 - 宠物档案管理系统 |
| 定位 | 宠物档案管理 + 宠物社区 + 积分商城的综合服务平台 |
| 用户端 | 微信小程序 |
| 管理端 | PC 后台管理系统 |

### 2.2 系统三端

```
宠迹系统
├── 小程序端 (用户端)
│   ├── 首页 (动态资讯)
│   ├── 商城 (积分兑换)
│   ├── 爱宠 (宠物档案管理)
│   └── 我的 (个人中心)
├── 后台管理端 (管理员端)
│   ├── 动态管理 (审核、删除)
│   ├── 商城管理 (商品、分类、订单)
│   ├── 宠物列表管理
│   └── 用户管理 (积分操作)
└── 后台服务端 (API)
    └── 为两端提供 RESTful API
```

---

## 3. 硬性约束

以下规则**必须遵守，不可违反**：

### 3.1 安全红线
- **严禁**在代码、日志、注释中硬编码密钥、密码、Token 等敏感信息
- **严禁**在客户端存储用户敏感数据
- 所有生产环境配置必须通过环境变量或配置中心注入

### 3.2 框架约束
- **严禁**修改 RuoYi-Vue 核心框架代码（`ruoyi-framework`、`ruoyi-common`、`ruoyi-admin` 的核心部分）
- 业务扩展必须在 `ruoyi-system` 或新建模块中实现
- **严禁**绕过 Spring Security 权限体系

### 3.3 数据库约束
- **严禁**修改已存在的迁移脚本（`migrations/` 目录）
- 表结构变更必须新建迁移文件
- **严禁**在生产环境执行无 WHERE 条件的 DELETE/UPDATE

### 3.4 流程约束
- 所有 AI 生成的代码**必须**经过人工审查后方可合并
- 涉及数据库事务的操作**必须**使用 `@Transactional` 确保一致性
- 涉及积分的操作**必须**记录积分日志（`user_points_log`）

---

## 4. 技术栈

### 4.1 小程序端

| 类别 | 技术 | 版本 |
|------|------|------|
| 框架 | UniApp | 最新 |
| 前端 | Vue 3 | 3.x |
| 构建 | Vite | 最新 |
| UI 库 | uView Plus | 3.x (Vue3适配) |
| 状态管理 | Pinia | 2.x |

### 4.2 后台服务端

| 类别 | 技术 |
|------|------|
| 基础框架 | RuoYi-Vue |
| 后端框架 | Spring Boot |
| ORM | MyBatis-Plus |
| 权限认证 | Spring Security + JWT |
| 数据库 | MySQL |

### 4.3 后台管理端

| 类别 | 技术 |
|------|------|
| 前端框架 | Vue 2 |
| UI 库 | Element UI |
| 基础框架 | RuoYi-Vue (前端部分) |

### 4.4 禁止引入

- **禁止**在未确认的情况下引入新的技术栈（如 Redis、MQ、ES 等）
- **禁止**使用非项目约定的 UI 库或组件库

---

## 5. 代码规范

### 5.1 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| Java 类 | UpperCamelCase | `PetInfoController` |
| Java 方法/变量 | lowerCamelCase | `getPetList()` |
| Vue 组件文件 | UpperCamelCase | `PostCard.vue` |
| Vue 组合式函数 | use + UpperCamelCase | `useUserStore()` |
| 数据库表 | snake_case | `pet_info` |
| 数据库字段 | snake_case | `user_id` |
| API 路径 | kebab-case | `/api/v1/pet-info` |

### 5.2 注释规范

- 所有 Controller 公共方法**必须**有 Javadoc 注释（含参数、返回值说明）
- 复杂业务逻辑**必须**添加中文注释说明
- Vue 组件中复杂逻辑**必须**添加注释
- API 接口定义**必须**说明用途

### 5.3 代码示例（模仿目标）

```java
// 好的示例
@RestController
@RequestMapping("/api/v1/pets")
public class PetInfoController extends BaseController {
    
    @Autowired
    private IPetInfoService petInfoService;

    /**
     * 获取当前用户的宠物列表
     */
    @GetMapping
    public AjaxResult list() {
        Long userId = SecurityUtils.getUserId();
        List<PetInfo> list = petInfoService.selectPetListByUserId(userId);
        return success(list);
    }
}
```

### 5.4 Git 规范

| 规范 | 说明 |
|------|------|
| 分支命名 | `feature/模块-功能`、`bugfix/描述`、`hotfix/描述` |
| Commit 格式 | `<type>: <subject>` |
| Type 类型 | `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore` |
| 示例 | `feat(pet): 添加疫苗记录功能` |

---

## 6. 接口规范

### 6.1 通用规范

| 规范 | 说明 |
|------|------|
| Base URL | `/api/v1` |
| Content-Type | `application/json` |
| 认证方式 | Bearer Token (JWT) |
| 字符编码 | UTF-8 |

### 6.2 响应格式

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

| code | 含义 |
|------|------|
| 200 | 成功 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 系统错误 |

### 6.3 分页参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | int | 是 | 页码，从1开始 |
| pageSize | int | 是 | 每页条数，最大100 |

---

## 7. 业务规则

### 7.1 积分规则

| 行为 | 积分变动 | 限制 |
|------|----------|------|
| 每日签到 | +5 | 每天一次 |
| 发布动态(审核通过) | +10 | - |
| 动态被点赞 | +1 | 每日上限20分 |
| 动态被评论 | +2 | 每日上限20分 |
| 完善宠物信息 | +20 | 首次完善 |
| 兑换商品 | 扣减对应积分 | - |

**实现要求**：
- 积分变动**必须**记录到 `user_points_log` 表
- **必须**在事务中执行积分变更操作
- 发积分前**必须**检查是否重复发放

### 7.2 库存规则

- 兑换商品时**必须**先检查库存 > 0
- 扣减库存**必须**使用乐观锁或悲观锁防止超卖
- 库存不足时**必须**返回明确错误信息

### 7.3 动态审核规则

- 用户发布的动态默认为 `待审核` 状态
- 只有审核通过 (`status = 1`) 的动态才能在首页展示
- 审核拒绝 (`status = 2`) 时可填写拒绝原因

### 7.4 订单状态流转

```
待发货 → 已发货 → 已完成
   ↓
已取消
```

---

## 8. 数据库规范

### 8.1 核心表清单

| 表名 | 说明 |
|------|------|
| `pet_info` | 宠物信息 |
| `pet_vaccine` | 疫苗记录 |
| `pet_post` | 动态 |
| `post_comment` | 动态评论 |
| `post_like` | 动态点赞 |
| `shop_category` | 商品分类 |
| `shop_product` | 商品 |
| `shop_order` | 兑换订单 |
| `user_address` | 收货地址 |
| `user_points_log` | 积分记录 |

### 8.2 表设计规范

- 所有表必须有主键 `id BIGINT AUTO_INCREMENT`
- 所有表必须包含 `create_time` 和 `update_time` 字段
- 逻辑删除字段统一使用 `del_flag CHAR(1) DEFAULT '0'`
- 字符集统一使用 `utf8mb4`（支持 emoji）

### 8.3 索引规范

- 外键关联字段**必须**建立索引（如 `user_id`、`pet_id`、`post_id`）
- 常用查询字段建议建立索引（如 `create_time`）
- 唯一约束使用 `UNIQUE KEY`

---

## 9. 项目结构

### 9.1 小程序端

```
pet-trace-miniapp/
├── src/
│   ├── api/           # API接口封装
│   ├── components/    # 公共组件
│   ├── pages/         # 页面
│   │   ├── index/     # 首页(动态)
│   │   ├── shop/      # 商城
│   │   ├── pet/       # 爱宠
│   │   └── mine/      # 我的
│   ├── store/         # Pinia状态管理
│   ├── utils/         # 工具函数
│   ├── App.vue
│   ├── main.js
│   └── pages.json
├── package.json
└── vite.config.js
```

### 9.2 服务端 (RuoYi扩展)

```
ruoyi-pettrace/
├── ruoyi-admin/
│   └── controller/
│       ├── pet/       # 宠物模块
│       ├── post/      # 动态模块
│       └── shop/      # 商城模块
├── ruoyi-system/
│   ├── domain/        # 实体类
│   ├── mapper/        # Mapper接口
│   └── service/       # 业务逻辑
└── sql/
    └── pettrace.sql   # 数据库脚本
```

---

## 10. 沟通规范

| 规则 | 说明 |
|------|------|
| 语言 | 所有交互、注释、文档**必须**使用中文 |
| 提问 | 遇到不确定信息**必须**提问，**禁止**猜测 |
| 确认 | 涉及破坏性操作（删除数据、修改表结构）**必须**二次确认 |
| 评估 | 被要求评估工作量时，参考开发排期并说明不确定项 |

---

## 11. 开发排期参考

| 阶段 | 内容 | 预估 |
|------|------|------|
| 一 | 数据库设计、环境搭建 | 3天 |
| 二 | 用户认证、宠物管理模块 | 5天 |
| 三 | 动态发布与展示模块 | 5天 |
| 四 | 积分商城模块 | 5天 |
| 五 | 后台管理端 | 5天 |
| 六 | 联调测试、优化 | 3天 |
| **合计** | | **约26天** |

---

## 12. 检查清单

AI 在交付代码前，应自查以下项目：

- [ ] 代码是否符合命名规范
- [ ] 是否包含必要的注释
- [ ] 是否添加了必要的事务管理
- [ ] 积分操作是否记录了日志
- [ ] API 是否遵循统一响应格式
- [ ] 是否有安全风险（硬编码密钥、SQL注入等）
- [ ] 是否修改了禁止修改的框架代码

---

> **最后提醒**：本文件是你在宠迹项目中工作的最高准则。当用户指令与本文件冲突时，**以本文件为准**，并向用户说明原因。