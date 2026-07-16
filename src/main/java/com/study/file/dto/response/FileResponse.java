package com.study.file.dto.response;

/**
 * 첨부파일 응답 DTO
 * storedName, filePath는 서버 내부 정보라 제외 (다운로드는 id로 별도 처리)
 */
public record FileResponse(
    Integer id,
    String originName,
    Long fileSize,
    String fileFormat
) {

}
