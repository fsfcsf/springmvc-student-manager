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
 * ═══════════════════════════════════════════════════════════════════
 * 学生管理控制器（Controller 层 / 表现层）—— 整个系统的请求入口
 * ═══════════════════════════════════════════════════════════════════
 * 【在三层架构中的位置】
 *   浏览器 → CharacterEncodingFilter(web.xml 编码过滤器)
 *         → DispatcherServlet(前端控制器，拦截所有 "/" 请求)
 *         → LoginInterceptor(登录拦截器) → 本类(Handler 处理器)
 *         → StudentService(业务层) → StudentDao(持久层) → MySQL
 *
 * 【面试高频：一次请求的完整执行流程（DispatcherServlet 工作原理）】
 *   1. 请求先被 web.xml 中的 CharacterEncodingFilter 统一转成 UTF-8；
 *   2. DispatcherServlet 接管请求，它是 MVC 的"总调度"，本身不写业务；
 *   3. HandlerMapping 根据 @RequestMapping 注解解析出目标方法，
 *      连同拦截器一起封装成 HandlerExecutionChain 执行链；
 *   4. 执行链先跑 LoginInterceptor.preHandle()（返回 false 直接中断）；
 *   5. HandlerAdapter 完成参数绑定：把请求参数按名字映射到
 *      @RequestParam / 实体属性上，再反射调用目标方法；
 *   6. 方法返回字符串的三种含义：
 *      "redirect:/xxx" → 重定向；"forward:/xxx" → 转发；其他 → 逻辑视图名；
 *   7. 逻辑视图名交给 InternalResourceViewResolver 拼接：
 *      前缀 "/" + 视图名 + 后缀 ".jsp" → 定位 webapp 下的真实页面；
 *   8. 渲染 JSP 时，EL 表达式 ${xxx} 从 request 域读取 Model 里放的数据。
 *
 * 【本类核心注解速记】
 *   @Controller              声明控制器，返回值按"逻辑视图名"解析
 *                           （@RestController 则直接把返回值写进响应体）
 *   @RequestMapping("/user") 类级注解：给本类所有方法统一加路径前缀
 *   @GetMapping/@PostMapping 限定请求方式：查询用 GET，写操作用 POST
 *   @Autowired               按类型注入，容器启动时由 Spring 自动装配
 *
 * @author 张三
 * @date 2025-11-10
 */
@Controller // 声明控制器：方法返回的字符串默认按"逻辑视图名"去解析 JSP
@RequestMapping("/user") // 类级路径前缀：本类所有方法的 URL 都以 /user 开头
public class StudentController {

    // 日志对象：SLF4J 门面 + Logback 实现（输出格式见 logback.xml）
    // static final 全类共享一个实例；构造参数传当前类，日志能定位到来源类
    private static final Logger log = LoggerFactory.getLogger(StudentController.class);

    // 按类型注入 StudentService：容器启动时扫描到 @Service 标注的 StudentServiceImpl，
    // 把实现类实例赋给这个字段（面向接口编程，将来换实现类此处代码不用动）
    @Autowired
    private StudentService studentService;

