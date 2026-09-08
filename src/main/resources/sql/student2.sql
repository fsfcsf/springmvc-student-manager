/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80041
 Source Host           : 127.0.0.1:3306
 Source Schema         : springdb

 Target Server Type    : MySQL
 Target Server Version : 80041
 File Encoding         : 65001

 Date: 08/09/2026 09:48:26
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for student2
-- ----------------------------
DROP TABLE IF EXISTS `student2`;
CREATE TABLE `student2`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `uname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `upass` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `upass_md5` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'MD5加密后的密码',
  `age` int NULL DEFAULT NULL,
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '手机号',
  `gender` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '性别（男/女）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `avatar` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '头像路径',
  `remember_token` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '记住我自动登录Token',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of student2
-- 学习点：INSERT 必须显式写出字段名。本表经历过多轮加字段升级（见 upgrade.sql），
-- 若按"INSERT INTO 表 VALUES (...)"按位置插入，列数对不上会直接报错：
-- Column count doesn't match value count at row 1
-- 大部分演示数据只填早期 6 个字段（upass_md5 为 NULL），
-- 用户首次登录时由 Service 的"惰性升级"逻辑自动补齐 upass_md5；
-- 最后 3 行是已升级过的数据：upass 存明文、upass_md5 存对应哈希
-- （如 0a113ef... 即 MD5('888')），与注册功能的"双写"设计一致
-- ----------------------------
INSERT INTO `student2` (id, name, uname, upass, age, email) VALUES (4, '张三', '123456', '123456', 40, '123456@qq.com');
INSERT INTO `student2` (id, name, uname, upass, age, email) VALUES (6, '张四', '456789', '456789', 40, '456789@q.com');
INSERT INTO `student2` (id, name, uname, upass, age, email) VALUES (55, '55', '55', '55', 55, '1@1.com');
INSERT INTO `student2` (id, name, uname, upass, age, email) VALUES (999, '999', '99', '99', 99, '444@QQ.COM');
INSERT INTO `student2` (id, name, uname, upass, age, email) VALUES (101, '101', '101', '101', 101, '1@1.com');
INSERT INTO `student2` (id, name, uname, upass, age, email) VALUES (145, '大', '答复', '00', 11, '333@QQ.com');
INSERT INTO `student2` (id, name, uname, upass, age, email) VALUES (16, '粉丝', '147', '147', 11, '333@QQ.com');
INSERT INTO `student2` (id, name, uname, upass, age, email) VALUES (9999, '9999', '9999', '99999', 99, '1@1.com');
INSERT INTO `student2` (id, name, uname, upass, age, email) VALUES (122, '777', '777', '777', 77, '444@QQ.COM');
INSERT INTO `student2` (id, name, uname, upass, age, email) VALUES (44, '666', '666', '666', 66, '3422099490@qq.com');
INSERT INTO `student2` (id, name, uname, upass, age, email) VALUES (88, '44', '44', '44', 44, '18339006732@139.com');
INSERT INTO `student2` (id, name, uname, upass, age, email) VALUES (888, '555', '555', '555', 55, '333@QQ.com');
INSERT INTO `student2` (id, name, uname, upass, age, email) VALUES (123, '123', '123', '123', 123, '34224@qq.com');
INSERT INTO `student2` (id, name, uname, upass, age, email) VALUES (4444, '4444', '4444', '4444', 4, '1@1.com');
INSERT INTO `student2` (id, name, uname, upass, age, email) VALUES (3, '00', '88', '00', 22, '111');
INSERT INTO `student2` (id, name, uname, upass, upass_md5, age, email) VALUES (44, '方法', '888', '888', '0a113ef6b61820daa5611c870ed8d5ee', 88, '888789@qq.com');
INSERT INTO `student2` (id, name, uname, upass, upass_md5, age, email) VALUES (1444, 'ddad', '999', '999', 'b706835de79a2b4e80506f582af3676a', 11, '3422099490@qq.com');
INSERT INTO `student2` (id, name, uname, upass, upass_md5, age, email) VALUES (232, '奥多西', '0111', '0111', '7d7c45b9a935cf9d845fc75679a41559', 11, '18339006732@139.com');

SET FOREIGN_KEY_CHECKS = 1;
