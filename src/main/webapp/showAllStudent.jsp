<%--
  学生列表页（核心视图）
  【数据来源】Controller 的 ModelAndView 放进来的 dateList（学生列表）
             和 pageInfo（PageHelper 分页信息），注意 dateList 是历史命名（应为 dataList）
  【学习要点】
  1. <c:forEach items="${dateList}" var="date">：JSTL 循环渲染表格行，
     ${date.id} 等价于调用 date.getId()
  2. 分页导航全部从 ${pageInfo} 取值：hasPreviousPage/prePage/nextPage/pages/total，
     翻页链接带上 pageSize 防止"翻页后每页条数被重置"
  3. 批量删除：复选框 name 统一为 ids，提交后 Spring 自动绑定为 List<Integer>
  4. 密码列写死 ******：视图层脱敏——哪怕库里存了明文也不该展示
  5. ${sessionScope.loginUser.uname}：显式从 Session 域取当前登录用户
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page isELIgnored="false" %>
<html>
<head>
    <title>学生列表</title>
    <script type="text/javascript">
        // 全选/取消全选复选框
        function checkAll(obj) {
            var boxes = document.getElementsByName("ids");
            for (var i = 0; i < boxes.length; i++) {
                boxes[i].checked = obj.checked;
            }
        }
        // 批量删除前确认
        function batchDelete() {
            var boxes = document.getElementsByName("ids");
            var count = 0;
            for (var i = 0; i < boxes.length; i++) {
                if (boxes[i].checked) count++;
            }
            if (count === 0) {
                alert("请至少选择一条记录");
                return false;
            }
            return confirm("确定要删除选中的 " + count + " 条记录吗？");
        }
        // 切换每页条数
        function changePageSize(select) {
            var pageSize = select.value;
            window.location.href = "${pageContext.request.contextPath}/user/showAllStudent?pageNum=1&pageSize=" + pageSize;
        }
    </script>
</head>
<body>
<div style="padding: 10px; background: #f0f0f0;">
    <c:if test="${not empty sessionScope.loginUser}">
        当前用户：${sessionScope.loginUser.uname} |
        <a href="${pageContext.request.contextPath}/user/profile">个人中心</a> |
        <a href="${pageContext.request.contextPath}/user/changePassword">修改密码</a> |
        <a href="${pageContext.request.contextPath}/user/logout">退出</a>
    </c:if>
</div>

<c:if test="${not empty message}">
    <div style="color: green; padding: 10px;">${message}</div>
</c:if>
<c:if test="${not empty error}">
    <div style="color: red; padding: 10px;">${error}</div>
</c:if>

<button type="button">
    <a href="${pageContext.request.contextPath}/register.jsp">新增</a>
</button>
<button type="button">
    <a href="${pageContext.request.contextPath}/user/export">导出Excel</a>
</button>

<%-- 多条件搜索表单 --%>
<form action="${pageContext.request.contextPath}/user/search" method="get" style="margin: 10px 0; padding: 10px; background: #f9f9f9; border: 1px solid #ddd;">
    ID：<input type="text" name="id" placeholder="按ID精确查询" size="6">
    姓名：<input type="text" name="name" placeholder="按姓名模糊查询" size="10">
    年龄：<input type="text" name="age" placeholder="按年龄精确查询" size="6">
    邮箱：<input type="text" name="email" placeholder="按邮箱模糊查询" size="15">
    <input type="submit" value="多条件搜索">
    <a href="${pageContext.request.contextPath}/user/showAllStudent" style="margin-left: 10px;">显示全部</a>
</form>

<%-- 批量删除表单 --%>
<form action="${pageContext.request.contextPath}/user/batchDelete" method="post" onsubmit="return batchDelete()">
    <table border="1">
        <tr>
            <th><input type="checkbox" onclick="checkAll(this)">全选</th>
            <th>编号</th>
            <th>姓名</th>
            <th>账号</th>
            <th>密码</th>
            <th>年龄</th>
            <th>邮箱</th>
            <th>手机号</th>
            <th>性别</th>
            <th>操作</th>
        </tr>
        <c:forEach var="date" items="${dateList}">
            <tr>
                <td><input type="checkbox" name="ids" value="${date.id}"></td>
                <td>${date.id}</td>
                <td>${date.name}</td>
                <td>${date.uname}</td>
                <td>******</td>
                <td>${date.age}</td>
                <td>${date.email}</td>
                <td>${date.phone}</td>
                <td>${date.gender}</td>
                <td>
                    <a href="${pageContext.request.contextPath}/user/editStudent?id=${date.id}">编辑</a>
                    <a href="${pageContext.request.contextPath}/user/deleteStudent?id=${date.id}"
                       onclick="return confirm('确定删除吗？')">删除</a>
                </td>
            </tr>
        </c:forEach>
        <c:if test="${empty dateList}">
            <tr><td colspan="10" style="text-align: center; color: #999;">暂无学生数据</td></tr>
        </c:if>
    </table>
    <c:if test="${not empty dateList}">
    <input type="submit" value="批量删除" style="margin-top: 5px;">
    </c:if>
</form>

<%-- 分页导航栏 + 每页条数选择器 --%>
<div style="margin-top: 15px; text-align: center;">
    <c:if test="${pageInfo.hasPreviousPage}">
        <a href="${pageContext.request.contextPath}/user/showAllStudent?pageNum=${pageInfo.prePage}&pageSize=${pageInfo.pageSize}">上一页</a>
    </c:if>
    <c:if test="${!pageInfo.hasPreviousPage}">
        <span style="color: #ccc;">上一页</span>
    </c:if>

    <c:forEach var="i" begin="1" end="${pageInfo.pages}">
        <c:if test="${i == pageInfo.pageNum}">
            <strong>[${i}]</strong>
        </c:if>
        <c:if test="${i != pageInfo.pageNum}">
            <a href="${pageContext.request.contextPath}/user/showAllStudent?pageNum=${i}&pageSize=${pageInfo.pageSize}">${i}</a>
        </c:if>
    </c:forEach>

    <c:if test="${pageInfo.hasNextPage}">
        <a href="${pageContext.request.contextPath}/user/showAllStudent?pageNum=${pageInfo.nextPage}&pageSize=${pageInfo.pageSize}">下一页</a>
    </c:if>
    <c:if test="${!pageInfo.hasNextPage}">
        <span style="color: #ccc;">下一页</span>
    </c:if>

    <br/>
    <span style="font-size: 12px; color: #666;">
        共 ${pageInfo.total} 条记录，第 ${pageInfo.pageNum} / ${pageInfo.pages} 页，
        每页
        <select onchange="changePageSize(this)" style="font-size: 12px;">
            <option value="5"  ${pageInfo.pageSize == 5  ? 'selected' : ''}>5 条</option>
            <option value="10" ${pageInfo.pageSize == 10 ? 'selected' : ''}>10 条</option>
            <option value="20" ${pageInfo.pageSize == 20 ? 'selected' : ''}>20 条</option>
            <option value="50" ${pageInfo.pageSize == 50 ? 'selected' : ''}>50 条</option>
        </select>
    </span>
</div>
</body>
</html>