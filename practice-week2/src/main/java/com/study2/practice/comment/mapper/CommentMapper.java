package com.study2.practice.comment.mapper;

import com.study2.practice.comment.entity.Comment;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * comment 테이블에 대한 MyBatis Mapper.
 * 등록/조회만 지원 (수정/삭제는 와이어프레임에 없어서 제공하지 않음).
 * 각 메서드는 resources/mappers/CommentMapper.xml의 동일한 id와 매칭된다.
 */
@Mapper
public interface CommentMapper {

  /** 댓글 등록. useGeneratedKeys로 채번된 id가 comment.id에 채워진다. */
  void insert(Comment comment);

  /** 게시글 id로 댓글 목록 조회. 오래된 순으로 정렬해서 최근 댓글이 마지막에 오게 함. */
  List<Comment> findByBoardId(Integer boardId);
}
