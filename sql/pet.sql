-- 1. 宠物信息表
CREATE TABLE pet_info (
                          id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                          user_id BIGINT NOT NULL COMMENT '用户ID（关联sys_user）',
                          name VARCHAR(50) NOT NULL COMMENT '宠物名称',
                          avatar VARCHAR(255) COMMENT '宠物头像',
                          breed VARCHAR(50) NOT NULL COMMENT '品种',
                          birthday DATE COMMENT '出生日期',
                          gender CHAR(1) COMMENT '性别（0-母 1-公）',
                          weight DECIMAL(5,2) COMMENT '体重(kg)',
                          color VARCHAR(30) COMMENT '毛色',
                          sterilization CHAR(1) DEFAULT '0' COMMENT '绝育状态（0-未绝育 1-已绝育）',
                          remark VARCHAR(500) COMMENT '备注',
                          del_flag CHAR(1) DEFAULT '0' COMMENT '删除标志（0-存在 1-删除）',
                          create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          PRIMARY KEY (id),
                          INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物信息表';

-- 2. 疫苗记录表
CREATE TABLE pet_vaccine (
                             id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                             pet_id BIGINT NOT NULL COMMENT '宠物ID',
                             vaccine_name VARCHAR(100) NOT NULL COMMENT '疫苗名称',
                             inoculation_date DATE COMMENT '接种日期',
                             next_date DATE COMMENT '下次接种日期',
                             create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             PRIMARY KEY (id),
                             INDEX idx_pet_id (pet_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='疫苗记录表';

-- 3. 动态表
CREATE TABLE pet_post (
                          id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                          user_id BIGINT NOT NULL COMMENT '发布用户ID',
                          pet_id BIGINT COMMENT '关联宠物ID',
                          content TEXT NOT NULL COMMENT '动态内容',
                          images JSON COMMENT '图片列表（JSON数组）',
                          status CHAR(1) DEFAULT '0' COMMENT '审核状态（0-待审核 1-通过 2-拒绝）',
                          like_count INT DEFAULT 0 COMMENT '点赞数',
                          comment_count INT DEFAULT 0 COMMENT '评论数',
                          view_count INT DEFAULT 0 COMMENT '浏览数',
                          del_flag CHAR(1) DEFAULT '0' COMMENT '删除标志',
                          create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          PRIMARY KEY (id),
                          INDEX idx_user_id (user_id),
                          INDEX idx_pet_id (pet_id),
                          INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态表';

-- 4. 动态评论表
CREATE TABLE post_comment (
                              id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                              post_id BIGINT NOT NULL COMMENT '动态ID',
                              user_id BIGINT NOT NULL COMMENT '评论用户ID',
                              parent_id BIGINT DEFAULT 0 COMMENT '父评论ID（0-一级评论）',
                              content VARCHAR(500) NOT NULL COMMENT '评论内容',
                              del_flag CHAR(1) DEFAULT '0' COMMENT '删除标志',
                              create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              PRIMARY KEY (id),
                              INDEX idx_post_id (post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态评论表';

-- 5. 动态点赞表
CREATE TABLE post_like (
                           id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                           post_id BIGINT NOT NULL COMMENT '动态ID',
                           user_id BIGINT NOT NULL COMMENT '用户ID',
                           create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           PRIMARY KEY (id),
                           UNIQUE KEY uk_post_user (post_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态点赞表';

-- 6. 商品分类表
CREATE TABLE shop_category (
                               id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                               parent_id BIGINT DEFAULT 0 COMMENT '父分类ID',
                               category_name VARCHAR(50) NOT NULL COMMENT '分类名称',
                               icon VARCHAR(255) COMMENT '分类图标',
                               sort_order INT DEFAULT 0 COMMENT '排序',
                               status CHAR(1) DEFAULT '1' COMMENT '状态（0-停用 1-正常）',
                               create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 7. 商品表
CREATE TABLE shop_product (
                              id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                              category_id BIGINT NOT NULL COMMENT '分类ID',
                              product_name VARCHAR(100) NOT NULL COMMENT '商品名称',
                              product_images TEXT COMMENT '商品图片（多个URL以逗号分隔）',
                              description TEXT COMMENT '商品描述',
                              points_price INT NOT NULL COMMENT '积分价格',
                              stock INT DEFAULT 0 COMMENT '库存数量',
                              total_exchange INT DEFAULT 0 COMMENT '总兑换数',
                              status CHAR(1) DEFAULT '1' COMMENT '状态（0-下架 1-上架）',
                              del_flag CHAR(1) DEFAULT '0' COMMENT '删除标志',
                              create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                              PRIMARY KEY (id),
                              INDEX idx_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 8. 兑换订单表
CREATE TABLE shop_order (
                            id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                            order_no VARCHAR(32) NOT NULL COMMENT '订单编号',
                            user_id BIGINT NOT NULL COMMENT '用户ID',
                            product_id BIGINT NOT NULL COMMENT '商品ID',
                            product_name VARCHAR(100) COMMENT '商品名称（快照）',
                            product_image VARCHAR(255) COMMENT '商品图片（快照）',
                            points_price INT NOT NULL COMMENT '兑换积分',
                            quantity INT DEFAULT 1 COMMENT '兑换数量',
                            total_points INT NOT NULL COMMENT '总积分',
                            address_id BIGINT COMMENT '收货地址ID',
                            status CHAR(1) DEFAULT '0' COMMENT '订单状态（0-待发货 1-已发货 2-已完成 3-已取消）',
                            express_no VARCHAR(50) COMMENT '快递单号',
                            express_company VARCHAR(50) COMMENT '快递公司',
                            create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            PRIMARY KEY (id),
                            UNIQUE KEY uk_order_no (order_no),
                            INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='兑换订单表';

-- 9. 用户收货地址表
CREATE TABLE user_address (
                              id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                              user_id BIGINT NOT NULL COMMENT '用户ID',
                              receiver_name VARCHAR(50) NOT NULL COMMENT '收件人',
                              receiver_phone VARCHAR(20) NOT NULL COMMENT '联系电话',
                              province VARCHAR(50) COMMENT '省份',
                              city VARCHAR(50) COMMENT '城市',
                              district VARCHAR(50) COMMENT '区县',
                              detail_address VARCHAR(200) NOT NULL COMMENT '详细地址',
                              is_default CHAR(1) DEFAULT '0' COMMENT '是否默认（0-否 1-是）',
                              create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              PRIMARY KEY (id),
                              INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收货地址表';

-- 10. 积分记录表
CREATE TABLE user_points_log (
                                 id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                 user_id BIGINT NOT NULL COMMENT '用户ID',
                                 points_change INT NOT NULL COMMENT '积分变动（正数为增加，负数为扣减）',
                                 points_balance INT NOT NULL COMMENT '变动后余额',
                                 change_type VARCHAR(20) NOT NULL COMMENT '变动类型（sign_in-签到 post-发布动态 exchange-兑换商品 admin-管理员操作）',
                                 relate_id BIGINT COMMENT '关联业务ID',
                                 remark VARCHAR(200) COMMENT '备注',
                                 create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 PRIMARY KEY (id),
                                 INDEX idx_user_id (user_id),
                                 INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分记录表';