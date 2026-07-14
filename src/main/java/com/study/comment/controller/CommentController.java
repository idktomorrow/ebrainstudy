package com.study.comment.controller;

import com.study.comment.dto.request.CommentCreateRequest;
import com.study.comment.dto.response.CommentResponse;
import com.study.comment.service.CommentService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 댓글 컨트롤러
 */
@RestController
public class CommentController {

  private final CommentService commentService;

  public CommentController(CommentService commentService) {
    this.commentService = commentService;
  }

  @PostMapping("/api/boards/{boardId}/comments")
  public CommentResponse createComment(@PathVariable Long boardId, @RequestBody CommentCreateRequest request) {
    return commentService.createComment(boardId, request);
  }

  @GetMapping("/api/boards/{boardId}/comments")
  public List<CommentResponse> getComments(@PathVariable Long boardId) {
    return commentService.getComments(boardId);
  }

}
