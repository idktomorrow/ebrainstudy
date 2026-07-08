package com.study.dao;

import com.study.connection.ConnectionTest;
import com.study.dto.BoardDto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class BoardDao {

  //한개의 객체를 미리 설정해둠
  private static BoardDao instance = new BoardDao();

  //외부에서 새로 생성할수없게 private으로 막기
  private BoardDao() {
  }

  //외부에서는 getInstance 메서드를 통해서만 호출 가능
  public static BoardDao getInstance() {
    return instance;
  }

  //게시글 등록을 위한 메서드
  public void insertBoard(BoardDto dto) {
    Connection conn = null;
    PreparedStatement pstmt = null;

    //실행할 쿼리문 - 데이터가 들어갈 자리는 ?로 비워둔다
    String sql = "INSERT INTO board (category, title, content, writer, password) " +
        "VALUES (?, ?, ?, ?, ?)";

    try {
      //공용 연결 메서드 호출
      conn = ConnectionTest.getConnection();

      //쿼리문을 실행할 객체 생성
      pstmt = conn.prepareStatement(sql);

      //쿼리문 ?에 순서에 맞게 값 세팅
      pstmt.setString(1, dto.getCategory());
      pstmt.setString(2, dto.getTitle());
      pstmt.setString(3, dto.getContent());
      pstmt.setString(4, dto.getWriter());
      pstmt.setString(5, dto.getPassword());

      //쿼리문 실행
      int result = pstmt.executeUpdate();

      //쿼리문 실행 결과
      if (result > 0) {
        System.out.println("게시글 등록 성공");
      }

      //예외처리
    } catch (Exception e) {
      e.printStackTrace();
    } finally {

      //연결 끊기
      try {
        if (pstmt != null) {
          pstmt.close();
        }

        if (conn != null) {
          conn.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public ArrayList<BoardDto> selectBoardList() {
    Connection conn = null;
    PreparedStatement pstmt = null;
    ArrayList<BoardDto> list = new ArrayList<>();
    //데이터를 담아올 바구니
    ResultSet rs = null;

    //실행할 쿼리문
    String sql = "SELECT * FROM board";

    try {
      conn = ConnectionTest.getConnection();
      pstmt = conn.prepareStatement(sql);
      rs = pstmt.executeQuery();

      while (rs.next()) {
        BoardDto dto = new BoardDto();
        //DB에서 가져온 값을 DTO에 넣어주기
        dto.setId(rs.getLong("id"));
        dto.setCategory(rs.getString("category"));
        dto.setTitle(rs.getString("title"));
        dto.setWriter(rs.getString("writer"));

        //다채운  DTO를 리스트에 넣어줌
        list.add(dto);
      }
    } catch (Exception e) {
      e.printStackTrace();
      //닫아주기
    } finally {
      try {
        if (rs != null) {
          rs.close();
        }
        if (pstmt != null) {
          pstmt.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return list;
  }
}
