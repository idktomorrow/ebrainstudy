package com.study2.practice.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study2.practice.board.dto.request.BoardCreateRequest;
import com.study2.practice.board.dto.request.BoardDeleteRequest;
import com.study2.practice.file.dto.request.AttachmentDeleteRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * 첨부파일 도메인 완전 통합 테스트. Controller -> Service -> Mapper -> 실제 MySQL은 물론,
 * 실제 디스크 파일 I/O까지 전부 진짜로 동작하는지 검증한다.
 *
 * app.upload-dir을 @DynamicPropertySource로 실제 uploads 폴더가 아니라 JUnit이 관리하는
 * 임시 폴더로 바꿔치기한다 — 그래야 테스트가 프로젝트의 진짜 uploads 폴더를 더럽히지 않고,
 * 테스트 끝나면 JUnit이 폴더 자체를 자동으로 지워준다. (DB는 @Transactional로 롤백되지만
 * 디스크 파일 쓰기는 트랜잭션의 영향을 안 받으므로 별도 처리가 필요하다)
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AttachmentIntegrationTest {

  @TempDir
  static Path uploadDir;

  @DynamicPropertySource
  static void overrideUploadDir(DynamicPropertyRegistry registry) {
    registry.add("app.upload-dir", () -> uploadDir.toString());
  }

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @DisplayName("업로드-다운로드-삭제 전체 흐름이 실제 디스크/DB와 함께 정상 동작한다")
  void uploadDownloadDelete_fullLifecycle() throws Exception {

    long filesBefore = countFiles();
    int boardId = createBoard();

    // 1. 업로드 -> 실제 디스크에 파일이 하나 늘었는지 확인
    byte[] content = "실제 업로드 통합테스트".getBytes(StandardCharsets.UTF_8);
    MockMultipartFile file = new MockMultipartFile("files", "real.txt", "text/plain", content);

    String uploadResponse = mockMvc.perform(multipart("/api/boards/{boardId}/files", boardId)
            .file(file)
            .param("password", "abc123!@#"))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
    int attachmentId = objectMapper.readTree(uploadResponse).get(0).asInt();

    assertThat(countFiles()).isEqualTo(filesBefore + 1);

    // 2. 게시글 상세 조회에 첨부파일 목록이 실제로 포함되는지 확인
    mockMvc.perform(get("/api/boards/{id}", boardId))
        .andExpect(jsonPath("$.attachments[0].originName").value("real.txt"));

    // 3. 다운로드해서 실제 디스크 파일 내용이 원본과 바이트 단위로 같은지 확인
    byte[] downloaded = mockMvc.perform(get("/api/files/{id}", attachmentId))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsByteArray();
    assertThat(downloaded).isEqualTo(content);

    // 4. 삭제 -> 실제 디스크 파일도 사라지는지 확인
    AttachmentDeleteRequest deleteRequest = new AttachmentDeleteRequest("abc123!@#");
    mockMvc.perform(delete("/api/files/{id}", attachmentId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(deleteRequest)))
        .andExpect(status().isOk());

    assertThat(countFiles()).isEqualTo(filesBefore);
  }

  @Test
  @DisplayName("첨부파일을 직접 안 지우고 게시글만 삭제해도 디스크 파일이 함께 정리된다")
  void deletingBoard_alsoDeletesAttachmentFileFromDisk() throws Exception {
    // 예전에 실제로 겪었던 "게시글만 삭제하면 디스크에 고아 파일이 남는" 버그의
    // 회귀 테스트를, Mock이 아니라 진짜 HTTP+DB+디스크로 검증한다.

    long filesBefore = countFiles();
    int boardId = createBoard();

    MockMultipartFile file = new MockMultipartFile("files", "cascade.txt", "text/plain",
        "삭제 연쇄 테스트".getBytes(StandardCharsets.UTF_8));
    mockMvc.perform(multipart("/api/boards/{boardId}/files", boardId)
            .file(file)
            .param("password", "abc123!@#"))
        .andExpect(status().isOk());

    assertThat(countFiles()).isEqualTo(filesBefore + 1);

    // 첨부파일 API는 건드리지 않고 게시글만 삭제
    BoardDeleteRequest deleteRequest = new BoardDeleteRequest("abc123!@#");
    mockMvc.perform(delete("/api/boards/{id}", boardId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(deleteRequest)))
        .andExpect(status().isOk());

    assertThat(countFiles()).isEqualTo(filesBefore);
  }

  private int createBoard() throws Exception {
    BoardCreateRequest request = new BoardCreateRequest(1, "김철수", "첨부파일 통합테스트", "본문입니다", "abc123!@#");
    String response = mockMvc.perform(post("/api/boards")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
    return Integer.parseInt(response);
  }

  private long countFiles() throws IOException {
    try (var files = Files.list(uploadDir)) {
      return files.count();
    }
  }
}
