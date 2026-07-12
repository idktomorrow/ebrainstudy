package com.study.category.mapper;

import com.study.category.dto.response.CategoryResponse;
import com.study.category.entity.CategoryEntity;
import org.mapstruct.Mapper;

/**
 * Entity <-> DTO 변환을 위한 MapStruct 매퍼
 */
@Mapper(componentModel = "spring")
public interface CategoryMapper {

  CategoryResponse toResponse(CategoryEntity category);
}
