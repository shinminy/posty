package com.posty.postingapi.service.application;

import com.posty.postingapi.domain.account.Account;
import com.posty.postingapi.domain.account.AccountRepository;
import com.posty.postingapi.domain.comment.Comment;
import com.posty.postingapi.domain.comment.CommentRepository;
import com.posty.postingapi.domain.post.Post;
import com.posty.postingapi.domain.post.PostRepository;
import com.posty.postingapi.dto.comment.CommentCreateRequest;
import com.posty.postingapi.dto.comment.CommentDetailResponse;
import com.posty.postingapi.dto.comment.CommentUpdateRequest;
import com.posty.postingapi.error.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private CommentService commentService;

    @Test
    @DisplayName("댓글 상세 조회 성공")
    void getCommentDetail_Success() {
        // given
        Long commentId = 1L;
        Account writer = Account.builder().id(1L).name("Writer").build();
        Post post = Post.builder().id(1L).title("Post").build();
        Comment comment = Comment.builder().id(commentId).content("Comment").writer(writer).post(post).build();

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when
        CommentDetailResponse response = commentService.getCommentDetail(commentId);

        // then
        assertThat(response.getId()).isEqualTo(commentId);
        assertThat(response.getContent()).isEqualTo("Comment");
        assertThat(response.getWriter().getName()).isEqualTo("Writer");
        assertThat(response.getPost().getTitle()).isEqualTo("Post");
    }

    @Test
    @DisplayName("댓글 조회 실패 - 존재하지 않는 댓글")
    void getCommentDetail_NotFound() {
        // given
        Long commentId = 1L;
        given(commentRepository.findById(commentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.getCommentDetail(commentId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("댓글 생성 성공")
    void createComment_Success() {
        // given
        Long postId = 1L;
        Long writerId = 1L;
        CommentCreateRequest request = new CommentCreateRequest(postId, "Test Comment", writerId);
        
        Post post = Post.builder().id(postId).title("Post").build();
        Account writer = Account.builder().id(writerId).name("Writer").build();

        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(accountRepository.findNonDeletedById(writerId)).willReturn(Optional.of(writer));

        // when
        CommentDetailResponse response = commentService.createComment(request);

        // then
        assertThat(response.getContent()).isEqualTo("Test Comment");
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("댓글 수정 성공")
    void updateComment_Success() {
        // given
        Long commentId = 1L;
        Comment comment = Comment.builder().id(commentId).content("Old Content").build();
        CommentUpdateRequest request = new CommentUpdateRequest("New Content");
        
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when
        commentService.updateComment(commentId, request);

        // then
        assertThat(comment.getContent()).isEqualTo("New Content");
        verify(commentRepository).save(comment);
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    void deleteComment_Success() {
        // given
        Long commentId = 1L;
        Comment comment = Comment.builder().id(commentId).build();
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when
        commentService.deleteComment(commentId);

        // then
        verify(commentRepository).delete(comment);
    }

    @Test
    @DisplayName("포스트별 댓글 목록 조회 성공")
    void getCommentsByPost_Success() {
        // given
        Long postId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Post post = Post.builder().id(postId).title("Post Title").build();
        Account writer = Account.builder().id(1L).name("Writer").build();
        Comment comment = Comment.builder().id(1L).content("Comment").writer(writer).post(post).build();
        Page<Comment> commentPage = new PageImpl<>(List.of(comment), pageable, 1);

        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(commentRepository.findAllByPostId(postId, pageable)).willReturn(commentPage);

        // when
        Page<CommentDetailResponse> result = commentService.getCommentsByPost(postId, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getContent()).isEqualTo("Comment");
        assertThat(result.getContent().get(0).getPost().getTitle()).isEqualTo("Post Title");
    }

    @Test
    @DisplayName("포스트별 댓글 목록 조회 실패 - 존재하지 않는 포스트")
    void getCommentsByPost_NotFound() {
        // given
        Long postId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        given(postRepository.findById(postId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.getCommentsByPost(postId, pageable))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("계정별 댓글 목록 조회 성공")
    void getCommentsByAccount_Success() {
        // given
        Long accountId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Account account = Account.builder().id(accountId).name("Tester").build();
        Post post = Post.builder().id(1L).title("Post Title").build();
        Comment comment = Comment.builder().id(1L).content("Comment").writer(account).post(post).build();
        Page<Comment> commentPage = new PageImpl<>(List.of(comment), pageable, 1);

        given(accountRepository.findNonDeletedById(accountId)).willReturn(Optional.of(account));
        given(commentRepository.findAllByWriterId(accountId, pageable)).willReturn(commentPage);

        // when
        Page<CommentDetailResponse> result = commentService.getCommentsByAccount(accountId, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getContent()).isEqualTo("Comment");
        assertThat(result.getContent().get(0).getWriter().getName()).isEqualTo("Tester");
    }

    @Test
    @DisplayName("계정별 댓글 목록 조회 실패 - 존재하지 않는 계정")
    void getCommentsByAccount_NotFound() {
        // given
        Long accountId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        given(accountRepository.findNonDeletedById(accountId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.getCommentsByAccount(accountId, pageable))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
