package com.study.command;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface BoardCommand {

  //모든 명령 객체가 반드시 구현해야하는 공통 메서드
  void execute(HttpServletRequest request, HttpServletResponse response) throws Exception;

}
