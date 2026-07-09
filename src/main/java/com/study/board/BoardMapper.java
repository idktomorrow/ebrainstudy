package com.study.board;

import org.apache.ibatis.annotations.Mapper;

/**
 * Board 테이블 접근을 위한 MyBatis 매퍼
 */
@Mapper
public interface BoardMapper {

  void insertBoard(BoardEntity board);
}
