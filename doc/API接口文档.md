# 宠迹 API 接口文档

> 版本：v1.6.0
> 更新时间：2026-08-20

---

## 目录

- [一、接口概述](#一接口概述)
- [二、用户认证模块](#二用户认证模块)
- [三、宠物管理模块](#三宠物管理模块)
- [四、动态模块](#四动态模块)
- [五、商城模块](#五商城模块)
- [六、轮播图模块](#六轮播图模块)
- [七、用户中心模块](#七用户中心模块)
- [八、后台管理模块](#八后台管理模块)
- [九、AI对话模块](#九ai对话模块)
- [十、公共模块](#十公共模块)
- [十一、公共数据结构](#十一公共数据结构)
- [十二、错误码说明](#十二错误码说明)

---

## 一、接口概述

### 1.1 Base URL

```
http://{host}:{port}/api/v1
```

> 所有业务接口的路径前缀为 `/api/v1`，下文接口路径均基于此前缀。公共模块接口路径前缀为 `/common`。

### 1.2 认证方式

采用 **Bearer Token** 认证，需在请求头中携带 Token：

```
Authorization: Bearer {token}
```

- 登录/注册接口无需 Token
- 标记为「需要登录」的接口必须携带有效 Token
- Token 过期或无效时返回 `401` 状态码

### 1.3 通用响应格式

所有接口统一返回 JSON 格式：

```json
{
  "code": 200,         // 状态码：200-成功，其他为失败
  "msg": "操作成功",    // 提示信息
  "data": {}           // 响应数据（对象/数组/null）
}
```

分页列表接口返回格式：

```json
{
  "code": 200,
  "msg": "查询成功",
  "rows": [],          // 数据列表
  "total": 100         // 总记录数
}
```

### 1.4 通用请求头

| 请求头 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Content-Type | string | 是 | `application/json; charset=utf-8`（文件上传除外） |
| Authorization | string | 否 | Bearer Token，需要登录的接口必填 |

---

## 二、用户认证模块

> 本模块路径前缀：`/api/v1` 或 `/api/v1/wx`，所有接口无需认证（匿名访问）。

---

### 2.1 用户注册（手机号+密码）

**接口说明**：小程序用户通过手机号+密码注册，注册成功自动登录，返回 Token。

| 项 | 值 |
|----|----|
| 接口路径 | `POST /register` |
| 是否登录 | 否 |
| 权限标识 | `@Anonymous` |

**请求体（Body）**：

```json
{
  "phone": "13800138000",        // 手机号（必填）
  "password": "123456",          // 密码（必填）
  "confirmPassword": "123456",   // 确认密码（必填，需与password一致）
  "nickname": "宠迹用户"          // 昵称（可选）
}
```

**请求示例（cURL）**：

```bash
curl -X POST 'http://localhost:8080/api/v1/register' \
  -H 'Content-Type: application/json' \
  -d '{
    "phone": "13800138000",
    "password": "123456",
    "confirmPassword": "123456",
    "nickname": "张三"
  }'
```

**成功响应示例**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",  // 登录令牌
    "userId": 1,                            // 用户ID
    "nickname": "张三",                     // 昵称
    "phone": "138****8000",                 // 脱敏手机号
    "points": 0                             // 初始积分
  }
}
```

---

### 2.2 用户账号密码登录

**接口说明**：已注册用户通过手机号+密码登录。

| 项 | 值 |
|----|----|
| 接口路径 | `POST /user/uertlogin` |
| 是否登录 | 否 |
| 权限标识 | - |

**请求体（Body）**：

```json
{
  "username": "13800138000",   // 手机号（必填）
  "password": "123456"          // 密码（必填）
}
```

**请求示例（cURL）**：

```bash
curl -X POST 'http://localhost:8080/api/v1/user/uertlogin' \
  -H 'Content-Type: application/json' \
  -d '{
    "username": "13800138000",
    "password": "123456"
  }'
```

**成功响应示例**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "token": "eyJhbGciOiJIUzUxMiJ9..."   // 登录令牌
}
```

**失败响应示例**：

```json
{
  "code": 500,
  "msg": "用户不存在"
}
```

---

### 2.3 微信登录

**接口说明**：小程序端通过微信授权码登录，新用户自动注册。

| 项 | 值 |
|----|----|
| 接口路径 | `POST /wx/login` |
| 是否登录 | 否 |
| 权限标识 | `@Anonymous` |

**请求体（Body）**：

```json
{
  "code": "wx_code_xxx",        // 微信登录凭证 code（必填）
  "nickName": "微信用户",        // 微信昵称（可选）
  "avatar": "https://.../avatar.jpg",  // 微信头像URL（可选）
  "gender": "1"                 // 性别 0-未知 1-男 2-女（可选）
}
```

**请求示例（cURL）**：

```bash
curl -X POST 'http://localhost:8080/api/v1/wx/login' \
  -H 'Content-Type: application/json' \
  -d '{
    "code": "081abc123",
    "nickName": "张三",
    "avatar": "https://thirdwx.qlogo.cn/mmopen/..."
  }'
```

**成功响应示例**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",   // 登录令牌
    "userId": 1,                             // 用户ID
    "nickName": "张三",                      // 昵称
    "avatar": "https://.../avatar.jpg",      // 头像
    "phonenumber": "138****8000",            // 脱敏手机号（未绑定则为null）
    "points": 520,                           // 用户积分
    "isNewUser": false                       // 是否为新注册用户
  }
}
```

---

### 2.4 绑定手机号

**接口说明**：微信登录用户绑定手机号（需已登录）。

| 项 | 值 |
|----|----|
| 接口路径 | `POST /wx/bind-phone` |
| 是否登录 | 是 |
| 权限标识 | `isAuthenticated()` |

**请求体（Body）**：

```json
{
  "code": "wx_phone_code_xxx"   // 微信手机号获取凭证 code（必填）
}
```

**请求示例（cURL）**：

```bash
curl -X POST 'http://localhost:8080/api/v1/wx/bind-phone' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...' \
  -d '{
    "code": "081abc456"
  }'
```

**成功响应示例**：

```json
{
  "code": 200,
  "msg": "操作成功"
}
```

---

## 三、宠物管理模块

> 模块路径：`/api/v1/pets`

---

### 3.1 获取当前用户宠物列表

**接口说明**：获取当前登录用户的所有宠物列表，每只宠物包含疫苗记录。

| 项 | 值 |
|----|----|
| 接口路径 | `GET /pets/list` |
| 是否登录 | 是 |
| 权限标识 | `pet:list` |

**请求参数**：无

**请求示例（cURL）**：

```bash
curl -X GET 'http://localhost:8080/api/v1/pets/list' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...'
```

**成功响应示例**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "id": 1,                          // 宠物ID
      "userId": 1,                      // 用户ID
      "name": "小黄",                    // 宠物名称
      "avatar": "https://.../dog.jpg",  // 宠物头像
      "breed": "金毛",                   // 品种
      "birthday": "2023-01-15",         // 出生日期
      "gender": "1",                    // 性别 0-母 1-公
      "weight": 25.5,                   // 体重(kg)
      "color": "金色",                   // 毛色
      "sterilization": "1",             // 绝育状态 0-未绝育 1-已绝育
      "remark": "性格温顺",               // 备注
      "delFlag": "0",                   // 逻辑删除标志 0-存在 1-删除
      "createTime": "2026-01-01 10:00:00",
      "updateTime": "2026-07-28 12:00:00",
      "userName": "张三",                // 所属用户名
      "vaccineList": [                  // 疫苗记录列表
        {
          "id": 1,
          "petId": 1,
          "vaccineName": "狂犬疫苗",
          "inoculationDate": "2026-01-15",
          "nextDate": "2027-01-15",
          "createTime": "2026-01-15 10:00:00"
        }
      ]
    }
  ]
}
```

**失败响应示例**：

```json
{
  "code": 401,
  "msg": "登录已过期，请重新登录"
}
```

---

### 3.2 获取宠物详情

**接口说明**：根据宠物ID获取宠物详细信息（含疫苗记录），无需登录。

| 项 | 值 |
|----|----|
| 接口路径 | `GET /pets/{petId}` |
| 是否登录 | 否 |
| 权限标识 | - |

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| petId | long | 是 | 宠物ID |

**请求示例（cURL）**：

```bash
curl -X GET 'http://localhost:8080/api/v1/pets/1'
```

**成功响应示例**：同 3.1 的 data 单条对象。

**失败响应示例**：

```json
{
  "code": 500,
  "msg": "宠物不存在"
}
```

---

### 3.3 添加宠物

**接口说明**：添加新宠物，可同时上传疫苗记录。首次完善宠物信息奖励20积分。

| 项 | 值 |
|----|----|
| 接口路径 | `POST /pets` |
| 是否登录 | 是 |
| 权限标识 | `pet:add` |

**请求体（Body）**：

```json
{
  "name": "小黄",                    // 宠物名称（必填）
  "avatar": "https://.../dog.jpg",  // 宠物头像（可选）
  "breed": "金毛",                   // 品种（必填）
  "birthday": "2023-01-15",         // 出生日期（可选）
  "gender": "1",                    // 性别 0-母 1-公（可选）
  "weight": 25.5,                   // 体重(kg)（可选）
  "color": "金色",                   // 毛色（可选）
  "sterilization": "0",             // 绝育状态 0-未绝育 1-已绝育（可选）
  "remark": "备注信息",               // 备注（可选）
  "vaccineList": [                  // 疫苗记录列表（可选）
    {
      "vaccineName": "狂犬疫苗",
      "inoculationDate": "2026-01-15",
      "nextDate": "2027-01-15"
    }
  ]
}
```

**请求示例（cURL）**：

```bash
curl -X POST 'http://localhost:8080/api/v1/pets' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...' \
  -d '{
    "name": "小黄",
    "breed": "金毛",
    "gender": "1",
    "vaccineList": [
      {
        "vaccineName": "狂犬疫苗",
        "inoculationDate": "2026-01-15"
      }
    ]
  }'
```

**成功响应示例**：

```json
{
  "code": 200,
  "msg": "操作成功"
}
```

---

### 3.4 更新宠物

**接口说明**：更新宠物信息（先删旧疫苗记录再新增）。校验是否为宠物主人。

| 项 | 值 |
|----|----|
| 接口路径 | `PUT /pets/{petId}` |
| 是否登录 | 是 |
| 权限标识 | `pet:edit` |

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| petId | long | 是 | 宠物ID |

**请求体（Body）**：同 3.3，userId 字段会被忽略（自动取当前登录用户）。

**请求示例（cURL）**：

```bash
curl -X PUT 'http://localhost:8080/api/v1/pets/1' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...' \
  -d '{
    "name": "小黄",
    "weight": 28.0
  }'
```

**成功响应示例**：

```json
{
  "code": 200,
  "msg": "操作成功"
}
```

**失败响应示例**：

```json
{
  "code": 500,
  "msg": "无权操作他人宠物"
}
```

---

### 3.5 删除宠物

**接口说明**：逻辑删除宠物（校验是否为宠物主人）。

| 项 | 值 |
|----|----|
| 接口路径 | `DELETE /pets/{petId}` |
| 是否登录 | 是 |
| 权限标识 | `pet:remove` |

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| petId | long | 是 | 宠物ID |

**请求示例（cURL）**：

```bash
curl -X DELETE 'http://localhost:8080/api/v1/pets/1' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...'
```

**成功响应示例**：

```json
{
  "code": 200,
  "msg": "操作成功"
}
```

**失败响应示例**：

```json
{
  "code": 500,
  "msg": "无权删除他人宠物"
}
```

---

## 四、动态模块

> 模块路径：`/api/v1/posts`

---

### 4.1 动态列表

**接口说明**：分页查询动态列表（仅展示审核通过的动态），支持关键词搜索，无需登录即可查看。

| 项 | 值 |
|----|----|
| 接口路径 | `GET /posts/list` |
| 是否登录 | 否 |
| 权限标识 | - |

**查询参数（Query）**：

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| pageNum | int | 否 | 1 | 当前页码 |
| pageSize | int | 否 | 10 | 每页条数，最大100 |
| keyword | string | 否 | - | 搜索关键词（匹配内容/用户名） |

**请求示例（cURL）**：

```bash
curl -X GET 'http://localhost:8080/api/v1/posts/list?pageNum=1&pageSize=10&keyword=可爱'
```

**成功响应示例**：

```json
{
  "code": 200,
  "msg": "查询成功",
  "rows": [
    {
      "id": 1,                          // 动态ID
      "userId": 1,                      // 发布用户ID
      "petId": 1,                       // 关联宠物ID（可为null）
      "content": "今天去公园遛狗啦！",   // 动态内容
      "images": "[\"url1\",\"url2\"]",  // 图片列表（JSON数组字符串）
      "status": "1",                    // 审核状态 0-待审核 1-通过 2-拒绝
      "likeCount": 100,                 // 点赞数
      "commentCount": 20,               // 评论数
      "viewCount": 500,                 // 浏览数
      "delFlag": "0",                   // 逻辑删除标志
      "createTime": "2026-07-28 10:00:00",
      "updateTime": "2026-07-28 10:00:00",
      "userName": "张三",                // 发布者用户名
      "userAvatar": "https://.../avatar.jpg"  // 发布者头像
    }
  ],
  "total": 58                           // 总记录数
}
```

---

### 4.2 我的动态

**接口说明**：获取当前登录用户发布的动态列表（含全部审核状态）。

| 项 | 值 |
|----|----|
| 接口路径 | `GET /posts/my` |
| 是否登录 | 是 |
| 权限标识 | `isAuthenticated()` |

**请求示例（cURL）**：

```bash
curl -X GET 'http://localhost:8080/api/v1/posts/my' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...'
```

**成功响应示例**：同 4.1 分页格式。

---

### 4.3 动态详情

**接口说明**：根据动态ID获取动态详情。

| 项 | 值 |
|----|----|
| 接口路径 | `GET /posts/{postId}` |
| 是否登录 | 否 |
| 权限标识 | - |

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| postId | long | 是 | 动态ID |

**请求示例（cURL）**：

```bash
curl -X GET 'http://localhost:8080/api/v1/posts/1'
```

**成功响应示例**：同 4.1 的 rows 单条对象。

---

### 4.4 发布动态

**接口说明**：发布新动态（审核通过后增加10积分）。动态初始状态为待审核。

| 项 | 值 |
|----|----|
| 接口路径 | `POST /posts` |
| 是否登录 | 是 |
| 权限标识 | `isAuthenticated()` |

**请求体（Body）**：

```json
{
  "petId": 1,                                   // 关联宠物ID（可选）
  "content": "今天带小黄去公园玩，太开心了！",    // 动态内容（必填）
  "images": "[\"https://.../img1.jpg\"]"       // 图片列表（JSON数组字符串，可选）
}
```

**请求示例（cURL）**：

```bash
curl -X POST 'http://localhost:8080/api/v1/posts' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...' \
  -d '{
    "content": "今天带小黄去公园玩",
    "images": "[\"https://example.com/img1.jpg\"]"
  }'
```

**成功响应示例**：

```json
{
  "code": 200,
  "msg": "操作成功"
}
```

---

### 4.5 删除动态

**接口说明**：删除动态（校验是否为发布者）。

| 项 | 值 |
|----|----|
| 接口路径 | `DELETE /posts/{postId}` |
| 是否登录 | 是 |
| 权限标识 | `isAuthenticated()` |

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| postId | long | 是 | 动态ID |

**请求示例（cURL）**：

```bash
curl -X DELETE 'http://localhost:8080/api/v1/posts/1' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...'
```

**成功响应示例**：

```json
{
  "code": 200,
  "msg": "操作成功"
}
```

**失败响应示例**：

```json
{
  "code": 500,
  "msg": "无权删除他人动态"
}
```

---

### 4.6 点赞/取消点赞

**接口说明**：对动态进行点赞或取消点赞操作，自动同步点赞数。被点赞用户每日上限20积分。

| 项 | 值 |
|----|----|
| 接口路径 | `POST /posts/{postId}/like` |
| 是否登录 | 是 |
| 权限标识 | `isAuthenticated()` |

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| postId | long | 是 | 动态ID |

**请求示例（cURL）**：

```bash
curl -X POST 'http://localhost:8080/api/v1/posts/1/like' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...'
```

**成功响应示例**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "liked": true   // true-已点赞 false-已取消点赞
}
```

---

### 4.7 评论列表

**接口说明**：获取动态的评论列表（树形结构，一级评论分页，含子评论）。

| 项 | 值 |
|----|----|
| 接口路径 | `GET /posts/{postId}/comments` |
| 是否登录 | 否 |
| 权限标识 | - |

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| postId | long | 是 | 动态ID |

**查询参数（Query）**：

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| pageNum | int | 否 | 1 | 当前页码（一级评论分页） |
| pageSize | int | 否 | 10 | 每页条数 |

**请求示例（cURL）**：

```bash
curl -X GET 'http://localhost:8080/api/v1/posts/1/comments?pageNum=1&pageSize=10'
```

**成功响应示例**：

```json
{
  "code": 200,
  "msg": "查询成功",
  "rows": [
    {
      "id": 1,                          // 评论ID
      "postId": 1,                      // 动态ID
      "userId": 2,                      // 评论用户ID
      "parentId": 0,                    // 父评论ID（0为一级评论）
      "content": "好可爱啊！",             // 评论内容
      "delFlag": "0",
      "createTime": "2026-07-28 11:00:00",
      "updateTime": "2026-07-28 11:00:00",
      "userName": "李四",                // 评论者用户名
      "userAvatar": "https://.../avatar.jpg",
      "children": [                     // 子评论列表
        {
          "id": 2,
          "postId": 1,
          "userId": 1,
          "parentId": 1,
          "content": "谢谢~",
          "delFlag": "0",
          "createTime": "2026-07-28 12:00:00",
          "updateTime": "2026-07-28 12:00:00",
          "userName": "张三",
          "userAvatar": "https://.../avatar.jpg",
          "children": []
        }
      ]
    }
  ],
  "total": 15
}
```

---

### 4.8 发表评论

**接口说明**：对动态发表评论（支持回复父评论）。被评论用户每日上限20积分。

| 项 | 值 |
|----|----|
| 接口路径 | `POST /posts/{postId}/comments` |
| 是否登录 | 是 |
| 权限标识 | `isAuthenticated()` |

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| postId | long | 是 | 动态ID |

**请求体（Body）**：

```json
{
  "parentId": 1,    // 父评论ID（可选，0或不传为一级评论）
  "content": "好可爱！"  // 评论内容（必填）
}
```

**请求示例（cURL）**：

```bash
curl -X POST 'http://localhost:8080/api/v1/posts/1/comments' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...' \
  -d '{
    "content": "好可爱啊！"
  }'
```

**成功响应示例**：

```json
{
  "code": 200,
  "msg": "操作成功"
}
```

---

## 五、商城模块

> 模块路径：`/api/v1/shop`

---

### 5.1 获取分类树

**接口说明**：获取商品分类树形结构（仅返回正常状态的分类）。

| 项 | 值 |
|----|----|
| 接口路径 | `GET /shop/categories` |
| 是否登录 | 否 |
| 权限标识 | - |

**请求示例（cURL）**：

```bash
curl -X GET 'http://localhost:8080/api/v1/shop/categories'
```

**成功响应示例**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "id": 1,                          // 分类ID
      "parentId": 0,                    // 父分类ID
      "categoryName": "食品",            // 分类名称
      "icon": "https://.../food.png",   // 分类图标
      "sortOrder": 1,                   // 排序
      "status": "1",                    // 状态 0-停用 1-正常
      "delFlag": "0",
      "createTime": "2026-01-01 10:00:00",
      "updateTime": "2026-01-01 10:00:00",
      "children": [                     // 子分类列表
        {
          "id": 2,
          "parentId": 1,
          "categoryName": "狗粮",
          "icon": "https://.../dog.png",
          "sortOrder": 1,
          "status": "1",
          "delFlag": "0",
          "createTime": "2026-01-01 10:00:00",
          "updateTime": "2026-01-01 10:00:00",
          "children": []
        }
      ]
    }
  ]
}
```

---

### 5.2 商品列表

**接口说明**：分页查询商品列表（仅返回上架商品），支持按分类和关键词筛选。

| 项 | 值 |
|----|----|
| 接口路径 | `GET /shop/products` |
| 是否登录 | 否 |
| 权限标识 | - |

**查询参数（Query）**：

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| categoryId | long | 否 | - | 分类ID |
| keyword | string | 否 | - | 搜索关键词（匹配商品名称） |
| pageNum | int | 否 | 1 | 当前页码 |
| pageSize | int | 否 | 10 | 每页条数，最大100 |

**请求示例（cURL）**：

```bash
curl -X GET 'http://localhost:8080/api/v1/shop/products?categoryId=2&keyword=金毛&pageNum=1&pageSize=10'
```

**成功响应示例**：

```json
{
  "code": 200,
  "msg": "查询成功",
  "rows": [
    {
      "id": 1,                                // 商品ID
      "categoryId": 2,                        // 分类ID
      "productName": "金毛幼犬专用粮",         // 商品名称
      "productImages": "[\"url1\",\"url2\"]", // 商品图片（JSON数组字符串）
      "description": "专为金毛幼犬配方...",    // 商品描述
      "pointsPrice": 100,                     // 积分价格
      "stock": 500,                           // 库存
      "totalExchange": 1200,                  // 总兑换数
      "status": "1",                          // 状态 0-下架 1-上架
      "delFlag": "0",
      "createTime": "2026-01-01 10:00:00",
      "updateTime": "2026-01-01 10:00:00",
      "categoryName": "狗粮"                   // 分类名称
    }
  ],
  "total": 28
}
```

---

### 5.3 商品详情

**接口说明**：根据商品ID获取商品详细信息。

| 项 | 值 |
|----|----|
| 接口路径 | `GET /shop/products/{productId}` |
| 是否登录 | 否 |
| 权限标识 | - |

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| productId | long | 是 | 商品ID |

**请求示例（cURL）**：

```bash
curl -X GET 'http://localhost:8080/api/v1/shop/products/1'
```

**成功响应示例**：同 5.2 的 rows 单条对象。

---

### 5.4 兑换商品

**接口说明**：使用积分兑换商品，扣减积分和库存，创建订单（全事务）。库存扣减使用乐观锁防止超卖。

| 项 | 值 |
|----|----|
| 接口路径 | `POST /shop/orders` |
| 是否登录 | 是 |
| 权限标识 | `isAuthenticated()` |

**请求体（Body）**：

```json
{
  "productId": 1,      // 商品ID（必填）
  "quantity": 1,       // 兑换数量（必填，默认1）
  "addressId": 1       // 收货地址ID（必填）
}
```

**请求示例（cURL）**：

```bash
curl -X POST 'http://localhost:8080/api/v1/shop/orders' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...' \
  -d '{
    "productId": 1,
    "quantity": 2,
    "addressId": 1
  }'
```

**成功响应示例**：

```json
{
  "code": 200,
  "msg": "兑换成功",
  "orderNo": "17221000000001234"   // 订单编号
}
```

**失败响应示例**：

```json
{
  "code": 500,
  "msg": "商品库存不足"
}
```

其他可能的错误消息：`商品不存在`、`商品已下架`、`用户积分不足`

---

### 5.5 我的订单列表

**接口说明**：获取当前用户的订单列表，支持按状态筛选。

| 项 | 值 |
|----|----|
| 接口路径 | `GET /shop/orders` |
| 是否登录 | 是 |
| 权限标识 | `isAuthenticated()` |

**查询参数（Query）**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| status | string | 否 | 订单状态（0-待发货 1-已发货 2-已完成 3-已取消） |

**请求示例（cURL）**：

```bash
curl -X GET 'http://localhost:8080/api/v1/shop/orders?status=0' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...'
```

**成功响应示例**：

```json
{
  "code": 200,
  "msg": "查询成功",
  "rows": [
    {
      "id": 1,                              // 订单ID
      "orderNo": "17221000000001234",       // 订单编号
      "userId": 1,                          // 用户ID
      "productId": 1,                       // 商品ID
      "productName": "金毛幼犬专用粮",       // 商品名称（快照）
      "productImage": "https://.../img.jpg",// 商品图片（快照）
      "pointsPrice": 100,                   // 兑换积分（单价）
      "quantity": 2,                        // 兑换数量
      "totalPoints": 200,                   // 总积分
      "addressId": 1,                       // 收货地址ID
      "status": "0",                        // 订单状态
      "expressNo": null,                    // 快递单号
      "expressCompany": null,               // 快递公司
      "delFlag": "0",
      "createTime": "2026-07-28 10:00:00",
      "updateTime": "2026-07-28 10:00:00",
      "userName": "张三"                    // 下单用户名
    }
  ],
  "total": 5
}
```

---

### 5.6 订单详情

**接口说明**：根据订单ID获取订单详情（校验归属，仅本人或管理员可看）。

| 项 | 值 |
|----|----|
| 接口路径 | `GET /shop/orders/{orderId}` |
| 是否登录 | 是 |
| 权限标识 | `isAuthenticated()` |

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orderId | long | 是 | 订单ID |

**请求示例（cURL）**：

```bash
curl -X GET 'http://localhost:8080/api/v1/shop/orders/1' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...'
```

**成功响应示例**：同 5.5 的 rows 单条对象。

---

## 六、轮播图模块

> 模块路径：`/api/v1/banner`（小程序端） & `/api/v1/admin/banner`（后台管理）
> 小程序端接口无需登录，后台管理接口需 `banner:list` 权限。

---

### 6.1 获取启用的轮播图列表（小程序端）

**接口说明**：返回当前启用且在展示时间段内的轮播图列表，按排序号升序、ID降序排列。首页顶部轮播图调用此接口。

| 项 | 值 |
|----|----|
| 接口路径 | `GET /banner/list` |
| 是否登录 | 否 |
| 权限标识 | `@Anonymous` |

**请求参数**：无

**请求示例（cURL）**：

```bash
curl -X GET 'http://localhost:8080/api/v1/banner/list'
```

**成功响应示例**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "id": 1,                              // 轮播图ID
      "title": "宠物健康指南",              // 标题
      "imageUrl": "/static/banner/banner1.jpg", // 图片地址
      "jumpType": "none",                   // 跳转类型
      "jumpTarget": null,                   // 跳转目标
      "sortOrder": 1,                       // 排序号（越小越靠前）
      "status": "1",                        // 状态 0-停用 1-启用
      "startTime": null,                    // 开始时间（null为不限）
      "endTime": null,                      // 结束时间（null为不限）
      "remark": null,                       // 备注
      "createTime": "2026-08-01 10:00:00",
      "updateTime": "2026-08-01 10:00:00"
    },
    {
      "id": 2,
      "title": "新品宠物玩具上线",
      "imageUrl": "/static/banner/banner2.jpg",
      "jumpType": "product",
      "jumpTarget": "1",
      "sortOrder": 2,
      "status": "1",
      "startTime": "2026-08-20 00:00:00",
      "endTime": "2026-09-20 23:59:59",
      "remark": "新品推广活动",
      "createTime": "2026-08-01 10:00:00",
      "updateTime": "2026-08-19 18:00:00"
    }
  ]
}
```

**跳转类型（jumpType）说明**：

| jumpType | 说明 | jumpTarget 示例 |
|----------|------|-----------------|
| `none` | 不跳转 | `null` |
| `post` | 动态详情 | 动态ID，如 `"1"` |
| `product` | 商品详情 | 商品ID，如 `"1"` |
| `url` | 外部链接 | 完整URL，如 `"https://example.com/activity"` |
| `miniapp` | 小程序内部页面 | 小程序页面路径，如 `"/pages/shop/index"` |

---

## 七、用户中心模块

> 模块路径：`/api/v1/user`

---

### 8.1 获取当前用户信息

**接口说明**：获取当前登录用户的基本信息和积分。

| 项 | 值 |
|----|----|
| 接口路径 | `GET /user/info` |
| 是否登录 | 是 |
| 权限标识 | `isAuthenticated()` |

**请求示例（cURL）**：

```bash
curl -X GET 'http://localhost:8080/api/v1/user/info' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...'
```

**成功响应示例**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "userId": 1,                              // 用户ID
    "nickName": "张三",                        // 昵称
    "avatar": "https://.../avatar.jpg",        // 头像
    "phonenumber": "138****8000",              // 脱敏手机号
    "points": 520,                             // 当前积分
    "loginType": "wx"                          // 登录类型 wx-微信 phone-手机号
  }
}
```

---

### 7.2 积分明细

**接口说明**：获取当前用户的积分变动记录（分页）。

| 项 | 值 |
|----|----|
| 接口路径 | `GET /user/points/log` |
| 是否登录 | 是 |
| 权限标识 | `isAuthenticated()` |

**请求示例（cURL）**：

```bash
curl -X GET 'http://localhost:8080/api/v1/user/points/log' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...'
```

**成功响应示例**：

```json
{
  "code": 200,
  "msg": "查询成功",
  "rows": [
    {
      "id": 1,                              // 记录ID
      "userId": 1,                          // 用户ID
      "pointsChange": 10,                   // 积分变动（正增负减）
      "pointsBalance": 150,                 // 变动后余额
      "changeType": "post",                 // 变动类型 sign_in/post/exchange/admin/like/comment/pet_complete
      "relateId": 1,                        // 关联业务ID
      "remark": "发布动态奖励",               // 备注
      "createTime": "2026-07-28 10:00:00"
    }
  ],
  "total": 20
}
```

---

### 7.3 地址列表

**接口说明**：获取当前用户的收货地址列表（默认地址排最前）。

| 项 | 值 |
|----|----|
| 接口路径 | `GET /user/addresses` |
| 是否登录 | 是 |
| 权限标识 | `isAuthenticated()` |

**请求示例（cURL）**：

```bash
curl -X GET 'http://localhost:8080/api/v1/user/addresses' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...'
```

**成功响应示例**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "id": 1,                              // 地址ID
      "userId": 1,                          // 用户ID
      "receiverName": "张三",                // 收件人
      "receiverPhone": "13800138000",       // 联系电话
      "province": "浙江省",                  // 省份
      "city": "杭州市",                      // 城市
      "district": "西湖区",                  // 区县
      "detailAddress": "文三路123号",        // 详细地址
      "isDefault": "1",                     // 是否默认 0-否 1-是
      "delFlag": "0",
      "createTime": "2026-01-01 10:00:00",
      "updateTime": "2026-01-01 10:00:00"
    }
  ]
}
```

---

### 7.4 添加地址

**接口说明**：添加新的收货地址。如设为默认，自动取消其他默认地址。

| 项 | 值 |
|----|----|
| 接口路径 | `POST /user/addresses` |
| 是否登录 | 是 |
| 权限标识 | `isAuthenticated()` |

**请求体（Body）**：

```json
{
  "receiverName": "张三",       // 收件人（必填）
  "receiverPhone": "13800138000",  // 联系电话（必填）
  "province": "浙江省",          // 省份（可选）
  "city": "杭州市",              // 城市（可选）
  "district": "西湖区",          // 区县（可选）
  "detailAddress": "文三路123号",  // 详细地址（必填）
  "isDefault": "1"              // 是否默认 0-否 1-是（可选，默认0）
}
```

**请求示例（cURL）**：

```bash
curl -X POST 'http://localhost:8080/api/v1/user/addresses' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...' \
  -d '{
    "receiverName": "张三",
    "receiverPhone": "13800138000",
    "province": "浙江省",
    "city": "杭州市",
    "district": "西湖区",
    "detailAddress": "文三路123号",
    "isDefault": "1"
  }'
