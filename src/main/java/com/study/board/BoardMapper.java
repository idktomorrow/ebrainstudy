package com.study.board;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * Board 테이블 접근을 위한 MyBatis 매퍼
 */
@Mapper
public interface BoardMapper {

  //게시물 등록
  void insertBoard(BoardEntity board);

  //게시물 목록 조회
  List<BoardEntity> selectBoardList();

  //게시물 상세 조회
  BoardEntity selectBoardDetail(Long id);

  //게시물 조회수 증가
  void increaseViewCount(Long id);

  //게시물 수정
  void updateBoard(BoardEntity board);

  //게시물 삭제
  void deleteBoard(Long id);

}
