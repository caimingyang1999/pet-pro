-- ============================================================
-- 宠迹 · 合规改造新增表
-- 创建日期：2026-09-10
--
-- 背景：小程序由「社交-笔记」类目转型为「宠物健康管理工具」，
--       移除用户发布动态 / 评论 / 点赞 / 关注等 UGC 社交能力，
--       新增体重记录、成长相册、养宠知识三类工具型数据表。
--
-- 说明：pet_post / post_comment / post_like / user_follow 等表**保留不删**，
--       后端接口也保留，仅前端不再调用（未来若升级企业主体可直接复用）。
--
-- 执行顺序：需在 sql/pet.sql（或 ry_20250522.sql + pet.sql）之后执行。
--           本脚本产出的 pet_article 是下列脚本的前置表：
--             article_interest_upgrade.sql（加 pet_type 字段）
--             article_category_dict.sql / pet_types_dict.sql / article_menu.sql
--
-- ⚠️ 幂等性说明（2026-09-12 调整）：
--   原先三张表用的是 `DROP TABLE IF EXISTS` + `CREATE TABLE`，
--   在**已有数据的库上重跑会把文章全部清空**。现统一改为 `CREATE TABLE IF NOT EXISTS`，
--   重复执行不会删除、不会覆盖任何已有数据；示例文章也加了"仅空表才灌"的保护。
--
-- ⚠️ 若后续脚本报 "Table 'xxx.pet_article' doesn't exist"，
--   说明执行的库里本脚本没跑成功（或用错了库），而不是脚本本身有问题。
-- ============================================================

SET NAMES utf8mb4;

-- 打印当前连接的库名，防止连错库（项目库名是 `pet-profile`，连字符，不是 pet_profile）
SELECT DATABASE() AS `当前数据库`;