```

**成功响应示例**：

```json
{
  "code": 200,
  "msg": "操作成功"
}
```

---

### 7.5 更新地址

**接口说明**：更新收货地址（校验归属）。如设为默认，自动取消其他默认地址。

| 项 | 值 |
|----|----|
| 接口路径 | `PUT /user/addresses/{id}` |
| 是否登录 | 是 |
| 权限标识 | `isAuthenticated()` |

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | long | 是 | 地址ID |

**请求体（Body）**：同 6.4。

**请求示例（cURL）**：

```bash
curl -X PUT 'http://localhost:8080/api/v1/user/addresses/1' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...' \
  -d '{
    "detailAddress": "文三路456号"
  }'
```

**成功响应示例**：

```json
{
  "code": 200,
  "msg": "操作成功"
}
```

**失败响应示例**：

```json
{
  "code": 500,
  "msg": "无权操作此地址"
}
```

---

### 7.6 删除地址

**接口说明**：删除收货地址（校验归属）。

| 项 | 值 |
|----|----|
| 接口路径 | `DELETE /user/addresses/{id}` |
| 是否登录 | 是 |
| 权限标识 | `isAuthenticated()` |

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | long | 是 | 地址ID |

**请求示例（cURL）**：

```bash
curl -X DELETE 'http://localhost:8080/api/v1/user/addresses/1' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...'
```

**成功响应示例**：

```json
{
  "code": 200,
  "msg": "操作成功"
}
```

---

## 八、后台管理模块

> 模块路径：`/api/v1/admin`
> 所有接口需管理员权限（`@ss.hasPermi('admin')`）

---

### 8.1 用户管理

#### 8.1.1 用户列表

**接口说明**：分页查询用户列表，含宠物数量、动态数量、积分信息。

| 项 | 值 |
|----|----|
| 接口路径 | `GET /admin/users` |
| 是否登录 | 是 |
| 权限标识 | `admin` |

**查询参数（Query）**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userName | string | 否 | 用户账号（模糊搜索） |
| phonenumber | string | 否 | 手机号码（模糊搜索） |
| pageNum | int | 否 | 当前页码（默认1） |
| pageSize | int | 否 | 每页条数（默认10） |

**请求示例（cURL）**：

```bash
curl -X GET 'http://localhost:8080/api/v1/admin/users?userName=zhangsan&pageNum=1&pageSize=10' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...'
```

**成功响应示例**：

```json
{
  "code": 200,
  "msg": "查询成功",
  "rows": [
    {
      "user": {
        "userId": 1,
        "userName": "zhangsan",
        "nickName": "张三",
        "avatar": "https://.../avatar.jpg",
        "phonenumber": "13800138000",
        "email": null,
        "sex": "1",
        "status": "0",
        "delFlag": "0",
        "loginType": "wx",
        "points": 520,
        "createTime": "2026-01-01 10:00:00"
      },
      "petCount": 2,          // 宠物数量
      "postCount": 15,        // 动态数量
      "points": 520           // 用户积分
    }
  ],
  "total": 100
}
```

#### 8.1.2 积分操作

**接口说明**：管理员为用户增加或扣减积分，自动记录积分日志。

| 项 | 值 |
|----|----|
| 接口路径 | `POST /admin/users/{userId}/points` |
| 是否登录 | 是 |
| 权限标识 | `admin` |

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | long | 是 | 用户ID |

**请求体（Body）**：

```json
{
  "pointsChange": 100,   // 变动积分（正数增加，负数扣减，必填，不能为0）
  "remark": "管理员奖励"   // 备注（可选）
}
```

**请求示例（cURL）**：

```bash
curl -X POST 'http://localhost:8080/api/v1/admin/users/1/points' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...' \
  -d '{
    "pointsChange": 100,
    "remark": "管理员奖励"
  }'