    /**
     * 处理登录请求（POST /user/login）
     * 依次校验账号密码非空、验证码，通过后交给 Service 完成登录
     *
     * 【执行流程】
     *   ① 非空校验 → ② 验证码比对 → ③ 验证码一次性销毁
     *   → ④ Service 校验账号密码 → ⑤ 成功写 Session 跳列表页；失败带错误信息回登录页
     *
     * 【学习要点】
     *   1. 验证码校验完必须立刻 removeAttribute 销毁——否则同一验证码可被
     *      反复提交，配合脚本就能无限试密码，"一次性"是验证码防刷的核心；
     *   2. 登录态存 Session（服务器内存），浏览器靠 Cookie 里的 JSESSIONID 关联；
     *      "记住我"则额外生成 Token 写入 Cookie + 数据库，实现跨 Session 自动登录；
     *   3. 密码错误统一提示"账号或密码错误"，不区分具体哪项错——
     *      避免向攻击者泄露"该账号是否存在"（防账号枚举）；
     *   4. model.addAttribute 的数据默认放 request 域，
     *      转发到 JSP 后用 ${error} 即可取到。
     */
    @PostMapping("/login") // 只处理 POST 请求（登录参数在请求体里，不易被浏览器历史/日志泄露）
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
     * 处理注册请求（POST /user/register）
     * 后端校验后封装成 Student，交给 Service 落库
     *
     * 【执行流程】
     *   ① 必填字段非空校验 → ② 邮箱正则校验 → ③ 封装 Student 对象
     *   → ④ Service 保存（内部同时写入明文 upass 与 MD5 后的 upass_md5）
     *   → ⑤ 重定向到注册成功页
     *
     * 【学习要点】
     *   1. 前端 required 只是体验层校验，攻击者绕开页面直接发请求即可跳过，
     *      所以后端必须重新校验一遍——"前后端双重校验"的意义所在；
     *   2. 邮箱正则 ^[\w.-]+@[\w.-]+\.[a-zA-Z]{2,}$：
     *      [\w.-] 匹配字母/数字/下划线/点/横线，+ 表示至少一位，
     *      顶层域名限定 2 位以上字母（.com / .cn）；
     *   3. addFlashAttribute：普通参数经 redirect 会丢失（重定向是浏览器发起的
     *      第二次请求，request 域数据随之销毁），Flash 属性临时存入 Session，
     *      重定向目标页取一次后自动删除——配合 PRG 模式
     *      （Post-Redirect-Get）防止刷新浏览器时重复提交表单；
     *   4. 改进方向：没有校验账号唯一性，同一 uname 可注册多次——
     *      生产上应先查重（或给 uname 加唯一索引）再插入。
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
     * 注册成功页面（GET /user/registerSuccess）
     * 单独拆一个方法承接重定向，页面用 ${message} 显示 Flash 属性里的成功信息
     */
    @GetMapping("/registerSuccess")
    public String registerSuccess() {
        return "registerSuccess"; // 跳转到注册成功页
    }

