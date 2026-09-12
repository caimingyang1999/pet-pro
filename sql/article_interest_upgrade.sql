-- =====================================================================
-- 宠迹 养宠知识「兴趣推荐」升级 SQL 脚本（幂等版，可重复执行）
--
-- 背景：
--   首页养宠知识改为按用户宠物类型优先推荐：
--     · 用户养猫  → 优先展示 cat 与 general 的文章
--     · 用户养狗  → 优先展示 dog 与 general 的文章
--     · 没有宠物  → 按平台默认排序（sort_order 升序、ID 降序）
--   因此需要给文章表补「适用宠物」字段，给宠物表补「宠物类型」字段。
--
-- 执行说明：
--   1. 在 compliance_upgrade.sql 之后执行；本脚本为增量脚本（ALTER + 数据回填）
--   2. 执行前请备份数据库，建议业务低峰期执行
--
-- 字段取值约定（与 ai_suggest_word 保持同一套七类词表）：
--   general-通用 / cat-猫 / dog-狗 / rabbit-兔子 / bird-鸟 / fish-鱼 / other-其他
--   pet_article.pet_type：七类都可用，general 与 NULL 都表示"所有宠物都适用"
--   pet_info.pet_type   ：宠物本身没有"通用"概念，只取 cat/dog/rabbit/bird/fish/other，
--                         NULL 视为 other，不参与兴趣优先
--
-- 说明：本脚本早期版本用 all 表示通用，后由 sql/pet_type_unify.sql 统一为 general。
--       这里已直接写入 general，全新库只执行本脚本也能得到正确词表。
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. 文章表 pet_article 新增 pet_type（适用宠物）
-- ---------------------------------------------------------------------
SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pet_article' AND COLUMN_NAME = 'pet_type'
        ),
        'SELECT ''pet_article.pet_type already exists, skip''',
        'ALTER TABLE `pet_article` ADD COLUMN `pet_type` VARCHAR(20) DEFAULT NULL COMMENT ''适用宠物（general-通用/cat-猫/dog-狗/rabbit-兔子/bird-鸟/fish-鱼/other-其他，NULL 视为通用）'' AFTER `category`'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 1.1 pet_type 索引（如果已存在则跳过）
SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.STATISTICS
            WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pet_article' AND INDEX_NAME = 'idx_pet_type'
        ),
        'SELECT ''idx_pet_type already exists, skip''',
        'ALTER TABLE `pet_article` ADD INDEX `idx_pet_type` (`pet_type`)'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------
-- 2. 宠物表 pet_info 新增 pet_type（宠物类型）
-- ---------------------------------------------------------------------
SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pet_info' AND COLUMN_NAME = 'pet_type'
        ),
        'SELECT ''pet_info.pet_type already exists, skip''',
        'ALTER TABLE `pet_info` ADD COLUMN `pet_type` VARCHAR(20) DEFAULT NULL COMMENT ''宠物类型（cat-猫/dog-狗/rabbit-兔子/bird-鸟/fish-鱼/other-其他，NULL 视为其他）'' AFTER `breed`'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------
-- 3. 老文章回填 pet_type
--    仅处理 pet_type 仍为 NULL 的记录，不覆盖后台已手工设置的值。
--    判断依据：标签 + 标题里的物种关键词。
-- ---------------------------------------------------------------------

-- 3.1 同时提到猫与狗 → 通用
UPDATE `pet_article`
SET `pet_type` = 'general'
WHERE `pet_type` IS NULL
  AND (`tags` LIKE '%猫%' OR `title` LIKE '%猫%')
  AND (`tags` LIKE '%狗%' OR `title` LIKE '%狗%' OR `tags` LIKE '%犬%' OR `title` LIKE '%犬%');

-- 3.2 只提到狗 → 狗
UPDATE `pet_article`
SET `pet_type` = 'dog'
WHERE `pet_type` IS NULL
  AND (`tags` LIKE '%狗%' OR `title` LIKE '%狗%' OR `tags` LIKE '%犬%' OR `title` LIKE '%犬%');

