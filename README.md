# Spring MVC 学生管理系统

> 学习 JavaWeb 期间的练手项目，基于 Spring MVC + MyBatis 构建的学生信息管理系统。

## 项目概述

一个完整的 B/S 架构学生信息管理系统，实现了从用户注册登录到学生数据 CRUD 的全流程功能。采用经典的三层架构（Controller → Service → DAO），通过 Spring MVC 处理 Web 请求，MyBatis 完成数据库持久化操作。

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring MVC | 5.2.25 | Web 框架，处理 HTTP 请求与响应 |
| Spring Context | 5.2.5 | IoC 容器，管理 Bean 生命周期 |
| Spring JDBC / TX | 5.2.5 | 事务管理与 JDBC 支持 |
| MyBatis | 3.5.1 | ORM 框架，SQL 映射与数据库交互 |
| MyBatis-Spring | 1.3.1 | MyBatis 与 Spring 集成 |
| Druid | 1.1.12 | 阿里巴巴数据库连接池 |
| MySQL | 5.x | 关系型数据库 |
| PageHelper | 5.3.0 | MyBatis 分页插件 |
| Apache POI | 4.1.2 | Excel 文件生成与导出 |
| Commons FileUpload | 1.4 | 文件上传处理 |
| SLF4J + Logback | 1.7.30 / 1.2.3 | 日志框架 |
| JSP + EL | — | 视图层模板渲染 |
| Servlet | 3.1 | Web 容器基础 |
| Maven | — | 项目构建与依赖管理 |
| JDK | 1.8 | Java 运行环境 |

## 功能列表

### 用户模块
- **注册**：填写学号、姓名、账号、密码、邮箱等信息完成注册，支持前后端双重校验
- **登录**：账号密码登录，MD5 加密校验，支持验证码防刷
- **记住我**：登录时勾选"记住我"，服务端生成 Token 存入 Cookie 和数据库，7 天内自动登录
- **修改密码**：校验原密码后修改为新密码，同时更新明文和 MD5 密文两个字段
- **个人中心**：查看和编辑个人信息，支持头像上传

### 学生管理模块
- **新增学生**：通过注册或管理端录入学生信息
- **查询所有**：分页展示全部学生，支持自定义每页条数
- **模糊搜索**：按姓名模糊匹配查询
- **多条件搜索**：按 ID、姓名、年龄、邮箱组合查询，任意字段为空时不参与筛选
- **编辑学生**：修改学生各项信息，表单回显原有数据
- **删除学生**：单条删除，带确认提示
- **批量删除**：勾选多条记录一键删除，返回删除数量

### 系统功能
- **验证码**：登录时生成 4 位随机字符验证码图片，存入 Session 校验
- **Excel 导出**：将全部学生数据导出为 .xls 文件，浏览器直接下载
- **头像上传**：支持上传图片作为头像，UUID 命名防止冲突，限制 2MB 大小
- **登录拦截器**：未登录用户无法访问管理页面，支持"记住我"Cookie 自动登录
- **全局异常处理**：`@ControllerAdvice` 统一捕获异常，返回友好提示，不暴露内部错误
- **字符编码过滤器**：全局 UTF-8 编码，解决中文乱码问题

## 项目结构

```
springmvc-student-manager
├── pom.xml                              # Maven 项目配置
├── README.md
└── src
    └── main
        ├── java/com/jingji
        │   ├── controller
        │   │   ├── StudentController.java       # 学生管理控制器（核心）
        │   │   ├── LoginInterceptor.java        # 登录拦截器
        │   │   └── GlobalExceptionHandler.java  # 全局异常处理器
        │   ├── service
        │   │   ├── StudentService.java          # 业务逻辑接口
        │   │   └── impl/StudentServiceImpl.java # 业务逻辑实现
        │   ├── dao
        │   │   ├── StudentDao.java              # 数据访问接口
        │   │   └── StudentDao.xml               # MyBatis SQL 映射
        │   ├── entity
        │   │   └── Student.java                 # 学生实体类
        │   └── util
        │       ├── MD5Util.java                 # MD5 加密工具
        │       └── CaptchaUtil.java             # 验证码生成工具
        ├── resources
        │   ├── springmvc.xml                    # Spring MVC 主配置
        │   ├── mybatis.xml                      # MyBatis 配置
        │   ├── jdbc.properties                  # 数据库连接配置
        │   ├── jdbc.properties.example          # 数据库配置模板
        │   ├── logback.xml                      # 日志配置
        │   └── sql
        │       ├── student2.sql                 # 初始建表脚本
        │       └── upgrade.sql                  # 数据库升级脚本
        └── webapp
            ├── index.jsp                        # 登录首页
            ├── login.jsp                        # 登录页
            ├── register.jsp                     # 注册页
            ├── registerSuccess.jsp              # 注册成功页
            ├── showAllStudent.jsp               # 学生列表（分页）
            ├── queryStudentByName.jsp           # 多条件搜索
            ├── editStudent.jsp                  # 编辑学生
            ├── changePassword.jsp               # 修改密码
            ├── profile.jsp                      # 个人中心
            └── WEB-INF
                └── web.xml                      # Web 部署描述符
```

