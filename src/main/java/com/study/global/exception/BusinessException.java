package com.study.global.exception;

import lombok.Getter;

/**
 * 비즈니스 로직상의 예외는 이 예외를 통해서만 던진다.
 * RuntimeException을 상속해서, throws 선언 없이 어디서든 던질 수 있다.
 */
@Getter
public class BusinessException extends RuntimeException {

  private final ErrorCode errorCode;
  private final String hint;

  // 기본 사용: 정의된 메시지 그대로 나감
  public BusinessException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
    this.hint = null;
  }

  // 메시지 커스텀
  public BusinessException(ErrorCode errorCode, String customMessage) {
    super(customMessage);
    this.errorCode = errorCode;
    this.hint = null;
  }

  // 메시지 + 힌트
  public BusinessException(ErrorCode errorCode, String customMessage, String hint) {
    super(customMessage);
    this.errorCode = errorCode;
    this.hint = hint;
  }
}
