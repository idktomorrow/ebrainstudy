<%--
  Created by IntelliJ IDEA.
  User: user
  Date: 2026-07-04
  Time: 오후 12:24
  To change this template use File | Settings | File Templates.
--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>게시글 목록</title>
</head>
<body>
<h2>게시글 목록</h2>
<table>
    <tr>
        <th>번호</th>
        <th>카테고리</th>
        <th>제목</th>
        <th>작성자</th>
    </tr>
<c:forEach var="dto" items="${list}">
    <tr>
        <td>${dto.id}</td>
        <td>${dto.category}</td>
        <td>${dto.title}</td>
        <td>${dto.writer}</td>
    </tr>
</c:forEach>
</table>
<br>
<a href="write.jsp">글쓰기</a>
</body>
</html>