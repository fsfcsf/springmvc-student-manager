<%--
  Created by IntelliJ IDEA.
  User: Administrator
  Date: 2025/10/15
  Time: 15:54
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>注册成功</title>
</head>
<body>
<h2>注册成功</h2>

<c:if test="${not empty message}">
    <div style="color: green; padding: 10px; font-size: 16px;">${message}</div>
</c:if>

<p>学生信息已成功保存到数据库。</p>
<a href="${pageContext.request.contextPath}/user/showAllStudent">
    <button type="button">返回管理页面</button>
</a>
</body>
</html>
