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

    <!-- 密码字段 - 确保可编辑 -->
    <div>
        <label>密码:</label>
        <input type="password" name="upass" value="${student.upass}" required>
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

    <button type="submit">保存修改</button>
        <a href="${pageContext.request.contextPath}/user/showAllStudents"
           style="margin-left: 10px;">返回列表</a>
</form>
</body>
</html>