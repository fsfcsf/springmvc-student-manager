package com.jingji.service.impl;

import com.jingji.dao.StudentDao;
import com.jingji.entity.Student;
import com.jingji.service.StudentService;
import com.jingji.util.MD5Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ═══════════════════════════════════════════════════════════════════
 * 学生业务逻辑实现类（Service 层）—— 承上启下的一层
 * ═══════════════════════════════════════════════════════════════════
 * 【职责边界（面试常问：三层架构各自管什么）】
 *   Controller：接收/校验参数、调用 Service、决定跳转——不写业务规则
 *   Service(本类)：业务规则与数据加工（密码加密、旧数据兼容升级、密码留空回填等）
 *   Dao：只管 SQL 增删改查，不含业务语义
 *
 * 【为什么先定义接口再写实现类】
 *   1. 面向接口编程：Controller 只依赖 StudentService 接口，不关心实现；
 *   2. 便于替换与 Mock：单元测试注入一个假实现即可脱离数据库测 Controller；
 *   3. Spring 的 @Autowired 按接口类型注入，自动匹配到唯一实现类。
 *
 * @author 张三
 * @date 2025-11-10
 */
@Service("studentService") // 声明为 Service 组件；括号指定 Bean 名称，springmvc.xml 拦截器里 ref="studentService" 引用的就是它
public class StudentServiceImpl implements StudentService {

    // 日志对象：Service 层记录业务动作（登录成功/密码修改/批量删除等），排查问题看这里
    private static final Logger log = LoggerFactory.getLogger(StudentServiceImpl.class);

    // 注入 Dao：StudentDao 是纯接口，MyBatis 通过 JDK 动态代理生成实现
    // （见 springmvc.xml 的 MapperScannerConfigurer），Spring 把代理对象注入进来
    @Autowired
    private StudentDao studentDao;

    /**
     * 新增学生
     * 密码"双写"：明文进 upass 字段，MD5 密文进 upass_md5 字段
     *
     * 【学习要点】
     *   1. 双字段是本项目为兼容旧数据做的过渡设计（新系统绝不存明文）：
     *      登录优先比对 upass_md5，老账号首次登录时再自动补齐密文；
     *   2. 加密属于业务规则，放在 Service 而非 Controller/Dao——
     *      无论哪个入口（注册页、将来的管理端导入）调用本方法都会加密，逻辑不散落。
     */
    @Override
    public int addStudent(Student student) {
        // 对密码进行 MD5 加密，存入 upass_md5 字段（用于登录校验）
        student.setUpassMd5(MD5Util.encrypt(student.getUpass()));
        int result = studentDao.insertStudent(student);
        log.info("新增学生: id={}, name={}", student.getId(), student.getName());
        return result;
    }

    /**
     * 根据 ID 查询单个学生
     */
    @Override
    public Student queryStudentById(int stuId) {
        return studentDao.queryStudentById(stuId);
    }

    /**
     * 查询所有学生
     */
    @Override
    public List<Student> queryAll() {
        log.debug("查询所有学生");
        return studentDao.queryAll();
    }

