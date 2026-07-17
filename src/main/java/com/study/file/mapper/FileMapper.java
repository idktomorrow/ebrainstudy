package com.study.file.mapper;

import com.study.file.dto.response.FileResponse;
import com.study.file.entity.FileEntity;
import org.mapstruct.Mapper;

/**
 * Entity <-> DTO 변환을 위한 MapStruct 매퍼
 */
@Mapper(componentModel = "spring")
public interface FileMapper {

  FileResponse toResponse(FileEntity file);

}
