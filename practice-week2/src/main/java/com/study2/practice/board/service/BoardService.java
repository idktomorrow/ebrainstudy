package com.study2.practice.board.service;

import com.study2.practice.board.dto.request.BoardCreateRequest;
import com.study2.practice.board.dto.request.BoardDeleteRequest;
import com.study2.practice.board.dto.request.BoardSearchRequest;
import com.study2.practice.board.dto.request.BoardUpdateRequest;
import com.study2.practice.board.dto.response.BoardDetailResponse;
import com.study2.practice.board.dto.response.BoardListResponse;
import com.study2.practice.board.dto.response.BoardSummaryResponse;
import com.study2.practice.board.entity.Board;
import com.study2.practice.board.mapper.BoardMapper;
import com.study2.practice.category.entity.Category;
import com.study2.practice.category.mapper.CategoryMapper;
import com.study2.practice.file.dto.response.AttachmentResponse;
import com.study2.practice.file.service.AttachmentService;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 게시글 CRUD 비즈니스 로직.
 * 카테고리 이름 매칭을 위해 CategoryMapper도 함께 사용한다.
 */
@Service
@RequiredArgsConstructor
public class BoardService {

  private static final int MAX_PAGE_SIZE = 100;

  private final BoardMapper boardMapper;
  private final CategoryMapper categoryMapper;   // 카테고리 이름 조회용으로 추가 주입
  private final AttachmentService attachmentService;   // 상세 조회 시 첨부파일 목록 조회, 삭제 시 디스크 파일 정리용

  /** 게시글 등록. 필드 검증 + 카테고리 존재 확인 후 insert. */
  public Integer createBoard(BoardCreateRequest request) {

    validateWriter(request.writer());
    validatePassword(request.password());
    validateTitle(request.title());
    validateContent(request.content());

    if (request.categoryId() == null || categoryMapper.findById(request.categoryId()) == null) {
      throw new IllegalArgumentException("존재하지 않는 카테고리입니다.");
    }

    Board board = new Board();
    board.setCategoryId(request.categoryId());
    board.setWriter(request.writer());
    board.setTitle(request.title());
    board.setContent(request.content());
    board.setPassword(request.password());

    boardMapper.insert(board);   // insert 후 board.getId()에 생성된 id가 채워짐

    return board.getId();
  }

  /** 게시글 상세 조회. 조회수 증가 + 카테고리 이름 매칭까지 함께 처리. */
  public BoardDetailResponse getBoardDetail(Integer id) {

    Board board = boardMapper.findById(id);
    if (board == null) {
      throw new NoSuchElementException("게시글을 찾을 수 없습니다.");
    }

    boardMapper.increaseViewCount(id);

    Category category = categoryMapper.findById(board.getCategoryId());

    List<AttachmentResponse> attachments = attachmentService.getAttachments(id).stream()
        .map(attachment -> new AttachmentResponse(
            attachment.getId(),
            attachment.getOriginName(),
            attachment.getFileSize()
        ))
        .toList();

    return new BoardDetailResponse(
        board.getId(),
        category.getName(),
        board.getTitle(),
        board.getWriter(),
        board.getContent(),
        board.getViewCount() + 1,   // DB엔 반영됐지만 board 객체엔 반영 안 돼서 +1을 직접 더함
        board.getCreatedAt(),
        board.getUpdatedAt(),
        attachments
    );
  }

  /** 게시글 수정. 비밀번호 확인 후 writer/title/content만 변경 (카테고리는 수정 대상 아님). */
  public void updateBoard(Integer id, BoardUpdateRequest request) {

    validateWriter(request.writer());
    validateTitle(request.title());
    validateContent(request.content());

    Board board = boardMapper.findById(id);
    if (board == null) {
      throw new NoSuchElementException("게시글을 찾을 수 없습니다.");
    }

    if (!board.getPassword().equals(request.password())) {
      throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
    }

    board.setWriter(request.writer());
    board.setTitle(request.title());
    board.setContent(request.content());

    boardMapper.update(board);
  }

  /** 게시글 삭제. 비밀번호 확인 후 삭제. */
  public void deleteBoard(Integer id, BoardDeleteRequest request) {

    Board board = boardMapper.findById(id);
    if (board == null) {
      throw new NoSuchElementException("게시글을 찾을 수 없습니다.");
    }

    if (!board.getPassword().equals(request.password())) {
      throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
    }

    // DB의 files 행은 FK CASCADE로 자동 삭제되지만, 디스크의 실제 파일은 별도로 지워야 함
    attachmentService.deleteFilesByBoardId(id);
    boardMapper.delete(id);
  }

