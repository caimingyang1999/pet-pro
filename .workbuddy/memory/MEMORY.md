# 宠迹 · 项目长期备忘

## 构建环境（重要）
- **后端必须用 JDK 21 构建**：本机默认 `mvn` 用 Homebrew JDK 26，会令 Lombok 注解处理失效 → 大量假错误。
  `export JAVA_HOME=$(/usr/libexec/java_home -v 21)` 再 `mvn -DskipTests -pl ruoyi-admin -am clean compile`。
- ⚠️ **严禁随手编译**：`ruoyi-admin` 引入 `spring-boot-devtools`(`fork=true`)，一编译就热重启用户本机 8080 服务（"电脑发烫"）。
  验证优先用纯 SQL；确需编译先征得同意，不要另起端口启服务。
- ✅ **安全的编译校验法**（不触发热重启，2026-09-12 验证可用）：只编译新增文件、产物输出到 /tmp，不碰项目 target：
  `mvn -o -pl ruoyi-system -am dependency:build-classpath -Dmdep.outputFile=/tmp/cp.txt`（不编译）
  → `javac -encoding UTF-8 -cp "ruoyi-system/target/classes:ruoyi-common/target/classes:ruoyi-framework/target/classes:$(cat /tmp/cp.txt)" -d /tmp/xxx 新增文件.java`
  （含 Lombok，能真查出符号错误）。后端管理端(Vue2)SFC 校验：`vue-template-compiler.compile` 模板 + `@babel/parser` 脚本。
- 小程序：`cd pettrace-miniapp && npm run build:mp-weixin`，产物 `dist/build/mp-weixin`，微信开发者工具导入上传。
- 本地 MySQL 端口 **3307**（库 `pet-profile`）。

## 部署与域名（2026-09-12 实测）
- **线上域名 `https://coof.xin`（不带端口）**：Nginx 443 反代后端 8080，`/api/v1/**` 与 `/profile/**`（图片）均已通，根路径是"宠迹管理系统"后台。证书 DigiCert，2026-12-02 到期。
- ⚠️ 微信小程序「服务器域名」**不接受 IP 与非 443 端口**，`.env` 的 `VITE_PROD_SERVER_BASE` 必须是该域名；服务器 8080 **未对公网开放**。
- ⚠️ 改完后端代码**必须重新打包部署到服务器**，否则线上跑旧版：曾出现线上 `/articles/list` `/banner/list` `/shop/*` 全 401（游客不可浏览，微信驳回原因 2），而本地 `@Anonymous` 早已补齐。
- 判活：`curl -sS --noproxy '*' https://coof.xin/api/v1/config/features`（返回 200 JSON 即链路正常）。


## 宠物类型词表（全端唯一，2026-09-11 统一）
`general`通用 / `cat`猫 / `dog`狗 / `rabbit`兔子 / `bird`鸟 / `fish`鱼 / `other`其他。
- 文章 `pet_article.pet_type` 用全 7 类，`general`(或NULL)=所有宠物适用；
- 宠物 `pet_info.pet_type` 无"通用"，只有后 6 类，`other`(或NULL)不参与个性化。
- 三处必须一致：小程序 `src/config/petTypes.js`(untracked，改前先 grep 引用方) + 后台字典 `pet_types` + 后端白名单。
- 标签 `tags` 保持自由文本(逗号分隔)，不用字典。

## 列表分页：PageHelper 合理化坑
`pagehelper.reasonable=true` 下 `pageNum>总页` 回退到末页 → 下拉重复、永不"到底"。
前端以接口返回 `total` 为准判定到底；前端追加按 id 去重兜底。根治是 yml 加 `pagehelper.reasonable:false`（全局，未动）。

## 日期字段约定（硬性）
DB 中所有 `DATE` 列实体必须用 `java.time.LocalDate`（非 `java.util.Date`），避免时区截断到前一天。
`DATETIME/TIMESTAMP` 继续用 `Date`。新增 DATE 列功能照做。

