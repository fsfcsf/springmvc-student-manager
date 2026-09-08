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
    // 密码（明文，注册时用户输入）
    private String upass;
    // MD5 加密后的密码（用于登录校验）
    private String upassMd5;
    // 手机号
    private String phone;
    // 性别（男/女）
    private String gender;
    // 创建时间
    private String createTime;
    // 最后更新时间
    private String updateTime;
    // 头像文件路径
    private String avatar;
    // 记住我自动登录 Token
    private String rememberToken;

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

    public String getUpassMd5() {
        return upassMd5;
    }

    public String getPhone() {
        return phone;
    }

    public String getGender() {
        return gender;
    }

    public String getCreateTime() {
        return createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getRememberToken() {
        return rememberToken;
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

    public void setUpassMd5(String upassMd5) {
        this.upassMd5 = upassMd5;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setRememberToken(String rememberToken) {
        this.rememberToken = rememberToken;
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
                ", phone='" + phone + '\'' +
                ", gender='" + gender + '\'' +
                '}';
    }
}