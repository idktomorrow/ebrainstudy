package com.study2.practice.global.exception;

import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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

  /**
   * Spring MVC/Servlet 레벨에서 요청 자체가 잘못된 경우 -> 400.
   * (타입 변환 실패, JSON 파싱 실패, 필수 파라미터/멀티파트 파트 누락 등)
   * 아래에 있는 catch-all Exception 핸들러가 이런 프레임워크 예외까지 전부
   * 500으로 잡아채는 문제가 있어서, 더 구체적인 이 핸들러를 따로 둠.
   */
  @ExceptionHandler({
      MethodArgumentTypeMismatchException.class,
      HttpMessageNotReadableException.class,
      MissingServletRequestParameterException.class,
      MissingServletRequestPartException.class,
      MultipartException.class
  })
  public ResponseEntity<ErrorResponse> handleBadRequestFramework(Exception e) {
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "잘못된 요청입니다."));
  }

  /**
   * 존재하지 않는 URL로 요청 -> 404.
   * (Spring 6부터는 매핑되는 핸들러/정적 리소스가 둘 다 없으면 NoResourceFoundException을
   * 던지는데, 이것도 catch-all Exception 핸들러가 잡아채서 500으로 바꿔버리는 문제가
   * 있었음 -> 원래 Spring이 자동으로 주는 404가 나가도록 구체적인 핸들러를 따로 둠)
   */
  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException e) {
    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "존재하지 않는 API입니다."));
  }

  /**
   * 존재하는 URL이지만 지원하지 않는 HTTP 메서드로 요청(예: GET만 있는 경로에 PATCH) -> 405.
   * 마찬가지로 catch-all Exception 핸들러가 가로채면 500으로 바뀌던 문제가 있어서 분리.
   */
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
    return ResponseEntity
        .status(HttpStatus.METHOD_NOT_ALLOWED)
        .body(new ErrorResponse(HttpStatus.METHOD_NOT_ALLOWED.value(), "지원하지 않는 HTTP 메서드입니다."));
  }

  /**
   * 지원하지 않는 Content-Type으로 요청(예: JSON을 받는 API에 application/xml로 요청) -> 415.
   * 이것도 catch-all Exception 핸들러가 가로채면 500으로 바뀌던 문제가 있어서 분리.
   * (Accept 헤더로 인한 HttpMediaTypeNotAcceptableException/406은 이 catch-all에 걸리기
   * 전에 Spring이 먼저 처리해서 이미 정상적으로 406이 나가는 것까지 확인함)
   */
  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException e) {
    return ResponseEntity
        .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
        .body(new ErrorResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(), "지원하지 않는 Content-Type입니다."));
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
