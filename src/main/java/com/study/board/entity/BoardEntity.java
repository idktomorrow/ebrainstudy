package com.study.board.entity;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 게시판 엔티티
 */
@Getter
@Setter
public class BoardEntity {
  private Long id;
  private Integer categoryId;
  private String title;
  private String writer;
  private String content;
  private String password;
  private Integer viewCount;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
