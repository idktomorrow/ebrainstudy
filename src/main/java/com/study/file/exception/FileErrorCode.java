package com.study.file.exception;

import com.study.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 첨부파일 도메인 에러 코드 (번호 대역: 2000~2999)
 */
@Getter
@RequiredArgsConstructor
public enum FileErrorCode implements ErrorCode {

  FILE_NOT_FOUND(2001, "NOT_FOUND", HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다."),
  FILE_UPLOAD_FAILED(2002, "UPLOAD_FAILED", HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
  FILE_DOWNLOAD_FAILED(2003, "DOWNLOAD_FAILED", HttpStatus.INTERNAL_SERVER_ERROR, "파일 다운로드에 실패했습니다."),
  FILE_DELETE_FAILED(2004, "DELETE_FAILED", HttpStatus.INTERNAL_SERVER_ERROR, "파일 삭제에 실패했습니다."),
  INVALID_FILE_PATH(2005, "INVALID_PATH", HttpStatus.BAD_REQUEST, "허용되지 않은 파일 경로입니다."),
  INVALID_FILE_TYPE(2006, "INVALID_TYPE", HttpStatus.BAD_REQUEST, "허용되지 않은 파일 형식입니다.");

  private final int numeric;
  private final String errorKey;
  private final HttpStatus httpStatus;
  private final String message;

  @Override
  public String getDomain() {
    return "FILE";
  }
}
