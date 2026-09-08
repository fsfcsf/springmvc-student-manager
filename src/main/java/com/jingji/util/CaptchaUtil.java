package com.jingji.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.Random;

/**
 * 验证码工具类
 * 生成 4 位随机字符 + 干扰线的图片验证码
 *
 * @author 张三
 * @date 2025-11-22
 */
public class CaptchaUtil {

    // 验证码图片宽度
    private static final int WIDTH = 100;
    // 验证码图片高度
    private static final int HEIGHT = 38;
    // 验证码字符个数
    private static final int CODE_LENGTH = 4;
    // 随机数生成器
    private static final Random RANDOM = new Random();
    // 验证码字符集，去掉容易混淆的 0、O、1、l
    private static final String CHAR_SET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";

    /**
     * 生成随机验证码字符串
     * @return 4 位随机字符
     */
    public static String generateCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = RANDOM.nextInt(CHAR_SET.length());
            sb.append(CHAR_SET.charAt(index));
        }
        return sb.toString();
    }

    /**
     * 把验证码字符串绘制成图片，通过输出流返回给浏览器
     * @param code 验证码字符串
     * @param out  输出流（通常是 response.getOutputStream()）
     */
    public static void drawImage(String code, OutputStream out) throws Exception {
        // 1. 创建空白图片缓冲区
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) image.getGraphics();

        // 2. 填充白色背景
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // 3. 逐个绘制验证码字符
        g.setFont(new Font("Arial", Font.BOLD, 22));
        for (int i = 0; i < code.length(); i++) {
            // 随机颜色
            g.setColor(new Color(RANDOM.nextInt(100), RANDOM.nextInt(100), RANDOM.nextInt(180)));
            // 字符位置加随机偏移，防止被 OCR 识别
            g.drawString(String.valueOf(code.charAt(i)), 18 * i + 8, 24 + RANDOM.nextInt(6));
        }

        // 4. 绘制干扰线（让机器更难识别）
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < 5; i++) {
            int x1 = RANDOM.nextInt(WIDTH);
            int y1 = RANDOM.nextInt(HEIGHT);
            int x2 = RANDOM.nextInt(WIDTH);
            int y2 = RANDOM.nextInt(HEIGHT);
            g.drawLine(x1, y1, x2, y2);
        }

        // 5. 输出为 JPEG 格式的图片流
        g.dispose();
        ImageIO.write(image, "JPEG", out);
    }
}