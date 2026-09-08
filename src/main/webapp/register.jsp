<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>学生注册</title>
</head>
<body>
<h2>注册页面</h2>

<c:if test="${not empty error}">
    <div style="color: red; padding: 10px;">${error}</div>
</c:if>

<form action="user/register" method="post">
    编号：<input type="text" name="id" placeholder="编号" required><br>
    姓名：<input type="text" name="name" placeholder="姓名" required><br>
    账号：<input type="text" name="uname" placeholder="账号" required><br>
    密码：<input type="password" name="upass" placeholder="密码" required><br>
    年龄：<input type="text" name="age" placeholder="年龄"><br>
    邮箱：<input type="email" name="email" placeholder="邮箱" required><br>
    <button type="submit">注册</button><br>
    <a href="index.jsp">登录</a>
</form>
</body>
</html>
