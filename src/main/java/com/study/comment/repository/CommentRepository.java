package com.study.comment.repository;

import com.study.comment.entity.CommentEntity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * Comment 테이블 접근을 위한 MyBatis 매퍼
 */
@Mapper
public interface CommentRepository {

  // 댓글 등록
  void insertComment(CommentEntity comment);

  // 게시글별 댓글 목록 조회
  List<CommentEntity> selectCommentsByBoardId(Long boardId);
}
