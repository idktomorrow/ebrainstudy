package com.study2.practice.comment.dto.response;

import java.time.LocalDateTime;

/**
 * 댓글 응답. 어느 게시글의 댓글인지는 이미 URL 경로로 알 수 있으므로 boardId는 포함하지 않는다.
 *
 * @param id        댓글 id
 * @param writer    작성자명
 * @param content   댓글 내용
 * @param createdAt 등록일시
 */
public record CommentResponse(
    Integer id,
    String writer,
    String content,
    LocalDateTime createdAt
) {
}
