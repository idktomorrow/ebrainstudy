package com.study2.practice.global.exception;

import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 처리. Service에서 던지는 예외를 일관된 HTTP 상태코드 + 응답 형태로 변환한다.
 * (지금까지 Service는 검증 실패/비밀번호 불일치에 IllegalArgumentException을,
 * 존재하지 않는 리소스 조회에 NoSuchElementException을 던지도록 구분해서 만들어뒀음)
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /** 잘못된 입력값(검증 실패, 비밀번호 불일치 등) -> 400 Bad Request */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
  }

  /** 존재하지 않는 리소스 조회 -> 404 Not Found */
  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(NoSuchElementException e) {
    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), e.getMessage()));
  }

  /** 그 외 예상 못한 예외 -> 500. 클라이언트에는 상세 원인을 알려주지 않고 서버 로그에만 남김 */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
    log.error("예상하지 못한 예외 발생", e);
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내부 오류가 발생했습니다."));
  }
}
