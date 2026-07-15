package com.study.file.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 첨부파일 엔티티
 */
@Getter
@Setter
@NoArgsConstructor
public class FileEntity {
  private Integer id;
  private Long boardId;
  private String originName;
  private String storedName;
  private String filePath;
  private Long fileSize;
  private String fileFormat;
}