    /**
     * 更新学生信息
     * 传了密码就同步更新明文/MD5 两个字段；没传则查库回填原密码，防止被覆盖成空
     *
     * 【学习要点】
     *   1. "密码留空 = 不修改"是编辑页的常见交互：表单不填 upass 提交后
     *      该字段是 null，直接 UPDATE 会把库里密码清空——所以先查旧记录，
     *      把 upass/upassMd5 回填到对象再整体 UPDATE；
     *   2. 想省掉这次"查旧再更新"，SQL 可改成 <if> 动态 SET 只更新非空字段，
     *      本项目选择整行覆盖，逻辑简单直接，两者是典型的取舍。
     */
    @Override
    public boolean updateStudent(Student student) {
        // 更新必须要有有效id，id为null或者<=0直接拒绝更新
        Integer stuId = student.getId();
        if(stuId == null || stuId <= 0){
            log.warn("更新学生失败，无效id：{}", stuId);
            return false;
        }

        //`!student.getUpass().isEmpty()` 只能判断非空字符串；传入空格 `" "` 会绕过判断
       // if (student.getUpass() != null && !student.getUpass().isBlank())  java11 语法
        //先 trim 去掉前后空格，再判断是否为空字符串，实现和`isBlank()`一样效果
        if (student.getUpass() != null && !student.getUpass().trim().isEmpty())
        {
            student.setUpassMd5(MD5Util.encrypt(student.getUpass()));
        } else {
            Student old = studentDao.queryStudentById(student.getId());
            if(old == null){
                log.warn("更新学生失败，id={}数据库不存在", student.getId());
                return false;
            }
            student.setUpass(old.getUpass());
            student.setUpassMd5(old.getUpassMd5());
        }

        boolean result = studentDao.updateStudentById(student) > 0; //调用 MyBatis 执行 update SQL，返回值是**数据库受影响行数 int** 大于0则说明有更新转为布尔后面传入全局日志中
        log.info("更新学生: id={}, name={}, 结果={}", student.getId(), student.getName(), result);
        return result;
    }

    /**
     * 根据 ID 删除学生
     * 单表单条删除天然原子（一条 SQL 要么成功要么失败），无需手动开事务
     */
    @Override
    public void deleteStudent(int stuId) {
        studentDao.deleteStudentById(stuId);
        log.info("删除学生: id={}", stuId); // {} 占位符由 SLF4J 填充，比字符串拼接高效且线程安全
        //SLF4J 日志打印，`{}`是占位符，运行时把`stuId`填进去。
          //优点：不做字符串拼接`"删除学生id:"+stuId`，性能更好，避免字符串对象创建。
    }

    /**
     * 批量删除学生
     * 一条 DELETE ... IN (...) 搞定，返回影响行数即删除条数
     *
     * @param ids 要删除的学生 ID 列表
     * @return 成功删除的条数
     */
    @Override
    public int batchDelete(List<Integer> ids) {
        int count = studentDao.deleteByIds(ids);
        log.info("批量删除: 共删除 {} 条, ids={}", count, ids);
        return count;
    }

    /**
     * 修改密码
     * 先校验原密码，通过后同时更新明文 upass 与 MD5 密文 upass_md5
     *
     * 【学习要点】
     *   1. 双重比对顺序：先比 MD5 字段（新数据），不匹配再比明文（旧数据），
     *
     *   2. 返回 boolean 而不是抛异常："原密码错误"是正常业务分支而非系统故障，
     *      由 Controller 转成用户可读的提示信息。
     */
    @Override
    public boolean changePassword(int stuId, String oldPass, String newPass) {
        // 1. 查出原用户信息
        Student student = studentDao.queryStudentById(stuId);

        // 判断用户是否存在
        if(student == null){
            log.warn("修改密码失败：用户不存在 stuId={}", stuId);
            return false;
        }
        // 2. 校验原密码：优先比对 upass_md5，如果为空则降级比对 upass 明文
        String encryptedOld = MD5Util.encrypt(oldPass);
        boolean oldPassOk = false;
        // 先比对加密字段
        if (student.getUpassMd5() != null
                && encryptedOld != null
                && encryptedOld.equals(student.getUpassMd5())) {
            oldPassOk = true;
        }
        // 再比对明文字段（兼容旧数据）
        if (!oldPassOk && oldPass.equals(student.getUpass())) {
            oldPassOk = true;
        }
        if (oldPassOk) {
            // 3. 加密新密码并更新，upass 存明文，upass_md5 存 MD5
            String encryptedNew = MD5Util.encrypt(newPass);
            studentDao.updatePassword(stuId, newPass, encryptedNew);
            log.info("修改密码成功: stuId={}", stuId);
            return true;
        }
        log.warn("修改密码失败（原密码错误）: stuId={}", stuId);
        return false;
    }

