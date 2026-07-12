package com.study.category.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Category 테이블과 매칭되는 Entity
 */
@Getter
@Setter
@NoArgsConstructor
public class CategoryEntity {
  private Integer id;
  private String name;
}
