package com.study.category.controller;

import com.study.category.dto.response.CategoryResponse;
import com.study.category.service.CategoryService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 카테고리 컨트롤러
 */
@RestController
public class CategoryController {

  private final CategoryService categoryService;
  public CategoryController(CategoryService categoryService) {
    this.categoryService = categoryService;
  }

  @GetMapping("/api/categories")
  public List<CategoryResponse> getCategories() {
    return categoryService.getCategories();
  }
}