    /**
     * 根据姓名模糊查询学生
     */
    @Override
    public List<Student> selectByName(String name) {
        log.debug("模糊查询学生: name={}", name);
        return studentDao.findByName(name);
    }

    /**
     * 多条件查询学生
     * 支持按 ID、姓名、年龄、邮箱组合查询
     */
    @Override
    public List<Student> searchStudents(Student student) {
        log.debug("多条件查询: id={}, name={}, age={}, email={}",
                student.getId(), student.getName(), student.getAge(), student.getEmail());
        return studentDao.searchStudents(student);
    }

    /**
     * 登录校验（核心方法）
     * 优先比对 upass_md5 字段；老账号（无密文）降级比对明文，并顺手完成"密码升级"
     *
     * @param uname 账号
     * @param upass 明文密码
     * @return 登录成功返回 Student 对象，失败返回 null
     *
     * 【学习要点——"惰性升级"迁移策略】
     *   1. 老数据只有明文密码，一次性全量 UPDATE 迁移风险大（脚本出错难回退），
     *      这里选择"登录时顺手升级"：明文比对通过后立刻补写密文，越登录越安全；
     *   2. MD5 的局限（面试必问）：无盐时同一密码密文相同，可被彩虹表反查；
     *      正确姿势是加盐 MD5 或 BCrypt/PBKDF2（自带随机盐 + 慢哈希），
     *      本项目用 MD5 学习"加密存储"的完整流程，面试时可主动讲出改进方向；
     *   3. 账号查不到也返回 null，与密码错误表现一致——防账号枚举。
     */
    @Override
    public Student login(String uname, String upass) {
        // 1. 根据账号查询用户
        Student student = studentDao.findByUname(uname);
        //前置非空条件
        if(uname == null || upass == null || uname.trim().isEmpty() || upass.trim().isEmpty()){
            log.warn("用户登录失败，参数为空: uname={}", uname);
            return null;
        }
        if (student != null) {
            // 2. 优先用 upass_md5 加密字段比对 把用户输入的密码做 MD5 加密，和数据库`upass_md5`字段比对
            String encryptedPass = MD5Util.encrypt(upass);
            if (student.getUpassMd5() != null
                    && encryptedPass != null
                    && encryptedPass.equals(student.getUpassMd5())) {
                log.info("用户登录成功: uname={}", uname);
                return student;
            }
            // 3. 降级用 upass 明文比对（兼容未升级的旧数据）
            if (upass.equals(student.getUpass())) {
                // 密码匹配成功，把 upass 明文和 upass_md5 加密值都补齐
                String newEncrypted = MD5Util.encrypt(upass);
                studentDao.updatePassword(student.getId(), upass, newEncrypted);
                //明文密码登录成功后，立刻调用`updatePassword`，生成 MD5 密文写入数据库 旧的没MD5加密的数据进行顺手加密
                log.info("用户登录成功，密码已自动升级: uname={}", uname);
                return student;
            }
        }
        // 账号不存在或密码错误
        log.warn("用户登录失败: uname={}", uname);
        return null;
    }

    /**
     * 更新学生头像（只改 avatar 一个字段，Controller 完成文件落盘后调用）
     */
    @Override
    public void updateAvatar(int stuId, String avatar) {
        studentDao.updateAvatar(stuId, avatar);
        log.info("更新头像: stuId={}, avatar={}", stuId, avatar);
    }

    /**
     * 根据记住我 Token 查询用户（拦截器自动登录时调用）
     * Token 是 32 位 UUID 字符串：不可预测（防伪造）、全局唯一（防串号），
     * 存在数据库里意味着服务重启后依然有效，比只存 Session 更持久
     */
    @Override
    public Student findByToken(String token) {
        return studentDao.findByToken(token);
    }

    /**
     * 保存记住我 Token（登录勾选"记住我"时写入；退出登录时传 null 清除）
     */
    @Override
    public void updateToken(int stuId, String token) {
        studentDao.updateToken(stuId, token);
        log.debug("更新记住我Token: stuId={}", stuId);
    }
}