## 快速开始

### 环境要求
- JDK 1.8+
- MySQL 5.x
- Maven 3.x
- Tomcat 8/9（或其他 Servlet 容器）

### 数据库初始化

1. 创建数据库：
```sql
CREATE DATABASE springdb DEFAULT CHARACTER SET utf8;
```

2. 执行建表脚本（二选一）：
   - 全新项目：运行 `src/main/resources/sql/student2.sql`
   - 升级旧表：运行 `src/main/resources/sql/upgrade.sql`

3. 配置数据库连接：复制 `jdbc.properties.example` 为 `jdbc.properties`，修改其中的数据库地址、用户名和密码。

### 运行项目

```bash
# 编译打包
mvn clean package

# 将 target/springMVCTest01.war 部署到 Tomcat 的 webapps 目录
# 启动 Tomcat 后访问 http://localhost:8080/springMVCTest01/
```

## 数据库设计

### student2 表结构

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT | 主键，自增 |
| name | VARCHAR | 姓名 |
| uname | VARCHAR | 登录账号 |
| upass | VARCHAR | 密码（明文存储） |
| upass_md5 | VARCHAR(32) | 密码（MD5 加密，用于登录校验） |
| age | INT | 年龄 |
| email | VARCHAR | 邮箱 |
| phone | VARCHAR(11) | 手机号 |
| gender | VARCHAR(2) | 性别 |
| avatar | VARCHAR(200) | 头像文件路径 |
| remember_token | VARCHAR(64) | 记住我 Token |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 最后更新时间 |

## 学到的内容

### Spring MVC 框架
- 理解 DispatcherServlet 前端控制器的工作流程：请求分发 → HandlerMapping → Controller → ViewResolver → 视图渲染
- 掌握 `@Controller`、`@RequestMapping`、`@GetMapping`、`@PostMapping` 等注解驱动开发
- 学会配置拦截器（Interceptor）实现登录校验，区分放行与拦截路径
- 使用 `@ControllerAdvice` + `@ExceptionHandler` 实现全局异常统一处理
- 理解 ModelAndView、Model、RedirectAttributes 等数据传递方式
- 掌握静态资源放行（`<mvc:default-servlet-handler/>`）与视图解析器配置

### MyBatis 持久层
- 理解接口代理机制：MapperScannerConfigurer 自动扫描 DAO 接口生成代理对象
- 掌握 XML 映射文件中动态 SQL 的编写：`<if>`、`<where>`、`<foreach>` 等标签
- 学会多参数传递：`@Param` 注解绑定参数名
- 实现多条件动态查询：根据传入对象的字段是否为空动态拼接 SQL
- 配置 PageHelper 分页插件，理解其拦截器原理

### 三层架构设计
- Controller 层：负责接收请求参数、调用 Service、返回视图/数据
- Service 层：封装业务逻辑，如密码加密、数据校验、事务管理
- DAO 层：只负责数据库操作，不包含业务逻辑
- 理解面向接口编程的优势：Service 和 DAO 均定义接口，方便扩展和测试

### 安全与认证
- MD5 密码加密存储，密码明文与密文分离存储的设计思路
- Session 管理：登录状态维护、验证码校验、退出销毁
- Cookie 使用：记住我功能的 Token 生成、存储与自动登录流程
- 拦截器实现认证：未登录用户重定向到登录页，保护敏感页面

### 前端交互
- JSP + EL 表达式实现数据展示与表单回显
- 分页导航：上一页、下一页、页码跳转、空状态提示
- 批量操作：复选框全选/取消全选，前端确认对话框
- 文件上传：`multipart/form-data` 表单，UUID 重命名防冲突

### 工程化实践
- Maven 依赖管理：理解 `pom.xml` 中依赖范围（compile/provided）的区别
- 配置文件分离：数据库连接信息提取到 `jdbc.properties`，便于环境切换
- 日志使用：SLF4J + Logback，在关键节点记录日志便于排查问题
- 资源文件归类：SQL 脚本统一放在 `src/main/resources/sql/` 目录
- 字符编码处理：全局配置 UTF-8 过滤器，解决中文乱码

### 其他技能
- Apache POI 操作 Excel：创建 HSSFWorkbook 工作簿，设置响应头实现文件下载
- Druid 连接池配置：理解连接池参数（maxActive 等）的含义
- SQL 脚本编写：ALTER TABLE 升级表结构，数据迁移与同步
- Git 版本管理：分支操作、提交规范、远程仓库推送