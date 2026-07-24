package com.study.board.dto.response;

import java.util.List;

/**
 * 게시판 목록 조회 응답 DTO
 * 페이징을 위해 목록과 총 건수를 함께 담음
 */
public record BoardListResponse(

    List<BoardResponse> boards,
    long totalCount
) {

}
