package com.study2.practice.category.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * category 테이블과 매핑되는 Entity.
 * MyBatis가 SELECT 결과를 이 클래스의 필드에 그대로 채워 넣는다.
 * 등록/수정/삭제 API는 없고 조회만 지원한다 (관리 기능 없이 DB 값만 사용).
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Category {

  private Integer id;
  private String name;

}
