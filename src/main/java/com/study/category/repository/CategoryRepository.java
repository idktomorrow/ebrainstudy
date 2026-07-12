package com.study.category.repository;

import com.study.category.entity.CategoryEntity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * Category 테이블 조회를 위한 MyBatis 매퍼
 */
@Mapper
public interface CategoryRepository {

  List<CategoryEntity> getCategories();
}
