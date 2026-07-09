package com.study.category;

import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 카테고리 비즈니스 로직
 */
@Service
public class CategoryService {

  private final CategoryMapper categoryMapper;
  private final CategoryConverter categoryConverter;

  public CategoryService(CategoryMapper categoryMapper, CategoryConverter categoryConverter) {
    this.categoryMapper = categoryMapper;
    this.categoryConverter = categoryConverter;

  }

  public List<CategoryResponse> getCategories() {
    List<CategoryEntity> categories = categoryMapper.getCategories();
    return categories.stream().map(categoryConverter::toResponse).toList();
  }

}
