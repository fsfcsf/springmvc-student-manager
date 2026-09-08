package com.jingji.service;

import com.jingji.entity.Student;

import java.util.List;

/**
 * 学生业务逻辑接口
 * 定义学生相关的业务方法
 *
 * @author 张三
 * @date 2025-11-10
 */
public interface StudentService {

    /**
     * 新增学生
     */
    int addStudent(Student student);

    /**
     * 根据 ID 查询学生
     */
    Student queryStudentById(int stuId);

    /**
     * 查询所有学生
     */
    List<Student> queryAll();

    /**
     * 更新学生信息
     */
    boolean updateStudent(Student student);

    /**
     * 根据 ID 删除学生
     */
    void deleteStudent(int stuId);

    /**
     * 根据姓名模糊查询
     */
    List<Student> selectByName(String name);

    /**
     * 登录校验：根据账号查询用户，校验密码
     * @param uname 账号
     * @param upass 明文密码
     * @return 登录成功返回 Student，失败返回 null
     */
    Student login(String uname, String upass);
}