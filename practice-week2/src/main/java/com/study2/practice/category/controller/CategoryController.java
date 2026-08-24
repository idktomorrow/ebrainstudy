package com.study2.practice.category.controller;

import com.study2.practice.category.dto.response.CategoryResponse;
import com.study2.practice.category.service.CategoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 카테고리 조회 API.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {

  private final CategoryService categoryService;

  /** 카테고리 전체 목록 조회. */
  @GetMapping
  public List<CategoryResponse> findAllCategories() {
    return categoryService.findAllCategories();
  }

}
