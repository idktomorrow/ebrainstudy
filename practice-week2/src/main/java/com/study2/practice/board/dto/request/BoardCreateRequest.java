package com.study2.practice.board.dto.request;

/**
 * 게시글 등록 요청.
 *
 * @param categoryId 소속 카테고리 (필수)
 * @param writer     작성자명 (필수, 3자 이상 5자 미만)
 * @param title      제목 (필수, 4자 이상 100자 미만)
 * @param content    내용 (필수, 4자 이상 2000자 미만)
 * @param password   비밀번호 (필수, 4자 이상 16자 미만, 영문/숫자/특수문자 포함)
 */
public record BoardCreateRequest(

    Integer categoryId,
    String writer,
    String title,
    String content,
    String password

) {
}
