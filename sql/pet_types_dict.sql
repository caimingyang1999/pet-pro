-- =====================================================================
-- 宠迹 · 「宠物类型」字典数据（dict_type = pet_types）
-- 创建日期：2026-09-12
--
-- 背景：
--   后台文章管理的「适用宠物」字段改为数据字典驱动，好处是运营在
--   「系统管理 - 字典管理」里就能改文案/加类型，不用改前端代码、不用重新发版。
--   字典类型 pet_types（宠物类型）已由人工在后台创建（dict_id=100），
--   但字典数据是空的，本脚本负责补齐七类数据。
--
-- ⚠️ 新增类型时必须同步的三处（缺一处就会出现"后台能选、小程序不认"的错位）：
--   1. 本字典（sys_dict_data）—— 后台下拉与列表标签的数据源
--   2. 后端白名单 `PetArticleServiceImpl.INTEREST_PET_TYPES`
--      —— 决定哪些类型参与首页"兴趣优先推荐"排序
--   3. 小程序本地词表 `pettrace-miniapp/src/config/petTypes.js`
--      —— 小程序端**不能**读字典接口（游客也要能浏览文章，而字典接口需要登录），
--         所以必须保留这份本地词表，它是"三处同步"里删不掉的一处。
--
-- 取值与 ai_suggest_word.pet_type 保持一致（2026-09-11 已统一，不再使用 all）：
--   general-通用 / cat-猫 / dog-狗 / rabbit-兔子 / bird-鸟 / fish-鱼 / other-其他
--
-- 幂等：字典类型用 NOT EXISTS 保护，字典数据按 (dict_type, dict_value) 去重，
--       可重复执行且**不会覆盖**你后来在后台改过的标签文案。
-- 排序：dict_sort 决定后台下拉里的先后顺序，本脚本与小程序端词表顺序一致。
-- 样式：list_class 决定后台列表里标签的颜色（info/primary/success/warning/danger），
--       现在统一用 info 灰色；想按类型区分颜色，直接在字典管理界面改即可。
-- =====================================================================

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
-- 1. 字典类型（其他环境若还没建，这里会补上；已有则跳过）
-- ---------------------------------------------------------------------
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`)
SELECT '宠物类型', 'pet_types', '0', 'admin', NOW(), '宠物类型列表（文章适用宠物、宠物档案）'
  FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'pet_types');

-- ---------------------------------------------------------------------
-- 2. 字典数据（七类）
-- ---------------------------------------------------------------------
INSERT INTO `sys_dict_data`
  (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 1, '通用', 'general', 'pet_types', '', 'info', 'Y', '0', 'admin', NOW(), '所有宠物都适用，兴趣推荐里的兜底档'
  FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'pet_types' AND `dict_value` = 'general');

INSERT INTO `sys_dict_data`
  (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 2, '猫', 'cat', 'pet_types', '', 'info', 'N', '0', 'admin', NOW(), '猫'
  FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'pet_types' AND `dict_value` = 'cat');

INSERT INTO `sys_dict_data`
  (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 3, '狗', 'dog', 'pet_types', '', 'info', 'N', '0', 'admin', NOW(), '狗'
  FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'pet_types' AND `dict_value` = 'dog');

INSERT INTO `sys_dict_data`
  (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 4, '兔子', 'rabbit', 'pet_types', '', 'info', 'N', '0', 'admin', NOW(), '兔子'
  FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'pet_types' AND `dict_value` = 'rabbit');

INSERT INTO `sys_dict_data`
  (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 5, '鸟', 'bird', 'pet_types', '', 'info', 'N', '0', 'admin', NOW(), '鸟'
  FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'pet_types' AND `dict_value` = 'bird');

INSERT INTO `sys_dict_data`
  (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 6, '鱼', 'fish', 'pet_types', '', 'info', 'N', '0', 'admin', NOW(), '鱼'
  FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'pet_types' AND `dict_value` = 'fish');

INSERT INTO `sys_dict_data`
  (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 7, '其他', 'other', 'pet_types', '', 'info', 'N', '0', 'admin', NOW(), '其他（不参与兴趣优先推荐）'
  FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'pet_types' AND `dict_value` = 'other');

-- ---------------------------------------------------------------------
-- 3. 校验（执行后自查）
-- ---------------------------------------------------------------------
-- SELECT dict_sort, dict_label, dict_value, list_class, status
--   FROM sys_dict_data WHERE dict_type = 'pet_types' ORDER BY dict_sort;