```

**成功响应示例**：

```json
{
  "code": 200,
  "msg": "操作成功"
}
```

**失败响应示例**：

```json
{
  "code": 500,
  "msg": "积分变动值不能为空或0"
}
```

---

### 8.2 宠物管理

#### 8.2.1 宠物列表

| 项 | 值 |
|----|----|
| 接口路径 | `GET /admin/pets` |
| 是否登录 | 是 |
| 权限标识 | `admin` |

**查询参数（Query）**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | string | 否 | 宠物名称（模糊搜索） |
| userId | long | 否 | 所属用户ID |
| pageNum | int | 否 | 当前页码（默认1） |
| pageSize | int | 否 | 每页条数（默认10） |

**请求示例（cURL）**：

```bash
curl -X GET 'http://localhost:8080/api/v1/admin/pets?name=小黄&pageNum=1&pageSize=10' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...'
```

**成功响应示例**：分页返回 PetInfo 列表。

#### 8.2.2 宠物详情

| 项 | 值 |
|----|----|
| 接口路径 | `GET /admin/pets/{petId}` |
| 是否登录 | 是 |
| 权限标识 | `admin` |

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| petId | long | 是 | 宠物ID |

**成功响应示例**：PetInfo 对象（含疫苗记录列表）。

#### 8.2.3 删除宠物（违规处理）

| 项 | 值 |
|----|----|
| 接口路径 | `DELETE /admin/pets/{petId}` |
| 是否登录 | 是 |
| 权限标识 | `admin` |

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| petId | long | 是 | 宠物ID |

**请求示例（cURL）**：

```bash
curl -X DELETE 'http://localhost:8080/api/v1/admin/pets/1' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...'
```

---

### 8.3 动态管理

#### 8.3.1 动态列表

| 项 | 值 |
|----|----|
| 接口路径 | `GET /admin/posts` |
| 是否登录 | 是 |
| 权限标识 | `admin` |

**查询参数（Query）**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| status | string | 否 | 审核状态（0-待审核 1-通过 2-拒绝） |
| userId | long | 否 | 发布用户ID |
| beginTime | string | 否 | 起始时间（yyyy-MM-dd） |
| endTime | string | 否 | 结束时间（yyyy-MM-dd） |
| pageNum | int | 否 | 当前页码（默认1） |
| pageSize | int | 否 | 每页条数（默认10） |

**请求示例（cURL）**：

```bash
curl -X GET 'http://localhost:8080/api/v1/admin/posts?status=0&beginTime=2026-07-01&endTime=2026-07-31' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...'
```

#### 8.3.2 动态详情

| 项 | 值 |
|----|----|
| 接口路径 | `GET /admin/posts/{postId}` |
| 是否登录 | 是 |
| 权限标识 | `admin` |

**路径参数**：petId → postId

#### 8.3.3 审核动态

**接口说明**：审核动态（通过/拒绝）。审核通过时，如为首次审核通过，给发布用户增加10积分。

| 项 | 值 |
|----|----|
| 接口路径 | `PUT /admin/posts/{postId}/audit` |
| 是否登录 | 是 |
| 权限标识 | `admin` |

**请求体（Body）**：

```json
{
  "status": "1"   // 审核状态 1-通过 2-拒绝
}
```

**请求示例（cURL）**：

```bash
curl -X PUT 'http://localhost:8080/api/v1/admin/posts/1/audit' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...' \
  -d '{
    "status": "1"
  }'
