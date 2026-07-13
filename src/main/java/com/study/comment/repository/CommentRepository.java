package com.study.comment.repository;

import com.study.comment.entity.CommentEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * Comment 테이블 접근을 위한 MyBatis 매퍼
 */
@Mapper
public interface CommentRepository {

  // 댓글 등록
  void insertComment(CommentEntity comment);
}
