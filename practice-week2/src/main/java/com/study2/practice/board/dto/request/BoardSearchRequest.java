package com.study2.practice.board.dto.request;

import java.time.LocalDate;

/**
 * 게시글 목록 조회 검색조건.
 *
 * @param keyword     제목+작성자+내용 통합 검색어. null이면 검색어 조건 없음
 * @param categoryId  카테고리 필터. null이면 전체 카테고리
 * @param startDate   등록일 검색 시작일. null이면 조건 없음
 * @param endDate     등록일 검색 종료일
 * @param page        페이지 번호 (1부터 시작)
 * @param size        페이지당 건수 (기본 10)
 */
public record BoardSearchRequest(
    String keyword,
    Integer categoryId,
    LocalDate startDate,
    LocalDate endDate,
    int page,
    int size
) {

}
