package com.study2.practice.comment.controller;

import com.study2.practice.comment.dto.request.CommentCreateRequest;
import com.study2.practice.comment.dto.response.CommentResponse;
import com.study2.practice.comment.service.CommentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 댓글 등록/조회 API. 게시글 하위 리소스라 경로에 boardId가 포함된다.
 */
@RestController
@RequestMapping("/api/boards/{boardId}/comments")
@RequiredArgsConstructor
public class CommentController {

  private final CommentService commentService;

  /** 게시글별 댓글 목록 조회. */
  @GetMapping
  public List<CommentResponse> getComments(@PathVariable Integer boardId) {
    return commentService.getComments(boardId);
  }

  /** 댓글 등록. 생성된 댓글의 id를 반환. */
  @PostMapping
  public Integer createComment(@PathVariable Integer boardId, @RequestBody CommentCreateRequest request) {
    return commentService.createComment(boardId, request);
  }
}
