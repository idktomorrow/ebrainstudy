package com.study2.practice.comment.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * comment 테이블과 매핑되는 Entity.
 * MyBatis가 SELECT 결과를 이 클래스의 필드에 그대로 채워 넣는다.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Comment {

  private Integer id;
  private Integer boardId;
  private String writer;
  private String content;
  private LocalDateTime createdAt;
}