## 关键产品/合规约定
- **游客可浏览**（微信审核硬要求）：未登录必须能看首页养宠知识、文章详情、商城分类与商品。
- 需登录操作走 `requireLogin()` 拦截动作，不可作页面级守卫（未登录恒 resolve(false)）。
- **个人主体，零 UGC**：禁发布/评论/点赞/关注/分享/社区/动态/圈子/广场等。后端 `pet_post` 等表保留不动，前端不调用。
- 界面禁现"微信"字样/官方绿。敏感词红线：动态/社区/发布/分享/评论/点赞/关注/粉丝/私信/帖子/广场/圈子。
- **个人主体 / AppID `wx262afb1a013d30f2`**：微信不开放「获取手机号」→ 登录页「手机号快捷登录」与注册页「手机号快捷注册」**已隐藏**（代码以注释保留，升级企业主体后取消注释即可恢复）；登录页主按钮为「账号密码登录」，协议勾选行在**表单页**（`handleLogin` 有未勾选拦截）；注册走表单（手机号+密码）。⚠️ 因此**生产库必须有可用账号**，否则审核员登录不了。
- 后台登录页为深色"深空极光+玻璃拟态"，改动保持深色；临时文件放 `ruoyi-ui/.backup/`。

## 登录态与过期处理（2026-09-12 重构）
- **不再 reLaunch 强跳登录页**：401 → 先静默重登（`uni.login` → `/wx/login`）→ 成功则**自动重放原请求**，用户无感；失败才弹窗，且用 `navigateTo` 保留页面栈，登录后 `navigateBack` 回原页继续原操作。
- 逻辑集中在 `src/utils/session.js`（单飞 `recovering`、挂起队列、60s 静默期、120s 超时释放）；`api/request.js` 只做 401 分流 + 用 `setSessionHooks` 同步 Pinia。
- ⚠️ **静默重登必须比对 `userId`**：后端 `/wx/login` 按 openid 查不到就直接 `createWxUser`（建新号+送积分），不校验会把未绑 openid 的老用户切成空账号。本地 `userId` 由 store `setUserId` 维护，`logout` 不清。
- 地址常量在 `src/config/server.js`，`api/request.js` 只 re-export（防 request↔session 循环依赖）。


## 图标体系（2026-09 统一）
全站走 `@/components/Icon.vue`(uni-icons 内嵌字体，离线可用)。禁 emoji 功能图标、禁 uView `<u-icon>`(远程CDN豆腐块)。
tabBar 图标只能 PNG：162×162 透明、未选中 #999、选中 #FF7E3D、线宽 8px 单色 outline，用 PIL 绘制。

## AI 文章生成（2026-09 新增）
- Node 服务 `~/Documents/code/models-server`：`POST /api/article/generate` 生成文章(含base64配图)，
  `GET /api/article/status` 探测能力。端口 3300（与顾问同服务）。
- 后端 `ruoyi-ai` 模块：`AiArticleController`(`/api/v1/admin/articles/generate`) → `AiArticleServiceImpl`
  调 Node，base64 经 `FileUtils.writeBytes(bytes, RuoYiConfig.getUploadPath())` 转存为 `/profile/...`，
  把正文 `__ARTICLE_IMG_n__` 占位符替换为真实 URL，文章落库 **草稿(status=0)**，人工审核后发布。
- 配图：硅基流动 AI(可插拔，无 key 则回退 `public/article-lib/类型-序号.png`)。
- ⚠️ **WebClient 缓冲上限坑**：Node 把配图以 base64 内嵌在 `/api/article/generate` 响应里，单篇最多 4 张 1024 图（实测 2 张≈3MB）。
  `aiArticleWebClient` 必须 `ExchangeStrategies` 设 `maxInMemorySize(64MB)`，否则默认 256KB 触发
  `DataBufferLimitException`（现象：日志报"200 OK ... Exceeded limit on max bytes to buffer : 262144"）。
  改这个 bean 后必须重新构建/重启 8080 才生效（运行时用的是旧 class）。

## 约束（AGENTS.md）
不改动 ruoyi-framework/ruoyi-common 核心；不修改已有迁移脚本；不硬编码密钥。积分变更走事务并写 `user_points_log`；交互/注释用中文。

## MySQL / 统计类 SQL 坑（2026-09-12 踩实）
- ⚠️ 本机 MySQL 开了 **`sql_mode=only_full_group_by`**：按天统计**不能**写
  `select date_format(create_time,'%m-%d') ... group by date(create_time)` → ERROR 1055。
  必须让 `group by` 与 `select` **表达式完全一致**：`group by date_format(create_time,'%m-%d')`。
- ⚠️ **占位符不要参与 INTERVAL 运算**（如 `interval #{days} - 1 day`），预编译下不可靠；
  改为 Java 算好起始时间传参：`create_time >= #{startDate}`（yyyy-MM-dd HH:mm:ss）。
- ⚠️ `shop_product.product_images` 实际是 **JSON 数组** `["/profile/...png"]`，域注释"逗号分隔"是错的；
  取主图要兼容两种格式（先去中括号/引号再按逗号切）。
