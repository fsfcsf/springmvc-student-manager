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
 * 学生业务逻辑实现类
 * 实现 StudentService 接口，处理具体的业务逻辑
 *
 * @author 张三
 * @date 2025-11-10
 */
@Service("studentService") // 声明为 Service 层组件，Spring 会自动创建对象
public class StudentServiceImpl implements StudentService {

    // 日志对象
    private static final Logger log = LoggerFactory.getLogger(StudentServiceImpl.class);

    // 自动注入 dao 层的接口代理对象
    @Autowired
    private StudentDao studentDao;

    /**
     * 新增学生
     * 把明文密码存入 upass 字段，MD5 加密后存入 upass_md5 字段
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
     * 如果传了密码，同时更新明文和 MD5 加密两个字段
     */
    @Override
    public boolean updateStudent(Student student) {
        // 判断密码是否为空，为空则保持原密码不变
        if (student.getUpass() != null && !student.getUpass().isEmpty()) {
            student.setUpassMd5(MD5Util.encrypt(student.getUpass()));
        } else {
            // 不修改密码时，从数据库取出原密码，防止被覆盖为 null
            Student old = studentDao.queryStudentById(student.getId());
            student.setUpass(old.getUpass());
            student.setUpassMd5(old.getUpassMd5());
        }
        boolean result = studentDao.updateStudentById(student) > 0;
        log.info("更新学生: id={}, name={}, 结果={}", student.getId(), student.getName(), result);
        return result;
    }

    /**
     * 根据 ID 删除学生
     */
    @Override
    public void deleteStudent(int stuId) {
        studentDao.deleteStudentById(stuId);
        log.info("删除学生: id={}", stuId);
    }

    /**
     * 批量删除学生
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
     * 先校验原密码是否正确，再加密新密码并更新
     */
    @Override
    public boolean changePassword(int stuId, String oldPass, String newPass) {
        // 1. 查出原用户信息
        Student student = studentDao.queryStudentById(stuId);
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
     * 登录校验
     * 优先用 upass_md5 字段比对，如果为空则降级用 upass 明文比对（兼容旧数据）
     *
     * @param uname 账号
     * @param upass 明文密码
     * @return 登录成功返回 Student 对象，失败返回 null
     */
    @Override
    public Student login(String uname, String upass) {
        // 1. 根据账号查询用户
        Student student = studentDao.findByUname(uname);
        if (student != null) {
            // 2. 优先用 upass_md5 加密字段比对
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
                log.info("用户登录成功，密码已自动升级: uname={}", uname);
                return student;
            }
        }
        // 账号不存在或密码错误
        log.warn("用户登录失败: uname={}", uname);
        return null;
    }

    /**
     * 更新学生头像
     */
    @Override
    public void updateAvatar(int stuId, String avatar) {
        studentDao.updateAvatar(stuId, avatar);
        log.info("更新头像: stuId={}, avatar={}", stuId, avatar);
    }

    /**
     * 根据记住我 Token 查询用户
     */
    @Override
    public Student findByToken(String token) {
        return studentDao.findByToken(token);
    }

    /**
     * 保存记住我 Token
     */
    @Override
    public void updateToken(int stuId, String token) {
        studentDao.updateToken(stuId, token);
        log.debug("更新记住我Token: stuId={}", stuId);
    }
}