package com.study2.practice.file.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * files 테이블과 매핑되는 Entity. 클래스명을 Files로 하지 않은 건
 * java.nio.file.Files와 이름이 겹쳐서 혼동을 줄이기 위함.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Attachment {

  private Integer id;
  private Integer boardId;
  private String originName;    // 사용자가 업로드한 원본 파일명
  private String storedName;    // 서버에 저장된 파일명 (충돌 방지용 UUID 기반)
  private String filePath;      // 서버 내 저장 경로
  private Long fileSize;        // 파일 크기(byte)
  private String fileFormat;    // 확장자
}
