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

  public BoardResponse updateBoard(Long id, BoardUpdateRequest request) {
    BoardEntity board = boardMapper.selectBoardDetail(id);

    // 비밀번호 검증
    if (!board.getPassword().equals(request.password())) {
      throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
    }

    //바꿀 부분 덮어쓰기
    board.setCategoryId(request.categoryId());
    board.setTitle(request.title());
    board.setContent(request.content());
    board.setUpdatedAt(LocalDateTime.now());

    boardMapper.updateBoard(board);
    return boardConverter.toResponse(board);
  }

  public void deleteBoard(Long id, String password) {
    BoardEntity board = boardMapper.selectBoardDetail(id);
    // 비밀번호 검증
    if (!board.getPassword().equals(password)) {
      throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
    }

    boardMapper.deleteBoard(id);
  }

}
