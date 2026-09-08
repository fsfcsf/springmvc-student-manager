-- ============================================
-- 学生管理系统 - 数据库升级脚本
-- 在 MySQL 中执行此脚本升级 student2 表结构
-- ============================================

USE springdb;

-- 1. 新增 MD5 密码字段（存储加密后的密码，与明文 upass 分离）
ALTER TABLE student2 ADD COLUMN upass_md5 VARCHAR(32) COMMENT 'MD5加密后的密码';

-- 2. 新增手机号字段
ALTER TABLE student2 ADD COLUMN phone VARCHAR(11) COMMENT '手机号';

-- 3. 新增性别字段
ALTER TABLE student2 ADD COLUMN gender VARCHAR(2) COMMENT '性别（男/女）';

-- 4. 新增创建时间字段
ALTER TABLE student2 ADD COLUMN create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';

-- 5. 新增更新时间字段
ALTER TABLE student2 ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间';

-- 6. 将已有数据中明文密码的 MD5 值同步到 upass_md5 字段
-- （数据库中密码长度=32 的已经是 MD5，直接复制；长度≠32 的是明文，需要手动处理）
-- 注意：MySQL 内置 MD5 函数，这里先更新长度=32 的已加密密码
UPDATE student2 SET upass_md5 = upass WHERE CHAR_LENGTH(upass) = 32;

-- 7. 把明文密码也用 MD5 加密后存入 upass_md5
UPDATE student2 SET upass_md5 = MD5(upass) WHERE CHAR_LENGTH(upass) != 32;

-- 8. 新增头像字段（存储头像文件路径）
ALTER TABLE student2 ADD COLUMN avatar VARCHAR(200) COMMENT '头像路径';

-- 9. 新增记住我 Token 字段
ALTER TABLE student2 ADD COLUMN remember_token VARCHAR(64) COMMENT '记住我自动登录Token';

-- 10. 将 id 字段改为自增（方便新增学生时自动生成主键）
ALTER TABLE student2 MODIFY COLUMN id INT NOT NULL AUTO_INCREMENT;