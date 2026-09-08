<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>学生管理系统</title>
</head>
<body>
<h2>欢迎来到学生管理系统</h2>

<c:if test="${not empty error}">
    <div style="color: red; padding: 10px;">${error}</div>
</c:if>

<form action="user/login" method="post">
    账号：<input type="text" name="uname" placeholder="请输入账号"><br>
    密码：<input type="password" name="password" placeholder="请输入密码"><br>
    <button type="submit">登录</button>
</form>
没有账号<a href="register.jsp">注册</a>
</body>
</html>