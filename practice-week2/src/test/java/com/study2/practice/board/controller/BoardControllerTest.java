package com.study2.practice.board.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study2.practice.board.dto.request.BoardCreateRequest;
import com.study2.practice.board.service.BoardService;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * BoardController 통합 테스트. @WebMvcTest로 실제 Spring MVC 요청 처리 파이프라인
 * (DispatcherServlet, JSON 직렬화, GlobalExceptionHandler)까지 통째로 띄우되, DB는
 * 안 건드리고 BoardService만 Mockito로 대체한다. Service 단위 테스트와 달리, 여기서만
 * 검증되는 건 "예외가 실제로 몇 번 HTTP 상태코드로 응답되는가" — 이건 예전에
 * GlobalExceptionHandler가 Spring 자체 400까지 500으로 덮어쓰던 버그의 회귀 테스트이기도 하다.
 */
@WebMvcTest(BoardController.class)
class BoardControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private BoardService boardService;

  @Test
  @DisplayName("Service가 IllegalArgumentException을 던지면 GlobalExceptionHandler를 거쳐 400으로 응답된다")
  void createBoard_returns400_whenServiceThrowsIllegalArgumentException() throws Exception {
    when(boardService.createBoard(any()))
        .thenThrow(new IllegalArgumentException("작성자는 3자 이상 5자 미만이어야 합니다."));
    BoardCreateRequest request =
        new BoardCreateRequest(1, "김", "제목입니다", "내용은충분히깁니다", "abc123!@#");

    mockMvc.perform(post("/api/boards")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.message").value("작성자는 3자 이상 5자 미만이어야 합니다."));
  }

  @Test
  @DisplayName("Service가 NoSuchElementException을 던지면 404로 응답된다")
  void getBoardDetail_returns404_whenNotFound() throws Exception {
    when(boardService.getBoardDetail(999))
        .thenThrow(new NoSuchElementException("게시글을 찾을 수 없습니다."));

    mockMvc.perform(get("/api/boards/999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.message").value("게시글을 찾을 수 없습니다."));
  }

  @Test
  @DisplayName("page 파라미터에 숫자가 아닌 값을 보내면 400으로 응답된다 (Service까지 안 가고 걸러짐)")
  void getBoardList_returns400_whenPageIsNotNumber() throws Exception {
    // 예전 버그: GlobalExceptionHandler의 catch-all이 이 Spring 프레임워크 예외까지
    // 500으로 잡아채고 있었음. 지금은 400으로 나가야 정상이다.
    mockMvc.perform(get("/api/boards").param("page", "abc").param("size", "10"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("정상 요청이면 200과 함께 등록된 게시글 id를 반환한다")
  void createBoard_returns200_onSuccess() throws Exception {
    when(boardService.createBoard(any())).thenReturn(42);
    BoardCreateRequest request =
        new BoardCreateRequest(1, "김철수", "제목입니다", "내용은충분히깁니다", "abc123!@#");

    mockMvc.perform(post("/api/boards")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(content().string("42"));
  }
}
