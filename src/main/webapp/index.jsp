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

<form action="${pageContext.request.contextPath}/user/login" method="post">
    账号：<input type="text" name="uname" placeholder="请输入账号" required><br>
    密码：<input type="password" name="password" placeholder="请输入密码" required><br>
    验证码：<input type="text" name="captcha" placeholder="请输入验证码" required maxlength="4" size="6">
    <img src="${pageContext.request.contextPath}/user/captcha"
         title="点击刷新验证码" style="cursor: pointer; vertical-align: middle;"
         onclick="this.src='${pageContext.request.contextPath}/user/captcha?t='+new Date().getTime()"><br>
    <label><input type="checkbox" name="rememberMe" value="true"> 记住我（7天内自动登录）</label><br>
    <button type="submit">登录</button>
</form>
没有账号<a href="${pageContext.request.contextPath}/register.jsp">注册</a>
</body>
</html>