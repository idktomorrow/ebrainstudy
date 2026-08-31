package com.study2.practice.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.study2.practice.board.entity.Board;
import com.study2.practice.board.mapper.BoardMapper;
import com.study2.practice.comment.dto.request.CommentCreateRequest;
import com.study2.practice.comment.dto.response.CommentResponse;
import com.study2.practice.comment.entity.Comment;
import com.study2.practice.comment.mapper.CommentMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CommentService 단위 테스트.
 * BoardMapper는 "댓글을 달 게시글이 실제로 존재하는지" 확인용으로만 mock 처리한다.
 */
@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

  @Mock
  private CommentMapper commentMapper;
  @Mock
  private BoardMapper boardMapper;

  @InjectMocks
  private CommentService commentService;

  @Nested
  @DisplayName("댓글 등록")
  class CreateComment {

    @Test
    @DisplayName("존재하지 않는 게시글이면 예외가 발생하고 등록을 시도하지 않는다")
    void failsWhenBoardNotFound() {
      when(boardMapper.findById(999)).thenReturn(null);
      CommentCreateRequest request = new CommentCreateRequest("이순신", "좋은 글이네요");

      assertThatThrownBy(() -> commentService.createComment(999, request))
          .isInstanceOf(NoSuchElementException.class)
          .hasMessage("게시글을 찾을 수 없습니다.");

      verify(commentMapper, never()).insert(any(Comment.class));
    }

    @Test
    @DisplayName("작성자가 공백이면 예외가 발생한다")
    void failsWhenWriterBlank() {
      when(boardMapper.findById(1)).thenReturn(new Board());
      CommentCreateRequest request = new CommentCreateRequest("   ", "좋은 글이네요");

      assertThatThrownBy(() -> commentService.createComment(1, request))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("작성자를 입력해주세요.");
    }

    @Test
    @DisplayName("작성자가 50자를 넘으면 예외가 발생한다 (writer 컬럼이 VARCHAR(50)이라 DB 에러 방지용)")
    void failsWhenWriterTooLong() {
      when(boardMapper.findById(1)).thenReturn(new Board());
      CommentCreateRequest request = new CommentCreateRequest("a".repeat(51), "좋은 글이네요");

      assertThatThrownBy(() -> commentService.createComment(1, request))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("작성자는 50자 이하여야 합니다.");
    }

    @Test
    @DisplayName("내용이 공백이면 예외가 발생한다")
    void failsWhenContentBlank() {
      when(boardMapper.findById(1)).thenReturn(new Board());
      CommentCreateRequest request = new CommentCreateRequest("이순신", "   ");

      assertThatThrownBy(() -> commentService.createComment(1, request))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("댓글 내용을 입력해주세요.");
    }

    @Test
    @DisplayName("내용이 2000자를 넘으면 예외가 발생한다")
    void failsWhenContentTooLong() {
      when(boardMapper.findById(1)).thenReturn(new Board());
      CommentCreateRequest request = new CommentCreateRequest("이순신", "a".repeat(2001));

      assertThatThrownBy(() -> commentService.createComment(1, request))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("댓글 내용은 2000자 이하여야 합니다.");
    }

    @Test
    @DisplayName("모든 조건을 만족하면 댓글을 등록하고 생성된 id를 반환한다")
    void success() {
      when(boardMapper.findById(1)).thenReturn(new Board());
      CommentCreateRequest request = new CommentCreateRequest("이순신", "좋은 글이네요");
      // insert는 원래 useGeneratedKeys로 DB가 id를 채워주는데, mock에선 직접 흉내낸다
      doAnswer(invocation -> {
        Comment comment = invocation.getArgument(0);
        comment.setId(7);
        return null;
      }).when(commentMapper).insert(any(Comment.class));

      Integer id = commentService.createComment(1, request);

      assertThat(id).isEqualTo(7);
      verify(commentMapper).insert(any(Comment.class));
    }
  }

  @Nested
  @DisplayName("댓글 목록 조회")
  class GetComments {

    @Test
    @DisplayName("존재하지 않는 게시글이면 예외가 발생한다")
    void failsWhenBoardNotFound() {
      when(boardMapper.findById(999)).thenReturn(null);

      assertThatThrownBy(() -> commentService.getComments(999))
          .isInstanceOf(NoSuchElementException.class)
          .hasMessage("게시글을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("댓글 목록을 조회하면 Entity가 Response DTO로 변환되어 반환된다")
    void returnsMappedResponses() {
      when(boardMapper.findById(1)).thenReturn(new Board());
      LocalDateTime now = LocalDateTime.now();
      List<Comment> comments = List.of(
          new Comment(1, 1, "이순신", "첫 댓글", now),
          new Comment(2, 1, "강감찬", "둘째 댓글", now)
      );
      when(commentMapper.findByBoardId(1)).thenReturn(comments);

      List<CommentResponse> result = commentService.getComments(1);

      assertThat(result).hasSize(2);
      assertThat(result.get(0).writer()).isEqualTo("이순신");
      assertThat(result.get(0).content()).isEqualTo("첫 댓글");
      assertThat(result.get(1).writer()).isEqualTo("강감찬");
    }
  }
}
