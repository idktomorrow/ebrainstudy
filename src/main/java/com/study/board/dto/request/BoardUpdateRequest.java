package com.study.board.dto.request;

/**
 * 게시글 수정 요청 DTO
 * 작성자는 변경X
 */
public record BoardUpdateRequest(
    Integer categoryId,
    String title,
    String content,
    String password
) {

}
