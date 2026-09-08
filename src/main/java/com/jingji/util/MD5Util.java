package com.jingji.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5 加密工具类
 * 用于对密码进行不可逆加密
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
            // 获取 MD5 加密算法的实例
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 把字符串转成字节数组后进行加密
            byte[] bytes = md.digest(source.getBytes());
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