package com.study.board;

import java.time.LocalDateTime;
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

}
