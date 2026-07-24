package com.study.board.service;

import com.study.board.dto.request.BoardCreateRequest;
import com.study.board.dto.request.BoardSearchCondition;
import com.study.board.dto.request.BoardUpdateRequest;
import com.study.board.dto.request.PasswordCheckRequest;
import com.study.board.dto.response.BoardListResponse;
import com.study.board.dto.response.BoardResponse;
import com.study.board.entity.BoardEntity;
import com.study.board.mapper.BoardMapper;
import com.study.board.repository.BoardRepository;
import com.study.file.service.FileService;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 게시판 비즈니스 로직
 */
@Service
public class BoardService {

  private final BoardRepository boardRepository;
  private final BoardMapper boardMapper;
  private final FileService fileService;

  public BoardService(BoardRepository boardRepository, BoardMapper boardMapper, FileService fileService) {
    this.boardRepository = boardRepository;
    this.boardMapper = boardMapper;
    this.fileService = fileService;
  }

  public BoardResponse createBoard(BoardCreateRequest request) {
    BoardEntity board = boardMapper.toEntity(request);     // 요청 -> 엔티티
    board.setViewCount(0);
    board.setCreatedAt(LocalDateTime.now());
    boardRepository.insertBoard(board);                    // Insert 실행
    return boardMapper.toResponse(board);                  // 엔티티 -> 응답
  }

  public BoardListResponse getBoardList(BoardSearchCondition condition) {
    // 등록일 종료값이 없으면 오늘, 시작값이 없으면 종료값 기준 1년 전 (요구사항: 디폴트 값이 최근 1년)
    LocalDate resolvedEndDate = (condition.endDate() != null) ? condition.endDate() : LocalDate.now();
    // 종료일을 먼저 구해놔야 시작일을 구할 수 있음
    LocalDate resolvedStartDate = (condition.startDate() != null) ? condition.startDate() : resolvedEndDate.minusYears(1);

    BoardSearchCondition resolvedCondition = new BoardSearchCondition(
        condition.categoryId(), condition.keyword(), resolvedStartDate, resolvedEndDate, condition.page(), condition.size()
    );

    List<BoardResponse> boards = boardRepository.selectBoardList(resolvedCondition, resolvedCondition.offset()).stream()
        .map(boardMapper::toResponse)
        .toList();
    long totalCount = boardRepository.countBoardList(resolvedCondition);
    return new BoardListResponse(boards, totalCount);
  }

  public BoardResponse getBoardDetail(Long id) {
    boardRepository.increaseViewCount(id);
    BoardEntity board = boardRepository.selectBoardDetail(id);
    return boardMapper.toResponse(board);
  }

  public BoardResponse updateBoard(Long id, BoardUpdateRequest request) {
    BoardEntity board = boardRepository.selectBoardDetail(id);

    // 비밀번호 검증
    if (!board.getPassword().equals(request.password())) {
      throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
    }

    //바꿀 부분 덮어쓰기
    board.setCategoryId(request.categoryId());
    board.setTitle(request.title());
    board.setContent(request.content());
    board.setUpdatedAt(LocalDateTime.now());

    boardRepository.updateBoard(board);
    return boardMapper.toResponse(board);
  }

  public void checkPassword(Long id, PasswordCheckRequest request) {
    BoardEntity board = boardRepository.selectBoardDetail(id);
    // 비밀번호 검증
    if (!board.getPassword().equals(request.password())) {
      throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
    }
  }

  public void deleteBoard(Long id, String password) throws IOException {
    BoardEntity board = boardRepository.selectBoardDetail(id);
    // 비밀번호 검증
    if (!board.getPassword().equals(password)) {
      throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
    }

    fileService.deleteFilesFromDisk(id);   // 먼저: 디스크 파일 정리
    boardRepository.deleteBoard(id);       // 그다음: DB에서 게시글 삭제
  }

}