    /**
     * 查询所有学生并分页展示（GET /user/showAllStudent）
     *
     * @param pageNum  当前页码，默认第 1 页
     * @param pageSize 每页显示条数，默认 5 条
     *
     * 【PageHelper 分页原理（面试高频）】
     *   1. PageHelper.startPage(pageNum, pageSize) 只是把分页参数存入 ThreadLocal，
     *      本身不执行任何 SQL；
     *   2. 紧随其后的第一条 MyBatis 查询会被 PageInterceptor 插件拦截，
     *      自动改写成"SELECT count(*) 统计总数 + 原 SQL 追加 LIMIT"两条语句；
     *   3. 正因为参数靠 ThreadLocal 传递，startPage 与查询必须紧挨着写，
     *      中间插入别的查询会导致分页参数"串"到别的 SQL 上；
     *   4. PageInfo 包装后自带分页元数据：total（总条数）、pages（总页数）、
     *      pageNum、prePage/nextPage、hasPreviousPage/hasNextPage，
     *      JSP 里直接 ${pageInfo.xxx} 取用。
     *
     * 【ModelAndView vs Model 的区别】
     *   ModelAndView：视图名和数据一体，都由自己 new，"一步到位"；
     *   Model：由 Spring 注入，方法返回值只写视图名字符串。
     *   两者最终等价，团队代码里更常见 Model + String 的组合。
     */
    @GetMapping("/showAllStudent") // 处理 GET 请求（查询操作，参数在 URL 上，可收藏可分享）
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
     * 根据姓名模糊查询学生（POST /user/selectByName）
     *
     * 【学习要点】
     *   1. % 通配符在 Java 侧拼接而不是写死在 SQL 里：映射文件用
     *      name like #{name}，#{} 预编译占位符由 PreparedStatement 转义，
     *      从根本上防 SQL 注入；
     *      （若写成 ${name} 字符串拼接，输入 ' or '1'='1 就能拖库）
     *   2. 该接口目前没有对应的前端入口，属于早期接口，保留作对照学习；
     *      新写的 search 接口已覆盖此功能且支持更多条件。
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
     * 多条件组合查询学生（GET /user/search，支持分页）
     * 支持按 ID、姓名、年龄、邮箱组合查询，任意字段为空时不参与筛选
     *
     * 【学习要点】
     *   1. "空字段不参与筛选"的典型实现：前端不填就传 0/空串，
     *      SQL 映射里用 <if test="id != 0"> 动态拼条件（见 StudentDao.xml）；
     *      用 0 当"空"意味着查不了"id=0"的真实数据——业务上无影响，
     *      但要意识到这是妥协写法，更严谨的做法是传 null 并判 null；
     *   2. 查询条件回显：把 id/name/age/email 再塞回 Model，
     *      JSP 表单用 value="${name}" 显示上次输入，用户体验的关键细节；
     *   3. 多条件分页翻页时，链接必须携带全部查询参数（&id=..&name=..），
     *      否则点"下一页"会丢失筛选条件——见 queryStudentByName.jsp。
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
     * 根据 ID 删除学生（GET /user/deleteStudent）
     * 删除后重定向回列表页，用 Flash 属性携带操作提示
     *
     * 【学习要点】
     *   1. 删除属于"写操作"，规范上应该用 POST（本项目为简化用了 GET）。
     *      GET 删除的风险：浏览器预加载、爬虫抓链接都会"顺手"触发删除（CSRF），
     *      面试可作为主动反思的加分点；
     *   2. 重定向而非转发：删除后刷新浏览器不会重复提交（PRG 模式），
     *      且地址栏回到列表页，语义更清晰；
     *   3. try-catch 兜底 + log.error 带堆栈：用户看到友好提示，
     *      开发看日志定位问题，两边兼顾。
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
     * 跳转到编辑学生页面（GET /user/editStudent?id=xx）
     * 先按 ID 查出学生放进 Model，JSP 用 ${student.xxx} 回显到表单
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
     * 处理更新学生请求（POST /user/updateStudent）
     *
     * 【学习要点】
     *   1. 参数 Student 没加任何注解——这是"实体参数绑定"：
     *      Spring 按 setter 名与表单 name 匹配自动填充（name→setName）；
     *      且该对象会自动以"类名首字母小写"（student）放入 Model，
     *      所以更新后回到 editStudent.jsp，${student.xxx} 仍能取到值；
     *   2. 密码框留空表示不修改：upass 为 null 时 Service 会查库回填原密码，
     *      防止把库里密码覆盖成空（见 StudentServiceImpl.updateStudent）。
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
     * 批量删除学生（POST /user/batchDelete）
     *
     * 【学习要点】
     *   1. 前端复选框 name 统一为 "ids"，提交后请求体形如 ids=1&ids=2&ids=3，
     *      Spring 自动绑定成 List<Integer>——"同名多值参数"的标准接法；
     *   2. 底层一条 DELETE ... WHERE id IN (...) 完成，只需一次网络往返
     *      （见 StudentDao.xml 的 foreach 写法）；
     *   3. 若是多表关联删除，就需要 @Transactional 保证要么全成功要么全回滚；
     *      本项目单表删除天然原子，不需要显式开事务。
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
     * 跳转到修改密码页面（GET /user/changePassword）
     * 只负责展示页面，无业务逻辑
     */
    @GetMapping("/changePassword")
    public String changePassword() {
        return "changePassword";
    }

