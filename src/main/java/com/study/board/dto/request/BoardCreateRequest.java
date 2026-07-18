package com.study.board.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 게시판 생성 요청 DTO
 */
public record BoardCreateRequest(
    @NotNull(message = "카테고리는 필수입니다.")
    Integer categoryId,

    @NotBlank(message = "제목은 필수입니다.")
    @Size(min = 4, max = 99, message = "제목은 4자 이상 100자 미만이어야 합니다.")
    String title,

    @NotBlank(message = "작성자는 필수입니다.")
    @Size(min = 3, max = 4, message = "작성자는 3자 이상 5자 미만이어야 합니다.")
    String writer,

    @NotBlank(message = "내용은 필수입니다.")
    @Size(min = 4, max = 1999, message = "내용은 4자 이상 2000자 미만이어야 합니다.")
    String content,

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 4, max = 15, message = "비밀번호는 4자 이상 16자 미만이어야 합니다.")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
        message = "비밀번호는 영문/숫자/특수문자를 모두 포함해야 합니다."
    )
    String password
) {

}
