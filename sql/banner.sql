-- ----------------------------
-- 轮播图表 banner
-- ----------------------------
DROP TABLE IF EXISTS `banner`;
CREATE TABLE `banner` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` VARCHAR(100) NOT NULL COMMENT '轮播图标题',
  `image_url` VARCHAR(255) NOT NULL COMMENT '图片地址',
  `jump_type` VARCHAR(20) NOT NULL DEFAULT 'none' COMMENT '跳转类型（none-不跳转/post-动态详情/product-商品详情/url-外部链接/miniapp-小程序页面）',
  `jump_target` VARCHAR(255) DEFAULT NULL COMMENT '跳转目标（根据jump_type存储对应的ID或URL）',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号（数字越小越靠前）',
  `status` CHAR(1) NOT NULL DEFAULT '1' COMMENT '状态（0-停用 1-启用）',
  `start_time` DATETIME DEFAULT NULL COMMENT '展示开始时间（NULL表示不限）',
  `end_time` DATETIME DEFAULT NULL COMMENT '展示结束时间（NULL表示不限）',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0-存在 1-删除）',
  PRIMARY KEY (`id`),
  KEY `idx_status_sort` (`status`, `sort_order`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轮播图表';

-- ----------------------------
-- 初始化轮播图数据
-- ----------------------------
INSERT INTO `banner` (`title`, `image_url`, `jump_type`, `jump_target`, `sort_order`, `status`) VALUES
('宠物健康指南', '/static/banner/banner1.jpg', 'none', NULL, 1, '1'),
('新品宠物玩具上线', '/static/banner/banner2.jpg', 'product', '1', 2, '1'),
('萌宠摄影大赛', '/static/banner/banner3.jpg', 'url', 'https://example.com/activity', 3, '1');
