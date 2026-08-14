package com.study2.practice.board.dto.response;

import java.time.LocalDateTime;

/**
 * 게시글 상세 조회 응답. BoardSummaryResponse와 달리 본문(content)을 포함한다.
 * 비밀번호는 포함하지 않는다.
 *
 * @param id           게시글 id
 * @param categoryName 카테고리명
 * @param title        제목
 * @param writer       작성자명
 * @param content      본문 내용
 * @param viewCount    조회수
 * @param createdAt    최초 등록일시
 * @param updatedAt    수정일시. 수정 이력이 없으면 null
 */
public record BoardDetailResponse(
    Integer id,
    String categoryName,
    String title,
    String writer,
    String content,
    Integer viewCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}