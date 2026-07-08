package com.study.command;

import com.study.dao.BoardDao;
import com.study.dto.BoardDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class BoardWriteCommand implements BoardCommand {

  @Override
  public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception {

    //request 객체에서 사용자가 입력한 파라미터 추출하기
    String category = request.getParameter("category");
    String title = request.getParameter("title");
    String writer = request.getParameter("writer");
    String password = request.getParameter("password");
    String content = request.getParameter("content");

    //DTO 객체 생성
    BoardDto dto = new BoardDto();

    //DTO에 값 세팅
    dto.setCategory(category);
    dto.setTitle(title);
    dto.setWriter(writer);
    dto.setPassword(password);
    dto.setContent(content);

    //DAO를 호출해서 DB에 저장할 비즈니스 로직 수행
    BoardDao dao = BoardDao.getInstance();
    dao.insertBoard(dto);

  }
}