```

**失败响应示例**：

```json
{
  "code": 500,
  "msg": "审核状态非法，仅支持 1-通过 2-拒绝"
}
```

#### 8.3.4 删除动态

| 项 | 值 |
|----|----|
| 接口路径 | `DELETE /admin/posts/{postId}` |
| 是否登录 | 是 |
| 权限标识 | `admin` |

---

### 8.4 商品分类管理

#### 8.4.1 分类树列表

| 项 | 值 |
|----|----|
| 接口路径 | `GET /admin/categories` |
| 是否登录 | 是 |
| 权限标识 | `admin` |

**成功响应示例**：返回完整分类树（含停用和正常状态）。

#### 8.4.2 分类详情

| 项 | 值 |
|----|----|
| 接口路径 | `GET /admin/categories/{categoryId}` |
| 是否登录 | 是 |
| 权限标识 | `admin` |

#### 8.4.3 新增分类

| 项 | 值 |
|----|----|
| 接口路径 | `POST /admin/categories` |
| 是否登录 | 是 |
| 权限标识 | `admin` |

**请求体（Body）**：

```json
{
  "parentId": 0,           // 父分类ID（默认0，即顶级分类）
  "categoryName": "玩具",   // 分类名称（必填）
  "icon": "https://.../toy.png",   // 分类图标（可选）
  "sortOrder": 1,          // 排序（可选，数字越小越靠前）
  "status": "1"            // 状态 0-停用 1-正常（可选，默认1）
}
```

#### 8.4.4 编辑分类

| 项 | 值 |
|----|----|
| 接口路径 | `PUT /admin/categories/{categoryId}` |
| 是否登录 | 是 |
| 权限标识 | `admin` |

**失败响应示例**：

```json
{
  "code": 500,
  "msg": "上级分类不能选择自身"
}
```

#### 8.4.5 删除分类

| 项 | 值 |
|----|----|
| 接口路径 | `DELETE /admin/categories/{categoryId}` |
| 是否登录 | 是 |
| 权限标识 | `admin` |

---

### 8.5 商品管理

#### 8.5.1 商品列表

| 项 | 值 |
|----|----|
| 接口路径 | `GET /admin/products` |
| 是否登录 | 是 |
| 权限标识 | `admin` |

**查询参数（Query）**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| categoryId | long | 否 | 分类ID |
| keyword | string | 否 | 搜索关键词（匹配商品名称） |
| pageNum | int | 否 | 当前页码（默认1） |
| pageSize | int | 否 | 每页条数（默认10） |

#### 8.5.2 商品详情

| 项 | 值 |
|----|----|
| 接口路径 | `GET /admin/products/{productId}` |
| 是否登录 | 是 |
| 权限标识 | `admin` |

#### 8.5.3 新增商品

| 项 | 值 |
|----|----|
| 接口路径 | `POST /admin/products` |
| 是否登录 | 是 |
| 权限标识 | `admin` |

**请求体（Body）**：

```json
{
  "categoryId": 2,                            // 分类ID（必填）
  "productName": "金毛幼犬专用粮",             // 商品名称（必填）
  "productImages": "[\"url1\",\"url2\"]",     // 商品图片（JSON数组字符串，可选）
  "description": "专为金毛幼犬配方，营养均衡",  // 商品描述（可选）
  "pointsPrice": 100,                         // 积分价格（必填）
  "stock": 500,                               // 库存（必填）
  "totalExchange": 0,                         // 总兑换数（可选，默认0）
  "status": "1"                               // 状态 0-下架 1-上架（可选，默认1）
}
```

#### 8.5.4 编辑商品

| 项 | 值 |
|----|----|
| 接口路径 | `PUT /admin/products/{productId}` |
| 是否登录 | 是 |
| 权限标识 | `admin` |

**请求体（Body）**：同 7.5.3，id 字段会被忽略。

#### 8.5.5 删除商品

| 项 | 值 |
|----|----|
| 接口路径 | `DELETE /admin/products/{productId}` |
| 是否登录 | 是 |
| 权限标识 | `admin` |

#### 8.5.6 商品上下架

| 项 | 值 |
|----|----|
| 接口路径 | `PUT /admin/products/{productId}/status` |
| 是否登录 | 是 |
| 权限标识 | `admin` |

**请求体（Body）**：

```json
{
  "status": "1"   // 0-下架 1-上架
}
```

**失败响应示例**：

```json
{
  "code": 500,
  "msg": "商品状态非法，仅支持 0-下架 1-上架"
}
```

---

### 8.6 订单管理

#### 8.6.1 订单列表

| 项 | 值 |
|----|----|
| 接口路径 | `GET /admin/orders` |
| 是否登录 | 是 |
| 权限标识 | `admin` |

**查询参数（Query）**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orderNo | string | 否 | 订单编号（模糊搜索） |
| status | string | 否 | 订单状态（0-待发货 1-已发货 2-已完成 3-已取消） |
| pageNum | int | 否 | 当前页码（默认1） |
| pageSize | int | 否 | 每页条数（默认10） |

#### 8.6.2 导出订单列表

**接口说明**：按条件导出订单数据为 Excel 文件。

| 项 | 值 |
|----|----|
| 接口路径 | `POST /admin/orders/export` |
| 是否登录 | 是 |
| 权限标识 | `admin` |
| Content-Type | `application/x-www-form-urlencoded` |

**查询参数（Query / Form）**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orderNo | string | 否 | 订单编号（模糊搜索） |
| status | string | 否 | 订单状态（0-待发货 1-已发货 2-已完成 3-已取消） |

**请求示例（cURL）**：

```bash
curl -X POST 'http://localhost:8080/api/v1/admin/orders/export?status=0' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...' \
  -o orders.xlsx
