package com.study.comment.dto.response;

import java.time.LocalDateTime;

/**
 * 댓글 응답 DTO
 */
public record CommentResponse(
    Integer id,
    Long boardId,
    String writer,
    String content,
    LocalDateTime createdAt
) {

}
