-- 签到记录表
CREATE TABLE user_sign_in (
                              id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                              user_id BIGINT NOT NULL COMMENT '用户ID',
                              sign_date DATE NOT NULL COMMENT '签到日期',
                              points_reward INT DEFAULT 5 COMMENT '签到奖励积分',
                              create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              PRIMARY KEY (id),
                              UNIQUE KEY uk_user_sign_date (user_id, sign_date) COMMENT '用户+日期唯一索引，防止同一天重复签到',
                              INDEX idx_user_id (user_id),
                              INDEX idx_sign_date (sign_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到记录表';
