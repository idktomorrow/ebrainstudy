package com.study2.practice.board.dto.response;

import java.time.LocalDateTime;

/**
 * 게시글 목록 한 건의 응답 형태. 본문(content)과 비밀번호는 포함하지 않는다.
 *
 * @param id            게시글 id
 * @param categoryName  카테고리명 (categoryId가 아닌 이름으로 응답)
 * @param title         제목
 * @param writer        작성자명
 * @param viewCount     조회수
 * @param createdAt     최초 등록일시
 * @param updatedAt     수정일시. 수정 이력이 없으면 null
 * @param hasAttachment 첨부파일 존재 여부 (목록 화면 아이콘 표시용)
 */
public record BoardSummaryResponse(

    Integer id,
    String categoryName,
    String title,
    String writer,
    Integer viewCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    boolean hasAttachment
) {

}
