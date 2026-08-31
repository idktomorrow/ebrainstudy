package com.study2.practice.board.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.study2.practice.board.dto.request.BoardCreateRequest;
import com.study2.practice.board.dto.request.BoardDeleteRequest;
import com.study2.practice.board.dto.request.BoardSearchRequest;
import com.study2.practice.board.dto.request.BoardUpdateRequest;
import com.study2.practice.board.dto.response.BoardDetailResponse;
import com.study2.practice.board.dto.response.BoardListResponse;
import com.study2.practice.board.entity.Board;
import com.study2.practice.board.mapper.BoardMapper;
import com.study2.practice.category.entity.Category;
import com.study2.practice.category.mapper.CategoryMapper;
import com.study2.practice.file.entity.Attachment;
import com.study2.practice.file.service.AttachmentService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * BoardService 단위 테스트.
 * 메서드가 5개(등록/상세조회/수정/삭제/목록)라 @Nested로 그룹을 나눠서, 테스트가 많아져도
 * 어떤 기능을 검증하는 건지 한눈에 보이게 정리했다. (Mockito의 JUnit5 확장은 @Nested
 * 안에서도 바깥 클래스의 @Mock/@InjectMocks를 그대로 쓸 수 있다)
 */
@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

  @Mock
  private BoardMapper boardMapper;
  @Mock
  private CategoryMapper categoryMapper;
  @Mock
  private AttachmentService attachmentService;

  @InjectMocks
  private BoardService boardService;

  @Nested
  @DisplayName("게시글 등록")
  class CreateBoard {

    @Test
    @DisplayName("모든 필드가 유효하면 등록하고 생성된 id를 반환한다")
    void success() {
      // given
      BoardCreateRequest request =
          new BoardCreateRequest(1, "김철수", "제목입니다", "내용은충분히깁니다", "abc123!@#");
      when(categoryMapper.findById(1)).thenReturn(new Category(1, "공지"));
      // insert는 원래 useGeneratedKeys로 DB가 id를 채워주는데, mock에선 그 동작이 없으니
      // doAnswer로 "DB가 id 100을 채번했다"는 상황을 직접 흉내낸다
      doAnswer(invocation -> {
        Board board = invocation.getArgument(0);
        board.setId(100);
        return null;
      }).when(boardMapper).insert(any(Board.class));

      // when
      Integer id = boardService.createBoard(request);

      // then
      assertThat(id).isEqualTo(100);
      verify(boardMapper).insert(any(Board.class));
    }

    @Test
    @DisplayName("작성자가 3자 미만이면 예외가 발생하고 저장을 시도하지 않는다")
    void failsWhenWriterTooShort() {
      BoardCreateRequest request =
          new BoardCreateRequest(1, "김", "제목입니다", "내용은충분히깁니다", "abc123!@#");

      assertThatThrownBy(() -> boardService.createBoard(request))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("작성자는 3자 이상 5자 미만이어야 합니다.");

      verify(boardMapper, never()).insert(any(Board.class));
    }

    @Test
    @DisplayName("비밀번호에 특수문자가 없으면 예외가 발생한다")
    void failsWhenPasswordMissingSpecialChar() {
      BoardCreateRequest request =
          new BoardCreateRequest(1, "김철수", "제목입니다", "내용은충분히깁니다", "abc12345");

      assertThatThrownBy(() -> boardService.createBoard(request))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("비밀번호는 영문, 숫자, 특수문자를 모두 포함해야 합니다.");
    }

    @Test
    @DisplayName("제목이 4자 미만이면 예외가 발생한다")
    void failsWhenTitleTooShort() {
      BoardCreateRequest request =
          new BoardCreateRequest(1, "김철수", "제목", "내용은충분히깁니다", "abc123!@#");

      assertThatThrownBy(() -> boardService.createBoard(request))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("제목은 4자 이상 100자 미만이어야 합니다.");
    }

    @Test
    @DisplayName("내용이 4자 미만이면 예외가 발생한다")
    void failsWhenContentTooShort() {
      BoardCreateRequest request =
          new BoardCreateRequest(1, "김철수", "제목입니다", "내용", "abc123!@#");

      assertThatThrownBy(() -> boardService.createBoard(request))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("내용은 4자 이상 2000자 미만이어야 합니다.");
    }

    @Test
    @DisplayName("존재하지 않는 카테고리면 예외가 발생한다")
    void failsWhenCategoryNotFound() {
      BoardCreateRequest request =
          new BoardCreateRequest(999, "김철수", "제목입니다", "내용은충분히깁니다", "abc123!@#");
      when(categoryMapper.findById(999)).thenReturn(null);

      assertThatThrownBy(() -> boardService.createBoard(request))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("존재하지 않는 카테고리입니다.");

      verify(boardMapper, never()).insert(any(Board.class));
    }
  }

  @Nested
  @DisplayName("게시글 상세 조회")
  class GetBoardDetail {

    @Test
    @DisplayName("존재하지 않는 게시글이면 예외가 발생한다")
    void failsWhenNotFound() {
      when(boardMapper.findById(999)).thenReturn(null);

      assertThatThrownBy(() -> boardService.getBoardDetail(999))
          .isInstanceOf(NoSuchElementException.class)
          .hasMessage("게시글을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("조회수를 증가시키고, 응답의 조회수엔 증가분(+1)이 즉시 반영된다")
    void increasesViewCountAndReflectsInResponse() {
      // given: DB엔 조회수 5로 저장돼 있다고 가정. increaseViewCount를 호출해도 이 board
      // 객체 자체는 갱신되지 않는 상황(실제 버그였던 지점)을 그대로 재현한 것
      Board board = new Board(1, 1, "제목", "김철수", "내용", "pw", 5,
          LocalDateTime.now(), null, null);
      when(boardMapper.findById(1)).thenReturn(board);
      when(categoryMapper.findById(1)).thenReturn(new Category(1, "공지"));
      when(attachmentService.getAttachments(1)).thenReturn(List.of());

      // when
      BoardDetailResponse response = boardService.getBoardDetail(1);

      // then: board 객체는 여전히 5지만, 응답엔 Service가 +1을 직접 더해서 6이 나가야 함
      assertThat(response.viewCount()).isEqualTo(6);
      assertThat(response.categoryName()).isEqualTo("공지");
      verify(boardMapper).increaseViewCount(1);
    }

    @Test
    @DisplayName("첨부파일이 있으면 응답에 목록이 포함된다")
    void includesAttachments() {
      Board board = new Board(1, 1, "제목", "김철수", "내용", "pw", 0,
          LocalDateTime.now(), null, null);
      when(boardMapper.findById(1)).thenReturn(board);
      when(categoryMapper.findById(1)).thenReturn(new Category(1, "공지"));
      when(attachmentService.getAttachments(1)).thenReturn(
          List.of(new Attachment(1, 1, "파일.txt", "uuid.txt", "/uploads/uuid.txt", 100L, "txt")));

      BoardDetailResponse response = boardService.getBoardDetail(1);

      assertThat(response.attachments()).hasSize(1);
      assertThat(response.attachments().get(0).originName()).isEqualTo("파일.txt");
    }
  }

  @Nested
  @DisplayName("게시글 수정")
  class UpdateBoard {

    @Test
    @DisplayName("존재하지 않는 게시글이면 예외가 발생한다")
    void failsWhenNotFound() {
      BoardUpdateRequest request =
          new BoardUpdateRequest("김철수", "제목입니다", "내용은충분히깁니다", "abc123!@#");
      when(boardMapper.findById(1)).thenReturn(null);

      assertThatThrownBy(() -> boardService.updateBoard(1, request))
          .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 예외가 발생하고 수정하지 않는다")
    void failsWhenPasswordMismatch() {
      BoardUpdateRequest request =
          new BoardUpdateRequest("김철수", "제목입니다", "내용은충분히깁니다", "wrongpw!1");
      Board board = new Board(1, 1, "원래제목", "원작성자", "원내용", "abc123!@#", 0,
          LocalDateTime.now(), null, null);
      when(boardMapper.findById(1)).thenReturn(board);

      assertThatThrownBy(() -> boardService.updateBoard(1, request))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("비밀번호가 일치하지 않습니다.");

      verify(boardMapper, never()).update(any(Board.class));
    }

    @Test
    @DisplayName("비밀번호가 일치하면 작성자/제목/내용만 변경하고 카테고리는 그대로 둔다")
    void successUpdatesOnlyAllowedFields() {
      BoardUpdateRequest request =
          new BoardUpdateRequest("박영희", "수정된 제목", "수정된 내용입니다", "abc123!@#");
      Board board = new Board(1, 1, "원래제목", "원작성자", "원내용", "abc123!@#", 0,
          LocalDateTime.now(), null, null);
      when(boardMapper.findById(1)).thenReturn(board);

      boardService.updateBoard(1, request);

      assertThat(board.getWriter()).isEqualTo("박영희");
      assertThat(board.getTitle()).isEqualTo("수정된 제목");
      assertThat(board.getContent()).isEqualTo("수정된 내용입니다");
      assertThat(board.getCategoryId()).isEqualTo(1);   // 카테고리는 수정 대상이 아니라 그대로여야 함
      verify(boardMapper).update(board);
    }
  }

  @Nested
  @DisplayName("게시글 삭제")
  class DeleteBoard {

    @Test
    @DisplayName("존재하지 않는 게시글이면 예외가 발생한다")
    void failsWhenNotFound() {
      when(boardMapper.findById(1)).thenReturn(null);

      assertThatThrownBy(() -> boardService.deleteBoard(1, new BoardDeleteRequest("abc123!@#")))
          .isInstanceOf(NoSuchElementException.class);

      verify(boardMapper, never()).delete(any());
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 아무것도 지우지 않는다")
    void failsWhenPasswordMismatch() {
      Board board = new Board(1, 1, "제목", "작성자", "내용", "abc123!@#", 0,
          LocalDateTime.now(), null, null);
      when(boardMapper.findById(1)).thenReturn(board);

      assertThatThrownBy(() -> boardService.deleteBoard(1, new BoardDeleteRequest("wrongpw!1")))
          .isInstanceOf(IllegalArgumentException.class);

      verify(boardMapper, never()).delete(any());
      verify(attachmentService, never()).deleteFilesByBoardId(any());
    }

    @Test
    @DisplayName("비밀번호가 일치하면 첨부파일을 먼저 정리하고 나서 게시글을 삭제한다")
    void successDeletesFilesBeforeBoard() {
      // 첨부파일 디스크 정리를 게시글 삭제보다 먼저 해야 한다는 게 예전에 겪었던
      // "고아 파일" 버그의 핵심이라, 순서까지 InOrder로 검증한다.
      Board board = new Board(1, 1, "제목", "작성자", "내용", "abc123!@#", 0,
          LocalDateTime.now(), null, null);
      when(boardMapper.findById(1)).thenReturn(board);

      boardService.deleteBoard(1, new BoardDeleteRequest("abc123!@#"));

      InOrder inOrder = Mockito.inOrder(attachmentService, boardMapper);
      inOrder.verify(attachmentService).deleteFilesByBoardId(1);
      inOrder.verify(boardMapper).delete(1);
    }
  }

  @Nested
  @DisplayName("게시글 목록 조회")
  class GetBoardList {

    @Test
    @DisplayName("페이지 번호가 1 미만이면 예외가 발생한다")
    void failsWhenPageLessThanOne() {
      BoardSearchRequest condition = new BoardSearchRequest(null, null, null, null, 0, 10);

      assertThatThrownBy(() -> boardService.getBoardList(condition))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("페이지 번호는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("페이지당 건수가 1 미만이면 예외가 발생한다")
    void failsWhenSizeLessThanOne() {
      BoardSearchRequest condition = new BoardSearchRequest(null, null, null, null, 1, 0);

      assertThatThrownBy(() -> boardService.getBoardList(condition))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("페이지당 건수는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("카테고리명을 매칭하고, 첨부파일 존재 여부를 boolean으로 변환해서 응답한다")
    void mapsCategoryNameAndHasAttachment() {
      BoardSearchRequest condition = new BoardSearchRequest(null, null, null, null, 1, 10);
      Board board = new Board(1, 1, "제목", "작성자", "내용", "pw", 0,
          LocalDateTime.now(), null, true);
      when(boardMapper.findAll(condition)).thenReturn(List.of(board));
      when(boardMapper.countAll(condition)).thenReturn(1);
      when(categoryMapper.findAll()).thenReturn(List.of(new Category(1, "공지")));

      BoardListResponse response = boardService.getBoardList(condition);

      assertThat(response.totalCount()).isEqualTo(1);
      assertThat(response.boards()).hasSize(1);
      assertThat(response.boards().get(0).categoryName()).isEqualTo("공지");
      assertThat(response.boards().get(0).hasAttachment()).isTrue();
    }

    @Test
    @DisplayName("제목이 80자를 넘으면 잘라서 '...'을 붙인다")
    void truncatesLongTitle() {
      BoardSearchRequest condition = new BoardSearchRequest(null, null, null, null, 1, 10);
      String longTitle = "가".repeat(90);
      Board board = new Board(1, 1, longTitle, "작성자", "내용", "pw", 0,
          LocalDateTime.now(), null, false);
      when(boardMapper.findAll(condition)).thenReturn(List.of(board));
      when(boardMapper.countAll(condition)).thenReturn(1);
      when(categoryMapper.findAll()).thenReturn(List.of(new Category(1, "공지")));

      BoardListResponse response = boardService.getBoardList(condition);

      String resultTitle = response.boards().get(0).title();
      assertThat(resultTitle).hasSize(83);   // 80자 + "..."
      assertThat(resultTitle).endsWith("...");
    }
  }
}
