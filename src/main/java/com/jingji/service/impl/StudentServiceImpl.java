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
     * 先把密码用 MD5 加密，再存入数据库
     */
    @Override
    public int addStudent(Student student) {
        // 对密码进行 MD5 加密，防止明文存储
        student.setUpass(MD5Util.encrypt(student.getUpass()));
        // 调用 dao 层插入数据
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
     * 如果传了密码才加密，否则保持原密码不变
     */
    @Override
    public boolean updateStudent(Student student) {
        // 判断密码是否为空，不为空才加密
        if (student.getUpass() != null && !student.getUpass().isEmpty()) {
            student.setUpass(MD5Util.encrypt(student.getUpass()));
        }
        // 执行更新，返回值大于 0 表示更新成功
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
     * 根据姓名模糊查询学生
     */
    @Override
    public List<Student> selectByName(String name) {
        log.debug("模糊查询学生: name={}", name);
        return studentDao.findByName(name);
    }

    /**
     * 登录校验
     * 先根据账号查用户，再比对加密后的密码
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
            // 2. 把用户输入的密码加密后，跟数据库中的密码对比
            String encryptedPass = MD5Util.encrypt(upass);
            if (encryptedPass != null && encryptedPass.equals(student.getUpass())) {
                log.info("用户登录成功: uname={}", uname);
                return student; // 密码匹配，登录成功
            }
        }
        // 账号不存在或密码错误
        log.warn("用户登录失败: uname={}", uname);
        return null;
    }
}