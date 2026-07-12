package com.study.board.mapper;

import com.study.board.dto.request.BoardCreateRequest;
import com.study.board.dto.response.BoardResponse;
import com.study.board.entity.BoardEntity;
import org.mapstruct.Mapper;

/**
 * Entity <-> DTO 변환을 위한 MapStruct 매퍼
 * componentModel = "spring": MapStruct 구현체를 스프링 빈으로 등록 (다른 클래스에서 생성자 주입으로 사용 가능하게)
 */
@Mapper(componentModel = "spring")
public interface BoardMapper {

  // 저장된 Entity를 응답(Response)으로 변환
  BoardResponse toResponse(BoardEntity board);

  // 등록 요청(Request)을 Entity로 변환 - Repository에 넘겨서 INSERT할 때 사용
  BoardEntity toEntity(BoardCreateRequest request);

}
