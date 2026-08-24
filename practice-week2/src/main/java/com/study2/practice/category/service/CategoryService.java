package com.study2.practice.category.service;

import com.study2.practice.category.dto.response.CategoryResponse;
import com.study2.practice.category.entity.Category;
import com.study2.practice.category.mapper.CategoryMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 카테고리 조회 비즈니스 로직. 등록/수정/삭제 없이 조회만 지원한다.
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

  private final CategoryMapper categoryMapper;

  /** 카테고리 전체 목록 조회. */
  public List<CategoryResponse> findAllCategories() {

    List<Category> categories = categoryMapper.findAll();

    return categories.stream()
        .map(category -> new CategoryResponse(category.getId(), category.getName())).toList();
  }
}
