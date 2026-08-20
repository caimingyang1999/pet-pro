-- =============================================
-- 宠迹 · AI对话功能建表脚本
-- 说明：小程序端AI对话功能，包含会话表和消息表
-- 作者：数据库设计工程师
-- 日期：2026-08-19
-- =============================================

-- ------------------------------
-- 1. AI对话会话表
-- ------------------------------
DROP TABLE IF EXISTS ai_chat_session;
CREATE TABLE ai_chat_session (
                                id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                user_id BIGINT NOT NULL COMMENT '用户ID（关联sys_user.user_id）',
                                session_title VARCHAR(100) DEFAULT '新对话' COMMENT '会话标题',
                                session_type VARCHAR(20) DEFAULT 'general' COMMENT '会话类型（general-通用咨询 pet_care-宠物护理 pet_medical-宠物医疗 pet_diet-宠物饮食 pet_behavior-宠物行为）',
                                model VARCHAR(50) DEFAULT 'gpt-3.5-turbo' COMMENT '使用的AI模型名称',
                                message_count INT DEFAULT 0 COMMENT '消息总数',
                                status CHAR(1) DEFAULT '1' COMMENT '会话状态（1-正常 0-已删除）',
                                del_flag CHAR(1) DEFAULT '0' COMMENT '删除标志（0-存在 1-删除）',
                                create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                PRIMARY KEY (id),
                                INDEX idx_user_id (user_id),
                                INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话会话表';

-- ------------------------------
-- 2. AI对话消息表
-- ------------------------------
DROP TABLE IF EXISTS ai_chat_message;
CREATE TABLE ai_chat_message (
                                 id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                 session_id BIGINT NOT NULL COMMENT '会话ID（关联ai_chat_session.id）',
                                 user_id BIGINT NOT NULL COMMENT '用户ID（关联sys_user.user_id）',
                                 role VARCHAR(20) NOT NULL COMMENT '消息角色（user-用户消息 assistant-AI回复 system-系统消息）',
                                 content TEXT NOT NULL COMMENT '消息内容',
                                 content_type VARCHAR(20) DEFAULT 'text' COMMENT '内容类型（text-文本 image-图片）',
                                 token_used INT DEFAULT 0 COMMENT '消耗Token数',
                                 model VARCHAR(50) COMMENT '使用的AI模型名称',
                                 status CHAR(1) DEFAULT '1' COMMENT '消息状态（1-成功 0-失败 2-生成中）',
                                 error_msg VARCHAR(500) COMMENT '错误信息（生成失败时记录）',
                                 del_flag CHAR(1) DEFAULT '0' COMMENT '删除标志（0-存在 1-删除）',
                                 create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 PRIMARY KEY (id),
                                 INDEX idx_session_id (session_id),
                                 INDEX idx_user_id (user_id),
                                 INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话消息表';
