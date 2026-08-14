package com.study2.practice.board.dto.request;

/**
 * 게시글 수정 요청. 카테고리는 수정 대상에서 제외한다 (와이어프레임상 표시만 가능).
 *
 * @param writer   작성자명
 * @param title    제목
 * @param content  내용
 * @param password 본인 확인용 비밀번호 (등록 시 입력한 값과 일치해야 함, 변경 대상 아님)
 */
public record BoardUpdateRequest(

    String writer,
    String title,
    String content,
    String password
) {

}
