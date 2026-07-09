package com.study.board;

import org.mapstruct.Mapper;

/**
 * Entity <-> DTO 변환을 위한 MapStruct 컨버터
 * componentModel = "spring": MapStruct 구현체를 스프링 빈으로 등록 (다른 클래스에서 생성자 주입으로 사용 가능하게)
 */
@Mapper(componentModel = "spring")
public interface BoardConverter {

  // 등록 요청(Request)을 Entity로 변환 - Mapper에 넘겨서 INSERT할 때 사용
  BoardResponse toResponse(BoardEntity board);

  // 저장된 Entity를 응답(Response)으로 변환
  BoardEntity toEntity(BoardCreateRequest request);

}