-- ----------------------------
-- 1、宠物体重记录表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `pet_weight_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `pet_id` BIGINT NOT NULL COMMENT '宠物ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID（冗余存储，便于按用户校验归属）',
  `weight` DECIMAL(5,2) NOT NULL COMMENT '体重(kg)',
  `record_date` DATE NOT NULL COMMENT '记录日期',
  `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
  `del_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0-存在 1-删除）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_pet_id` (`pet_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_pet_date` (`pet_id`, `record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物体重记录表';

-- ----------------------------
-- 2、宠物成长相册表（仅自己可见，不公开、无互动）
-- ----------------------------
CREATE TABLE IF NOT EXISTS `pet_album` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `pet_id` BIGINT NOT NULL COMMENT '宠物ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `title` VARCHAR(100) DEFAULT NULL COMMENT '标题',
  `content` VARCHAR(500) DEFAULT NULL COMMENT '描述',
  `images` JSON DEFAULT NULL COMMENT '图片列表（JSON数组，存放相对路径）',
  `record_date` DATE DEFAULT NULL COMMENT '记录日期',
  `del_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0-存在 1-删除）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_pet_id` (`pet_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_pet_date` (`pet_id`, `record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物成长相册表';

-- ----------------------------
-- 3、养宠知识文章表（平台发布，用户只读）
-- ----------------------------
CREATE TABLE IF NOT EXISTS `pet_article` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` VARCHAR(100) NOT NULL COMMENT '文章标题',
  `summary` VARCHAR(255) DEFAULT NULL COMMENT '摘要（列表页展示）',
  `cover_image` VARCHAR(255) DEFAULT NULL COMMENT '封面图地址',
  `content` MEDIUMTEXT COMMENT '正文（富文本 HTML）',
  `category` VARCHAR(50) DEFAULT NULL COMMENT '分类（如：喂养/健康/训练/洗护）',
  `tags` VARCHAR(200) DEFAULT NULL COMMENT '标签（逗号分隔）',
  `source` VARCHAR(100) DEFAULT NULL COMMENT '来源（如：宠迹编辑部）',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览量',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号（数字越小越靠前）',
  `status` CHAR(1) NOT NULL DEFAULT '1' COMMENT '状态（0-草稿 1-已发布）',
  `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0-存在 1-删除）',
  PRIMARY KEY (`id`),
  KEY `idx_status_sort` (`status`, `sort_order`),
  KEY `idx_category` (`category`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='养宠知识文章表';

-- ----------------------------
-- 4、养宠知识初始化数据（示例文章，可直接在后台替换）
-- ----------------------------
INSERT INTO `pet_article`
  (`title`, `summary`, `cover_image`, `content`, `category`, `tags`, `source`, `sort_order`, `status`, `create_by`)
SELECT `title`, `summary`, `cover_image`, `content`, `category`, `tags`, `source`, `sort_order`, `status`, `create_by`
FROM (
    SELECT '幼犬疫苗接种全流程指南', '从 6 周龄开始，幼犬需要按程序完成三针联苗与一针狂犬疫苗。本文梳理每个阶段的时间点与注意事项。', '', '<p>幼犬的免疫程序直接决定了它一生的健康基础。一般建议从 6~8 周龄开始首针，之后每隔 21 天接种一针，共三针联苗，最后在 3 月龄后接种狂犬疫苗。</p><p><strong>接种前的注意事项</strong></p><p>接种前需确保幼犬身体健康、无腹泻与发热症状，并已完成体内外驱虫。刚带回家的幼犬建议先适应环境 7 天再接种。</p><p><strong>接种后的注意事项</strong></p><p>接种后 7 天内避免洗澡、外出与剧烈运动，部分幼犬可能出现轻微发热或食欲下降，通常 1~2 天可自行恢复；若出现面部肿胀、呼吸急促等过敏反应，请立即就医。</p>', '健康', '疫苗,幼犬,免疫', '宠迹编辑部', 1, '1', 'admin',
    SELECT '猫咪体重管理：如何判断胖瘦是否健康', '猫咪肥胖会显著增加糖尿病、关节病的风险。用手摸肋骨与俯视腰线，是居家最简单的判断方法。', '', '<p>成年猫的理想体重因品种差异较大，单看数字并不准确，更推荐用「体况评分」来判断。</p><p><strong>居家三步判断法</strong></p><p>第一步，用手轻抚猫咪两侧肋骨：能摸到肋骨但看不到，属于正常；需要用力按压才能摸到，说明偏胖。</p><p>第二步，从上方俯视：腰部应有轻微内收的曲线；若腰部与胸腔同宽甚至更宽，说明超重。</p><p>第三步，从侧面观察腹部：腹部线条应大致平直；若明显下垂形成「肚子」，需要控制饮食。</p><p>建议每月固定时间称重一次并记录，体重短期波动超过 10% 时应及时咨询兽医。</p>', '健康', '体重,猫咪,肥胖', '宠迹编辑部', 2, '1', 'admin',
    SELECT '换粮期肠胃不适？试试七日过渡法', '突然换粮是引起猫咪狗狗软便的常见原因。用七天时间按比例混合新旧粮，可以大幅降低肠胃应激。', '', '<p>宠物的肠道菌群需要时间适应新食物，突然换粮容易引起软便甚至呕吐。推荐采用「七日过渡法」：</p><p>第 1~2 天：新粮占 25%，旧粮占 75%；<br/>第 3~4 天：新粮占 50%，旧粮占 50%；<br/>第 5~6 天：新粮占 75%，旧粮占 25%；<br/>第 7 天：完全转换为新粮。</p><p>过渡期间请勿同时更换零食与罐头，避免无法判断不适来源。若出现持续腹泻，应暂停换粮并就医。</p>', '喂养', '换粮,肠胃,软便', '宠迹编辑部', 3, '1', 'admin',
    SELECT '新手养猫必备清单：接猫回家前要准备什么', '猫砂盆、猫粮、航空箱、饮水碗、指甲剪……第一次养猫不必买太多，这些是真正用得上的基础物品。', '', '<p>接猫回家前，先把「吃、喝、拉、睡」四件事准备好，其余物品可以等猫咪适应后再逐步添置。</p><p><strong>必需物品</strong></p><p>猫粮（沿用原主粮，至少吃两周再考虑换粮）、猫砂与猫砂盆、饮水碗与食盆、航空箱或猫包、猫抓板。</p><p><strong>建议物品</strong></p><p>指甲剪、宠物专用梳子、益生菌、体内外驱虫药。</p><p><strong>环境准备</strong></p><p>提前封好纱窗，收起电线与易碎品，为猫咪准备一个安静、可躲藏的小空间。刚到家的前三天尽量不要频繁打扰，让它自行探索。</p>', '喂养', '新手,猫咪,清单', '宠迹编辑部', 4, '1', 'admin',
    SELECT '狗狗日常训练入门：从「坐下」开始', '正向强化是最高效的训练方式。用零食奖励正确行为，每次训练控制在 5~10 分钟，一天两次即可。', '', '<p>训练的核心是「及时奖励」。狗狗做出正确行为的瞬间（1 秒内）给予奖励，它才能建立行为与奖励的关联。</p><p><strong>训练「坐下」的步骤</strong></p><p>1. 手拿零食放在狗狗鼻子前，慢慢向上后方移动，它的头会自然上扬、屁股下沉；<br/>2. 屁股触地的瞬间说出口令「坐」，并立即给零食；<br/>3. 重复 5~8 次为一组，一天两组；<br/>4. 熟练后再逐步加入手势与口令，减少零食频率。</p><p>训练时请保持耐心，避免呵斥或按压身体，否则容易造成抵触。</p>', '训练', '训练,狗狗,行为', '宠迹编辑部', 5, '1', 'admin'
) AS `seed`
WHERE NOT EXISTS (SELECT 1 FROM `pet_article` LIMIT 1);
