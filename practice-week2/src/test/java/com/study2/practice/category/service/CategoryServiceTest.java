package com.study2.practice.category.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.study2.practice.category.dto.response.CategoryResponse;
import com.study2.practice.category.entity.Category;
import com.study2.practice.category.mapper.CategoryMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CategoryService 단위 테스트.
 * CategoryMapper를 Mockito로 mock 처리해서, 실제 DB 연결 없이 Service의 변환 로직만 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

  @Mock
  private CategoryMapper categoryMapper;

  @InjectMocks
  private CategoryService categoryService;

  @Test
  @DisplayName("카테고리 목록을 조회하면 Entity가 Response DTO로 변환되어 반환된다")
  void findAllCategories_returnsMappedResponses() {
    // given: Mapper가 카테고리 2건을 반환한다고 가정
    List<Category> categories = List.of(
        new Category(1, "공지"),
        new Category(2, "자유")
    );
    when(categoryMapper.findAll()).thenReturn(categories);

    // when
    List<CategoryResponse> result = categoryService.findAllCategories();

    // then: 개수와 각 필드(id, name)가 정확히 매핑됐는지 확인
    assertThat(result).hasSize(2);
    assertThat(result.get(0).id()).isEqualTo(1);
    assertThat(result.get(0).name()).isEqualTo("공지");
    assertThat(result.get(1).id()).isEqualTo(2);
    assertThat(result.get(1).name()).isEqualTo("자유");
  }

  @Test
  @DisplayName("카테고리가 하나도 없으면 빈 리스트를 반환한다")
  void findAllCategories_returnsEmptyList_whenNoCategoriesExist() {
    // given
    when(categoryMapper.findAll()).thenReturn(List.of());

    // when
    List<CategoryResponse> result = categoryService.findAllCategories();

    // then
    assertThat(result).isEmpty();
  }
}
