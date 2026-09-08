<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>学生注册</title>
</head>
<body>
<h2>注册页面</h2>

<c:if test="${not empty error}">
    <div style="color: red; padding: 10px;">${error}</div>
</c:if>

<form action="${pageContext.request.contextPath}/user/register" method="post">
    编号：<input type="text" name="id" placeholder="编号" required><br>
    姓名：<input type="text" name="name" placeholder="姓名" required><br>
    账号：<input type="text" name="uname" placeholder="账号" required><br>
    密码：<input type="password" name="upass" placeholder="密码" required><br>
    年龄：<input type="text" name="age" placeholder="年龄"><br>
    邮箱：<input type="email" name="email" placeholder="邮箱" required><br>
    手机：<input type="text" name="phone" placeholder="手机号"><br>
    性别：<select name="gender">
            <option value="">请选择</option>
            <option value="男">男</option>
            <option value="女">女</option>
          </select><br>
    <button type="submit">注册</button><br>
    <a href="${pageContext.request.contextPath}/index.jsp">登录</a>
</form>
</body>
</html>
