package com.jingji.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * 全局异常处理器
 * 统一处理 Controller 层抛出的异常
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