package com.study.board.controller;

import com.study.board.dto.request.BoardCreateRequest;
import com.study.board.dto.request.BoardUpdateRequest;
import com.study.board.dto.response.BoardResponse;
import com.study.board.service.BoardService;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
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

  @GetMapping("/api/boards")
  public List<BoardResponse> getBoardList() {
    return boardService.getBoardList();
  }

  @GetMapping("/api/boards/{id}")
  public BoardResponse getBoardDetail(@PathVariable Long id) {
    return boardService.getBoardDetail(id);
  }

  @PutMapping("/api/boards/{id}")
  public BoardResponse updateBoard(@PathVariable Long id, @RequestBody BoardUpdateRequest request) {
    return boardService.updateBoard(id, request);
  }

  @DeleteMapping("/api/boards/{id}")
  public void deleteBoard(@PathVariable Long id, @RequestParam String password) {
    boardService.deleteBoard(id, password);
  }

}
