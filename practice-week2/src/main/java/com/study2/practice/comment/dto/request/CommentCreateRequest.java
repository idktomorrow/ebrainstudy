package com.study2.practice.comment.dto.request;

/**
 * 댓글 등록 요청. 대상 게시글 id는 URL 경로로 받으므로 여기엔 없다.
 *
 * @param writer  댓글 작성자명
 * @param content 댓글 내용
 */
public record CommentCreateRequest(
    String writer,
    String content
) {
}
