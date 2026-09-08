package com.jingji.dao;

import com.jingji.entity.Student;

import java.util.List;

/**
 * 学生数据访问接口
 * MyBatis 会自动生成这个接口的代理实现类
 *
 * @author 张三
 * @date 2025-11-10
 */
public interface StudentDao {

    /**
     * 新增学生
     */
    int insertStudent(Student student);

    /**
     * 根据 ID 查询学生
     */
    Student queryStudentById(int stuId);

    /**
     * 查询所有学生
     */
    List<Student> queryAll();

    /**
     * 根据 ID 删除学生
     */
    void deleteStudentById(int stuId);

    /**
     * 根据 ID 更新学生信息
     */
    int updateStudentById(Student student);

    /**
     * 根据姓名模糊查询
     */
    List<Student> findByName(String name);

    /**
     * 根据账号查询用户（用于登录校验）
     */
    Student findByUname(String uname);
}