package com.study2.practice.board.dto.response;

import java.util.List;

/**
 * 게시글 목록 조회 API의 최종 응답. 페이지네이션을 위해 목록과 총 건수를 함께 담는다.
 *
 * @param boards     현재 페이지의 게시글 목록
 * @param totalCount 검색조건에 해당하는 전체 게시글 건수
 */
public record BoardListResponse(

    List<BoardSummaryResponse> boards,
    int totalCount
) {

}
