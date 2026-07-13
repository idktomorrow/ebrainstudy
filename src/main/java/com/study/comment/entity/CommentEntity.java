package com.study.comment.entity;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 댓글 엔티티
 */
@Getter
@Setter
@NoArgsConstructor
public class CommentEntity {
  private Integer id;
  private Long boardId;
  private String writer;
  private String content;
  private LocalDateTime createdAt;
}
