package com.study.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 모든 컨트롤러에서 발생하는 예외를 한곳에서 잡아 통일된 응답으로 변환
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  // 우리가 직접 던진 비즈니스 예외
  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e,
      HttpServletRequest request) {
    ErrorCode errorCode = e.getErrorCode();
    ErrorResponse response = ErrorResponse.of(errorCode, request.getRequestURI(), e.getMessage(),
        e.getHint());
    return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
  }

  // @Valid 검증 실패 시 스프링이 던지는 예외
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException e, HttpServletRequest request) {
    // 필드별로 뭐가 왜 틀렸는지 details 목록으로 변환
    List<ErrorResponse.FieldErrorDetail> details = e.getBindingResult().getFieldErrors().stream()
        .map(fieldError -> new ErrorResponse.FieldErrorDetail(
            fieldError.getField(),
            fieldError.getDefaultMessage(),
            fieldError.getRejectedValue()
        ))
        .toList();

    ErrorResponse response = ErrorResponse.ofValidation(
        GlobalErrorCode.VALIDATION_FAILED,
        request.getRequestURI(),
        GlobalErrorCode.VALIDATION_FAILED.getMessage(),
        details
    );
    return ResponseEntity.status(GlobalErrorCode.VALIDATION_FAILED.getHttpStatus()).body(response);
  }

  // 경로/쿼리 파라미터 타입이 안 맞을 때 (예: id 자리에 숫자가 아닌 값) - 스프링이 컨트롤러 실행 전에 던지는 예외
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatch(
      MethodArgumentTypeMismatchException e, HttpServletRequest request) {
    ErrorResponse response = ErrorResponse.of(
        GlobalErrorCode.VALIDATION_FAILED,
        request.getRequestURI(),
        "요청 파라미터 형식이 올바르지 않습니다.",
        null
    );
    return ResponseEntity.status(GlobalErrorCode.VALIDATION_FAILED.getHttpStatus()).body(response);
  }

  // DB 제약조건 위반 (예: 존재하지 않는 게시글 id로 댓글을 달아 외래키 위반) - 도메인별로 미리 막지 못한 경우의 안전망
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
      DataIntegrityViolationException e, HttpServletRequest request) {
    ErrorResponse response = ErrorResponse.of(
        GlobalErrorCode.VALIDATION_FAILED,
        request.getRequestURI(),
        "요청 데이터가 참조 제약 조건을 위반했습니다 (예: 존재하지 않는 게시글).",
        null
    );
    return ResponseEntity.status(GlobalErrorCode.VALIDATION_FAILED.getHttpStatus()).body(response);
  }
}
