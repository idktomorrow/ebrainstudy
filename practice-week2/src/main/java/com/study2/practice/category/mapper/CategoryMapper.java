package com.study2.practice.category.mapper;

import com.study2.practice.category.entity.Category;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * category 테이블에 대한 MyBatis Mapper.
 * 각 메서드는 resources/mappers/CategoryMapper.xml의 동일한 id와 매칭된다.
 */
@Mapper
public interface CategoryMapper {

  /** 카테고리 전체 조회. */
  List<Category> findAll();

  /** id로 카테고리 단건 조회 (게시글의 카테고리명 매칭용). */
  Category findById(Integer id);
}
