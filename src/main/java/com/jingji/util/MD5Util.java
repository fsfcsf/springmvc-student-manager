package com.jingji.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * ═══════════════════════════════════════════════════════════════════
 * MD5 加密工具类 —— 对密码做单向摘要（学习用途）
 * ═══════════════════════════════════════════════════════════════════
 * 【MD5 是什么】
 *   消息摘要算法：任意长度输入 → 固定 128 位（32 个十六进制字符）输出；
 *   同一输入永远同一输出、无法从密文反推明文——所以叫"不可逆"。
 *
 * 【局限与正确姿势（面试必问）】
 *   1. 彩虹表攻击：常用密码的 MD5 值早已被做成对照表批量反查；
 *   2. 无盐：两个用户密码相同 → 密文相同，拖库后一目了然；
 *   3. MD5 太快：算得越快越容易被暴力枚举；
 *   4. 生产推荐：BCrypt / PBKDF2 / SHA-256 + 随机盐（慢哈希 + 每用户独立盐）。
 *   本项目用 MD5 学习"密码加密存储"的完整流程，同时了解其不足即可。
 *
 * @author 张三
 * @date 2025-11-15
 */
public class MD5Util {

    /**
     * 对字符串进行 MD5 加密
     *
     * @param source 原始字符串（明文密码）
     * @return 加密后的 32 位十六进制字符串
     */
    public static String encrypt(String source) {
        // 空字符串不加密，直接返回 null
        if (source == null || source.isEmpty()) {
            return null;
        }
        try {
            // 获取 MD5 加密算法的实例（Java 安全 API，还有 SHA-1/SHA-256 可选）
            MessageDigest md = MessageDigest.getInstance("MD5");
            // digest()：一次完成"更新数据 + 计算摘要"，返回 16 字节（128 位）
            // getBytes() 显式指定 UTF-8：不传参则用平台默认编码（Windows 中文系统
            // 是 GBK），一旦换环境（Linux 服务器默认 UTF-8）同一密码会算出不同
            // 摘要，老账号全部登录失败——哈希必须钉死同一种字符编码
            byte[] bytes = md.digest(source.getBytes(StandardCharsets.UTF_8));
            // 把字节数组转成十六进制字符串
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                // %02x 表示输出两位十六进制数，不足两位前面补 0
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // MD5 算法不存在时抛出异常（理论上不会发生）
            throw new RuntimeException("MD5加密失败", e);
        }
    }
}