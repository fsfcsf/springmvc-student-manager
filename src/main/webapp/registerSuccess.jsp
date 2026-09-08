<%--
  注册成功页：显示 Flash 属性 ${message}（PRG 模式的最后一环——
  注册 POST → redirect 到本页 → 一次性读取 Session 里的成功提示后自动清除）
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
