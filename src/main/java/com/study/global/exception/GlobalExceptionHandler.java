package com.study.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
}
