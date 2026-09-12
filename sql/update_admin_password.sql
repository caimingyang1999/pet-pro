-- ============================================================
-- 重置超级管理员（admin）登录密码
-- ------------------------------------------------------------
-- 目标账号 : admin (user_id = 1)
-- 加密方式 : Spring Security BCryptPasswordEncoder（强度 10）
-- 存储格式 : $2a$10$ + 22位盐 + 31位密文，共 60 字符
-- ------------------------------------------------------------
-- 说明：
--   1. 下方 password 为 BCrypt 哈希值，不可反推明文；
--      每次加密结果都不同（盐随机），但都能校验通过。
--   2. 如需再换密码，请勿手写哈希，改用后端
--      SecurityUtils.encryptPassword(明文) 重新生成。
--   3. 直接改库即可生效：登录时密码由数据库读取校验，
--      不受 Redis 缓存影响；已登录的 token 不会失效。
-- ============================================================

UPDATE sys_user
SET password        = '$2a$10$IT/kaaUqdz409Q.4/qnPaeug6PxtI6MQGazjP9SwJUDhhfXooqrE2',
    pwd_update_date = NOW(),
    update_by       = 'admin',
    update_time     = NOW()
WHERE user_id = 1
  AND user_name = 'admin';

-- ------------------------------------------------------------
-- 执行后校验（应命中 1 行）
-- ------------------------------------------------------------
SELECT user_id,
       user_name,
       nick_name,
       status,
       del_flag,
       pwd_update_date,
       password
FROM sys_user
WHERE user_id = 1
  AND user_name = 'admin';
