package com.study.controller;

import com.study.command.BoardCommand;
import com.study.command.BoardListCommand;
import com.study.command.BoardWriteCommand;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("*.do")
public class BoardController extends HttpServlet {

  //클라이언트의 GET / POST 요청을 doAction 메서드로 전달

  /**
   *
   * @param request an {@link HttpServletRequest} object that contains the request the client has made of the servlet
   *
   * @param response an {@link HttpServletResponse} object that contains the response the servlet sends to the client
   *
   * @throws ServletException
   * @throws IOException
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doAction(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doAction(request, response);
  }

  //요청 교통정리를 담당하는 액션 메서드
  private void doAction(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    //요청 파라미터의 한글 깨짐을 방지한다
    request.setCharacterEncoding("UTF-8");

    //요청 주소에서 명령어 추출
    String uri = request.getRequestURI();
    String contextPath = request.getContextPath();
    String commandPath = uri.substring(contextPath.length());

    BoardCommand command = null;

    try {
      //추출한 경로에 따라 알맞은 명령 실행
      if (commandPath.equals("/write.do")) {
        command = new BoardWriteCommand(); //게시글 등록
        command.execute(request, response); //실행
        response.sendRedirect("list.do"); //실행 후 목록 페이지로 이동
      }
      else if (commandPath.equals("/list.do")) {
        command = new BoardListCommand(); //게시글 목록
        command.execute(request, response); //실행
        request.getRequestDispatcher("list.jsp").forward(request, response); //실행 후 목록 페이지로 이동
      }

      //예외처리
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


}