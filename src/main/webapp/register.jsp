<%--
  注册页面（视图层）
  【学习要点】
  1. input 的 name 必须与 Controller @RequestParam("xxx") 的名字一一对应，
     这是前后端参数传递的"暗号"；required 是 HTML5 原生的前端校验
  2. 性别用 <select> 下拉框：value 固定为 男/女，从源头避免脏数据入库
  3. 前端校验只是体验（绕开页面直接发请求即可跳过），
     后端 Controller 里还有第二道非空/格式校验——双重校验
--%>
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
