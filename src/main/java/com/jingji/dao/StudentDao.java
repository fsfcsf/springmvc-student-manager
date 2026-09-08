package com.jingji.dao;

import com.jingji.entity.Student;
import org.apache.ibatis.annotations.Param;

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
     * 批量删除学生
     * @param ids 要删除的学生 ID 列表
     */
    int deleteByIds(List<Integer> ids);

    /**
     * 根据 ID 更新学生信息
     */
    int updateStudentById(Student student);

    /**
     * 修改密码
     * @param stuId 学生 ID
     * @param plainPass 新密码（明文）
     * @param md5Pass 新密码（MD5 加密）
     */
    int updatePassword(@Param("stuId") int stuId, @Param("plainPass") String plainPass, @Param("md5Pass") String md5Pass);

    /**
     * 根据姓名模糊查询
     */
    List<Student> findByName(String name);

    /**
     * 多条件查询学生
     * 支持按 ID、姓名、年龄、邮箱组合查询，姓名和邮箱支持模糊匹配
     * @param student 查询条件封装对象（字段为 null 时不参与查询）
     */
    List<Student> searchStudents(Student student);

    /**
     * 根据账号查询用户（用于登录校验）
     */
    Student findByUname(String uname);

    /**
     * 更新头像路径
     * @param stuId 学生 ID
     * @param avatar 头像文件路径
     */
    int updateAvatar(@Param("stuId") int stuId, @Param("avatar") String avatar);

    /**
     * 根据记住我 Token 查询用户（用于自动登录）
     * @param token 记住我 Token
     */
    Student findByToken(String token);

    /**
     * 更新记住我 Token
     * @param stuId 学生 ID
     * @param token 新的 Token（传 null 则清除）
     */
    int updateToken(@Param("stuId") int stuId, @Param("token") String token);
}