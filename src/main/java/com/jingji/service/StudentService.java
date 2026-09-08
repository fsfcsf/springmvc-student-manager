package com.jingji.service;

import com.jingji.entity.Student;

import java.util.List;

/**
 * 学生业务逻辑接口（Service 层的"契约"）
 * 只定义"做什么"（方法签名），不定义"怎么做"——实现类在 service.impl 包下
 *
 * 【为什么 Controller 依赖接口而不是实现类】
 *   1. 解耦：换实现（比如加缓存的版本）Controller 一行不用改；
 *   2. 可测：单元测试注入 Mock 实现即可脱离数据库测 Controller 逻辑；
 *   3. Spring 的 @Autowired 按接口类型注入，自动匹配到唯一实现类，
 *      若有多个实现需配合 @Qualifier("beanName") 指定。
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
     * 批量删除学生
     * @param ids 要删除的学生 ID 列表
     * @return 成功删除的条数
     */
    int batchDelete(List<Integer> ids);

    /**
     * 修改密码
     * @param stuId 学生 ID
     * @param oldPass 原密码（明文）
     * @param newPass 新密码（明文）
     * @return true 成功，false 原密码错误
     */
    boolean changePassword(int stuId, String oldPass, String newPass);

    /**
     * 根据姓名模糊查询
     */
    List<Student> selectByName(String name);

    /**
     * 多条件查询学生
     * 支持按 ID、姓名、年龄、邮箱组合查询，姓名和邮箱支持模糊匹配
     * @param student 查询条件（字段为 null 或 0 时不参与查询）
     * @return 匹配的学生列表
     */
    List<Student> searchStudents(Student student);

    /**
     * 登录校验：根据账号查询用户，校验密码
     * @param uname 账号
     * @param upass 明文密码
     * @return 登录成功返回 Student，失败返回 null
     */
    Student login(String uname, String upass);

    /**
     * 更新学生头像
     * @param stuId 学生 ID
     * @param avatar 头像文件路径
     */
    void updateAvatar(int stuId, String avatar);

    /**
     * 根据记住我 Token 查询用户（用于自动登录）
     * @param token 记住我 Token
     * @return 用户对象，未找到返回 null
     */
    Student findByToken(String token);

    /**
     * 保存记住我 Token
     * @param stuId 学生 ID
     * @param token 新的 Token
     */
    void updateToken(int stuId, String token);
}