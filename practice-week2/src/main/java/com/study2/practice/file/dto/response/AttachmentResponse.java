package com.study2.practice.file.dto.response;

/**
 * 첨부파일 응답. 다운로드는 별도 API(GET /api/files/{id})로 하므로
 * 여기엔 파일 내용이 아니라 표시에 필요한 메타데이터만 담는다.
 *
 * @param id         첨부파일 id (다운로드/삭제 시 사용)
 * @param originName 원본 파일명 (확장자 포함)
 * @param fileSize   파일 크기(byte)
 */
public record AttachmentResponse(
    Integer id,
    String originName,
    Long fileSize
) {
}
