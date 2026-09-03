package com.study2.practice.category.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.study2.practice.category.dto.response.CategoryResponse;
import com.study2.practice.category.service.CategoryService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * CategoryController 통합 테스트. 파라미터/바디가 전혀 없는 제일 단순한 컨트롤러라
 * @WebMvcTest로 "요청이 실제로 JSON 응답까지 잘 나오는지"만 확인한다.
 */
@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private CategoryService categoryService;

  @Test
  @DisplayName("카테고리 목록을 JSON 배열로 응답한다")
  void getCategories_returnsList() throws Exception {
    when(categoryService.findAllCategories())
        .thenReturn(List.of(new CategoryResponse(1, "공지"), new CategoryResponse(2, "자유")));

    mockMvc.perform(get("/api/categories"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].name").value("공지"))
        .andExpect(jsonPath("$[1].name").value("자유"));
  }

  @Test
  @DisplayName("카테고리가 하나도 없으면 빈 배열을 응답한다")
  void getCategories_returnsEmptyArray_whenNoCategories() throws Exception {
    when(categoryService.findAllCategories()).thenReturn(List.of());

    mockMvc.perform(get("/api/categories"))
        .andExpect(status().isOk())
        .andExpect(content().json("[]"));
  }
}
