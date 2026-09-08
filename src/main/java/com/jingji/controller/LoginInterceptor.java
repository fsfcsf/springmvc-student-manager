package com.jingji.controller;

import com.jingji.entity.Student;
import com.jingji.service.StudentService;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 登录拦截器
 * 检查用户是否登录，支持"记住我"Cookie 自动登录
 *
 * @author 张三
 * @date 2025-11-20
 */
public class LoginInterceptor implements HandlerInterceptor {

    // 自动注入 Service（注意：拦截器不由 Spring 管理，这里用 setter 注入）
    private StudentService studentService;

    public void setStudentService(StudentService studentService) {
        this.studentService = studentService;
    }

    /**
     * 在请求处理之前执行
     * 返回 true 放行，返回 false 拦截
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        // 1. 先检查 Session 中是否有登录用户
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("loginUser") != null) {
            return true; // 已登录，放行
        }

        // 2. 检查"记住我"Cookie，尝试自动登录
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("rememberToken".equals(cookie.getName())) {
                    String token = cookie.getValue();
                    if (token != null && !token.isEmpty()) {
                        // 根据 Token 查询用户
                        Student student = studentService.findByToken(token);
                        if (student != null) {
                            // 自动登录成功，把用户信息存入 Session
                            request.getSession().setAttribute("loginUser", student);
                            return true; // 放行
                        }
                    }
                }
            }
        }

        // 3. 未登录，重定向到首页
        response.sendRedirect(request.getContextPath() + "/index.jsp");
        return false; // 不放行
    }
}