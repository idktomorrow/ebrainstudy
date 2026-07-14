package com.study.comment.service;

import com.study.comment.dto.request.CommentCreateRequest;
import com.study.comment.dto.response.CommentResponse;
import com.study.comment.entity.CommentEntity;
import com.study.comment.mapper.CommentMapper;
import com.study.comment.repository.CommentRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 댓글 비즈니스 로직
 */
@Service
public class CommentService {

  private final CommentRepository commentRepository;
  private final CommentMapper commentMapper;

  public CommentService(CommentRepository commentRepository, CommentMapper commentMapper) {
    this.commentRepository = commentRepository;
    this.commentMapper = commentMapper;
  }

  public CommentResponse createComment(Long boardId, CommentCreateRequest request) {
    CommentEntity comment = commentMapper.toEntity(request);  // 요청 -> 엔티티
    comment.setBoardId(boardId);
    comment.setCreatedAt(LocalDateTime.now());
    commentRepository.insertComment(comment);                 // Insert 실행
    return commentMapper.toResponse(comment);                 // 엔티티 -> 응답
  }

  public List<CommentResponse> getComments(Long boardId) {
    return commentRepository.selectCommentsByBoardId(boardId).stream()
        .map(commentMapper::toResponse)
        .toList();
  }

}
