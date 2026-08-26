package com.study2.practice.file.controller;

import com.study2.practice.file.dto.request.AttachmentDeleteRequest;
import com.study2.practice.file.entity.Attachment;
import com.study2.practice.file.service.AttachmentService;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 첨부파일 업로드/다운로드/삭제 API.
 * 업로드는 게시글 하위 리소스(/api/boards/{boardId}/files)로, 다운로드/삭제는
 * 파일 자체가 식별자로 충분해서 최상위 리소스(/api/files/{id})로 분리했다.
 */
@RestController
@RequiredArgsConstructor
public class AttachmentController {

  private final AttachmentService attachmentService;

  /** 게시글에 파일 업로드 (여러 개 가능). 게시글 비밀번호 확인 후 처리. 생성된 첨부파일 id 목록을 반환. */
  @PostMapping("/api/boards/{boardId}/files")
  public List<Integer> uploadFiles(
      @PathVariable Integer boardId,
      @RequestParam("files") List<MultipartFile> files,
      @RequestParam("password") String password
  ) {
    return attachmentService.uploadFiles(boardId, files, password);
  }

  /** 첨부파일 다운로드. 서버 URI 링크가 아니라 바이너리를 직접 응답 본문에 실어 보낸다. */
  @GetMapping("/api/files/{id}")
  public ResponseEntity<Resource> downloadFile(@PathVariable Integer id) {

    Attachment attachment = attachmentService.getAttachmentForDownload(id);
    Resource resource = attachmentService.loadFileAsResource(attachment);

    // 파일명에 한글이 들어갈 수 있어서 Content-Disposition 헤더는 URL 인코딩해서 넣음
    String encodedName = URLEncoder.encode(attachment.getOriginName(), StandardCharsets.UTF_8);

    // 파일 종류를 가리지 않고 항상 '다운로드'로 처리되도록 octet-stream 고정
    // (지정 안 하면 Spring이 요청의 Accept 헤더를 보고 엉뚱한 Content-Type을 추론할 수 있음)
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName)
        .body(resource);
  }

  /** 첨부파일 삭제. 이 파일이 속한 게시글의 비밀번호 확인 후 처리. */
  @DeleteMapping("/api/files/{id}")
  public void deleteFile(@PathVariable Integer id, @RequestBody AttachmentDeleteRequest request) {
    attachmentService.deleteAttachment(id, request.password());
  }
}
