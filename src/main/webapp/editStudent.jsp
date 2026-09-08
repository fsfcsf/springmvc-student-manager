<%--
  编辑学生页（表单回显的典型示范）
  【学习要点】
  1. 隐藏域 <input type="hidden" name="id">：id 不需要用户看到/修改，
     但提交时必须带上，让 UPDATE 知道改哪一行
  2. 回显三件套：文本框 value="${student.name}"；密码框留空（placeholder 提示
     "留空则不修改"）；下拉框用 EL 三目 ${student.gender == '男' ? 'selected' : ''}
     控制哪一项被选中——性别回显的关键写法
  3. 提交后 Spring 按 setter 名自动把表单封装成 Student 对象（实体参数绑定）
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>编辑学生信息</title>
    <style>
        .form-group { margin: 10px 0; }
        label { display: inline-block; width: 80px; }
        input { padding: 5px; width: 200px; }
    </style>
</head>
<body>
<h2>编辑学生信息</h2>

<c:if test="${not empty message}">
    <div style="color: green; padding: 10px;">${message}</div>
</c:if>

<form action="${pageContext.request.contextPath}/user/updateStudent" method="post">
    <input type="hidden" name="id" value="${student.id}">

    <!-- 姓名字段 -->
    <div>
        <label>姓名:</label>
        <input type="text" name="name" value="${student.name}" required>
    </div>

    <!-- 账号字段 - 确保可编辑 -->
    <div>
        <label>账号:</label>
        <input type="text" name="uname" value="${student.uname}" required>
    </div>

    <!-- 密码字段 — 不显示原密码，留空表示不修改 -->
    <div>
        <label>密码:</label>
        <input type="password" name="upass" placeholder="留空则不修改密码">
    </div>

    <!-- 年龄字段 -->
    <div>
        <label>年龄:</label>
        <input type="number" name="age" value="${student.age}" required>
    </div>

    <!-- 邮箱字段 -->
    <div>
        <label>邮箱:</label>
        <input type="email" name="email" value="${student.email}" required>
    </div>

    <!-- 手机号字段 -->
    <div>
        <label>手机号:</label>
        <input type="text" name="phone" value="${student.phone}">
    </div>

    <!-- 性别字段 -->
    <div>
        <label>性别:</label>
        <select name="gender">
            <option value=""  ${empty student.gender ? 'selected' : ''}>请选择</option>
            <option value="男" ${student.gender == '男' ? 'selected' : ''}>男</option>
            <option value="女" ${student.gender == '女' ? 'selected' : ''}>女</option>
        </select>
    </div>

    <button type="submit">保存修改</button>
        <a href="${pageContext.request.contextPath}/user/showAllStudent"
           style="margin-left: 10px;">返回列表</a>
</form>
</body>
</html>