-- 3.3 只提到猫 → 猫
UPDATE `pet_article`
SET `pet_type` = 'cat'
WHERE `pet_type` IS NULL
  AND (`tags` LIKE '%猫%' OR `title` LIKE '%猫%');

-- 3.4 其余（喂养/洗护等泛内容）→ 通用，任何宠物都能看到
UPDATE `pet_article`
SET `pet_type` = 'general'
WHERE `pet_type` IS NULL;

-- ---------------------------------------------------------------------
-- 4. 老宠物回填 pet_type
--    仅处理 pet_type 仍为 NULL 的记录，依据品种关键词做保守判断；
--    匹配不到的保持 NULL（视为"其他"，不参与个性化推荐）。
-- ---------------------------------------------------------------------

-- 4.1 猫（常见品种/俗称）
UPDATE `pet_info`
SET `pet_type` = 'cat'
WHERE `pet_type` IS NULL
  AND (
        `breed` LIKE '%猫%'   OR `breed` LIKE '%渐%'   OR `breed` LIKE '%布偶%'
     OR `breed` LIKE '%暹罗%' OR `breed` LIKE '%加菲%' OR `breed` LIKE '%狸花%'
     OR `breed` LIKE '%缅因%' OR `breed` LIKE '%折耳%' OR `breed` LIKE '%波斯%'
     OR `breed` LIKE '%短毛%' OR `breed` LIKE '%橘%'   OR `breed` LIKE '%喵%'
  );

-- 4.2 狗（常见品种）
UPDATE `pet_info`
SET `pet_type` = 'dog'
WHERE `pet_type` IS NULL
  AND (
        `breed` LIKE '%狗%'   OR `breed` LIKE '%犬%'   OR `breed` LIKE '%金毛%'
     OR `breed` LIKE '%柯基%' OR `breed` LIKE '%泰迪%' OR `breed` LIKE '%贵宾%'
     OR `breed` LIKE '%比熊%' OR `breed` LIKE '%哈士奇%' OR `breed` LIKE '%拉布拉多%'
     OR `breed` LIKE '%边牧%' OR `breed` LIKE '%萨摩%' OR `breed` LIKE '%博美%'
     OR `breed` LIKE '%吉娃娃%' OR `breed` LIKE '%雪纳瑞%' OR `breed` LIKE '%法斗%'
     OR `breed` LIKE '%柴犬%' OR `breed` LIKE '%斗牛%'  OR `breed` LIKE '%腊肠%'
     OR `breed` LIKE '%松狮%' OR `breed` LIKE '%杜宾%'  OR `breed` LIKE '%罗威纳%'
     OR `breed` LIKE '%德牧%'
  );

-- 4.3 兔子
UPDATE `pet_info`
SET `pet_type` = 'rabbit'
WHERE `pet_type` IS NULL
  AND (`breed` LIKE '%兔%' OR `breed` LIKE '%垂耳%' OR `breed` LIKE '%侏儒%');

-- 4.4 鸟
UPDATE `pet_info`
SET `pet_type` = 'bird'
WHERE `pet_type` IS NULL
  AND (
        `breed` LIKE '%鸟%'   OR `breed` LIKE '%鹦鹉%' OR `breed` LIKE '%虎皮%'
     OR `breed` LIKE '%文鸟%' OR `breed` LIKE '%八哥%' OR `breed` LIKE '%画眉%'
  );

-- 4.5 鱼
UPDATE `pet_info`
SET `pet_type` = 'fish'
WHERE `pet_type` IS NULL
  AND (`breed` LIKE '%鱼%' OR `breed` LIKE '%锦鲤%' OR `breed` LIKE '%斗鱼%');

-- ---------------------------------------------------------------------
-- 5. 校验（可单独执行查看回填结果）
-- ---------------------------------------------------------------------
-- SELECT id, title, category, tags, pet_type FROM pet_article ORDER BY id;
-- SELECT id, user_id, name, breed, pet_type FROM pet_info ORDER BY id;
