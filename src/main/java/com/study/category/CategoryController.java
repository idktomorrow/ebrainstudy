package com.study.category;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
