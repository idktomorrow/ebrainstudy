package com.study.category.service;

import com.study.category.dto.response.CategoryResponse;
import com.study.category.entity.CategoryEntity;
import com.study.category.mapper.CategoryMapper;
import com.study.category.repository.CategoryRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 카테고리 비즈니스 로직
 */
@Service
public class CategoryService {

  private final CategoryRepository categoryRepository;
  private final CategoryMapper categoryMapper;

  public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
    this.categoryRepository = categoryRepository;
    this.categoryMapper = categoryMapper;
  }

  public List<CategoryResponse> getCategories() {
    List<CategoryEntity> categories = categoryRepository.getCategories();
    return categories.stream().map(categoryMapper::toResponse).toList();
  }

}
