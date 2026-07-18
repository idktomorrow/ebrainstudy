package com.study.comment.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 댓글 등록 요청 DTO
 */
public record CommentCreateRequest(
    @NotBlank(message = "작성자는 필수입니다.")
    String writer,

    @NotBlank(message = "내용은 필수입니다.")
    String content
) {

}
