<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page isELIgnored="false" %>
<html>
<head>
    <title>学生列表</title>
</head>
<body>
<div style="padding: 10px; background: #f0f0f0;">
    <c:if test="${not empty sessionScope.loginUser}">
        当前用户：${sessionScope.loginUser.uname} |
        <a href="${pageContext.request.contextPath}/user/logout">退出</a>
    </c:if>
</div>

<c:if test="${not empty message}">
    <div style="color: green; padding: 10px;">${message}</div>
</c:if>

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
        <th>账号</th>
        <th>密码</th>
        <th>年龄</th>
        <th>邮箱</th>
        <th>操作</th>
    </tr>
    <c:forEach var="date" items="${dateList}">
        <tr>
            <td>${date.id}</td>
            <td>${date.name}</td>
            <td>${date.uname}</td>
            <td>******</td>
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

<%-- 分页导航栏 --%>
<div style="margin-top: 15px; text-align: center;">
    <%-- 上一页：如果当前是第一页，链接不可点击 --%>
    <c:if test="${pageInfo.hasPreviousPage}">
        <a href="${pageContext.request.contextPath}/user/showAllStudent?pageNum=${pageInfo.prePage}&pageSize=${pageInfo.pageSize}">上一页</a>
    </c:if>
    <c:if test="${!pageInfo.hasPreviousPage}">
        <span style="color: #ccc;">上一页</span>
    </c:if>

    <%-- 中间页码：遍历所有页码，当前页高亮显示 --%>
    <c:forEach var="i" begin="1" end="${pageInfo.pages}">
        <c:if test="${i == pageInfo.pageNum}">
            <strong>[${i}]</strong>
        </c:if>
        <c:if test="${i != pageInfo.pageNum}">
            <a href="${pageContext.request.contextPath}/user/showAllStudent?pageNum=${i}&pageSize=${pageInfo.pageSize}">${i}</a>
        </c:if>
    </c:forEach>

    <%-- 下一页：如果当前是最后一页，链接不可点击 --%>
    <c:if test="${pageInfo.hasNextPage}">
        <a href="${pageContext.request.contextPath}/user/showAllStudent?pageNum=${pageInfo.nextPage}&pageSize=${pageInfo.pageSize}">下一页</a>
    </c:if>
    <c:if test="${!pageInfo.hasNextPage}">
        <span style="color: #ccc;">下一页</span>
    </c:if>

    <%-- 分页信息统计 --%>
    <br/>
    <span style="font-size: 12px; color: #666;">
        共 ${pageInfo.total} 条记录，第 ${pageInfo.pageNum} / ${pageInfo.pages} 页，每页 ${pageInfo.pageSize} 条
    </span>
</div>
</body>
</html>
