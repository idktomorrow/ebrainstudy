package com.study.file.service;

import com.study.file.dto.response.FileResponse;
import com.study.file.entity.FileEntity;
import com.study.file.mapper.FileMapper;
import com.study.file.repository.FileRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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
  public FileResponse uploadFile(Long boardId, MultipartFile file) throws IOException {

    // 사용자가 업로드한 원본 파일 명
    String originName = file.getOriginalFilename();
    // 저장할 파일 명(UUID)
    String storedName = UUID.randomUUID().toString() + "_" + originName;

    // "./uploads" 문자열을 자바가 다룰 수 있는 경로 객체로 변환
    Path uploadPath = Paths.get(uploadDir);
    // 업로드 폴더가 없으면 새로 생성, 이미 있으면 그냥 넘어감
    Files.createDirectories(uploadPath);
    // 업로드 폴더 경로 + 저장할 파일명을 합쳐서 최종 저장 경로 생성
    Path destination = uploadPath.resolve(storedName);

    // 디스크에 파일 바이트 쓰기
    file.transferTo(destination);

    // DB에 파일 정보 저장
    FileEntity fileEntity = new FileEntity();
    fileEntity.setBoardId(boardId);
    fileEntity.setOriginName(originName);
    fileEntity.setStoredName(storedName);
    fileEntity.setFilePath(uploadDir);
    fileEntity.setFileSize(file.getSize());
    fileEntity.setFileFormat(originName.substring(originName.lastIndexOf(".") + 1));

    fileRepository.insertFile(fileEntity);

    return fileMapper.toResponse(fileEntity);
  }

  // 첨부 파일 여러 개 업로드
  public List<FileResponse> uploadFiles(Long boardId, List<MultipartFile> files) throws IOException {
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

    return file;
  }

  // 파일 다운로드
  public Resource loadAsResource(FileEntity file) throws IOException {
    // 폴더 경로 + UUID 저장명으로 실제 경로 조립
    Path filePath = Paths.get(file.getFilePath()).resolve(file.getStoredName());
    // 그 경로를 가리키는 손잡이를 만들어 반환
    return new UrlResource(filePath.toUri());
  }

  // 파일 삭제
  public void deleteFile(Integer id) throws IOException{
    FileEntity file = fileRepository.selectFileById(id);
    Path filePath = Paths.get(file.getFilePath()).resolve(file.getStoredName());
    fileRepository.deleteFile(id); // DB에서 삭제
    Files.deleteIfExists(filePath); // 디스크에서 삭제
  }

}
