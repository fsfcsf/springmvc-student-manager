<%--
  多条件查询结果页
  【与列表页的区别】
  1. 表单回显：value="${name}"——Controller 把查询条件塞回 Model，
     输入框显示上次输入的内容，用户可以微调条件再查
  2. 翻页链接必须携带全部查询参数（&id=${id}&name=${name}&...）——
     多条件分页最容易丢条件，丢了点"下一页"就变成查全部
  3. 查询表单 method="get"：参数在 URL 上，整页搜索结果可以直接复制分享
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page isELIgnored="false" %>
<html>
<head>
    <title>查询结果</title>
    <script type="text/javascript">
        function checkAll(obj) {
            var boxes = document.getElementsByName("ids");
            for (var i = 0; i < boxes.length; i++) {
                boxes[i].checked = obj.checked;
            }
        }
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
        function changePageSize(select) {
            var pageSize = select.value;
            window.location.href = "${pageContext.request.contextPath}/user/search?pageNum=1&pageSize=" + pageSize + "&id=${id}&name=${name}&age=${age}&email=${email}";
        }
    </script>
</head>
<body>
<button type="button">
    <a href="${pageContext.request.contextPath}/register.jsp">新增</a>
</button>
<button type="button">
    <a href="${pageContext.request.contextPath}/user/showAllStudent">返回全部列表</a>
</button>
<button type="button">
    <a href="${pageContext.request.contextPath}/user/export">导出Excel</a>
</button>

<%-- 多条件搜索表单 --%>
<form action="${pageContext.request.contextPath}/user/search" method="get" style="margin: 10px 0; padding: 10px; background: #f9f9f9; border: 1px solid #ddd;">
    ID：<input type="text" name="id" value="${id}" placeholder="按ID精确查询" size="6">
    姓名：<input type="text" name="name" value="${name}" placeholder="按姓名模糊查询" size="10">
    年龄：<input type="text" name="age" value="${age}" placeholder="按年龄精确查询" size="6">
    邮箱：<input type="text" name="email" value="${email}" placeholder="按邮箱模糊查询" size="15">
    <input type="submit" value="多条件搜索">
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
            <tr><td colspan="10" style="text-align: center; color: #999;">没有找到匹配的学生</td></tr>
        </c:if>
    </table>
    <c:if test="${not empty dateList}">
        <input type="submit" value="批量删除" style="margin-top: 5px;">
    </c:if>
</form>

<%-- 分页导航栏 --%>
<c:if test="${not empty pageInfo}">
<div style="margin-top: 15px; text-align: center;">
    <c:if test="${pageInfo.hasPreviousPage}">
        <a href="${pageContext.request.contextPath}/user/search?pageNum=${pageInfo.prePage}&pageSize=${pageInfo.pageSize}&id=${id}&name=${name}&age=${age}&email=${email}">上一页</a>
    </c:if>
    <c:if test="${!pageInfo.hasPreviousPage}">
        <span style="color: #ccc;">上一页</span>
    </c:if>

    <c:forEach var="i" begin="1" end="${pageInfo.pages}">
        <c:if test="${i == pageInfo.pageNum}">
            <strong>[${i}]</strong>
        </c:if>
        <c:if test="${i != pageInfo.pageNum}">
            <a href="${pageContext.request.contextPath}/user/search?pageNum=${i}&pageSize=${pageInfo.pageSize}&id=${id}&name=${name}&age=${age}&email=${email}">${i}</a>
        </c:if>
    </c:forEach>

    <c:if test="${pageInfo.hasNextPage}">
        <a href="${pageContext.request.contextPath}/user/search?pageNum=${pageInfo.nextPage}&pageSize=${pageInfo.pageSize}&id=${id}&name=${name}&age=${age}&email=${email}">下一页</a>
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
</c:if>
</body>
</html>