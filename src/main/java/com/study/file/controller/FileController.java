package com.study.file.controller;

import com.study.file.dto.response.FileResponse;
import com.study.file.entity.FileEntity;
import com.study.file.service.FileService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 첨부파일 컨트롤러
 */
@RestController
public class FileController {

  private final FileService fileService;

  public FileController(FileService fileService) {
    this.fileService = fileService;
  }

  // 게시글에 첨부파일 업로드 (multipart/form-data 요청)
  @PostMapping("/api/boards/{boardId}/files")
  public List<FileResponse> uploadFiles(
      @PathVariable Long boardId,
      @RequestParam("files") List<MultipartFile> files) throws IOException {
    return fileService.uploadFiles(boardId, files);
  }

  // 게시글 별 첨부파일 목록 조회
  @GetMapping("/api/boards/{boardId}/files")
  public List<FileResponse> getFilesByBoardId(@PathVariable Long boardId) {
    return fileService.getFilesByBoardId(boardId);
  }

  // 파일 다운로드
  @GetMapping("/api/files/{id}/download")
  public ResponseEntity<Resource> downloadFile(@PathVariable Integer id) throws IOException {
    // 장부(DB)에서 파일 정보 조회
    FileEntity file = fileService.getFileById(id);
    // 디스크의 실제 파일을 가리키는 손잡이 받아오기
    Resource resource = fileService.loadAsResource(file);

    // 저장될 파일명 이름표 (한글 깨짐 방지 UTF-8 인코딩)
    ContentDisposition contentDisposition = ContentDisposition.attachment()
        .filename(file.getOriginName(), StandardCharsets.UTF_8)
        .build();

    // 응답 조립: 200 OK + 헤더 2개 + 본문(손잡이)
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(resource);
  }

  // 파일 삭제
  @DeleteMapping("/api/files/{id}")
  public void deleteFile(@PathVariable Integer id) throws IOException {
    fileService.deleteFile(id);
  }
}
