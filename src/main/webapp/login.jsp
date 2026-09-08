<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>登录</title>
</head>
<body>
<h1>登录页面</h1>

<c:if test="${not empty error}">
    <div style="color: red;">${error}</div>
</c:if>

<form action="${pageContext.request.contextPath}/user/login" method="post">
    账号：<input type="text" name="uname" placeholder="请输入账号"><br>
    密码：<input type="password" name="password" placeholder="请输入密码"><br>
    <button type="submit">登录</button>
</form>
<a href="${pageContext.request.contextPath}/index.jsp">返回首页</a>
</body>
</html>