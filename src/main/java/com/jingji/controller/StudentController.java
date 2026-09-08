package com.jingji.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jingji.entity.Student;
import com.jingji.service.StudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * 学生管理控制器
 * 处理学生相关的增删改查请求
 *
 * @author 张三
 * @date 2025-11-10
 */
@Controller // 声明这是一个控制器类
@RequestMapping("/user") // 所有请求路径都以 /user 开头
public class StudentController {

    // 日志对象，用来打印日志信息
    private static final Logger log = LoggerFactory.getLogger(StudentController.class);

    // 自动注入 StudentService 接口的实现类
    @Autowired
    private StudentService studentService;

    /**
     * 处理登录请求
     * 从表单获取账号密码，调用 service 进行校验
     */
    @PostMapping("/login") // 只处理 POST 请求
    public String login(@RequestParam("uname") String uname,
                        @RequestParam("password") String password,
                        HttpServletRequest request,
                        Model model) {
        // 1. 先判断账号密码是否为空
        if (uname.trim().isEmpty() || password.trim().isEmpty()) {
            model.addAttribute("error", "账号和密码不能为空");
            return "index"; // 返回登录页
        }

        // 2. 调用 service 层进行登录校验
        Student student = studentService.login(uname, password);
        if (student != null) {
            // 3. 登录成功，把用户信息存到 session 中
            HttpSession session = request.getSession();
            session.setAttribute("loginUser", student);
            log.info("用户登录成功: {}", uname);
            return "redirect:/user/showAllStudent"; // 重定向到学生列表
        } else {
            // 4. 登录失败，返回错误信息
            model.addAttribute("error", "账号或密码错误");
            return "index";
        }
    }

    /**
     * 处理注册请求
     * 接收表单数据，校验后保存到数据库
     */
    @PostMapping("/register")
    public String register(@RequestParam("id") int id,
                           @RequestParam("name") String name,
                           @RequestParam("uname") String uname,
                           @RequestParam("upass") String upass,
                           @RequestParam("age") int age,
                           @RequestParam("email") String email,
                           Model model) {
        // 后端输入校验：防止空数据提交
        if (name.trim().isEmpty() || uname.trim().isEmpty()
                || upass.trim().isEmpty() || email.trim().isEmpty()) {
            model.addAttribute("error", "请填写所有必填字段");
            return "register"; // 返回注册页
        }
        // 校验邮箱格式是否正确
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            model.addAttribute("error", "邮箱格式不正确");
            return "register";
        }

        // 封装学生对象
        Student student = new Student();
        student.setId(id);
        student.setName(name);
        student.setUname(uname);
        student.setUpass(upass);
        student.setAge(age);
        student.setEmail(email);

        // 调用 service 保存到数据库
        studentService.addStudent(student);
        return "registerSuccess"; // 跳转到注册成功页
    }

    /**
     * 查询所有学生并显示到页面（支持分页）
     * @param pageNum  当前页码，默认第 1 页
     * @param pageSize 每页显示条数，默认 5 条
     */
    @GetMapping("/showAllStudent") // 处理 GET 请求
    public ModelAndView showAllStudent(@RequestParam(defaultValue = "1") int pageNum,
                                       @RequestParam(defaultValue = "5") int pageSize) {
        // 使用 PageHelper 分页：传入页码和每页条数，紧跟着的第一个查询会被自动分页
        PageHelper.startPage(pageNum, pageSize);
        // 调用 service 查询所有学生数据
        List<Student> list = studentService.queryAll();
        // 用 PageInfo 包装查询结果，获取分页信息（总页数、总条数等）
        PageInfo<Student> pageInfo = new PageInfo<>(list);

        // 使用 ModelAndView 携带数据并跳转页面
        ModelAndView mav = new ModelAndView("showAllStudent");
        mav.addObject("dateList", list);       // 学生列表
        mav.addObject("pageInfo", pageInfo);   // 分页信息，页面用 ${pageInfo.xxx} 取值
        return mav;
    }

    /**
     * 根据姓名模糊查询学生
     */
    @PostMapping("/selectByName")
    public ModelAndView selectByName(@RequestParam("name") String name) {
        // 拼接模糊查询的 SQL 条件，前后加 %
        String names = "%" + name + "%";
        List<Student> students = studentService.selectByName(names);

        // 返回查询结果页面
        ModelAndView mav = new ModelAndView("queryStudentByName");
        mav.addObject("dateList", students);
        return mav;
    }

    /**
     * 根据 ID 删除学生
     * 使用 RedirectAttributes 在重定向后传递提示信息
     */
    @GetMapping("/deleteStudent")
    public String deleteStudent(@RequestParam("id") int stuId,
                                RedirectAttributes redirectAttributes) {
        try {
            // 执行删除操作
            studentService.deleteStudent(stuId);
            // addFlashAttribute 可以在重定向后仍然取到数据
            redirectAttributes.addFlashAttribute("message", "学生删除成功！");
        } catch (Exception e) {
            // 删除失败时记录日志并返回错误信息
            log.error("删除学生失败: id={}", stuId, e);
            redirectAttributes.addFlashAttribute("error", "删除失败：" + e.getMessage());
        }
        // 重定向回学生列表页
        return "redirect:/user/showAllStudent";
    }

    /**
     * 跳转到编辑学生页面
     * 根据 ID 查询学生信息，回显到表单中
     */
    @GetMapping("/editStudent")
    public String editStudent(@RequestParam("id") int id, Model model) {
        // 根据 ID 查询学生信息
        Student student = studentService.queryStudentById(id);
        // 把学生对象放到 model 中，页面用 ${student.xxx} 取值
        model.addAttribute("student", student);
        return "editStudent"; // 跳转到编辑页面
    }

    /**
     * 处理更新学生请求
     * 接收表单提交的修改数据
     */
    @PostMapping("/updateStudent")
    public String updateStudent(Student student, Model model) {
        try {
            // Spring 会自动把表单参数封装到 Student 对象中
            studentService.updateStudent(student);
            model.addAttribute("message", "更新成功！");
        } catch (Exception e) {
            log.error("更新学生失败: id={}", student.getId(), e);
            model.addAttribute("error", "更新失败：" + e.getMessage());
        }
        return "editStudent"; // 更新后留在编辑页面，显示提示信息
    }

    /**
     * 退出登录
     * 销毁 session，跳回首页
     */
    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        // 获取当前 session，参数 false 表示没有 session 时不创建新的
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate(); // 销毁 session
        }
        return "redirect:/index.jsp"; // 跳回登录首页
    }
}