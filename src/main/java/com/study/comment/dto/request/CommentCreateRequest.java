package com.study.comment.dto.request;

/**
 * 댓글 등록 요청 DTO
 */
public record CommentCreateRequest(
    String writer,
    String content
) {

}