- MyBatis 配置：`mapperLocations: classpath*:mapper/**/*Mapper.xml`、`map-underscore-to-camel-case: true`、
  `@MapperScan("com.ruoyi.**.mapper")`（新增 Mapper 放 `com.ruoyi.system.mapper` 自动生效）。

## 后台首页数据总览（Dashboard，2026-09-12 新增）
- 接口 `GET /api/v1/admin/dashboard/overview?days=7|14|30`（`AdminDashboardController`，`@ss.hasPermi('admin')`）。
- 链路：`DashboardMapper(SQL) → DashboardServiceImpl → DashboardOverviewVO`；前端 `src/api/dashboard.js`。
- 前端模式：`index.vue` **一次性拉取 + props 下发**各子组件（组件不再各自请求）；
  趋势图/用户增长图的"近N天"下拉 `@range-change` 回传父级重新拉取。原 `dashboard/home/mock.js` 已删。
- 已删除的展示：待处理事项里的「**用户投诉**」（无数据支撑）、欢迎栏「**数据导出**」按钮（仅提示开发中）。

## 后台管理端要点
- 前端 dev server 端口 **1024**（`port=1024 npm run dev -- --no-open`）；后端 8080 由用户自起，`/dev-api→:8080`。
- 最小前端校验：小程序端用 `@vue/compiler-sfc`(compileTemplate+compileScript) 验模板脚本、`sass.renderSync({data: uni.scss内容+样式块})` 验样式（直接 `@import "@/uni.scss"` 找不到，要先读文件拼接）。
- 沙箱 HTTP 代理会让 `curl localhost:任意端口` 对不存在服务返回 502；确认端口用 `lsof -nP -iTCP:端口 -sTCP:LISTEN`。macOS 无 `timeout` 命令。
- **管理员密码重置**：`sys_user.password` 是 Spring Security `BCryptPasswordEncoder`(Strength 10) 的 `$2a$10$...`（60 字符），
  校验直读 DB 无缓存 → 改库立即生效。生成哈希用
  `~/.workbuddy/binaries/python/envs/default/bin/python -c "import bcrypt;print(bcrypt.hashpw(b'明文',bcrypt.gensalt(rounds=10,prefix=b'2a')).decode())"`
  （`prefix=b'2a'` 必须指定，否则默认输出 `$2b$`）。脚本见 `sql/update_admin_password.sql`。

## 小程序渲染层坑（2026-09-12 踩实）
- ⚠️ **模板 `v-if` 引入 setup 导入的函数必须带括号**：`v-if="isShopDemo"` 传到视图层的是函数对象，
  setData 无法序列化 → 恒为假、组件永不渲染且无报错。必须写 `v-if="isShopDemo()"`。
- ⚠️ **开发者工具对"新增的自定义组件文件"热重载不重新注册**：新组件上线后页面标签变未知节点、整块消失。
  点一次「编译」即恢复；自动化验证用 `cli auto --auto-port 9420`（会完整构建，顺带修复）。
- ⚠️ **`<rich-text>` 内节点不继承页面 scoped 样式**：正文 `<img>` 不内联宽度会被微信按原始尺寸（AI 图 1024px）渲染 → 撑破屏幕。
  已在 `pages/article/detail.vue` 的 `contentNodes` 里用 `normalizeArticleImages()` 给所有 `<img>` 清除旧 `style/width/height` 并注入
  `width:100%;height:auto;display:block;border-radius:8px;margin:16px 0;`。**其它用 rich-text 渲染正文的地方（如动态详情）同理，需自行加内联样式。**
- **tabBar 页自定义标题栏统一用 `components/NavBar.vue` + `utils/navbar.js`**（标题与胶囊对齐）；
  标题栏右上角一律会被微信胶囊遮挡，操作按钮只能放 `#left` 或页面头部区域内。
- **商城演示模式**（个人作品，兑换不真实发货）：开关与全部文案集中在 `config/shopDemo.js`
  （`features.shopDemoMode`，.env `VITE_SHOP_DEMO_MODE=false` 可关）；提示条组件 `components/DemoNotice.vue`。
  涉及兑换的文案不得出现"发货/物流"承诺。

## 开发者工具自动化验证（可复用）
`"/Applications/wechatwebdevtools.app/Contents/MacOS/cli" open/auto --project <dist/dev/mp-weixin> --auto-port 9420`，
配 `miniprogram-automator`（装在 /tmp/automator-test）connect 后 reLaunch/switchTab + screenshot。
坑：page.$()/page.data() 会挂起不可用；只有导航、currentPage、screenshot 可靠；screenshot 偶发挂起重试即可。
