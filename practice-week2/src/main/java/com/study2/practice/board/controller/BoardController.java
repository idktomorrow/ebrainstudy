package com.study2.practice.board.controller;

import com.study2.practice.board.dto.request.BoardCreateRequest;
import com.study2.practice.board.dto.request.BoardDeleteRequest;
import com.study2.practice.board.dto.request.BoardSearchRequest;
import com.study2.practice.board.dto.request.BoardUpdateRequest;
import com.study2.practice.board.dto.response.BoardDetailResponse;
import com.study2.practice.board.dto.response.BoardListResponse;
import com.study2.practice.board.service.BoardService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 게시글 CRUD API.
 */
@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

  private final BoardService boardService;

  /** 게시글 목록 조회 (검색 + 페이지네이션). 쿼리파라미터를 BoardSearchRequest로 묶어서 Service에 전달. */
  @GetMapping
  public BoardListResponse getBoardList(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer categoryId,
      @RequestParam(required = false) LocalDate startDate,
      @RequestParam(required = false) LocalDate endDate,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int size
  ) {
    BoardSearchRequest condition = new BoardSearchRequest(keyword, categoryId, startDate, endDate, page, size);
    return boardService.getBoardList(condition);
  }

  /** 게시글 상세 조회. */
  @GetMapping("/{id}")
  public BoardDetailResponse getBoardDetail(@PathVariable Integer id) {
    return boardService.getBoardDetail(id);
  }

  /** 게시글 등록. 생성된 게시글의 id를 반환. */
  @PostMapping
  public Integer createBoard(@RequestBody BoardCreateRequest request) {
    return boardService.createBoard(request);
  }

  /** 게시글 수정. */
  @PutMapping("/{id}")
  public void updateBoard(@PathVariable Integer id, @RequestBody BoardUpdateRequest request) {
    boardService.updateBoard(id, request);
  }

  /** 게시글 삭제. 비밀번호는 요청 본문으로 받음. */
  @DeleteMapping("/{id}")
  public void deleteBoard(@PathVariable Integer id, @RequestBody BoardDeleteRequest request) {
    boardService.deleteBoard(id, request);
  }
}
