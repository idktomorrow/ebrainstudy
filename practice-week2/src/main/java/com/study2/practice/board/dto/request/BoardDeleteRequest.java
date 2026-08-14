package com.study2.practice.board.dto.request;

/**
 * 게시글 삭제 요청. 삭제 전 비밀번호 확인 레이어에서 사용.
 *
 * @param password 본인 확인용 비밀번호
 */
public record BoardDeleteRequest(

    String password
) {

}