    /**
     * 处理修改密码请求（POST /user/changePassword）
     * 校验两次输入一致 + 原密码正确后，交由 Service 更新明文与 MD5 两个字段
     *
     * 【学习要点】
     *   1. 当前登录用户从 Session 取（loginUser），而不是信前端传的用户 id——
     *      否则改 URL 里的 id 就能改任何人的密码（越权漏洞）；
     *   2. "新密码 == 确认密码"在 Controller 校验（输入格式范畴），
     *      "原密码是否正确"交给 Service（需要查库，业务规则）——
     *      校验逻辑分层也是三层架构思想的体现。
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
     * 导出学生数据为 Excel（GET /user/export）
     *
     * 【文件下载的本质】
     *   把文件的字节流写进 response 输出流，再用两个响应头"告诉"浏览器怎么处理：
     *     Content-Type: application/vnd.ms-excel      → 这是一个 Excel 文件
     *     Content-Disposition: attachment;filename=x  → 弹"另存为"而不是直接打开
     *
     * 【POI 学习要点】
     *   1. 两个工作簿实现：HSSFWorkbook→.xls（2003 版，单表上限 65536 行）；
     *      XSSFWorkbook→.xlsx（2007+，约 104 万行）；
     *   2. 层级结构：Workbook(工作簿)→Sheet(页签)→Row(行)→Cell(单元格)，
     *      自上而下 create，行/列下标都从 0 开始；
     *   3. 中文文件名必须 URLEncoder.encode，否则部分浏览器下载时文件名乱码；
     *   4. 方法返回 void：不走视图解析器，响应由代码"亲手"写给浏览器——
     *      这也是返回 JSON、验证码图片等场景的通用套路。
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
     * 退出登录（GET /user/logout）
     * 三步清理：数据库 Token → Session → 浏览器 Cookie
     *
     * 【学习要点】
     *   1. 退出必须同时清三处，漏掉任何一处都会出现"幽灵登录"：
     *      只清 Session 不清 Cookie Token → 下次访问拦截器读到 Token
     *      又自动登录回来了，等于没退；
     *   2. request.getSession(false)：拿已有 Session，没有就返回 null 不新建——
     *      退出场景本就该"有则销毁，无则跳过"；
     *   3. 删除 Cookie 的技巧：同名 + 同 path + setMaxAge(0)，
     *      浏览器据此让该 Cookie 立即过期。
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
     * 生成验证码图片（GET /user/captcha）
     * 随机生成 4 位字符存入 Session，同时把字符画成图片写给浏览器
     *
     * 【学习要点】
     *   1. 返回 void + 直接操作 response：不走视图解析器（同 Excel 导出）；
     *   2. 三行禁缓存头（Pragma/Cache-Control/Expires）缺一不可——
     *      否则浏览器缓存图片，点"换一张"可能刷不出新图；
     *   3. 前端刷新小技巧：<img> 的 src 加时间戳参数（captcha?t=时间戳），
     *      URL 变了浏览器才认为是"新资源"重新发起请求。
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
     * 个人中心页面（GET /user/profile）
     *
     * 【学习要点】
     *   1. 不直接用 Session 里的对象渲染，而是按 id 重新查库——
     *      Session 里的数据是登录那一刻的"快照"，别处改了资料这里就是旧的；
     *   2. 查完顺手 session.setAttribute 覆盖旧快照，
     *      保证全站其他页面读到的 loginUser 同步为最新值。
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
     * 更新个人信息（POST /user/updateProfile）
     *
     * 【学习要点】
     *   1. 只更新姓名/年龄/邮箱/手机/性别等资料字段，
     *      setUpass(null) 明确置空密码——Service 对空密码会查库回填原值，
     *      防止误把密码覆盖或二次加密（双保险）；
     *   2. 更新后重新查库并刷新 Session 中的 loginUser，
     *      页面上其他地方读取的登录信息才能立刻生效。
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
     * 上传头像（POST /user/uploadAvatar）
     *
     * 【文件上传三要素】
     *   1. 表单 method="post" + enctype="multipart/form-data"（见 profile.jsp）；
     *   2. Spring 容器里必须注册 id 叫 multipartResolver 的 Bean——
     *      id 必须一字不差，写错名字 Spring 会当作没有上传功能（见 springmvc.xml）；
     *   3. Controller 用 MultipartFile 接收，Spring 已把请求流解析好。
     *
     * 【学习要点】
     *   1. UUID 重命名：用户都传"头像.jpg"会互相覆盖，UUID 保证文件名唯一；
     *   2. getRealPath("/uploads")：拿到部署目录下 uploads 文件夹的磁盘绝对路径；
     *      注意 war 重新部署会清空该目录，生产环境应存独立磁盘或对象存储 OSS；
     *   3. 安全改进点：未校验后缀白名单（jpg/png/gif），
     *      理论上可上传 .jsp 到服务器导致远程执行（RCE），可自行补白名单过滤；
     *   4. 数据库存相对路径 /uploads/xxx.jpg 而非绝对路径：
     *      换服务器、换盘符都不受影响，JSP 拼上 contextPath 即可显示。
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