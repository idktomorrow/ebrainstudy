package com.study.command;

import com.study.dao.BoardDao;
import com.study.dto.BoardDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;

public class BoardListCommand implements BoardCommand {

  @Override
  public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception {

    //DAO를 통해 DB에서 글 목록 가져오기
    BoardDao dao = BoardDao.getInstance();
    ArrayList<BoardDto> list = dao.selectBoardList();

    //가져온 리스트를 request 객체에 저장하여 JSP로 전달
    request.setAttribute("list", list);
  }
}
