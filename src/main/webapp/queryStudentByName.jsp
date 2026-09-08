<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page isELIgnored="false" %>
<html>
<head>
    <title>查询结果</title>
</head>
<body>
<button type="button">
    <a href="../register.jsp">新增</a>
</button>
<form action="selectByName" method="post">
    <input type="text" name="name" value="${name}" placeholder="请输入姓名">
    <input type="submit" value="查询">
</form>
<table border="1">
    <tr>
        <th>编号</th>
        <th>姓名</th>
        <th>年龄</th>
        <th>邮箱</th>
        <th>操作</th>
    </tr>
    <c:forEach var="date" items="${dateList}">
        <tr>
            <td>${date.id}</td>
            <td>${date.name}</td>
            <td>${date.age}</td>
            <td>${date.email}</td>
            <td>
                <a href="${pageContext.request.contextPath}/user/editStudent?id=${date.id}">编辑</a>
                <a href="${pageContext.request.contextPath}/user/deleteStudent?id=${date.id}"
                   onclick="return confirm('确定删除吗？')">删除</a>
            </td>
        </tr>
    </c:forEach>
</table>
</body>
</html>