```

**成功响应**：直接返回 Excel 文件流下载。

#### 8.6.3 订单详情

**接口说明**：根据订单ID获取订单基本信息（仅订单表数据）。

| 项 | 值 |
|----|----|
| 接口路径 | `GET /admin/orders/{orderId}` |
| 是否登录 | 是 |
| 权限标识 | `admin` |

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orderId | long | 是 | 订单ID |

**请求示例（cURL）**：

```bash
curl -X GET 'http://localhost:8080/api/v1/admin/orders/1' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...'
```

**成功响应示例**：同 5.5 的 rows 单条对象（仅订单字段，不含用户和地址详情）。

> 如需同时获取关联的用户信息和收货地址信息，请使用 [7.6.5 订单详情（含用户和地址）](#765-订单详情含用户和地址)。

#### 8.6.4 更新订单状态

**接口说明**：更新订单状态（发货等）。发货时需填写快递单号和快递公司。

| 项 | 值 |
|----|----|
| 接口路径 | `PUT /admin/orders/{orderId}/status` |
| 是否登录 | 是 |
| 权限标识 | `admin` |

**请求体（Body）**：

```json
{
  "status": "1",                  // 订单状态（必填：0-待发货 1-已发货 2-已完成 3-已取消）
  "expressNo": "SF1234567890",    // 快递单号（发货时必填）
  "expressCompany": "顺丰速运"     // 快递公司（发货时必填）
}
```

**请求示例（cURL）**：

```bash
curl -X PUT 'http://localhost:8080/api/v1/admin/orders/1/status' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...' \
  -d '{
    "status": "1",
    "expressNo": "SF1234567890",
    "expressCompany": "顺丰速运"
  }'
```

**失败响应示例**：

```json
{
  "code": 500,
  "msg": "发货时请填写快递单号和快递公司"
}
```

#### 8.6.5 订单详情（含用户和地址）

**接口说明**：根据订单ID获取订单详情，返回订单信息以及关联的下单用户信息和收货地址信息（三表关联查询）。适用于后台发货、客服等需要完整订单上下文的场景。

| 项 | 值 |
|----|----|
| 接口路径 | `GET /admin/orders/{orderId}/detail` |
| 是否登录 | 是 |
| 权限标识 | `admin` |

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orderId | long | 是 | 订单ID |

**请求示例（cURL）**：

```bash
curl -X GET 'http://localhost:8080/api/v1/admin/orders/1/detail' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...'
```

**成功响应示例**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "id": 1,                              // 订单ID
    "orderNo": "17221000000001234",       // 订单编号
    "userId": 1,                          // 用户ID
    "productId": 1,                       // 商品ID
    "productName": "金毛幼犬专用粮",       // 商品名称（快照）
    "productImage": "https://.../img.jpg",// 商品图片（快照）
    "pointsPrice": 100,                   // 兑换积分（单价）
    "quantity": 2,                        // 兑换数量
    "totalPoints": 200,                   // 总积分
    "addressId": 1,                       // 收货地址ID
    "status": "0",                        // 订单状态 0-待发货 1-已发货 2-已完成 3-已取消
    "expressNo": null,                    // 快递单号
    "expressCompany": null,               // 快递公司
    "createTime": "2026-07-28 10:00:00",
    "updateTime": "2026-07-28 10:00:00",
    "user": {                             // 关联用户信息
      "userId": 1,                        // 用户ID
      "nickName": "张三",                 // 昵称
      "avatar": "https://.../avatar.jpg", // 头像
      "phonenumber": "13800138000"        // 手机号
    },
    "address": {                          // 关联收货地址信息
      "id": 1,                            // 地址ID
      "userId": 1,                        // 用户ID
      "receiverName": "张三",             // 收件人
      "receiverPhone": "13800138000",     // 联系电话
      "province": "浙江省",               // 省份
      "city": "杭州市",                   // 城市
      "district": "西湖区",               // 区县
      "detailAddress": "文三路123号",     // 详细地址
      "isDefault": "1",                   // 是否默认 0-否 1-是
      "createTime": "2026-01-01 10:00:00"
    }
  }
}
```

**失败响应示例**：

```json
{
  "code": 500,
  "msg": "订单不存在"
}
```

---

### 8.7 轮播图管理

#### 8.7.1 分页查询轮播图列表

**接口说明**：后台分页查询所有轮播图，支持标题模糊搜索、状态筛选。按排序号升序、ID降序排列。

| 项 | 值 |
|----|----|
| 接口路径 | `GET /admin/banner/list` |
| 是否登录 | 是 |
| 权限标识 | `banner:list` |

**查询参数（Query）**：

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| title | string | 否 | - | 标题（模糊搜索） |
| status | string | 否 | - | 状态（0-停用 1-启用） |
| pageNum | int | 否 | 1 | 当前页码 |
| pageSize | int | 否 | 10 | 每页条数，最大100 |

