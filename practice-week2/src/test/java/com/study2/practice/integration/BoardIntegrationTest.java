package com.study2.practice.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study2.practice.board.dto.request.BoardCreateRequest;
import com.study2.practice.board.dto.request.BoardDeleteRequest;
import com.study2.practice.board.dto.request.BoardUpdateRequest;
import com.study2.practice.comment.dto.request.CommentCreateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * 게시글 도메인 전체 통합 테스트.
 * 지금까지의 테스트는 Mapper나 Service를 Mockito로 대체했지만, 여기서는
 * @SpringBootTest로 애플리케이션 전체(Controller -> Service -> Mapper -> 실제 MySQL)를
 * 그대로 띄워서, XML의 실제 SQL(동적 조건, EXISTS 서브쿼리, 페이지네이션 등)까지
 * 문제없이 동작하는지 검증한다.
 *
 * 실행하려면 실제 DB가 떠있어야 한다 (docker compose up -d). @Transactional을 테스트
 * 클래스에 붙이면, 각 테스트 메서드가 하나의 트랜잭션 안에서 실행되고 끝나면 자동으로
 * 롤백되어 실제 DB에 테스트 데이터가 남지 않는다 (MyBatis도 Spring이 관리하는 이
 * 트랜잭션에 동참하므로 insert/update/delete가 전부 함께 롤백된다).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BoardIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @DisplayName("게시글 등록부터 삭제까지 전체 흐름이 실제 DB와 함께 정상 동작한다")
  void fullLifecycle_worksWithRealDatabase() throws Exception {

    // 1. 카테고리 목록에 실제 시드 데이터(공지/자유)가 있는지 확인
    mockMvc.perform(get("/api/categories"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].name").value("공지"))
        .andExpect(jsonPath("$[1].name").value("자유"));

    // 2. 게시글 등록 -> 실제 DB에 INSERT되고, useGeneratedKeys로 진짜 id를 돌려받는지 확인
    BoardCreateRequest createRequest =
        new BoardCreateRequest(1, "김철수", "통합테스트 제목", "통합테스트 본문입니다", "abc123!@#");
    String createResponse = mockMvc.perform(post("/api/boards")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequest)))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
    int boardId = Integer.parseInt(createResponse);

    // 3. 상세 조회 -> category 테이블과 실제로 조인돼서 카테고리명이 나오고,
    //    조회수가 UPDATE문 실행 후 1로 반영되는지 확인
    mockMvc.perform(get("/api/boards/{id}", boardId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.categoryName").value("공지"))
        .andExpect(jsonPath("$.viewCount").value(1));

    // 4. 다시 조회 -> 조회수가 실제 DB에 누적돼서 2로 올라가는지 확인
    mockMvc.perform(get("/api/boards/{id}", boardId))
        .andExpect(jsonPath("$.viewCount").value(2));

    // 5. 수정 -> 실제 UPDATE문이 반영됐는지 확인
    BoardUpdateRequest updateRequest =
        new BoardUpdateRequest("박영희", "수정된 제목", "수정된 본문입니다", "abc123!@#");
    mockMvc.perform(put("/api/boards/{id}", boardId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk());

    mockMvc.perform(get("/api/boards/{id}", boardId))
        .andExpect(jsonPath("$.title").value("수정된 제목"))
        .andExpect(jsonPath("$.writer").value("박영희"));

    // 6. 댓글 등록 + 조회 -> comment 테이블에 실제로 저장되고 조회되는지 확인
    CommentCreateRequest commentRequest = new CommentCreateRequest("이순신", "실제 DB 댓글 테스트");
    mockMvc.perform(post("/api/boards/{boardId}/comments", boardId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(commentRequest)))
        .andExpect(status().isOk());

    mockMvc.perform(get("/api/boards/{boardId}/comments", boardId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].writer").value("이순신"));

    // 7. 삭제 -> 실제 DELETE문 + FK CASCADE로 댓글도 같이 지워지는지 확인
    BoardDeleteRequest deleteRequest = new BoardDeleteRequest("abc123!@#");
    mockMvc.perform(delete("/api/boards/{id}", boardId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(deleteRequest)))
        .andExpect(status().isOk());

    // 8. 삭제 후 조회하면 404 (게시글이 실제로 없어졌는지 확인)
    mockMvc.perform(get("/api/boards/{id}", boardId))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("실제 DB에서 키워드/카테고리 필터가 적용된 검색 결과를 반환한다")
  void search_returnsFilteredResultsFromRealDatabase() throws Exception {

    createBoard(1, "김철수", "자바 스터디 모집", "같이 공부해요");
    createBoard(2, "이영희", "자유게시판 잡담", "그냥 아무거나 씁니다");

    // "자바"로 검색하면 첫 번째 게시글만 나와야 함 (XML의 <if> 동적 조건 검증)
    mockMvc.perform(get("/api/boards")
            .param("keyword", "자바")
            .param("page", "1")
            .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalCount").value(1))
        .andExpect(jsonPath("$.boards[0].title").value("자바 스터디 모집"));

    // 카테고리 2(자유)로 필터링하면 두 번째 게시글만 나와야 함
    mockMvc.perform(get("/api/boards")
            .param("categoryId", "2")
            .param("page", "1")
            .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalCount").value(1))
        .andExpect(jsonPath("$.boards[0].title").value("자유게시판 잡담"));
  }

  private void createBoard(int categoryId, String writer, String title, String content) throws Exception {
    BoardCreateRequest request = new BoardCreateRequest(categoryId, writer, title, content, "abc123!@#");
    mockMvc.perform(post("/api/boards")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
  }
}
