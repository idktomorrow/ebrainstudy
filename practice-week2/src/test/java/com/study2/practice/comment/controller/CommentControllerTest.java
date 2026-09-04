package com.study2.practice.comment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study2.practice.comment.dto.request.CommentCreateRequest;
import com.study2.practice.comment.dto.response.CommentResponse;
import com.study2.practice.comment.service.CommentService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * CommentController 통합 테스트. @WebMvcTest로 실제 Spring MVC 파이프라인
 * (경로변수 바인딩, JSON 직렬화, GlobalExceptionHandler)까지 검증한다.
 */
@WebMvcTest(CommentController.class)
class CommentControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private CommentService commentService;

  @Test
  @DisplayName("게시글의 댓글 목록을 JSON 배열로 응답한다")
  void getComments_returnsList() throws Exception {
    when(commentService.getComments(1)).thenReturn(List.of(
        new CommentResponse(1, "이순신", "첫 댓글", LocalDateTime.now())));

    mockMvc.perform(get("/api/boards/1/comments"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].writer").value("이순신"));
  }

  @Test
  @DisplayName("존재하지 않는 게시글의 댓글을 조회하면 404로 응답된다")
  void getComments_returns404_whenBoardNotFound() throws Exception {
    when(commentService.getComments(999))
        .thenThrow(new NoSuchElementException("게시글을 찾을 수 없습니다."));

    mockMvc.perform(get("/api/boards/999/comments"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("게시글을 찾을 수 없습니다."));
  }

  @Test
  @DisplayName("정상 요청이면 200과 함께 등록된 댓글 id를 반환한다")
  void createComment_returns200_onSuccess() throws Exception {
    when(commentService.createComment(eq(1), any())).thenReturn(5);
    CommentCreateRequest request = new CommentCreateRequest("이순신", "좋은 글이네요");

    mockMvc.perform(post("/api/boards/1/comments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(content().string("5"));
  }

  @Test
  @DisplayName("Service가 검증 실패로 예외를 던지면 400으로 응답된다")
  void createComment_returns400_whenValidationFails() throws Exception {
    when(commentService.createComment(eq(1), any()))
        .thenThrow(new IllegalArgumentException("작성자를 입력해주세요."));
    CommentCreateRequest request = new CommentCreateRequest("", "좋은 글이네요");

    mockMvc.perform(post("/api/boards/1/comments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("작성자를 입력해주세요."));
  }
}