**请求示例（cURL）**：

```bash
curl -X GET 'http://localhost:8080/api/v1/admin/banner/list?status=1&pageNum=1&pageSize=10' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...'
```

**成功响应示例**：

```json
{
  "code": 200,
  "msg": "查询成功",
  "rows": [
    {
      "id": 1,
      "title": "宠物健康指南",
      "imageUrl": "/static/banner/banner1.jpg",
      "jumpType": "none",
      "jumpTarget": null,
      "sortOrder": 1,
      "status": "1",
      "startTime": null,
      "endTime": null,
      "remark": null,
      "createBy": "admin",
      "createTime": "2026-08-01 10:00:00",
      "updateBy": null,
      "updateTime": "2026-08-01 10:00:00"
    }
  ],
  "total": 3
}
```

---

#### 8.7.2 获取轮播图详情

**接口说明**：根据轮播图ID获取详细信息。

| 项 | 值 |
|----|----|
| 接口路径 | `GET /admin/banner/{id}` |
| 是否登录 | 是 |
| 权限标识 | `banner:list` |

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | long | 是 | 轮播图ID |

**请求示例（cURL）**：

```bash
curl -X GET 'http://localhost:8080/api/v1/admin/banner/1' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...'
```

**成功响应示例**：同轮播图列表 rows 单条对象。

**失败响应示例**：

```json
{
  "code": 500,
  "msg": "轮播图不存在"
}
```

---

#### 8.7.3 新增轮播图

**接口说明**：新增轮播图。sortOrder 为空时自动取当前最大值+1，status 为空时默认启用（'1'），createBy 自动取当前登录用户。

| 项 | 值 |
|----|----|
| 接口路径 | `POST /admin/banner/` |
| 是否登录 | 是 |
| 权限标识 | `banner:list` |

**请求体（Body）**：

```json
{
  "title": "双十一大促活动",              // 标题（必填）
  "imageUrl": "/static/banner/1111.jpg",   // 图片地址（必填，通过公共上传接口获取）
  "jumpType": "product",                   // 跳转类型（必填，none/post/product/url/miniapp）
  "jumpTarget": "100",                     // 跳转目标（根据jump_type填写，可选）
  "sortOrder": 1,                          // 排序号（可选，默认当前最大值+1）
  "status": "1",                           // 状态（可选，0-停用 1-启用，默认1）
  "startTime": "2026-11-01 00:00:00",      // 展示开始时间（可选，null为不限）
  "endTime": "2026-11-11 23:59:59",        // 展示结束时间（可选，null为不限）
  "remark": "双十一限时活动，多款商品5折"   // 备注（可选）
}
```

**请求示例（cURL）**：

```bash
curl -X POST 'http://localhost:8080/api/v1/admin/banner/' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...' \
  -d '{
    "title": "双十一大促活动",
    "imageUrl": "/static/banner/1111.jpg",
    "jumpType": "product",
    "jumpTarget": "100",
    "status": "1",
    "startTime": "2026-11-01 00:00:00",
    "endTime": "2026-11-11 23:59:59"
  }'
```

**成功响应示例**：

```json
{
  "code": 200,
  "msg": "新增成功"
}
```

**失败响应示例**（参数校验失败）：

```json
{
  "code": 400,
  "msg": "标题不能为空"
}
```

---

#### 8.7.4 编辑轮播图

**接口说明**：编辑轮播图信息。updateBy 自动取当前登录用户。

| 项 | 值 |
|----|----|
| 接口路径 | `PUT /admin/banner/{id}` |
| 是否登录 | 是 |
| 权限标识 | `banner:list` |

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | long | 是 | 轮播图ID |

**请求体（Body）**：同 8.7.3，id 字段忽略（以路径参数为准）。

**请求示例（cURL）**：

```bash
curl -X PUT 'http://localhost:8080/api/v1/admin/banner/1' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...' \
  -d '{
    "title": "宠物健康指南（更新版）",
    "imageUrl": "/static/banner/banner1_v2.jpg",
    "jumpType": "none"
  }'
```

**成功响应示例**：

```json
{
  "code": 200,
  "msg": "编辑成功"
}
```

---

#### 8.7.5 删除轮播图

**接口说明**：删除轮播图（逻辑删除，del_flag 置为 '1'，不物理删除数据）。

| 项 | 值 |
|----|----|
| 接口路径 | `DELETE /admin/banner/{id}` |
| 是否登录 | 是 |
| 权限标识 | `banner:list` |

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | long | 是 | 轮播图ID |

**请求示例（cURL）**：

```bash
curl -X DELETE 'http://localhost:8080/api/v1/admin/banner/1' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...'
```

**成功响应示例**：

```json
{
  "code": 200,
  "msg": "删除成功"
}
```

---

### 8.8 AI搜索推荐词管理

> 管理AI对话页的搜索提示词库，支持按宠物类型、分类维护，并配置权重与启停状态。

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 推荐词列表 | GET | `/api/v1/admin/suggest-words/list` | 分页查询推荐词 |
| 推荐词详情 | GET | `/api/v1/admin/suggest-words/{id}` | 获取推荐词详情 |
| 新增推荐词 | POST | `/api/v1/admin/suggest-words` | 新增推荐词 |
| 编辑推荐词 | PUT | `/api/v1/admin/suggest-words/{id}` | 编辑推荐词 |
| 删除推荐词 | DELETE | `/api/v1/admin/suggest-words/{id}` | 逻辑删除推荐词 |

---

#### 8.8.1 推荐词列表

**接口说明**：分页查询推荐词列表，支持按提示词内容、宠物类型、分类、状态筛选，按权重降序排列。

| 项 | 值 |
|----|----|
| 接口路径 | `GET /admin/suggest-words/list` |
| 是否登录 | 是 |
| 权限标识 | `admin` |

**查询参数（Query）**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| word | string | 否 | 提示词内容（模糊查询） |
| petType | string | 否 | 宠物类型（general/cat/dog/rabbit/bird/fish/other） |
| category | string | 否 | 分类（general/care/diet/medical/behavior/training） |
| status | string | 否 | 状态（0-停用 1-启用） |
| pageNum | int | 否 | 页码，默认1 |
| pageSize | int | 否 | 每页条数，默认10 |

**请求示例（cURL）**：

```bash
curl -X GET 'http://localhost:8080/api/v1/admin/suggest-words/list?petType=cat&pageNum=1&pageSize=10' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...'
```

**成功响应示例**：

```json
{
  "code": 200,
  "msg": "查询成功",
  "rows": [
    {
      "id": 9,
      "word": "猫咪不吃饭怎么办？",
      "petType": "cat",
      "category": "diet",
      "weight": 100,
      "status": "1",
      "delFlag": "0",
      "createTime": "2026-08-20 10:00:00",
      "updateTime": "2026-08-20 10:00:00"
    }
  ],
  "total": 10
}
```

---

#### 8.8.2 推荐词详情

**接口说明**：根据ID获取推荐词详情。

| 项 | 值 |
|----|----|
| 接口路径 | `GET /admin/suggest-words/{id}` |
| 是否登录 | 是 |
| 权限标识 | `admin` |

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | long | 是 | 推荐词ID |

**请求示例（cURL）**：

```bash
curl -X GET 'http://localhost:8080/api/v1/admin/suggest-words/9' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...'
```

**成功响应示例**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "id": 9,
    "word": "猫咪不吃饭怎么办？",
    "petType": "cat",
    "category": "diet",
    "weight": 100,
    "status": "1",
    "delFlag": "0",
    "createTime": "2026-08-20 10:00:00",
    "updateTime": "2026-08-20 10:00:00"
  }
}
```

---

#### 8.8.3 新增推荐词

**接口说明**：新增一条AI搜索推荐词。

| 项 | 值 |
|----|----|
| 接口路径 | `POST /admin/suggest-words` |
| 是否登录 | 是 |
| 权限标识 | `admin` |

**请求体（Body）**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| word | string | 是 | 提示词内容 |
| petType | string | 是 | 宠物类型（general/cat/dog/rabbit/bird/fish/other） |
| category | string | 否 | 分类，默认 general（general/care/diet/medical/behavior/training） |
| weight | int | 否 | 基础权重，默认100（数字越大越靠前） |
| status | string | 否 | 状态，默认1（0-停用 1-启用） |

**请求示例（cURL）**：

```bash
curl -X POST 'http://localhost:8080/api/v1/admin/suggest-words' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...' \
  -H 'Content-Type: application/json' \
  -d '{
    "word": "猫咪挑食怎么办？",
    "petType": "cat",
    "category": "diet",
    "weight": 90,
    "status": "1"
  }'
```

**成功响应示例**：

```json
{
  "code": 200,
  "msg": "新增成功"
}
```

---

#### 8.8.4 编辑推荐词

**接口说明**：根据ID编辑推荐词信息。

| 项 | 值 |
|----|----|
| 接口路径 | `PUT /admin/suggest-words/{id}` |
| 是否登录 | 是 |
| 权限标识 | `admin` |

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | long | 是 | 推荐词ID |

**请求体（Body）**：同 [8.8.3 新增推荐词](#883-新增推荐词)

**请求示例（cURL）**：

```bash
curl -X PUT 'http://localhost:8080/api/v1/admin/suggest-words/9' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...' \
  -H 'Content-Type: application/json' \
  -d '{
    "word": "猫咪挑食怎么办？",
    "petType": "cat",
    "category": "diet",
    "weight": 95,
    "status": "1"
  }'
