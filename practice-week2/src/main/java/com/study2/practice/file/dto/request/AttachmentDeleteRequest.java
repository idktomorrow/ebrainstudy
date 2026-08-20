package com.study2.practice.file.dto.request;

/**
 * 첨부파일 삭제 요청. 첨부파일 자체엔 비밀번호가 없어서, 이 파일이 속한 게시글의
 * 비밀번호로 검증한다 (게시글 수정/삭제와 동일한 권한 모델).
 *
 * @param password 첨부파일이 속한 게시글의 비밀번호
 */
public record AttachmentDeleteRequest(
    String password
) {
}
