package com.study2.practice.category.dto.response;

/**
 * 카테고리 조회 응답.
 *
 * @param id   카테고리 id
 * @param name 카테고리명
 */
public record CategoryResponse(

    Integer id,
    String name
) {}