  /** 게시글 목록 조회. 검색조건에 맞는 목록 + 총 건수를 함께 응답. */
  public BoardListResponse getBoardList(BoardSearchRequest condition) {

    validatePagination(condition.page(), condition.size());

    BoardSearchRequest escapedCondition = escapeKeywordForLike(condition);
    List<Board> boards = boardMapper.findAll(escapedCondition);
    int totalCount = boardMapper.countAll(escapedCondition);

    // 카테고리 이름을 게시글마다 매번 조회하면 N번 쿼리가 나가니, 한 번에 다 가져와서 Map으로 매칭
    Map<Integer, String> categoryNames = categoryMapper.findAll().stream()
        .collect(Collectors.toMap(Category::getId, Category::getName));

    List<BoardSummaryResponse> summaries = boards.stream()
        .map(board -> new BoardSummaryResponse(
            board.getId(),
            categoryNames.get(board.getCategoryId()),
            truncateTitle(board.getTitle()),
            board.getWriter(),
            board.getViewCount(),
            board.getCreatedAt(),
            board.getUpdatedAt(),
            Boolean.TRUE.equals(board.getHasAttachment())
        ))
        .toList();

    return new BoardListResponse(summaries, totalCount);
  }

  /**
   * 검색어에 LIKE의 와일드카드 문자(%, _)가 그대로 들어있으면, "평범한 특수문자 검색"이
   * 아니라 패턴으로 해석돼버림 — 예를 들어 키워드로 "%"를 검색하면 CONCAT('%', '%', '%')가
   * '%%%' 패턴이 돼서 모든 게시글이 매칭돼버림(실제로 curl로 재현 확인). MySQL은 LIKE에서
   * '\'를 기본 escape 문자로 쓰므로, %/_/\\ 앞에 '\'를 붙여서 리터럴로 취급되게 만든다.
   */
  private BoardSearchRequest escapeKeywordForLike(BoardSearchRequest condition) {
    if (condition.keyword() == null) {
      return condition;
    }
    String escaped = condition.keyword()
        .replace("\\", "\\\\")
        .replace("%", "\\%")
        .replace("_", "\\_");
    return new BoardSearchRequest(escaped, condition.categoryId(), condition.startDate(),
        condition.endDate(), condition.page(), condition.size());
  }

  private void validatePagination(int page, int size) {
    // 음수/0이 그대로 SQL의 LIMIT ${(page-1)*size}, ${size}에 들어가면 SQL 문법 에러(500)로
    // 이어지므로, 여기서 미리 걸러서 400으로 응답되게 함
    if (page < 1) {
      throw new IllegalArgumentException("페이지 번호는 1 이상이어야 합니다.");
    }
    if (size < 1 || size > MAX_PAGE_SIZE) {
      throw new IllegalArgumentException("페이지당 건수는 1 이상 " + MAX_PAGE_SIZE + " 이하여야 합니다.");
    }
    // size 상한을 둬도 page를 극단적으로 크게 보내면 (page-1)*size가 int 범위를 넘겨
    // LIMIT에 음수가 들어가는 오버플로가 재현됨(예: page=3, size=2000000000) -> long으로
    // 미리 계산해서 오버플로 자체를 막음
    if ((long) (page - 1) * size > Integer.MAX_VALUE) {
      throw new IllegalArgumentException("페이지 번호가 너무 큽니다.");
    }
  }

  private void validateWriter(String writer) {
    // 길이만 보면 "   "(공백 3칸)도 통과해버림 -> isBlank()로 공백만 있는 값도 같이 막음
    if (writer == null || writer.isBlank() || writer.length() < 3 || writer.length() >= 5) {
      throw new IllegalArgumentException("작성자는 3자 이상 5자 미만이어야 합니다.");
    }
  }

  private void validatePassword(String password) {
    if (password == null || password.isBlank() || password.length() < 4 || password.length() >= 16) {
      throw new IllegalArgumentException("비밀번호는 4자 이상 16자 미만이어야 합니다.");
    }
    if (!password.matches(".*[a-zA-Z].*")
        || !password.matches(".*[0-9].*")
        || !password.matches(".*[^a-zA-Z0-9].*")) {
      throw new IllegalArgumentException("비밀번호는 영문, 숫자, 특수문자를 모두 포함해야 합니다.");
    }
  }

  private void validateTitle(String title) {
    if (title == null || title.isBlank() || title.length() < 4 || title.length() >= 100) {
      throw new IllegalArgumentException("제목은 4자 이상 100자 미만이어야 합니다.");
    }
  }

  private void validateContent(String content) {
    if (content == null || content.isBlank() || content.length() < 4 || content.length() >= 2000) {
      throw new IllegalArgumentException("내용은 4자 이상 2000자 미만이어야 합니다.");
    }
  }

  /** 목록 화면용 제목 축약. 80자 넘으면 '...'으로 줄임. */
  private String truncateTitle(String title) {
    if (title.length() > 80) {
      return title.substring(0, 80) + "...";
    }
    return title;
  }
}
