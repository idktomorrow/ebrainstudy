package com.study.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 특정 도메인 소속이 아닌, 애플리케이션 전역 에러 코드 (번호 대역: 1~999)
 */
@Getter
@RequiredArgsConstructor
public enum GlobalErrorCode implements ErrorCode {

  VALIDATION_FAILED(1, "VALIDATION_FAILED", HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다."),
  INTERNAL_SERVER_ERROR(2, "INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

  private final int numeric;
  private final String errorKey;
  private final HttpStatus httpStatus;
  private final String message;

  @Override
  public String getDomain() {
    return "GLOBAL";
  }
}