```

**成功响应示例**：

```json
{
  "code": 200,
  "msg": "编辑成功"
}
```

---

#### 8.8.5 删除推荐词

**接口说明**：逻辑删除推荐词（del_flag 置为 '1'，不物理删除数据）。

| 项 | 值 |
|----|----|
| 接口路径 | `DELETE /admin/suggest-words/{id}` |
| 是否登录 | 是 |
| 权限标识 | `admin` |

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | long | 是 | 推荐词ID |

**请求示例（cURL）**：

```bash
curl -X DELETE 'http://localhost:8080/api/v1/admin/suggest-words/9' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...'
```

**成功响应示例**：

```json
{
  "code": 200,
  "msg": "删除成功"
}
```

---

## 九、AI对话模块

> 模块路径前缀：`/api/v1/ai`
> 本模块基于 Server-Sent Events（SSE）实现流式 AI 对话，对接 Node.js + LangChain AI 服务，并将对话记录持久化到数据库，支持多轮对话记忆。
> 所有接口均需登录认证。

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| AI流式对话 | POST | `/api/v1/ai/chat` | 流式返回 AI 回答，保留多轮记忆 |
| 清空对话记忆 | DELETE | `/api/v1/ai/chat/history/{sessionId}` | 清空指定会话的历史记录 |
| 获取会话列表 | GET | `/api/v1/ai/sessions` | 获取当前用户的所有对话会话 |
| 获取会话消息记录 | GET | `/api/v1/ai/chat/history/{sessionId}` | 获取指定会话的完整消息记录 |
| 获取AI搜索推荐词 | GET | `/api/v1/ai/suggest-words` | 根据用户宠物信息智能推荐搜索提示词 |

---

### 9.1 AI流式对话（SSE）

**接口说明**：发起 AI 对话，通过 SSE 流式返回 AI 回答。每轮对话会保存到数据库，并基于 `sessionId` 维持多轮上下文记忆。

| 项 | 值 |
|----|----|
| 请求方法 | `POST` |
| 路径 | `/api/v1/ai/chat` |
| Content-Type | `application/json` |
| Accept | `text/event-stream` |
| 需要登录 | 是 |

**请求体参数**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| message | string | 是 | 用户本轮输入内容 |
| sessionId | string | 否 | 会话 ID（格式 `pet-{id}`），用于多轮记忆隔离。不传时服务端自动创建新会话并在响应中返回 `sessionId` |

**请求示例**：

```bash
curl -N -X POST http://localhost:8080/api/v1/ai/chat \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -H "Authorization: Bearer {token}" \
  -d '{"message":"我家猫咪三个月大，该打什么疫苗？"}'
```

> `-N` 表示不使用缓冲，立即输出流式内容。

**响应**：

- Content-Type: `text/event-stream`
- Cache-Control: `no-cache`
- Connection: `keep-alive`

响应为 SSE 流，由多段 `data:` 行组成，每个事件之间用空行 `\n\n` 分隔。

**事件类型**：

每个 `data:` 载荷均为 JSON，按 `type` 字段区分：

| type | 出现次数 | 说明 | 载荷字段 |
|------|---------|------|----------|
| `chunk` | 多次（每个 token 一段） | AI 流式输出的增量内容 | `content`(string), `sessionId`(string) |
| `complete` | 1 次（成功时） | 整轮对话结束的完整结果 | `data`({success, reply, sessionId}), `sessionId` |
| `error` | 1 次（失败时） | 错误信息 | `message`(string), `sessionId` |

正常结束时还会发送一个 SSE 结束事件：

```
event:end
data:{"done":true}
```

**响应示例（流式）**：

```
data: {"type":"chunk","content":"三个月","sessionId":"pet-1"}

data: {"type":"chunk","content":"的","sessionId":"pet-1"}

data: {"type":"chunk","content":"猫咪","sessionId":"pet-1"}

...

data: {"type":"complete","data":{"success":true,"reply":"三个月的猫咪...（完整回复）","sessionId":"pet-1"},"sessionId":"pet-1"}

event:end
data:{"done":true}
```

**参数校验错误**（未传 `message`）返回普通 JSON，状态码 400：

```json
{
  "code": 400,
  "msg": "请提供对话消息"
}
```

---

### 9.2 清空对话记忆

**接口说明**：清空指定会话的对话记忆，同时清理 Node.js AI 服务的内存历史与数据库中的消息记录。

| 项 | 值 |
|----|----|
| 请求方法 | `DELETE` |
| 路径 | `/api/v1/ai/chat/history/{sessionId}` |
| 需要登录 | 是 |

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| sessionId | string | 是 | 会话 ID（格式 `pet-{id}`） |

**请求示例**：

```bash
curl -X DELETE http://localhost:8080/api/v1/ai/chat/history/pet-1 \
  -H "Authorization: Bearer {token}"
```

**响应示例**：

```json
{
  "code": 200,
  "msg": "对话记忆已清空"
}
```

---

### 9.3 获取会话列表

**接口说明**：获取当前登录用户的所有 AI 对话会话列表，按创建时间倒序排列。

| 项 | 值 |
|----|----|
| 请求方法 | `GET` |
| 路径 | `/api/v1/ai/sessions` |
| 需要登录 | 是 |

**请求示例**：

```bash
curl http://localhost:8080/api/v1/ai/sessions \
  -H "Authorization: Bearer {token}"
```

**响应示例**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "id": 1,
      "userId": 100,
      "sessionTitle": "我家猫咪三个月大，该打什么疫苗？",
      "sessionType": "general",
      "model": "deepseek-chat",
      "messageCount": 4,
      "status": "1",
      "createTime": "2026-08-19 15:30:00",
      "updateTime": "2026-08-19 15:31:00",
      "nickName": "宠物爱好者"
    }
  ]
}
```

---

### 9.4 获取会话消息记录

**接口说明**：获取指定会话的完整消息记录（包含用户消息和 AI 回复），按时间正序排列。用于前端展示历史对话。

| 项 | 值 |
|----|----|
| 请求方法 | `GET` |
| 路径 | `/api/v1/ai/chat/history/{sessionId}` |
| 需要登录 | 是 |

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| sessionId | string | 是 | 会话 ID（格式 `pet-{id}`） |

**请求示例**：

```bash
curl http://localhost:8080/api/v1/ai/chat/history/pet-1 \
  -H "Authorization: Bearer {token}"
```

**响应示例**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "id": 1,
      "sessionId": 1,
      "userId": 100,
      "role": "user",
      "content": "我家猫咪三个月大，该打什么疫苗？",
      "contentType": "text",
      "status": "1",
      "createTime": "2026-08-19 15:30:00"
    },
    {
      "id": 2,
      "sessionId": 1,
      "userId": 100,
      "role": "assistant",
      "content": "三个月的猫咪建议接种猫三联疫苗...",
      "contentType": "text",
      "model": "deepseek-chat",
      "status": "1",
      "createTime": "2026-08-19 15:30:05"
    }
  ]
}
```

---

### 9.5 获取AI搜索推荐词

**接口说明**：用户进入 AI 对话页面时调用，系统根据当前用户已添加的宠物信息智能推荐搜索提示词。
- 用户养猫 → 推荐猫相关提示词
- 用户养狗 → 推荐狗相关提示词
- 用户同时养猫和狗 → 两类提示词都返回，按宠物数量比例分配
- 用户养多只同类型宠物 → 该类型提示词权重提高
- 用户未添加宠物 → 返回通用热门提示词

每次返回顺序会叠加随机扰动，避免完全一致。

| 项 | 值 |
|----|----|
| 请求方法 | `GET` |
| 路径 | `/api/v1/ai/suggest-words` |
| 需要登录 | 是 |

**查询参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| limit | int | 否 | 返回数量，默认 4 条 |

**请求示例**：

```bash
curl -X GET "http://localhost:8080/api/v1/ai/suggest-words?limit=4" \
  -H "Authorization: Bearer {token}"
```

**响应示例**（养猫+养狗的用户）：

```json
{
  "code": 200,
  "msg": "获取成功",
  "data": [
    {
      "id": 9,
      "word": "猫咪不吃饭怎么办？",
      "petType": "cat",
      "category": "diet",
      "score": 100
    },
    {
      "id": 19,
      "word": "狗狗挑食怎么办？",
      "petType": "dog",
      "category": "diet",
      "score": 100
    },
    {
      "id": 10,
      "word": "猫咪多久洗一次澡？",
      "petType": "cat",
      "category": "care",
      "score": 95
    },
    {
      "id": 1,
      "word": "宠物多久做一次体检？",
      "petType": "general",
      "category": "medical",
      "score": 80
    }
  ]
}
```

**字段说明**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 推荐词主键 ID |
| word | string | 提示词内容 |
| petType | string | 宠物类型（general/cat/dog/rabbit/bird/fish/other） |
| category | string | 分类（general/care/diet/medical/behavior/training） |
| score | int | 推荐得分，用于排序（数字越大越靠前） |

**推荐算法说明**：

1. 查询用户全部宠物，按 `breed` 字段映射到宠物类型并统计数量分布。
2. 名额分配：通用类型固定 2 条，每种宠物类型保底 1 条，剩余按宠物数量比例分配，零头归给宠物最多的类型。
3. 从词库中按 `weight` 降序取词，最终得分 = 基础权重 × 类型匹配系数 × 随机扰动（0.95~1.05）。
4. 通用类型匹配系数为 0.8，命中用户宠物类型为 1.0，按得分降序返回。

---

### 9.6 多轮记忆机制说明

- 服务端以 `sessionId`（格式 `pet-{数据库会话ID}`）为标识，在 Node.js AI 服务的内存中维护对话历史。
- 每轮对话时，Node.js 会把历史消息连同本轮输入一起发送给 LLM，实现上下文记忆。
- 默认保留最近 20 条消息，超过则自动淘汰最早的消息，避免 token 超限。
- 所有对话记录同步持久化到数据库（`ai_chat_session` 和 `ai_chat_message` 表），服务重启后可通过消息记录接口恢复历史。
- **会话 ID 生成规则**：首次对话不传 `sessionId`，服务端创建数据库会话记录后生成 `pet-{id}` 格式的 sessionId，通过 SSE 响应回传给前端；后续对话前端需带上此 `sessionId` 以维持多轮记忆。

---

## 十、公共模块

> 模块路径前缀：`/common`（无 `/api/v1` 前缀）
> 本模块包含文件上传、下载等通用功能。

---

### 10.1 单文件上传

**接口说明**：上传单个文件，返回访问 URL。支持图片、文档等常见格式。

| 项 | 值 |
|----|----|
| 接口路径 | `POST /common/upload` |
| 是否登录 | 是（RuoYi默认开启认证） |
| Content-Type | `multipart/form-data` |

**请求参数（Form Data）**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | file | 是 | 要上传的文件 |

**请求示例（cURL）**：

```bash
curl -X POST 'http://localhost:8080/common/upload' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...' \
  -F 'file=@/Users/xxx/Pictures/pet.jpg'
