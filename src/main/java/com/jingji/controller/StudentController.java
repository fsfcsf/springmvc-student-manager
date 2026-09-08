package com.jingji.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jingji.entity.Student;
import com.jingji.service.StudentService;
import com.jingji.util.CaptchaUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.UUID;

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
                        @RequestParam("captcha") String captcha,
                        @RequestParam(defaultValue = "false") boolean rememberMe,
                        HttpServletRequest request,
                        HttpServletResponse response,
                        Model model) {
        // 1. 先判断账号密码是否为空
        if (uname.trim().isEmpty() || password.trim().isEmpty()) {
            model.addAttribute("error", "账号和密码不能为空");
            return "index"; // 返回登录页
        }
        // 2. 校验验证码
        HttpSession session = request.getSession();
        String sessionCaptcha = (String) session.getAttribute("captcha");
        if (sessionCaptcha == null || !sessionCaptcha.equalsIgnoreCase(captcha.trim())) {
            model.addAttribute("error", "验证码错误");
            return "index";
        }
        // 3. 清除 session 中的验证码，防止重复使用
        session.removeAttribute("captcha");

        // 4. 调用 service 层进行登录校验
        Student student = studentService.login(uname, password);
        if (student != null) {
            // 5. 登录成功，把用户信息存到 session 中
            session.setAttribute("loginUser", student);

            // 6. 记住我：生成 Token 存入 Cookie 和数据库
            if (rememberMe) {
                String token = UUID.randomUUID().toString().replace("-", "");
                Cookie cookie = new Cookie("rememberToken", token);
                cookie.setMaxAge(7 * 24 * 60 * 60); // 7 天有效
                cookie.setPath("/");
                response.addCookie(cookie);
                studentService.updateToken(student.getId(), token);
                log.info("记住我已开启: uname={}", uname);
            }

            log.info("用户登录成功: {}", uname);
            return "redirect:/user/showAllStudent"; // 重定向到学生列表
        } else {
            // 7. 登录失败，返回错误信息
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
                           @RequestParam(defaultValue = "") String phone,
                           @RequestParam(defaultValue = "") String gender,
                           Model model,
                           RedirectAttributes redirectAttributes) {
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
        student.setPhone(phone);
        student.setGender(gender);

        // 调用 service 保存到数据库
        studentService.addStudent(student);
        // 使用 flash 属性传递成功信息，重定向后仍可获取
        redirectAttributes.addFlashAttribute("message", "学生 " + name + " 注册成功！");
        return "redirect:/user/registerSuccess"; // 重定向到注册成功页
    }

    /**
     * 注册成功页面
     * 显示成功信息，提供返回管理页面的按钮
     */
    @GetMapping("/registerSuccess")
    public String registerSuccess() {
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
     * 多条件查询学生（支持分页）
     * 支持按 ID、姓名、年龄、邮箱组合查询，任意字段为空时不参与查询
     */
    @GetMapping("/search")
    public ModelAndView search(@RequestParam(defaultValue = "0") int id,
                               @RequestParam(defaultValue = "") String name,
                               @RequestParam(defaultValue = "0") int age,
                               @RequestParam(defaultValue = "") String email,
                               @RequestParam(defaultValue = "1") int pageNum,
                               @RequestParam(defaultValue = "5") int pageSize) {
        // 把查询条件封装到 Student 对象中
        Student condition = new Student();
        condition.setId(id);
        condition.setName(name);
        condition.setAge(age);
        condition.setEmail(email);

        // 使用 PageHelper 分页
        PageHelper.startPage(pageNum, pageSize);
        List<Student> list = studentService.searchStudents(condition);
        PageInfo<Student> pageInfo = new PageInfo<>(list);

        // 返回结果页面，同时回传查询条件用于表单回显
        ModelAndView mav = new ModelAndView("queryStudentByName");
        mav.addObject("dateList", list);
        mav.addObject("pageInfo", pageInfo);
        mav.addObject("id", id == 0 ? "" : id);       // 回显查询条件
        mav.addObject("name", name);
        mav.addObject("age", age == 0 ? "" : age);
        mav.addObject("email", email);
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
     * 批量删除学生
     * 接收前端传过来的 ids 数组，一次删除多条记录
     */
    @PostMapping("/batchDelete")
    public String batchDelete(@RequestParam("ids") List<Integer> ids,
                              RedirectAttributes redirectAttributes) {
        if (ids == null || ids.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "请至少选择一条记录");
            return "redirect:/user/showAllStudent";
        }
        try {
            int count = studentService.batchDelete(ids);
            redirectAttributes.addFlashAttribute("message", "批量删除成功，共删除 " + count + " 条记录");
        } catch (Exception e) {
            log.error("批量删除失败: ids={}", ids, e);
            redirectAttributes.addFlashAttribute("error", "批量删除失败：" + e.getMessage());
        }
        return "redirect:/user/showAllStudent";
    }

    /**
     * 跳转到修改密码页面
     */
    @GetMapping("/changePassword")
    public String changePassword() {
        return "changePassword";
    }

    /**
     * 处理修改密码请求
     * 校验原密码，加密新密码，更新数据库
     */
    @PostMapping("/changePassword")
    public String changePassword(@RequestParam("oldPass") String oldPass,
                                 @RequestParam("newPass") String newPass,
                                 @RequestParam("confirmPass") String confirmPass,
                                 HttpServletRequest request,
                                 Model model) {
        // 从 Session 中获取当前登录用户
        Student loginUser = (Student) request.getSession().getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/index.jsp";
        }

        // 校验新密码和确认密码是否一致
        if (!newPass.equals(confirmPass)) {
            model.addAttribute("error", "两次输入的新密码不一致");
            return "changePassword";
        }

        // 调用 service 修改密码
        boolean result = studentService.changePassword(loginUser.getId(), oldPass, newPass);
        if (result) {
            model.addAttribute("message", "密码修改成功！");
        } else {
            model.addAttribute("error", "原密码错误，修改失败");
        }
        return "changePassword";
    }

    /**
     * 导出学生数据为 Excel 文件
     * 使用 Apache POI 生成 .xls 文件，通过 response 输出流下载
     */
    @GetMapping("/export")
    public void exportExcel(HttpServletResponse response) {
        OutputStream os = null;
        Workbook workbook = null;
        try {
            // 1. 查询所有学生数据
            List<Student> list = studentService.queryAll();

            // 2. 创建 Excel 工作簿（.xls 格式）
            workbook = new HSSFWorkbook();
            Sheet sheet = workbook.createSheet("学生信息表");

            // 3. 创建表头行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"编号", "姓名", "账号", "年龄", "邮箱", "手机号", "性别"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            // 4. 填充数据行
            for (int i = 0; i < list.size(); i++) {
                Student s = list.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(s.getId());
                row.createCell(1).setCellValue(s.getName());
                row.createCell(2).setCellValue(s.getUname());
                row.createCell(3).setCellValue(s.getAge());
                row.createCell(4).setCellValue(s.getEmail());
                row.createCell(5).setCellValue(s.getPhone());
                row.createCell(6).setCellValue(s.getGender());
            }

            // 5. 设置响应头，告诉浏览器这是一个 Excel 文件下载
            response.setContentType("application/vnd.ms-excel");
            String fileName = URLEncoder.encode("学生信息表.xls", "UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

            // 6. 把 Excel 数据写入响应输出流
            os = response.getOutputStream();
            workbook.write(os);
            os.flush();
            log.info("导出 Excel 成功，共 {} 条数据", list.size());
        } catch (Exception e) {
            log.error("导出 Excel 失败", e);
            // 如果响应头还没发送，返回错误信息给用户
            try {
                response.setContentType("text/html;charset=UTF-8");
                response.getWriter().write("<script>alert('导出失败：" + e.getMessage() + "');history.back();</script>");
            } catch (Exception ignored) {
            }
        } finally {
            // 无论成功还是失败，都要关闭资源
            try {
                if (workbook != null) {
                    workbook.close();
                }
            } catch (Exception ignored) {
            }
            try {
                if (os != null) {
                    os.close();
                }
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 退出登录
     * 销毁 session，清除记住我 Cookie，跳回首页
     */
    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        // 获取当前 session，参数 false 表示没有 session 时不创建新的
        HttpSession session = request.getSession(false);
        if (session != null) {
            Student loginUser = (Student) session.getAttribute("loginUser");
            if (loginUser != null) {
                studentService.updateToken(loginUser.getId(), null); // 清除数据库中的 Token
            }
            session.invalidate(); // 销毁 session
        }
        // 清除记住我 Cookie
        Cookie cookie = new Cookie("rememberToken", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        return "redirect:/index.jsp"; // 跳回登录首页
    }

    /**
     * 生成验证码图片
     * 生成 4 位随机字符，存入 Session，返回图片流
     */
    @GetMapping("/captcha")
    public void captcha(HttpServletRequest request, HttpServletResponse response) {
        try {
            // 1. 生成随机验证码字符串
            String code = CaptchaUtil.generateCode();
            // 2. 存入 Session，供登录时校验
            request.getSession().setAttribute("captcha", code);
            // 3. 设置响应头，告诉浏览器这是一张图片
            response.setContentType("image/jpeg");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expires", 0);
            // 4. 把验证码画成图片，写入响应流
            CaptchaUtil.drawImage(code, response.getOutputStream());
        } catch (Exception e) {
            log.error("生成验证码失败", e);
        }
    }

    /**
     * 个人中心页面
     * 从 Session 获取当前登录用户，展示个人信息
     */
    @GetMapping("/profile")
    public String profile(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Student loginUser = (Student) session.getAttribute("loginUser");
            if (loginUser != null) {
                // 重新从数据库查询，确保数据是最新的
                Student fresh = studentService.queryStudentById(loginUser.getId());
                model.addAttribute("user", fresh);
                // 更新 Session 中的用户信息
                session.setAttribute("loginUser", fresh);
            }
        }
        return "profile";
    }

    /**
     * 更新个人信息
     */
    @PostMapping("/updateProfile")
    public String updateProfile(@RequestParam("name") String name,
                                @RequestParam("age") int age,
                                @RequestParam("email") String email,
                                @RequestParam(defaultValue = "") String phone,
                                @RequestParam(defaultValue = "") String gender,
                                HttpServletRequest request,
                                Model model) {
        HttpSession session = request.getSession(false);
        Student loginUser = (Student) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/index.jsp";
        }

        // 重新查询并更新
        Student student = studentService.queryStudentById(loginUser.getId());
        student.setName(name);
        student.setAge(age);
        student.setEmail(email);
        student.setPhone(phone);
        student.setGender(gender);
        student.setUpass(null); // 不修改密码，防止二次加密
        studentService.updateStudent(student);

        // 更新 Session
        Student fresh = studentService.queryStudentById(loginUser.getId());
        session.setAttribute("loginUser", fresh);
        model.addAttribute("user", fresh);
        model.addAttribute("message", "个人信息更新成功！");
        return "profile";
    }

    /**
     * 上传头像
     */
    @PostMapping("/uploadAvatar")
    public String uploadAvatar(@RequestParam("avatarFile") MultipartFile file,
                               HttpServletRequest request,
                               Model model) {
        HttpSession session = request.getSession(false);
        Student loginUser = (Student) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/index.jsp";
        }

        try {
            if (!file.isEmpty()) {
                // 1. 获取原始文件名，提取后缀
                String originalName = file.getOriginalFilename();
                String suffix = originalName.substring(originalName.lastIndexOf("."));
                // 2. 生成唯一文件名（UUID + 后缀）
                String fileName = UUID.randomUUID().toString().replace("-", "") + suffix;
                // 3. 保存到 webapp/uploads/ 目录下
                String uploadDir = request.getServletContext().getRealPath("/uploads");
                File dir = new File(uploadDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                file.transferTo(new File(dir, fileName));
                // 4. 把文件路径存入数据库
                String avatarPath = "/uploads/" + fileName;
                studentService.updateAvatar(loginUser.getId(), avatarPath);

                // 5. 更新 Session 中的头像
                Student fresh = studentService.queryStudentById(loginUser.getId());
                session.setAttribute("loginUser", fresh);
                model.addAttribute("user", fresh);
                model.addAttribute("message", "头像上传成功！");
            }
        } catch (Exception e) {
            log.error("头像上传失败", e);
            model.addAttribute("error", "头像上传失败：" + e.getMessage());
        }
        return "profile";
    }
}