package com.study2.practice.file.service;

import com.study2.practice.board.entity.Board;
import com.study2.practice.board.mapper.BoardMapper;
import com.study2.practice.file.entity.Attachment;
import com.study2.practice.file.mapper.AttachmentMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 첨부파일 업로드/다운로드/삭제 비즈니스 로직.
 * 파일 실체는 로컬 디스크(application.yaml의 app.upload-dir)에 저장하고,
 * 메타데이터(원본명/저장명/경로/크기/확장자)만 DB(files 테이블)에 기록한다.
 */
@Service
@RequiredArgsConstructor
public class AttachmentService {

  // 업로드 허용 확장자 화이트리스트. 실행 파일(exe, sh, bat 등)처럼 위험한 형식은 의도적으로 제외
  private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
      "pdf", "doc", "docx", "hwp", "xls", "xlsx", "ppt", "pptx", "txt",
      "jpg", "jpeg", "png", "gif", "zip"
  );

  private final AttachmentMapper attachmentMapper;
  private final BoardMapper boardMapper;

  @Value("${app.upload-dir}")
  private String uploadDir;

  /**
   * 게시글에 파일들을 첨부. 대상 게시글 존재 확인 + 비밀번호 확인 + 확장자 전부 검증 후,
   * 파일마다 디스크 저장 + 메타데이터 insert.
   * (첨부파일 관리는 게시글 수정 화면의 일부라, 게시글 비밀번호로 검증한다)
   */
  public List<Integer> uploadFiles(Integer boardId, List<MultipartFile> files, String password) {

    Board board = boardMapper.findById(boardId);
    if (board == null) {
      throw new NoSuchElementException("게시글을 찾을 수 없습니다.");
    }
    if (!board.getPassword().equals(password)) {
      throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
    }

    // 저장 시작 전에 전부 검증 -> 하나라도 허용 안 되는 확장자/파일명이면 아무 파일도 저장하지 않고 통째로 거부
    files.forEach(file -> {
      validateExtension(extractExtension(file.getOriginalFilename()));
      validateOriginName(file.getOriginalFilename());
    });

    return files.stream()
        .map(file -> uploadOne(boardId, file))
        .toList();
  }

  private void validateExtension(String extension) {
    if (extension.isEmpty() || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
      throw new IllegalArgumentException("허용되지 않는 파일 형식입니다: ." + extension);
    }
  }

  private void validateOriginName(String originName) {
    // files.origin_name 컬럼이 VARCHAR(500)이라, 이걸 안 막으면 DB에서
    // "Data too long for column" 에러가 나서 500으로 응답돼버림 (comment.writer와 같은 종류의 버그)
    if (originName != null && originName.length() > 500) {
      throw new IllegalArgumentException("파일명이 너무 깁니다 (500자 이하만 가능).");
    }
  }

  private Integer uploadOne(Integer boardId, MultipartFile file) {

    String originName = file.getOriginalFilename();
    String extension = extractExtension(originName);
    String storedName = UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);
    Path targetPath;

    try {
      Path uploadPath = Path.of(uploadDir);
      Files.createDirectories(uploadPath);   // uploads 폴더가 없으면 생성

      targetPath = uploadPath.resolve(storedName);
      file.transferTo(targetPath);           // 실제 파일 내용을 디스크에 저장
    } catch (IOException e) {
      throw new UncheckedIOException("파일 저장에 실패했습니다: " + originName, e);
    }

    // 디스크 저장은 끝났고 이제 DB에 메타데이터를 기록하는데, 이 단계가 실패하면
    // (DB 연결 문제 등, IOException이 아니라서 위 catch로는 안 잡힘) 방금 쓴 파일이
    // DB에 기록 하나 없이 디스크에만 고아로 남는다. insert 실패 시 방금 쓴 파일을 정리한다.
    try {
      Attachment attachment = new Attachment();
      attachment.setBoardId(boardId);
      attachment.setOriginName(originName);
      attachment.setStoredName(storedName);
      attachment.setFilePath(targetPath.toString());
      attachment.setFileSize(file.getSize());
      attachment.setFileFormat(extension);

      attachmentMapper.insert(attachment);

      return attachment.getId();
    } catch (RuntimeException e) {
      deleteQuietly(targetPath);
      throw e;
    }
  }

  private void deleteQuietly(Path path) {
    try {
      Files.deleteIfExists(path);
    } catch (IOException ignored) {
      // 정리 실패는 무시 -> 원래 발생한 예외(DB 저장 실패)를 그대로 던지는 게 더 중요
    }
  }

  private String extractExtension(String originName) {
    if (originName == null || !originName.contains(".")) {
      return "";
    }
    return originName.substring(originName.lastIndexOf('.') + 1);
  }

  /** 게시글에 첨부된 파일 목록 조회 (상세 화면용 메타데이터만). */
  public List<Attachment> getAttachments(Integer boardId) {
    return attachmentMapper.findByBoardId(boardId);
  }

  /** 다운로드용 리소스 조회. 서버 URI를 노출하지 않고 바이너리를 직접 스트리밍하기 위해 사용. */
  public Attachment getAttachmentForDownload(Integer id) {
    Attachment attachment = attachmentMapper.findById(id);
    if (attachment == null) {
      throw new NoSuchElementException("첨부파일을 찾을 수 없습니다.");
    }
    return attachment;
  }

  /** 실제 파일 리소스를 읽어온다 (Controller가 바이너리 응답을 만들 때 사용). */
  public Resource loadFileAsResource(Attachment attachment) {
    try {
      Path path = Path.of(attachment.getFilePath());
      return new UrlResource(path.toUri());
    } catch (IOException e) {
      throw new UncheckedIOException("파일을 읽을 수 없습니다: " + attachment.getOriginName(), e);
    }
  }

  /**
   * 첨부파일 삭제. 디스크 파일 + DB 메타데이터 둘 다 지운다.
   * 첨부파일 자체엔 비밀번호가 없어서, 이 파일이 속한 게시글의 비밀번호로 검증한다.
   */
  public void deleteAttachment(Integer id, String password) {
    Attachment attachment = attachmentMapper.findById(id);
    if (attachment == null) {
      throw new NoSuchElementException("첨부파일을 찾을 수 없습니다.");
    }

    Board board = boardMapper.findById(attachment.getBoardId());
    if (board == null) {
      throw new NoSuchElementException("게시글을 찾을 수 없습니다.");
    }
    if (!board.getPassword().equals(password)) {
      throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
    }

    try {
      Files.deleteIfExists(Path.of(attachment.getFilePath()));
    } catch (IOException e) {
      throw new UncheckedIOException("파일 삭제에 실패했습니다: " + attachment.getOriginName(), e);
    }

    attachmentMapper.delete(id);
  }

  /**
   * 게시글이 삭제될 때 호출. DB의 files 행은 FK ON DELETE CASCADE로 게시글과 함께
   * 자동 삭제되지만, 디스크의 실제 파일은 DB가 알 수 없는 영역이라 여기서 직접 지운다.
   * (게시글만 지우고 이 메서드를 안 부르면 디스크에 고아 파일이 남는다)
   */
  public void deleteFilesByBoardId(Integer boardId) {
    for (Attachment attachment : attachmentMapper.findByBoardId(boardId)) {
      try {
        Files.deleteIfExists(Path.of(attachment.getFilePath()));
      } catch (IOException e) {
        throw new UncheckedIOException("파일 삭제에 실패했습니다: " + attachment.getOriginName(), e);
      }
    }
  }
}
