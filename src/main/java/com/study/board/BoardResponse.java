package com.study.board;

import java.time.LocalDateTime;

/**
 * 게시판 응답 DTO
 * password는 클라이언트에게 노출되면 안 되므로 제외
 */
public record BoardResponse(
    Long id,
    Integer categoryId,
    String title,
    String writer,
    String content,
    Integer viewCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

}
