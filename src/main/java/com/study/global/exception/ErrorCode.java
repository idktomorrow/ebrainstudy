package com.study.global.exception;

import org.springframework.http.HttpStatus;

/**
 * 모든 도메인 에러 코드 enum이 구현해야 하는 공통 규격
 */
public interface ErrorCode {

  int getNumeric();

  String getErrorKey();

  HttpStatus getHttpStatus();

  String getMessage();

  String getDomain();

  // [도메인]-[에러키] 조합 코드. 모든 도메인에서 규칙이 동일해서 기본 구현으로 제공
  default String getCode() {
    return getDomain() + "-" + getErrorKey();
  }
}
