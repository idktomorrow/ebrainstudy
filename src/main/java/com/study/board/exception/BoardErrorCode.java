package com.study.board.exception;

import com.study.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 게시판 도메인 에러 코드 (번호 대역: 1000~1999)
 */
@Getter
@RequiredArgsConstructor
public enum BoardErrorCode implements ErrorCode {

  BOARD_NOT_FOUND(1001, "NOT_FOUND", HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."),
  BOARD_PASSWORD_MISMATCH(1002, "PASSWORD_MISMATCH", HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다.");

  private final int numeric;
  private final String errorKey;
  private final HttpStatus httpStatus;
  private final String message;

  @Override
  public String getDomain() {
    return "BOARD";
  }
}
