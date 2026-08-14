package com.study2.practice.category.service;

import com.study2.practice.category.dto.response.CategoryResponse;
import com.study2.practice.category.entity.Category;
import com.study2.practice.category.mapper.CategoryMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {

  private final CategoryMapper categoryMapper;

  public List<CategoryResponse> findAllCategories() {

    List<Category> categories = categoryMapper.findAll();

    return categories.stream()
        .map(category -> new CategoryResponse(category.getId(), category.getName())).toList();
  }
}
