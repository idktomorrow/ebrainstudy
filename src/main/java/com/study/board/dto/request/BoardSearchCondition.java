package com.study.board.dto.request;

import java.time.LocalDate;

public record BoardSearchCondition(
    Integer categoryId,   // 카테고리 필터 (null이면 전체)
    String keyword,       // 제목/작성자/내용 검색어 (null이면 전체)
    LocalDate startDate,  // 등록일 시작
    LocalDate endDate,    // 등록일 끝
    int page,             // 화면에 보이는 페이지 번호 (1부터 시작)
    int size              // 페이지당 게시글 수
) {
  // 실제 DB LIMIT/OFFSET 계산에 쓸 offset 값 (0부터 시작하는 인덱스로 변환)
  public int offset() {
    return (page - 1) * size;
  }
}
