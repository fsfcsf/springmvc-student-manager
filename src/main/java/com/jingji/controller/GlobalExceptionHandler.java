package com.jingji.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * ═══════════════════════════════════════════════════════════════════
 * 全局异常处理器 —— 所有 Controller 抛出的异常统一在这里"兜底"
 * ═══════════════════════════════════════════════════════════════════
 * 【没有它会怎样】
 *   异常一路抛到 Tomcat，用户看到的是 HTTP 500 白页 + 异常堆栈——
 *   既不友好，还把类名/SQL 等内部信息暴露给外部（信息泄露风险）。
 *
 * 【@ControllerAdvice 原理（面试高频）】
 *   本质是 AOP 思想的 Web 化应用：DispatcherServlet 把标了
 *   @ControllerAdvice 的 Bean 中的 @ExceptionHandler 方法注册成
 *   全局异常解析表，任何 Controller 抛出的匹配类型异常
 *   都会被路由到对应方法处理——Controller 里从此不用写 try-catch。
 *
 * 【可扩展的分级处理】
 *   @ExceptionHandler(具体异常.class)   → 精确匹配优先于 Exception 兜底
 *   方法加 @ResponseBody 返回 JSON      → 前后端分离场景的标准做法
 *   自定义 BusinessException 与系统异常分开 → 业务错提示用户、系统错记日志报警
 *
 * @author 张三
 * @date 2025-11-20
 */
@ControllerAdvice // 声明这是一个全局异常处理类
public class GlobalExceptionHandler {

    // 日志对象
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理所有 Exception 类型的异常
     * 记录日志并返回友好的错误提示
     */
    @ExceptionHandler(Exception.class) // 捕获所有异常
    public String handleException(Exception e, Model model) {
        // 打印异常堆栈到日志
        log.error("系统异常: ", e);
        // 给用户返回友好的提示信息，不暴露具体错误
        model.addAttribute("error", "系统繁忙，请稍后再试");
        return "index"; // 返回首页
    }
}