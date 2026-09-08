package com.jingji.controller;

import com.jingji.entity.Student;
import com.jingji.service.StudentService;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * ═══════════════════════════════════════════════════════════════════
 * 登录拦截器 —— 未登录用户一律挡回首页（认证鉴权）
 * ═══════════════════════════════════════════════════════════════════
 * 【拦截器 vs 过滤器（面试高频对比）】
 *   Filter（过滤器）     Servlet 规范，由 Tomcat 调用，作用域是"所有请求"，
 *                        粒度粗；本项目 web.xml 里的编码过滤器就是它；
 *   Interceptor（拦截器）Spring MVC 提供，由 DispatcherServlet 调用，
 *                        能拿到 Handler（即将执行的方法）信息，粒度细，
 *                        preHandle / postHandle / afterCompletion 三个时机可插手。
 *   执行顺序：Filter → DispatcherServlet → Interceptor.preHandle → Controller
 *
 * 【拦截器的三个回调时机】
 *   preHandle        目标方法之前——本类只重写了它，返回 false 直接中断请求
 *   postHandle       目标方法之后、视图渲染之前——可以改 Model
 *   afterCompletion  视图渲染完成之后——适合清理资源、记录耗时（类似 finally）
 *
 * 【本类的判定流程】
 *   ① Session 里有 loginUser？→ 已登录，放行
 *   ② 没有？检查 Cookie 里的 rememberToken，能查到用户 → 自动登录，放行
 *   ③ 都没有 → 重定向到登录页，拦截
 *
 * @author 张三
 * @date 2025-11-20
 */
public class LoginInterceptor implements HandlerInterceptor {

    // 依赖 Service 做"Token 换用户"。
    // 注意：本类没有 @Component 注解，不走注解扫描那条路，而是在
    // springmvc.xml 的 <mvc:interceptor> 里用 <bean> 声明、
    // 由 <property> 完成 setter 注入——所以这里不能指望 @Autowired 生效
    private StudentService studentService;

    public void setStudentService(StudentService studentService) {
        this.studentService = studentService;
    }

    /**
     * 目标方法执行之前调用
     * 返回 true 放行（继续执行后续拦截器和 Controller 方法），返回 false 中断
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        // 1. 先检查 Session 中是否有登录用户
        HttpSession session = request.getSession(false); //`request.getSession(false)`：**不创建新 session**。
        if (session != null && session.getAttribute("loginUser") != null) {
            return true; // `session != null`：服务器拿到了客户端传过来的 JSESSIONID，session 对象真实存在 并且已登录，放行 session 存在并且存有`loginUser`，代表本次会话已经登录，直接放行。
        }

        // 2. 检查"记住我"Cookie，尝试自动登录
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("rememberToken".equals(cookie.getName())) {
                    String token = cookie.getValue().trim();  //cookie 拿到的 token 如果带空格 即""，数据库匹配失败；可增加`token.trim()`
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

        // 3. 未登录，重定向到首页 这里是登录界面
        response.sendRedirect(request.getContextPath() + "/index.jsp");
        return false; // 不放行
    }
}