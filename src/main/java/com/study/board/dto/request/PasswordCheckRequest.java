package com.study.board.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 게시글 비밀번호 확인 요청 DTO
 */
public record PasswordCheckRequest(
    @NotBlank(message = "비밀번호는 필수입니다.")
    String password
) {

}
