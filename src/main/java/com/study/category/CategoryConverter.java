package com.study.category;

import org.mapstruct.Mapper;

/**
 * Entity <-> DTO 변환을 위한 MapStruct 컨버터
 */
@Mapper(componentModel = "spring")
public interface CategoryConverter {

  CategoryResponse toResponse(CategoryEntity category);
}
