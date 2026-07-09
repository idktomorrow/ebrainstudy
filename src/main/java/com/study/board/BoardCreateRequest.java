package com.study.board;

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
