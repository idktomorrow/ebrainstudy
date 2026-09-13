package com.study2.practice.file.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study2.practice.file.dto.request.AttachmentDeleteRequest;
import com.study2.practice.file.entity.Attachment;
import com.study2.practice.file.service.AttachmentService;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * AttachmentController 통합 테스트. 멀티파트 업로드가 껴있어서 다른 컨트롤러 테스트와
 * 달리 MockMvc의 multipart() 요청 빌더를 쓴다 (기본으로 POST 요청을 만들어준다).
 */
@WebMvcTest(AttachmentController.class)
class AttachmentControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private AttachmentService attachmentService;

  @Test
  @DisplayName("정상 업로드 시 200과 함께 생성된 첨부파일 id 목록을 반환한다")
  void uploadFiles_returns200_onSuccess() throws Exception {
    when(attachmentService.uploadFiles(anyInt(), any(), any())).thenReturn(List.of(1, 2));
    MockMultipartFile file = new MockMultipartFile("files", "test.txt", "text/plain",
        "내용".getBytes(StandardCharsets.UTF_8));

    mockMvc.perform(multipart("/api/boards/{boardId}/files", 1)
            .file(file)
            .param("password", "abc123!@#"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0]").value(1));
  }

  @Test
  @DisplayName("password 파라미터 없이 업로드하면 400으로 응답된다")
  void uploadFiles_returns400_whenPasswordMissing() throws Exception {
    MockMultipartFile file = new MockMultipartFile("files", "test.txt", "text/plain",
        "내용".getBytes(StandardCharsets.UTF_8));

    mockMvc.perform(multipart("/api/boards/{boardId}/files", 1).file(file))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Service가 비밀번호 불일치로 예외를 던지면 400으로 응답된다")
  void uploadFiles_returns400_whenServiceThrowsIllegalArgument() throws Exception {
    when(attachmentService.uploadFiles(anyInt(), any(), any()))
        .thenThrow(new IllegalArgumentException("비밀번호가 일치하지 않습니다."));
    MockMultipartFile file = new MockMultipartFile("files", "test.txt", "text/plain",
        "내용".getBytes(StandardCharsets.UTF_8));

    mockMvc.perform(multipart("/api/boards/{boardId}/files", 1)
            .file(file)
            .param("password", "wrongpw!1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("비밀번호가 일치하지 않습니다."));
  }

  @Test
  @DisplayName("정상 다운로드 시 200과 함께 파일 내용, Content-Disposition 헤더가 반환된다")
  void downloadFile_returns200_withFileContent() throws Exception {
    byte[] content = "다운로드 테스트".getBytes(StandardCharsets.UTF_8);
    Attachment attachment = new Attachment(1, 1, "test.txt", "uuid.txt", "/x/uuid.txt",
        (long) content.length, "txt");
    when(attachmentService.getAttachmentForDownload(1)).thenReturn(attachment);
    when(attachmentService.loadFileAsResource(attachment))
        .thenReturn(new ByteArrayResource(content));

    mockMvc.perform(get("/api/files/{id}", 1))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
        .andExpect(header().string("Content-Disposition", containsString("test.txt")))
        .andExpect(content().bytes(content));
  }

  @Test
  @DisplayName("파일명에 공백이 있으면 Content-Disposition에 '+'가 아니라 %20으로 인코딩된다")
  void downloadFile_encodesSpaceAsPercent20_notPlus() throws Exception {
    byte[] content = "test".getBytes(StandardCharsets.UTF_8);
    Attachment attachment = new Attachment(1, 1, "my file.txt", "uuid.txt", "/x/uuid.txt",
        (long) content.length, "txt");
    when(attachmentService.getAttachmentForDownload(1)).thenReturn(attachment);
    when(attachmentService.loadFileAsResource(attachment))
        .thenReturn(new ByteArrayResource(content));

    mockMvc.perform(get("/api/files/{id}", 1))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Disposition", containsString("my%20file.txt")));
  }

  @Test
  @DisplayName("존재하지 않는 첨부파일을 다운로드하려 하면 404로 응답된다")
  void downloadFile_returns404_whenNotFound() throws Exception {
    when(attachmentService.getAttachmentForDownload(999))
        .thenThrow(new NoSuchElementException("첨부파일을 찾을 수 없습니다."));

    mockMvc.perform(get("/api/files/{id}", 999))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("정상 삭제 요청이면 200으로 응답된다")
  void deleteFile_returns200_onSuccess() throws Exception {
    AttachmentDeleteRequest request = new AttachmentDeleteRequest("abc123!@#");

    mockMvc.perform(delete("/api/files/{id}", 1)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("존재하지 않는 첨부파일을 삭제하려 하면 404로 응답된다")
  void deleteFile_returns404_whenNotFound() throws Exception {
    doThrow(new NoSuchElementException("첨부파일을 찾을 수 없습니다."))
        .when(attachmentService).deleteAttachment(anyInt(), any());
    AttachmentDeleteRequest request = new AttachmentDeleteRequest("abc123!@#");

    mockMvc.perform(delete("/api/files/{id}", 999)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound());
  }
}
