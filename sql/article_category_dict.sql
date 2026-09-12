-- =====================================================================
-- 宠迹 · 「文章分类」字典数据（dict_type = article_category）
-- 创建日期：2026-09-12
--
-- 背景：
--   后台文章管理的「分类」字段原本在前端硬编码
--   `categoryOptions: ["喂养","健康","训练","洗护","疾病"]`，现在改为数据字典驱动，
--   与「适用宠物」(pet_types) 保持一致：运营在「系统管理 - 字典管理」里就能改文案/加分类，
--   不用改前端代码、不用重新发版。
--
-- ⚠️ 与 pet_types 的区别：
--   pet_types 的 dict_value 用的是代码（cat/dog/…），因为还要和后端推荐白名单、
--   小程序本地词表三处同步；而「文章分类」纯展示、无任何后端耦合，
--   所以这里直接把中文作为 dict_value，与库里已存的 category 值完全相同，零数据迁移。
--
-- 取值：喂养 / 健康 / 训练 / 洗护 / 疾病（沿用原前端硬编码的五类）
--
-- 幂等：字典类型用 NOT EXISTS 保护，字典数据按 (dict_type, dict_value) 去重，
--       可重复执行且**不会覆盖**你后来在后台改过的标签文案。
-- 排序：dict_sort 决定后台下拉里的先后顺序。
-- 样式：list_class 决定后台列表标签颜色（info/primary/success/warning/danger），
--       这里按语义给五类配了不同颜色方便区分；想统一或调整，直接在字典管理界面改即可。
-- =====================================================================

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
-- 1. 字典类型（其他环境若还没建，这里会补上；已有则跳过）
-- ---------------------------------------------------------------------
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`)
SELECT '文章分类', 'article_category', '0', 'admin', NOW(), '文章主题分类（养宠知识）'
  FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'article_category');

-- ---------------------------------------------------------------------
-- 2. 字典数据（五类）
-- ---------------------------------------------------------------------
INSERT INTO `sys_dict_data`
  (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 1, '喂养', '喂养', 'article_category', '', 'success', 'Y', '0', 'admin', NOW(), '喂养知识'
  FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'article_category' AND `dict_value` = '喂养');

INSERT INTO `sys_dict_data`
  (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 2, '健康', '健康', 'article_category', '', 'warning', 'N', '0', 'admin', NOW(), '健康科普'
  FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'article_category' AND `dict_value` = '健康');

INSERT INTO `sys_dict_data`
  (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 3, '训练', '训练', 'article_category', '', 'primary', 'N', '0', 'admin', NOW(), '行为训练'
  FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'article_category' AND `dict_value` = '训练');

INSERT INTO `sys_dict_data`
  (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 4, '洗护', '洗护', 'article_category', '', 'info', 'N', '0', 'admin', NOW(), '清洁洗护'
  FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'article_category' AND `dict_value` = '洗护');

INSERT INTO `sys_dict_data`
  (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 5, '疾病', '疾病', 'article_category', '', 'danger', 'N', '0', 'admin', NOW(), '疾病防治'
  FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'article_category' AND `dict_value` = '疾病');

-- ---------------------------------------------------------------------
-- 3. 校验（执行后自查）
-- ---------------------------------------------------------------------
-- SELECT dict_sort, dict_label, dict_value, list_class, status
--   FROM sys_dict_data WHERE dict_type = 'article_category' ORDER BY dict_sort;
