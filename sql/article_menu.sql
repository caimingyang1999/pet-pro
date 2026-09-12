-- ============================================================
-- 宠迹 · 后台「养宠知识文章管理」菜单与权限
-- 创建日期：2026-09-11
--
-- 背景：小程序改造后新增「养宠知识文章」浏览能力（表 pet_article，
--       建表脚本见 compliance_upgrade.sql），后台需要配套的文章管理页面，
--       本脚本负责注册菜单入口与按钮权限。
--
-- 说明：
--   1. 本脚本只新增菜单，不改动任何已有迁移脚本与数据；
--   2. 菜单挂在「操作管理」（menu_id = 2009）下，与轮播图、推荐词同级；
--   3. 权限标识与 AdminArticleController 中的 @PreAuthorize 一一对应：
--      article:list / article:query / article:add / article:edit / article:remove；
--   4. 超级管理员（admin）无需授权即拥有全部权限；若需给其它角色使用，
--      请在「系统管理 - 角色管理」中勾选对应菜单与按钮。
--
-- 执行顺序：先执行 compliance_upgrade.sql（建 pet_article 表），再执行本脚本。
-- ============================================================

SET NAMES utf8mb4;

-- ----------------------------
-- 1、清理旧记录，保证脚本可重复执行
-- ----------------------------
DELETE FROM `sys_role_menu` WHERE `menu_id` IN (2012, 2013, 2014, 2015, 2016);
DELETE FROM `sys_menu` WHERE `menu_id` IN (2012, 2013, 2014, 2015, 2016);

-- ----------------------------
-- 2、文章管理菜单（挂在「操作管理」下）
-- ----------------------------
INSERT INTO `sys_menu`
  (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
(2012, '文章管理', 2009, 3, '/pet/article', 'system/pet/article/index', NULL, 'article', 1, 0, 'C', '0', '0', 'article:list', 'documentation', 'admin', sysdate(), '', NULL, '养宠知识文章管理菜单');

-- ----------------------------
-- 3、文章管理按钮权限
-- ----------------------------
INSERT INTO `sys_menu`
  (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
(2013, '文章查询', 2012, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'article:query',  '#', 'admin', sysdate(), '', NULL, ''),
(2014, '文章新增', 2012, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'article:add',    '#', 'admin', sysdate(), '', NULL, ''),
(2015, '文章修改', 2012, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'article:edit',   '#', 'admin', sysdate(), '', NULL, ''),
(2016, '文章删除', 2012, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'article:remove', '#', 'admin', sysdate(), '', NULL, '');
