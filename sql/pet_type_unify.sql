-- =====================================================================
-- 宠迹 · 宠物种类（pet_type）取值词表统一
-- 创建日期：2026-09-11
--
-- 背景：
--   article_interest_upgrade.sql 给文章表/宠物表加了 pet_type，用的是
--     pet_article.pet_type = cat / dog / all     （all-通用）
--     pet_info.pet_type    = cat / dog / other
--   但项目更早存在的 ai_suggest_word.pet_type 用的是另一套：
--     general-通用 / cat-猫 / dog-狗 / rabbit-兔子 / bird-鸟 / fish-鱼 / other-其他
--   同一个库里出现两套词表，前端要写两套映射，长期必然出错。
--
-- 本次统一为 ai_suggest_word 那套（七类）：
--   general-通用 / cat-猫 / dog-狗 / rabbit-兔子 / bird-鸟 / fish-鱼 / other-其他
--   其中 all 一律重命名为 general（语义相同）。
--
-- ⚠️ 执行要求：本脚本必须与后端代码改动**同时上线**。
--   因为 XML 里的兴趣优先排序 SQL 会把 'general' 当作"通用"档，
--   若只跑 SQL 不改代码，通用文章会暂时被排到最后一档（不影响正确性，只是排序变差）。
--
-- 幂等性：UPDATE 重复执行无副作用；MODIFY COLUMN 本身幂等。
-- 执行顺序：article_interest_upgrade.sql 之后。
-- =====================================================================

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
-- 1. 文章表：all → general，并放宽字段长度（与 ai_suggest_word 一致）
-- ---------------------------------------------------------------------
UPDATE `pet_article` SET `pet_type` = 'general' WHERE `pet_type` = 'all';

ALTER TABLE `pet_article`
  MODIFY COLUMN `pet_type` VARCHAR(20) DEFAULT NULL
  COMMENT '适用宠物（general-通用/cat-猫/dog-狗/rabbit-兔子/bird-鸟/fish-鱼/other-其他，NULL 视为通用）';

-- ---------------------------------------------------------------------
-- 2. 宠物表：放宽字段长度 + 补全注释
--    宠物本身没有"通用"的概念，取值只到 cat/dog/rabbit/bird/fish/other
-- ---------------------------------------------------------------------
ALTER TABLE `pet_info`
  MODIFY COLUMN `pet_type` VARCHAR(20) DEFAULT NULL
  COMMENT '宠物类型（cat-猫/dog-狗/rabbit-兔子/bird-鸟/fish-鱼/other-其他，NULL 视为其他，不参与兴趣优先）';

-- ---------------------------------------------------------------------
-- 3. 参考：ai_suggest_word.pet_type 已是七类词表，无需处理
--    下面这条仅用于人工核对三张表的注释是否一致
-- ---------------------------------------------------------------------
-- SELECT table_name, column_name, column_type, column_comment
--   FROM information_schema.columns
--  WHERE table_schema = DATABASE() AND column_name = 'pet_type';

-- ---------------------------------------------------------------------
-- 4. 校验：统一后每张表的取值分布
-- ---------------------------------------------------------------------
-- SELECT pet_type, COUNT(*) FROM pet_article GROUP BY pet_type;
-- SELECT pet_type, COUNT(*) FROM pet_info    GROUP BY pet_type;
-- SELECT pet_type, COUNT(*) FROM ai_suggest_word GROUP BY pet_type;
