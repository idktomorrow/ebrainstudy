<%--
  Created by IntelliJ IDEA.
  User: user
  Date: 2026-07-04
  Time: 오전 12:11
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>글쓰기</title>
</head>
<body>
<h2>게시글 작성</h2>
<form action="write.do" method="post">
    카테고리: <input type="text" name="category"><br>
    제목: <input type="text" name="title"><br>
    작성자: <input type="text" name="writer"><br>
    비밀번호: <input type="password" name="password"><br>
    내용: <textarea name="content" rows="10" cols="50"></textarea><br>
    <input type="submit" value="글쓰기">
    <input type="reset" value="취소">
</form>
</body>
</html>
