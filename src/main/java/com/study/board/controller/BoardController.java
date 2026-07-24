package com.study.board.controller;

import com.study.board.dto.request.BoardCreateRequest;
import com.study.board.dto.request.BoardSearchCondition;
import com.study.board.dto.request.BoardUpdateRequest;
import com.study.board.dto.request.PasswordCheckRequest;
import com.study.board.dto.response.BoardListResponse;
import com.study.board.dto.response.BoardResponse;
import com.study.board.service.BoardService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
  public BoardResponse createBoard(
      @RequestPart("request") @Valid BoardCreateRequest request,  //JSON -> BoardCreateRequest
      @RequestParam(value = "files", required = false) List<MultipartFile> files
  ) throws IOException {
    return boardService.createBoard(request, files);
  }

  @GetMapping("/api/boards")
  public BoardListResponse getBoardList(
      @RequestParam(required = false) Integer categoryId,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) LocalDate startDate,
      @RequestParam(required = false) LocalDate endDate,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int size
  ) {
    BoardSearchCondition condition = new BoardSearchCondition(categoryId, keyword, startDate,
        endDate, page, size);
    return boardService.getBoardList(condition);
  }

  @GetMapping("/api/boards/{id}")
  public BoardResponse getBoardDetail(@PathVariable Long id) {
    return boardService.getBoardDetail(id);
  }

  @PutMapping("/api/boards/{id}")
  public BoardResponse updateBoard(
      @PathVariable Long id,
      @RequestPart("request") @Valid BoardUpdateRequest request,
      @RequestParam(value = "files", required = false) List<MultipartFile> files
      ) throws IOException {
    return boardService.updateBoard(id, request, files);
  }

  @PostMapping("/api/boards/{id}/password-check")
  public void checkPassword(@PathVariable Long id,
      @Valid @RequestBody PasswordCheckRequest request) {
    boardService.checkPassword(id, request);
  }

  @DeleteMapping("/api/boards/{id}")
  public void deleteBoard(@PathVariable Long id, @RequestParam String password) throws IOException {
    boardService.deleteBoard(id, password);
  }

}