```

**成功响应示例**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "url": "http://localhost:8080/profile/upload/2026/07/31/abc123.jpg",  // 完整访问URL
  "fileName": "/profile/upload/2026/07/31/abc123.jpg",                    // 相对路径
  "newFileName": "abc123.jpg",                                             // 新文件名
  "originalFilename": "pet.jpg"                                             // 原始文件名
}
```

---

### 10.2 多文件上传

**接口说明**：一次上传多个文件，返回以逗号分隔的 URL 列表。

| 项 | 值 |
|----|----|
| 接口路径 | `POST /common/uploads` |
| 是否登录 | 是 |
| Content-Type | `multipart/form-data` |

**请求参数（Form Data）**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| files | file[] | 是 | 多个文件 |

**请求示例（cURL）**：

```bash
curl -X POST 'http://localhost:8080/common/uploads' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...' \
  -F 'files=@/Users/xxx/Pictures/img1.jpg' \
  -F 'files=@/Users/xxx/Pictures/img2.jpg'
```

**成功响应示例**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "urls": "http://localhost:8080/profile/upload/xxx1.jpg,http://localhost:8080/profile/upload/xxx2.jpg",
  "fileNames": "/profile/upload/xxx1.jpg,/profile/upload/xxx2.jpg",
  "newFileNames": "xxx1.jpg,xxx2.jpg",
  "originalFilenames": "img1.jpg,img2.jpg"
}
```

---

### 10.3 通用下载

**接口说明**：下载服务器生成的文件（如导出报表）。

| 项 | 值 |
|----|----|
| 接口路径 | `GET /common/download` |
| 是否登录 | 是 |

**查询参数（Query）**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| fileName | string | 是 | 下载目录中的文件名（含 `_` 分隔前缀） |
| delete | boolean | 否 | 下载后是否删除源文件（默认false） |

**请求示例（cURL）**：

```bash
curl -X GET 'http://localhost:8080/common/download?fileName=订单数据_1722400000.xlsx&delete=true' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...' \
  -O
```

**成功响应**：直接返回文件流。

---

### 10.4 本地资源下载

**接口说明**：下载存储在本地资源路径下的文件（如数据库中存储的 `/profile/...` 路径）。

| 项 | 值 |
|----|----|
| 接口路径 | `GET /common/download/resource` |
| 是否登录 | 是 |

**查询参数（Query）**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| resource | string | 是 | 资源路径（如 `/profile/upload/2026/07/31/xxx.jpg`） |

**请求示例（cURL）**：

```bash
curl -X GET 'http://localhost:8080/common/download/resource?resource=/profile/upload/2026/07/31/xxx.jpg' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...' \
  -O
```

---

## 十一、公共数据结构

### 11.1 通用响应对象（AjaxResult）

| 字段 | 类型 | 说明 |
|------|------|------|
| code | int | 状态码，200表示成功 |
| msg | string | 提示信息 |
| data | object/array/null | 响应数据 |
| *（扩展字段） | any | 如 token、orderNo、liked 等，按接口文档返回 |

### 11.2 分页响应对象（TableDataInfo）

| 字段 | 类型 | 说明 |
|------|------|------|
| code | int | 状态码，200表示成功 |
| msg | string | 提示信息 |
| rows | array | 数据列表 |
| total | long | 总记录数 |

### 11.3 业务状态码枚举

| 类别 | 字段 | 值 | 说明 |
|------|------|----|------|
| 逻辑删除 | delFlag | 0/1 | 0-存在 1-删除 |
| 性别 | gender | 0/1 | 0-母 1-公（宠物） / 0-未知 1-男 2-女（用户） |
| 绝育状态 | sterilization | 0/1 | 0-未绝育 1-已绝育 |
| 动态审核状态 | status | 0/1/2 | 0-待审核 1-通过 2-拒绝 |
| 商品状态 | status | 0/1 | 0-下架 1-上架 |
| 分类状态 | status | 0/1 | 0-停用 1-正常 |
| 轮播图状态 | status | 0/1 | 0-停用 1-启用 |
| 订单状态 | status | 0/1/2/3 | 0-待发货 1-已发货 2-已完成 3-已取消 |
| 轮播图跳转类型 | jumpType | string | none-不跳转 post-动态详情 product-商品详情 url-外部链接 miniapp-小程序页面 |
| 积分变动类型 | changeType | string | sign_in-签到 post-发布动态 like-点赞 comment-评论 pet_complete-完善宠物 exchange-兑换商品 admin-管理员操作 |
| 默认地址 | isDefault | 0/1 | 0-否 1-是 |
| 登录类型 | loginType | string | wx-微信登录 phone-手机号注册登录 |

### 11.4 积分规则一览

| 行为 | 变动类型 | 积分变动 | 限制 |
|------|----------|----------|------|
| 每日签到 | sign_in | +5 | 每天一次 |
| 发布动态（审核通过） | post | +10 | 每条动态仅发放一次 |
| 动态被点赞 | like | +1 | 每日上限20分 |
| 动态被评论 | comment | +2 | 每日上限20分 |
| 首次完善宠物信息 | pet_complete | +20 | 每只宠物仅首次 |
| 兑换商品 | exchange | -对应积分 | 需 ≥ 商品积分价格 |
| 管理员操作 | admin | ±N/A | 无限制 |

### 11.5 JSON类型字段说明

以下字段在数据库中为 JSON 类型，在接口中以 **字符串** 形式传输：

| 实体 | 字段 | 格式说明 |
|------|------|----------|
| PetPost | images | JSON数组字符串，如 `["url1","url2"]` |
| ShopProduct | productImages | JSON数组字符串，如 `["url1","url2"]` |

### 11.6 登录方式返回 Token 对比

| 登录接口 | Token 返回位置 | 附加字段 |
|----------|----------------|----------|
| POST /register | data.token | userId、nickname、phone、points |
| POST /user/uertlogin | 顶层 token 键 | - |
| POST /wx/login | data.token | userId、nickName、avatar、phonenumber、points、isNewUser |

---

## 十二、错误码说明

### 12.1 HTTP 状态码

| 状态码 | 说明 | 触发场景 |
|--------|------|----------|
| 200 | 操作成功 | 接口正常返回 |
| 400 | 请求参数错误 | 参数校验失败、格式错误 |
| 401 | 未授权/登录已过期 | Token无效、缺失或过期 |
| 403 | 权限不足 | 无接口访问权限（如非管理员访问/admin） |
| 404 | 资源不存在 | 请求路径错误 |
| 500 | 业务异常 | 业务逻辑错误（具体看 msg） |

### 12.2 常见业务异常消息

| 错误消息（msg） | 说明 | 常见触发接口 |
|------------------|------|--------------|
| 手机号或密码不能为空 | 登录时缺少手机号或密码 | /user/uertlogin |
| 用户不存在 | 查询不到对应用户 | /user/uertlogin、/admin/users/{id}/points |
| 用户密码不匹配 | 密码错误 | /user/uertlogin |
| 宠物不存在 | petId 无效或已删除 | /pets/{petId}、/admin/pets/{petId} |
| 无权操作他人宠物 | 更新/删除非本人宠物 | PUT/DELETE /pets/{petId} |
| 无权删除他人宠物 | 删除非本人宠物 | DELETE /pets/{petId} |
| 无权删除他人动态 | 删除非本人发布的动态 | DELETE /posts/{postId} |
| 无权操作此地址 | 修改/删除非本人地址 | PUT/DELETE /user/addresses/{id} |
| 商品不存在 | productId 无效 | /shop/products/{productId}、兑换 |
| 商品已下架 | 兑换下架的商品 | POST /shop/orders |
| 商品库存不足 | 兑换数量超过库存 | POST /shop/orders |
| 用户积分不足 | 积分不足以兑换商品 | POST /shop/orders |
| 积分变动值不能为空或0 | 管理员操作积分时参数错误 | /admin/users/{userId}/points |
| 订单不存在 | orderId 无效 | /shop/orders/{orderId}、/admin/orders/{orderId} |
| 订单状态不能为空 | 更新订单状态时缺少参数 | /admin/orders/{orderId}/status |
| 发货时请填写快递单号和快递公司 | 发货状态更新缺少物流信息 | /admin/orders/{orderId}/status |
| 分类不存在 | categoryId 无效 | PUT /admin/categories/{categoryId} |
| 上级分类不能选择自身 | 编辑分类时将自身设为父分类 | PUT /admin/categories/{categoryId} |
| 审核状态非法，仅支持 1-通过 2-拒绝 | 动态审核状态参数错误 | /admin/posts/{postId}/audit |
| 商品状态非法，仅支持 0-下架 1-上架 | 商品上下架参数错误 | /admin/products/{productId}/status |
| 轮播图不存在 | bannerId 无效或已逻辑删除 | /admin/banner/{id}、删除/编辑接口 |
| 标题不能为空 | 新增轮播图时缺少title参数 | POST /admin/banner/ |
| 图片地址不能为空 | 新增轮播图时缺少imageUrl参数 | POST /admin/banner/ |
| 跳转类型不能为空 | 新增轮播图时缺少jumpType参数 | POST /admin/banner/ |

---

> **文档维护说明**：后端接口如有增删改，请同步更新本文档。建议每次发版前核对 Controller 代码与文档一致性。
