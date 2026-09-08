<%--
  修改密码页：原密码 + 新密码 + 确认密码三个字段。
  ${message}/${error} 分别显示成功/失败提示（Controller 放进 Model）；
  "两次输入是否一致"的前端提示最终以后端 Controller 校验为准
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>修改密码</title>
</head>
<body>
<h2>修改密码</h2>

<c:if test="${not empty message}">
    <div style="color: green; padding: 10px;">${message}</div>
</c:if>
<c:if test="${not empty error}">
    <div style="color: red; padding: 10px;">${error}</div>
</c:if>

<form action="${pageContext.request.contextPath}/user/changePassword" method="post">
    <table>
        <tr>
            <td>原密码：</td>
            <td><input type="password" name="oldPass" required></td>
        </tr>
        <tr>
            <td>新密码：</td>
            <td><input type="password" name="newPass" required></td>
        </tr>
        <tr>
            <td>确认新密码：</td>
            <td><input type="password" name="confirmPass" required></td>
        </tr>
        <tr>
            <td colspan="2">
                <input type="submit" value="修改密码">
                <a href="${pageContext.request.contextPath}/user/showAllStudent">返回</a>
            </td>
        </tr>
    </table>
</form>
</body>
</html>