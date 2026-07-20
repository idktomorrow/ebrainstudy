package com.study.board.service;

import com.study.board.dto.request.BoardCreateRequest;
import com.study.board.dto.request.BoardUpdateRequest;
import com.study.board.dto.request.PasswordCheckRequest;
import com.study.board.dto.response.BoardResponse;
import com.study.board.entity.BoardEntity;
import com.study.board.mapper.BoardMapper;
import com.study.board.repository.BoardRepository;
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

  public BoardService(BoardRepository boardRepository, BoardMapper boardMapper) {
    this.boardRepository = boardRepository;
    this.boardMapper = boardMapper;
  }

  public BoardResponse createBoard(BoardCreateRequest request) {
    BoardEntity board = boardMapper.toEntity(request);     // 요청 -> 엔티티
    board.setViewCount(0);
    board.setCreatedAt(LocalDateTime.now());
    boardRepository.insertBoard(board);                    // Insert 실행
    return boardMapper.toResponse(board);                  // 엔티티 -> 응답
  }

  public List<BoardResponse> getBoardList() {
    return boardRepository.selectBoardList().stream()
        .map(boardMapper::toResponse)
        .toList();
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

  public void deleteBoard(Long id, String password) {
    BoardEntity board = boardRepository.selectBoardDetail(id);
    // 비밀번호 검증
    if (!board.getPassword().equals(password)) {
      throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
    }

    boardRepository.deleteBoard(id);
  }

}
