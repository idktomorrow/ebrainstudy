package com.study.board;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 게시판 비즈니스 로직
 */
@Service
public class BoardService {

  private final BoardMapper boardMapper;
  private final BoardConverter boardConverter;

  public BoardService(BoardMapper boardMapper, BoardConverter boardConverter) {
    this.boardMapper = boardMapper;
    this.boardConverter = boardConverter;
  }

  public BoardResponse createBoard(BoardCreateRequest request) {
    BoardEntity board = boardConverter.toEntity(request);  // 요청 -> 엔티티
    board.setViewCount(0);
    board.setCreatedAt(LocalDateTime.now());
    boardMapper.insertBoard(board);                        // Insert 실행
    return boardConverter.toResponse(board);               // 엔티티 -> 응답
  }

  public List<BoardResponse> getBoardList() {
    return boardMapper.selectBoardList().stream()
        .map(boardConverter::toResponse)
        .toList();
  }

  public BoardResponse getBoardDetail(Long id) {
    boardMapper.increaseViewCount(id);
    BoardEntity board = boardMapper.selectBoardDetail(id);
    return boardConverter.toResponse(board);
  }


}
