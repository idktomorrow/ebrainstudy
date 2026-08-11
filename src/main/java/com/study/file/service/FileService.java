package com.study.file.service;

import com.study.file.dto.response.FileResponse;
import com.study.file.entity.FileEntity;
import com.study.file.exception.FileErrorCode;
import com.study.file.mapper.FileMapper;
import com.study.file.repository.FileRepository;
import com.study.global.exception.BusinessException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 비즈니스 로직
 */
@Service
public class FileService {

  // 업로드 허용 확장자 (실행파일 등 위험한 확장자 차단)
  private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
      "jpg", "jpeg", "png", "gif", "pdf", "txt", "hwp", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "zip"
  );

  // 첨부파일 저장 경로 설정
  @Value("${file.upload-dir}")
  private String uploadDir;

  private final FileRepository fileRepository;
  private final FileMapper fileMapper;

  public FileService(FileRepository fileRepository, FileMapper fileMapper) {
    this.fileRepository = fileRepository;
    this.fileMapper = fileMapper;
  }


  // 첨부 파일 업로드
  public FileResponse uploadFile(Long boardId, MultipartFile file) {

    // 사용자가 업로드한 원본 파일 명
    String originName = file.getOriginalFilename();
    // 확장자만 뽑아서 허용 목록에 있는지 확인 (디스크에 쓰기 전에 먼저 막음)
    String extension = originName.substring(originName.lastIndexOf(".") + 1).toLowerCase();
    if (!ALLOWED_EXTENSIONS.contains(extension)) {
      throw new BusinessException(FileErrorCode.INVALID_FILE_TYPE);
    }

    // 저장할 파일 명(UUID)
    String storedName = UUID.randomUUID().toString() + "_" + originName;

    // 업로드 폴더 경로 + 저장할 파일명을 합쳐서 최종 저장 경로 생성 (경로 조작 방어 포함)
    Path destination = resolveSafePath(storedName);

    try {
      // 업로드 폴더가 없으면 새로 생성, 이미 있으면 그냥 넘어감
      Files.createDirectories(destination.getParent());
      // 디스크에 파일 바이트 쓰기
      file.transferTo(destination);
    } catch (IOException e) {
      throw new BusinessException(FileErrorCode.FILE_UPLOAD_FAILED);
    }

    // DB에 파일 정보 저장
    FileEntity fileEntity = new FileEntity();
    fileEntity.setBoardId(boardId);
    fileEntity.setOriginName(originName);
    fileEntity.setStoredName(storedName);
    fileEntity.setFilePath(uploadDir);
    fileEntity.setFileSize(file.getSize());
    fileEntity.setFileFormat(extension);

    fileRepository.insertFile(fileEntity);

    return fileMapper.toResponse(fileEntity);
  }

  // 첨부 파일 여러 개 업로드
  public List<FileResponse> uploadFiles(Long boardId, List<MultipartFile> files) {
    // 업로드 결과(응답 DTO)들을 담을 빈 리스트 생성
    List<FileResponse> responses = new ArrayList<>();
    // 받은 파일들을 하나씩 꺼내서
    for (MultipartFile file : files) {
      // 기존의 한 개짜리 업로드 메서드를 재사용하고, 그 결과를 리스트에 추가
      responses.add(uploadFile(boardId, file));
    }
    // 업로드된 파일들의 정보 목록을 반환
    return responses;
  }

  // 게시글 별 첨부파일 목록 조회
  public List<FileResponse> getFilesByBoardId(Long boardId) {
    List<FileEntity> files = fileRepository.selectFilesByBoardId(boardId);

    return files.stream().map(fileMapper::toResponse).toList();
  }

  // 파일 id로 상세 조회
  public FileEntity getFileById(Integer id) {
    FileEntity file = fileRepository.selectFileById(id);
    if (file == null) {
      throw new BusinessException(FileErrorCode.FILE_NOT_FOUND);
    }
    return file;
  }

  // 파일 다운로드
  public Resource loadAsResource(FileEntity file) {
    // 저장된 파일명으로 실제 경로 조립 (경로 조작 방어 포함)
    Path filePath = resolveSafePath(file.getStoredName());
    try {
      // 그 경로를 가리키는 손잡이를 만들어 반환
      return new UrlResource(filePath.toUri());
    } catch (IOException e) {
      throw new BusinessException(FileErrorCode.FILE_DOWNLOAD_FAILED);
    }
  }

  // 파일 삭제
  public void deleteFile(Integer id) {
    FileEntity file = getFileById(id); // 없는 파일이면 여기서 FILE_NOT_FOUND
    Path filePath = resolveSafePath(file.getStoredName());
    fileRepository.deleteFile(id); // DB에서 삭제
    try {
      Files.deleteIfExists(filePath); // 디스크에서 삭제
    } catch (IOException e) {
      throw new BusinessException(FileErrorCode.FILE_DELETE_FAILED);
    }
  }

  // 게시글 삭제 시 호출됨. 해당 게시글의 파일들을 디스크에서만 삭제 (DB는 CASCADE로 이미 처리됨)
  public void deleteFilesFromDisk(Long boardId) {
    // 삭제되기 전에 파일 목록을 미리 조회해둠
    List<FileEntity> files = fileRepository.selectFilesByBoardId(boardId);
    // 목록을 돌면서 디스크에서 하나씩 삭제
    for (FileEntity file : files) {
      Path filePath = resolveSafePath(file.getStoredName());
      try {
        Files.deleteIfExists(filePath);
      } catch (IOException e) {
        throw new BusinessException(FileErrorCode.FILE_DELETE_FAILED);
      }
    }
  }

  // 저장 파일명으로 최종 경로를 만들되, 그 경로가 업로드 폴더를 벗어나지 않는지 검증한다 (Directory Traversal 방어)
  private Path resolveSafePath(String storedName) {
    Path uploadBase = Paths.get(uploadDir).toAbsolutePath().normalize();
    Path target = uploadBase.resolve(storedName).normalize();

    if (!target.startsWith(uploadBase)) {
      throw new BusinessException(FileErrorCode.INVALID_FILE_PATH);
    }
    return target;
  }
}
