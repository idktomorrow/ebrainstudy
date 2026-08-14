package com.study2.practice.file.mapper;

import com.study2.practice.file.entity.Attachment;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * files 테이블에 대한 MyBatis Mapper.
 * 각 메서드는 resources/mappers/AttachmentMapper.xml의 동일한 id와 매칭된다.
 */
@Mapper
public interface AttachmentMapper {

  /** 첨부파일 메타데이터 등록. useGeneratedKeys로 채번된 id가 attachment.id에 채워진다. */
  void insert(Attachment attachment);

  /** 게시글에 첨부된 파일 목록 조회. */
  List<Attachment> findByBoardId(Integer boardId);

  /** 첨부파일 id로 단건 조회 (다운로드/삭제 시 사용). */
  Attachment findById(Integer id);

  /** 첨부파일 메타데이터 삭제. */
  void delete(Integer id);
}
