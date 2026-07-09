package com.study.board;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 게시판 컨트롤러
 */
@RestController
public class BoardController {
  private final BoardService boardService;
  public BoardController(BoardService boardService) {
    this.boardService = boardService;
  }

  @PostMapping("/api/boards")
  public BoardResponse createBoard(@RequestBody BoardCreateRequest request) {
    return boardService.createBoard(request);
  }

}
