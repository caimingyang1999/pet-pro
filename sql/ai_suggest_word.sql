-- =============================================
-- 宠迹 · AI搜索推荐词建表脚本
-- 说明：AI对话页搜索提示词，根据用户宠物类型智能推荐
-- 作者：Java后端开发工程师
-- 日期：2026-08-20
-- =============================================

-- ------------------------------
-- 1. AI搜索推荐词表
-- ------------------------------
DROP TABLE IF EXISTS ai_suggest_word;
CREATE TABLE ai_suggest_word (
                                id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                word VARCHAR(100) NOT NULL COMMENT '提示词内容',
                                pet_type VARCHAR(20) NOT NULL DEFAULT 'general' COMMENT '宠物类型（general-通用/cat-猫/dog-狗/rabbit-兔子/bird-鸟/fish-鱼/other-其他）',
                                category VARCHAR(30) DEFAULT 'general' COMMENT '分类（general-通用/care-护理/diet-饮食/medical-医疗/behavior-行为/training-训练）',
                                weight INT DEFAULT 100 COMMENT '基础权重（数字越大越靠前）',
                                status CHAR(1) DEFAULT '1' COMMENT '状态（0-停用 1-启用）',
                                del_flag CHAR(1) DEFAULT '0' COMMENT '删除标志（0-存在 1-删除）',
                                create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                PRIMARY KEY (id),
                                INDEX idx_pet_type_status (pet_type, status),
                                INDEX idx_weight (weight DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI搜索推荐词表';

-- ------------------------------
-- 2. 初始化数据
-- ------------------------------

-- 通用提示词
INSERT INTO ai_suggest_word (word, pet_type, category, weight) VALUES
('宠物多久做一次体检？', 'general', 'medical', 100),
('如何给宠物选择合适的粮食？', 'general', 'diet', 95),
('宠物掉毛严重怎么办？', 'general', 'care', 90),
('宠物疫苗需要打哪些？', 'general', 'medical', 85),
('宠物多久洗一次澡？', 'general', 'care', 80),
('如何训练宠物定点排便？', 'general', 'training', 75),
('宠物突然不吃饭怎么回事？', 'general', 'diet', 70),
('宠物呕吐怎么办？', 'general', 'medical', 65);

-- 猫咪提示词
INSERT INTO ai_suggest_word (word, pet_type, category, weight) VALUES
('猫咪不吃饭怎么办？', 'cat', 'diet', 100),
('猫咪多久洗一次澡？', 'cat', 'care', 95),
('猫咪需要打什么疫苗？', 'cat', 'medical', 90),
('如何训练猫咪用猫砂盆？', 'cat', 'training', 85),
('猫咪吐毛球正常吗？', 'cat', 'medical', 80),
('猫咪发情期怎么办？', 'cat', 'behavior', 75),
('猫咪掉毛严重怎么处理？', 'cat', 'care', 70),
('猫咪可以吃哪些人类食物？', 'cat', 'diet', 65),
('猫咪一直叫是什么原因？', 'cat', 'behavior', 60),
('猫咪多久驱虫一次？', 'cat', 'medical', 55);

-- 狗狗提示词
INSERT INTO ai_suggest_word (word, pet_type, category, weight) VALUES
('狗狗挑食怎么办？', 'dog', 'diet', 100),
('狗狗多久洗一次澡？', 'dog', 'care', 95),
('狗狗需要打什么疫苗？', 'dog', 'medical', 90),
('如何训练狗狗定点排便？', 'dog', 'training', 85),
('狗狗拆家怎么办？', 'dog', 'behavior', 80),
('狗狗呕吐拉稀怎么办？', 'dog', 'medical', 75),
('狗狗掉毛严重怎么处理？', 'dog', 'care', 70),
('狗狗可以吃哪些水果？', 'dog', 'diet', 65),
('狗狗出门爆冲怎么训练？', 'dog', 'training', 60),
('狗狗多久驱虫一次？', 'dog', 'medical', 55);

-- 兔子提示词
INSERT INTO ai_suggest_word (word, pet_type, category, weight) VALUES
('兔子吃什么草比较好？', 'rabbit', 'diet', 100),
('兔子需要洗澡吗？', 'rabbit', 'care', 90),
('兔子拉肚子怎么办？', 'rabbit', 'medical', 85),
('兔子会认主人吗？', 'rabbit', 'behavior', 80);

-- 鸟类提示词
INSERT INTO ai_suggest_word (word, pet_type, category, weight) VALUES
('鹦鹉吃什么食物？', 'bird', 'diet', 100),
('鹦鹉怎么训练说话？', 'bird', 'training', 90),
('鸟笼需要多大合适？', 'bird', 'care', 85),
('鹦鹉掉毛正常吗？', 'bird', 'medical', 80);

-- 鱼类提示词
INSERT INTO ai_suggest_word (word, pet_type, category, weight) VALUES
('鱼缸多久换一次水？', 'fish', 'care', 100),
('观赏鱼吃什么饲料？', 'fish', 'diet', 90),
('鱼身上长白点怎么办？', 'fish', 'medical', 85),
('鱼缸水质怎么维护？', 'fish', 'care', 80);
