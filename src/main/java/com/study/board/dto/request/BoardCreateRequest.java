package com.study.board.dto.request;

/**
 * 게시판 생성 요청 DTO
 */
public record BoardCreateRequest(
    Integer categoryId,
    String title,
    String writer,
    String content,
    String password
) {

}
