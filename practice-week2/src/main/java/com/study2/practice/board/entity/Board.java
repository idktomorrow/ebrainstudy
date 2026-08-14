package com.study2.practice.board.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * board 테이블과 매핑되는 Entity.
 * MyBatis가 SELECT 결과를 이 클래스의 필드에 그대로 채워 넣는다.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Board {

  private Integer id;
  private Integer categoryId;
  private String title;
  private String writer;
  private String content;
  private String password;
  private Integer viewCount;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private Boolean hasAttachment;   // 목록 조회 전용 필드. findAll의 EXISTS 서브쿼리 결과만 채워짐

}
