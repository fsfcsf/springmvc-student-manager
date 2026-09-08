package com.jingji.entity;

/**
 * 学生实体类
 * 对应数据库中的 student2 表
 *
 * @author 张三
 * @date 2025-11-10
 */
public class Student {

    // 编号（主键）
    private int id;
    // 姓名
    private String name;
    // 年龄
    private int age;
    // 账号（登录用）
    private String uname;
    // 邮箱
    private String email;
    // 密码（存储的是 MD5 加密后的密文）
    private String upass;

    // 无参构造方法
    public Student() {
    }

    // ========== Getter 和 Setter 方法 ==========

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getUname() {
        return uname;
    }

    public String getUpass() {
        return upass;
    }

    public String getEmail() {
        return email;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUpass(String upass) {
        this.upass = upass;
    }

    // 重写 toString 方法，方便打印调试
    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", uname='" + uname + '\'' +
                ", email='" + email + '\'' +
                ", upass='" + upass + '\'' +
                '}';
    }
}