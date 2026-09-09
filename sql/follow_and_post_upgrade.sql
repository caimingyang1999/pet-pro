-- =====================================================================
-- 宠迹 社交关系 & 动态升级 SQL 脚本（幂等版，可重复执行）
-- 执行说明：
--   1. 本脚本在当前数据库上增量执行（ALTER/新增表）
--   2. 建议在业务低峰期执行；执行前请备份数据库
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. 动态表 pet_post 升级：新增 video_url / video_cover 字段
--    仅当字段不存在时才 ADD（兼容 MySQL 8.0+ IF NOT EXISTS）
-- ---------------------------------------------------------------------

-- 1.1 video_url 字段（如果已存在则跳过）
SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pet_post' AND COLUMN_NAME = 'video_url'
        ),
        'SELECT ''video_url already exists, skip''',
        'ALTER TABLE `pet_post` ADD COLUMN `video_url` VARCHAR(500) DEFAULT NULL COMMENT ''视频/实况照片地址'' AFTER `images`'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 1.2 video_cover 字段（如果已存在则跳过）
SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pet_post' AND COLUMN_NAME = 'video_cover'
        ),
        'SELECT ''video_cover already exists, skip''',
        'ALTER TABLE `pet_post` ADD COLUMN `video_cover` VARCHAR(500) DEFAULT NULL COMMENT ''视频封面图地址'' AFTER `video_url`'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------
-- 2. 用户关注关系表 user_follow
--    说明：follower_id 主动关注 followee_id
--    使用 CREATE TABLE IF NOT EXISTS 保证可重复执行
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user_follow` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `follower_id`   BIGINT       NOT NULL COMMENT '关注者用户ID',
  `followee_id`   BIGINT       NOT NULL COMMENT '被关注者用户ID',
  `del_flag`      CHAR(1)      DEFAULT '0' COMMENT '删除标志（0-存在 2-已删除）',
  `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_follower_followee` (`follower_id`, `followee_id`),
  KEY `idx_followee_id` (`followee_id`),
  KEY `idx_follower_id` (`follower_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户关注关系表';

-- ---------------------------------------------------------------------
-- 执行完毕提示
-- ---------------------------------------------------------------------
SELECT '✅ 宠迹 社交关系 & 动态升级 执行完成' AS result;
