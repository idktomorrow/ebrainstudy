package com.study.file.repository;

import com.study.file.entity.FileEntity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * File 테이블 접근을 위한 MyBatis 매퍼
 */
@Mapper
public interface FileRepository {

  //파일 등록
  void insertFile(FileEntity file);

  //게시글 별 첨부파일 목록 조회
  List<FileEntity> selectFilesByBoardId(Long boardId);

  //파일 id로 상세 조회
  FileEntity selectFileById(Integer id);

}
