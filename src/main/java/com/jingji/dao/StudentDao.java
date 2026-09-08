package com.jingji.dao;

import com.jingji.entity.Student;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * ═══════════════════════════════════════════════════════════════════
 * 学生数据访问接口（DAO 层 / Mapper）
 * ═══════════════════════════════════════════════════════════════════
 * 【只写接口不写实现——MyBatis 接口代理原理（面试高频）】
 *   1. springmvc.xml 的 MapperScannerConfigurer 扫描本包，
 *      为每个接口注册 MapperFactoryBean；
 *   2. 其内部调用 sqlSession.getMapper(StudentDao.class)，
 *      返回 JDK 动态代理对象（MapperProxy）；
 *   3. 调用接口方法时，代理按"接口全限定名(namespace) + 方法名(id)"
 *      定位 StudentDao.xml 中对应的 SQL 执行——所以方法名必须与 XML 的 id 一致；
 *   4. SQL 写在 XML 里而不是注解里：动态 SQL 标签(<if>/<where>/<foreach>)
 *      在 XML 中更清晰，复杂 SQL 也是行业惯例。
 *
 * 【@Param 的作用】
 *   多参数方法必须用它给每个参数命名，SQL 里才能用 #{命名} 取值；
 *   单参数（对象/单值）可以不加，MyBatis 自动绑定。
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