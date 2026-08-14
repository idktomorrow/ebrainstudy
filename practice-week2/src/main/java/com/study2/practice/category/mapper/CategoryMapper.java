package com.study2.practice.category.mapper;

import com.study2.practice.category.entity.Category;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper {

  List<Category> findAll();   // 카테고리 전체 조회. XML의 id="findAll"과 매칭됨

  Category findById(Integer id);
}
