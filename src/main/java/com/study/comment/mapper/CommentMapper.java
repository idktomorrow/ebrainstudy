package com.study.comment.mapper;

import com.study.comment.dto.request.CommentCreateRequest;
import com.study.comment.dto.response.CommentResponse;
import com.study.comment.entity.CommentEntity;
import org.mapstruct.Mapper;

/**
 * Entity <-> DTO 변환을 위한 MapStruct 매퍼
 */
@Mapper(componentModel = "spring")
public interface CommentMapper {

  // 등록 요청(Request)을 Entity로 변환 - Repository에 넘겨서 INSERT할 때 사용
  CommentEntity toEntity(CommentCreateRequest request);

  // 저장된 Entity를 응답(Response)으로 변환
  CommentResponse toResponse(CommentEntity comment);
}
