package com.study2.practice.board.mapper;

import com.study2.practice.board.dto.request.BoardSearchRequest;
import com.study2.practice.board.entity.Board;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BoardMapper {

  //게시물 등록
  void insert(Board board);

  //게시물 상세 조회
  Board findById(Integer id);

  //게시물 조회수 증가
  void increaseViewCount(Integer id);

  //게시물 수정
  void update(Board board);

  //게시물 삭제
  void delete(Integer id);

  // 목록 조회 (검색+페이지네이션)
  List<Board> findAll(BoardSearchRequest condition);

  // 검색된 총 건수 (페이지네이션용, 총 N건 표시)
  int countAll(BoardSearchRequest condition);
}
