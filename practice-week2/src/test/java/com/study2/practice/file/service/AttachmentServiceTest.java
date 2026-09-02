package com.study2.practice.file.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.study2.practice.board.entity.Board;
import com.study2.practice.board.mapper.BoardMapper;
import com.study2.practice.file.entity.Attachment;
import com.study2.practice.file.mapper.AttachmentMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * AttachmentService 단위 테스트.
 * 실제 디스크에 파일을 쓰는 로직이라, @TempDir로 테스트마다 새 임시 폴더를 만들어 격리한다
 * (테스트가 끝나면 JUnit이 자동으로 지워준다). uploadDir은 @Value로 주입되는 필드라
 * 생성자로는 못 넣으니, ReflectionTestUtils로 직접 값을 넣어준다.
 */
@ExtendWith(MockitoExtension.class)
class AttachmentServiceTest {

  @Mock
  private AttachmentMapper attachmentMapper;
  @Mock
  private BoardMapper boardMapper;

  @InjectMocks
  private AttachmentService attachmentService;

  @TempDir
  Path tempDir;

  private Board board;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(attachmentService, "uploadDir", tempDir.toString());

    board = new Board();
    board.setId(1);
    board.setPassword("abc123!@#");
  }

  @Nested
  @DisplayName("파일 업로드")
  class UploadFiles {

    @Test
    @DisplayName("존재하지 않는 게시글이면 예외가 발생한다")
    void failsWhenBoardNotFound() {
      when(boardMapper.findById(999)).thenReturn(null);
      MultipartFile file = new MockMultipartFile("files", "a.txt", "text/plain",
          "내용".getBytes(StandardCharsets.UTF_8));

      assertThatThrownBy(() -> attachmentService.uploadFiles(999, List.of(file), "abc123!@#"))
          .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 예외가 발생하고 디스크에 아무것도 안 남는다")
    void failsWhenPasswordMismatch() {
      when(boardMapper.findById(1)).thenReturn(board);
      MultipartFile file = new MockMultipartFile("files", "a.txt", "text/plain",
          "내용".getBytes(StandardCharsets.UTF_8));

      assertThatThrownBy(() -> attachmentService.uploadFiles(1, List.of(file), "wrongpw!1"))
          .isInstanceOf(IllegalArgumentException.class);

      assertThat(tempDir).isEmptyDirectory();
    }

    @Test
    @DisplayName("허용되지 않는 확장자면 예외가 발생하고 디스크에 저장되지 않는다")
    void failsWhenExtensionNotAllowed() {
      when(boardMapper.findById(1)).thenReturn(board);
      MultipartFile file = new MockMultipartFile("files", "malware.exe",
          "application/octet-stream", "내용".getBytes(StandardCharsets.UTF_8));

      assertThatThrownBy(() -> attachmentService.uploadFiles(1, List.of(file), "abc123!@#"))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("허용되지 않는 파일 형식");

      assertThat(tempDir).isEmptyDirectory();
    }

    @Test
    @DisplayName("파일명이 500자를 넘으면 예외가 발생한다")
    void failsWhenOriginNameTooLong() {
      when(boardMapper.findById(1)).thenReturn(board);
      String longName = "a".repeat(501) + ".txt";
      MultipartFile file = new MockMultipartFile("files", longName, "text/plain",
          "내용".getBytes(StandardCharsets.UTF_8));

      assertThatThrownBy(() -> attachmentService.uploadFiles(1, List.of(file), "abc123!@#"))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("파일명이 너무 깁니다");
    }

    @Test
    @DisplayName("여러 파일 중 하나라도 허용되지 않으면 전부 저장하지 않는다 (all-or-nothing)")
    void rejectsAllWhenAnyFileInvalid() {
      when(boardMapper.findById(1)).thenReturn(board);
      MultipartFile goodFile = new MockMultipartFile("files", "good.txt", "text/plain",
          "내용".getBytes(StandardCharsets.UTF_8));
      MultipartFile badFile = new MockMultipartFile("files", "bad.exe",
          "application/octet-stream", "내용".getBytes(StandardCharsets.UTF_8));

      assertThatThrownBy(() ->
          attachmentService.uploadFiles(1, List.of(goodFile, badFile), "abc123!@#"))
          .isInstanceOf(IllegalArgumentException.class);

      // good.txt는 그 자체로는 허용되는 파일이지만, bad.exe 때문에 배치 전체가 거부되어
      // 디스크에 아무것도 남으면 안 된다 (예전에 겪었던 "부분 저장" 버그의 회귀 테스트)
      assertThat(tempDir).isEmptyDirectory();
      verify(attachmentMapper, never()).insert(any(Attachment.class));
    }

    @Test
    @DisplayName("정상 업로드 시 디스크에 실제 파일이 저장되고, DB에도 메타데이터가 기록된다")
    void success() throws IOException {
      when(boardMapper.findById(1)).thenReturn(board);
      doAnswer(invocation -> {
        Attachment attachment = invocation.getArgument(0);
        attachment.setId(10);
        return null;
      }).when(attachmentMapper).insert(any(Attachment.class));

      byte[] content = "파일 내용입니다".getBytes(StandardCharsets.UTF_8);
      MultipartFile file = new MockMultipartFile("files", "hello.txt", "text/plain", content);

      List<Integer> ids = attachmentService.uploadFiles(1, List.of(file), "abc123!@#");

      assertThat(ids).containsExactly(10);
      // 디스크에 정확히 파일 1개가 저장됐고, 내용도 원본과 같은지 확인
      try (var files = Files.list(tempDir)) {
        List<Path> saved = files.toList();
        assertThat(saved).hasSize(1);
        assertThat(Files.readAllBytes(saved.get(0))).isEqualTo(content);
      }
    }

    @Test
    @DisplayName("DB 저장이 실패하면, 이미 디스크에 쓴 파일을 정리한다 (고아 파일 방지)")
    void cleansUpFileWhenDbInsertFails() {
      when(boardMapper.findById(1)).thenReturn(board);
      doThrow(new RuntimeException("DB 연결 실패")).when(attachmentMapper).insert(any(Attachment.class));
      MultipartFile file = new MockMultipartFile("files", "hello.txt", "text/plain",
          "내용".getBytes(StandardCharsets.UTF_8));

      assertThatThrownBy(() -> attachmentService.uploadFiles(1, List.of(file), "abc123!@#"))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("DB 연결 실패");

      // insert가 실패했으니, 방금 디스크에 쓴 파일은 남아있으면 안 된다
      assertThat(tempDir).isEmptyDirectory();
    }
  }

  @Nested
  @DisplayName("첨부파일 목록/다운로드 조회")
  class Read {

    @Test
    @DisplayName("게시글의 첨부파일 목록은 Mapper 결과를 그대로 반환한다")
    void getAttachments_delegatesToMapper() {
      List<Attachment> attachments = List.of(
          new Attachment(1, 1, "a.txt", "uuid-a.txt", "/x/uuid-a.txt", 10L, "txt"));
      when(attachmentMapper.findByBoardId(1)).thenReturn(attachments);

      List<Attachment> result = attachmentService.getAttachments(1);

      assertThat(result).isEqualTo(attachments);
    }

    @Test
    @DisplayName("존재하지 않는 첨부파일이면 예외가 발생한다")
    void getAttachmentForDownload_failsWhenNotFound() {
      when(attachmentMapper.findById(999)).thenReturn(null);

      assertThatThrownBy(() -> attachmentService.getAttachmentForDownload(999))
          .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("실제 디스크 파일을 읽어서 원본과 동일한 내용의 Resource로 반환한다")
    void loadFileAsResource_readsActualFileContent() throws IOException {
      Path savedFile = tempDir.resolve("test.txt");
      byte[] content = "다운로드 테스트 내용".getBytes(StandardCharsets.UTF_8);
      Files.write(savedFile, content);

      Attachment attachment = new Attachment(1, 1, "test.txt", "stored.txt",
          savedFile.toString(), (long) content.length, "txt");

      Resource resource = attachmentService.loadFileAsResource(attachment);

      assertThat(resource.exists()).isTrue();
      try (var in = resource.getInputStream()) {
        assertThat(in.readAllBytes()).isEqualTo(content);
      }
    }
  }

  @Nested
  @DisplayName("첨부파일 삭제")
  class DeleteAttachment {

    @Test
    @DisplayName("존재하지 않는 첨부파일이면 예외가 발생한다")
    void failsWhenNotFound() {
      when(attachmentMapper.findById(999)).thenReturn(null);

      assertThatThrownBy(() -> attachmentService.deleteAttachment(999, "abc123!@#"))
          .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 삭제하지 않는다")
    void failsWhenPasswordMismatch() throws IOException {
      Path savedFile = tempDir.resolve("test.txt");
      Files.writeString(savedFile, "내용");
      Attachment attachment = new Attachment(1, 1, "test.txt", "stored.txt",
          savedFile.toString(), 10L, "txt");
      when(attachmentMapper.findById(1)).thenReturn(attachment);
      when(boardMapper.findById(1)).thenReturn(board);

      assertThatThrownBy(() -> attachmentService.deleteAttachment(1, "wrongpw!1"))
          .isInstanceOf(IllegalArgumentException.class);

      // 삭제가 거부됐으니 파일이 그대로 남아있어야 함
      assertThat(savedFile).exists();
      verify(attachmentMapper, never()).delete(any());
    }

    @Test
    @DisplayName("비밀번호가 일치하면 디스크 파일과 DB 메타데이터를 모두 지운다")
    void success() throws IOException {
      Path savedFile = tempDir.resolve("test.txt");
      Files.writeString(savedFile, "내용");
      Attachment attachment = new Attachment(1, 1, "test.txt", "stored.txt",
          savedFile.toString(), 10L, "txt");
      when(attachmentMapper.findById(1)).thenReturn(attachment);
      when(boardMapper.findById(1)).thenReturn(board);

      attachmentService.deleteAttachment(1, "abc123!@#");

      assertThat(savedFile).doesNotExist();
      verify(attachmentMapper).delete(1);
    }
  }

  @Nested
  @DisplayName("게시글 삭제 시 첨부파일 디스크 정리")
  class DeleteFilesByBoardId {

    @Test
    @DisplayName("게시글에 딸린 첨부파일들을 디스크에서 전부 지운다")
    void deletesAllFilesForBoard() throws IOException {
      Path file1 = tempDir.resolve("file1.txt");
      Path file2 = tempDir.resolve("file2.txt");
      Files.writeString(file1, "내용1");
      Files.writeString(file2, "내용2");

      List<Attachment> attachments = List.of(
          new Attachment(1, 1, "file1.txt", "stored1.txt", file1.toString(), 10L, "txt"),
          new Attachment(2, 1, "file2.txt", "stored2.txt", file2.toString(), 10L, "txt")
      );
      when(attachmentMapper.findByBoardId(1)).thenReturn(attachments);

      attachmentService.deleteFilesByBoardId(1);

      assertThat(file1).doesNotExist();
      assertThat(file2).doesNotExist();
    }
  }
}
