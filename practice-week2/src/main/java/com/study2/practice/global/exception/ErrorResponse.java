package com.study2.practice.global.exception;

/**
 * API 에러 응답 공통 형태.
 *
 * @param status  HTTP 상태 코드
 * @param message 에러 메시지
 */
public record ErrorResponse(
    int status,
    String message
) {
}
