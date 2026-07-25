package com.study.global.exception;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 에러 발생 시 클라이언트에게 내려가는 통일된 응답 포맷
 */
public record ErrorResponse(
    OffsetDateTime timestamp,
    String traceId,       // 로그 추적용 ID. 지금은 별도 로그 연동 없이 응답에만 채워서 내려줌
    String path,
    String code,           // [도메인]-[에러키]
    String errorKey,
    int numeric,
    String title,          // HTTP 상태 코드 타이틀 (예: "Not Found")
    String message,
    List<FieldErrorDetail> details, // @Valid 실패 시에만 채워짐, 그 외엔 null
    String hint             // 선택. 없으면 null
) {

  // 검증 실패 시 필드 하나에 대한 상세 정보
  public record FieldErrorDetail(String field, String issue, Object rejected) {

  }

  // 일반 BusinessException용
  public static ErrorResponse of(ErrorCode errorCode, String path, String message, String hint) {
    return new ErrorResponse(
        OffsetDateTime.now(),
        UUID.randomUUID().toString(),
        path,
        errorCode.getCode(),
        errorCode.getErrorKey(),
        errorCode.getNumeric(),
        errorCode.getHttpStatus().getReasonPhrase(),
        message,
        null,
        hint
    );
  }

  // @Valid 검증 실패용 (details 포함)
  public static ErrorResponse ofValidation(ErrorCode errorCode, String path, String message,
      List<FieldErrorDetail> details) {
    return new ErrorResponse(
        OffsetDateTime.now(),
        UUID.randomUUID().toString(),
        path,
        errorCode.getCode(),
        errorCode.getErrorKey(),
        errorCode.getNumeric(),
        errorCode.getHttpStatus().getReasonPhrase(),
        message,
        details,
        null
    );
  }
}
