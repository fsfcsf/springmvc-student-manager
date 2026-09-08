<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page isELIgnored="false" %>
<html>
<head>
    <title>个人中心</title>
    <style>
        .container { max-width: 600px; margin: 20px auto; padding: 20px; border: 1px solid #ddd; }
        .form-group { margin: 10px 0; }
        label { display: inline-block; width: 80px; }
        input, select { padding: 5px; width: 200px; }
        .avatar { width: 100px; height: 100px; border-radius: 50%; object-fit: cover; border: 2px solid #ddd; }
        .avatar-default { width: 100px; height: 100px; border-radius: 50%; background: #eee;
                          display: inline-block; text-align: center; line-height: 100px; color: #999; }
    </style>
</head>
<body>
<div class="container">
    <h2>个人中心</h2>

    <c:if test="${not empty message}">
        <div style="color: green; padding: 10px;">${message}</div>
    </c:if>
    <c:if test="${not empty error}">
        <div style="color: red; padding: 10px;">${error}</div>
    </c:if>

    <%-- 头像区域 --%>
    <div style="text-align: center; margin: 20px 0;">
        <c:if test="${not empty user.avatar}">
            <img src="${pageContext.request.contextPath}${user.avatar}" class="avatar" alt="头像">
        </c:if>
        <c:if test="${empty user.avatar}">
            <span class="avatar-default">无头像</span>
        </c:if>

        <%-- 头像上传表单 --%>
        <form action="${pageContext.request.contextPath}/user/uploadAvatar" method="post"
              enctype="multipart/form-data" style="margin-top: 10px;">
            <input type="file" name="avatarFile" accept="image/*" required>
            <button type="submit">上传头像</button>
        </form>
    </div>

    <hr>

    <%-- 个人信息编辑表单 --%>
    <form action="${pageContext.request.contextPath}/user/updateProfile" method="post">
        <div class="form-group">
            <label>编号：</label>
            <span>${user.id}</span>
        </div>
        <div class="form-group">
            <label>账号：</label>
            <span>${user.uname}</span>
        </div>
        <div class="form-group">
            <label>姓名：</label>
            <input type="text" name="name" value="${user.name}" required>
        </div>
        <div class="form-group">
            <label>年龄：</label>
            <input type="number" name="age" value="${user.age}" required>
        </div>
        <div class="form-group">
            <label>邮箱：</label>
            <input type="email" name="email" value="${user.email}" required>
        </div>
        <div class="form-group">
            <label>手机号：</label>
            <input type="text" name="phone" value="${user.phone}">
        </div>
        <div class="form-group">
            <label>性别：</label>
            <select name="gender">
                <option value=""  ${empty user.gender ? 'selected' : ''}>请选择</option>
                <option value="男" ${user.gender == '男' ? 'selected' : ''}>男</option>
                <option value="女" ${user.gender == '女' ? 'selected' : ''}>女</option>
            </select>
        </div>
        <button type="submit">保存修改</button>
    </form>

    <div style="margin-top: 15px;">
        <a href="${pageContext.request.contextPath}/user/showAllStudent">返回管理页面</a> |
        <a href="${pageContext.request.contextPath}/user/changePassword">修改密码</a> |
        <a href="${pageContext.request.contextPath}/user/logout">退出登录</a>
    </div>
</div>
</body>
</html>