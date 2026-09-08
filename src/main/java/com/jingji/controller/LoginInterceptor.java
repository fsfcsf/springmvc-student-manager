package com.jingji.controller;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 登录拦截器
 * 检查用户是否登录，未登录则跳转到首页
 *
 * @author 张三
 * @date 2025-11-20
 */
public class LoginInterceptor implements HandlerInterceptor {

    /**
     * 在请求处理之前执行
     * 返回 true 放行，返回 false 拦截
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        // 获取当前 session，参数 false 表示不创建新 session
        HttpSession session = request.getSession(false);
        // 判断 session 中是否有登录用户信息
        if (session != null && session.getAttribute("loginUser") != null) {
            return true; // 已登录，放行
        }
        // 未登录，重定向到首页
        response.sendRedirect(request.getContextPath() + "/index.jsp");
        return false; // 不放行
    }
}