package com.study2.practice.comment.service;

import com.study2.practice.board.mapper.BoardMapper;
import com.study2.practice.comment.dto.request.CommentCreateRequest;
import com.study2.practice.comment.dto.response.CommentResponse;
import com.study2.practice.comment.entity.Comment;
import com.study2.practice.comment.mapper.CommentMapper;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 댓글 등록/조회 비즈니스 로직.
 * 게시글 존재 확인을 위해 BoardMapper도 함께 사용한다.
 */
@Service
@RequiredArgsConstructor
public class CommentService {

  private final CommentMapper commentMapper;
  private final BoardMapper boardMapper;

  /** 댓글 등록. 대상 게시글 존재 확인 + 필드 검증 후 insert. */
  public Integer createComment(Integer boardId, CommentCreateRequest request) {

    if (boardMapper.findById(boardId) == null) {
      throw new NoSuchElementException("게시글을 찾을 수 없습니다.");
    }

    validateWriter(request.writer());
    validateContent(request.content());

    Comment comment = new Comment();
    comment.setBoardId(boardId);
    comment.setWriter(request.writer());
    comment.setContent(request.content());

    commentMapper.insert(comment);   // insert 후 comment.getId()에 생성된 id가 채워짐

    return comment.getId();
  }

  /** 게시글별 댓글 목록 조회. */
  public List<CommentResponse> getComments(Integer boardId) {

    if (boardMapper.findById(boardId) == null) {
      throw new NoSuchElementException("게시글을 찾을 수 없습니다.");
    }

    List<Comment> comments = commentMapper.findByBoardId(boardId);

    return comments.stream()
        .map(comment -> new CommentResponse(
            comment.getId(),
            comment.getWriter(),
            comment.getContent(),
            comment.getCreatedAt()
        ))
        .toList();
  }

  private void validateWriter(String writer) {
    if (writer == null || writer.isBlank()) {
      throw new IllegalArgumentException("작성자를 입력해주세요.");
    }
    // comment.writer 컬럼이 VARCHAR(50)이라, 이걸 안 막으면 DB에서
    // "Data too long for column" 에러가 나서 500으로 응답돼버림
    if (writer.length() > 50) {
      throw new IllegalArgumentException("작성자는 50자 이하여야 합니다.");
    }
  }

  private void validateContent(String content) {
    if (content == null || content.isBlank()) {
      throw new IllegalArgumentException("댓글 내용을 입력해주세요.");
    }
    // content 컬럼은 TEXT라 DB 에러는 안 나지만(6만자 넘어야 문제), 게시글 content(2000자
    // 제한)와의 일관성 + 비정상적으로 긴 댓글 방지를 위해 같은 기준으로 상한을 둠
    if (content.length() > 2000) {
      throw new IllegalArgumentException("댓글 내용은 2000자 이하여야 합니다.");
    }
  }
}
