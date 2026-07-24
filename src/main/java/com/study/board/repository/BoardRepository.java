package com.study.board.repository;

import com.study.board.dto.request.BoardSearchCondition;
import com.study.board.entity.BoardEntity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Board 테이블 접근을 위한 MyBatis 매퍼
 */
@Mapper
public interface BoardRepository {

  //게시물 등록
  void insertBoard(BoardEntity board);

  //게시물 목록 조회 - 페이징
  List<BoardEntity> selectBoardList(@Param("condition") BoardSearchCondition condition, @Param("offset") int offset);

  //게시물 목록 조회 - 총 건수
  long countBoardList(@Param("condition") BoardSearchCondition condition);

  //게시물 상세 조회
  BoardEntity selectBoardDetail(Long id);

  //게시물 조회수 증가
  void increaseViewCount(Long id);

  //게시물 수정
  void updateBoard(BoardEntity board);

  //게시물 삭제
  void deleteBoard(Long id);

}
