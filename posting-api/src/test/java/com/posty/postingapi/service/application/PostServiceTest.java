package com.posty.postingapi.service.application;

import com.posty.postingapi.domain.account.Account;
import com.posty.postingapi.domain.account.AccountRepository;
import com.posty.postingapi.domain.comment.CommentRepository;
import com.posty.postingapi.domain.post.Post;
import com.posty.postingapi.domain.post.PostBlockRepository;
import com.posty.postingapi.domain.post.PostRepository;
import com.posty.postingapi.domain.post.PostBlock;
import com.posty.postingapi.domain.post.event.PostChangedEvent;
import com.posty.postingapi.domain.series.Series;
import com.posty.postingapi.domain.series.SeriesRepository;
import com.posty.postingapi.domain.series.event.SeriesChangedEvent;
import com.posty.postingapi.dto.post.*;
import com.posty.postingapi.error.ResourceNotFoundException;
import com.posty.postingapi.infrastructure.cache.WriterCacheManager;
import com.posty.postingapi.properties.PaginationProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostBlockRepository postBlockRepository;

    @Mock
    private SeriesRepository seriesRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private WriterCacheManager writerCacheManager;

    @Mock
    private MediaService mediaService;

    @Mock
    private PaginationProperties paginationProperties;

    private PostService postService;

    @BeforeEach
    void setUp() {
        given(paginationProperties.getDefaultPage()).willReturn(0);
        given(paginationProperties.getDefaultSize()).willReturn(10);
        
        postService = new PostService(
                postRepository, postBlockRepository, seriesRepository, accountRepository, commentRepository,
                applicationEventPublisher, writerCacheManager, mediaService,
                paginationProperties
        );
    }

    @Test
    @DisplayName("포스트 상세 조회 성공")
    void getPostDetail_Success() {
        // given
        Long postId = 1L;
        Post post = Post.builder().id(postId).title("Title").build();
        List<String> writers = List.of("Writer");
        Page<PostBlock> blockData = new PageImpl<>(List.of());

        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(writerCacheManager.loadWritersOfPosts(postId)).willReturn(writers);
        given(postBlockRepository.findAllByPostId(eq(postId), any(Pageable.class))).willReturn(blockData);

        // when
        PostDetailResponse response = postService.getPostDetail(postId, 0, 10);

        // then
        assertThat(response.getId()).isEqualTo(postId);
        assertThat(response.getTitle()).isEqualTo("Title");
        assertThat(response.getWriters()).containsExactly("Writer");
    }

    @Test
    @DisplayName("포스트 조회 실패 - 존재하지 않는 포스트")
    void getPostDetail_NotFound() {
        // given
        Long postId = 1L;
        given(postRepository.findById(postId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.getPostDetail(postId, 0, 10))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("포스트 생성 성공")
    void createPost_Success() {
        // given
        Long postId = 100L;
        Long seriesId = 1L;
        Long writerId = 1L;
        Series series = Series.builder().id(seriesId).build();
        Account writer = Account.builder().id(writerId).name("writer").build();
        
        PostCreateRequest request = new PostCreateRequest(
                seriesId,
                "Test Title",
                List.of(new PostBlockCreateRequest(1, writerId, new TextContentRequest("Test Content")))
        );
        request.normalize();

        given(seriesRepository.findById(seriesId)).willReturn(Optional.of(series));
        given(accountRepository.findNonDeletedById(writerId)).willReturn(Optional.of(writer));

        Post savedPost = Post.builder()
                .id(postId)
                .title(request.getTitle())
                .series(series)
                .build();
        given(postRepository.save(any(Post.class))).willReturn(savedPost);

        // when
        PostDetailResponse response = postService.createPost(request);

        // then
        assertThat(response.getId()).isEqualTo(postId);
        assertThat(response.getTitle()).isEqualTo(request.getTitle());
        verify(postRepository).save(any(Post.class));

        ArgumentCaptor<SeriesChangedEvent> eventCaptor = ArgumentCaptor.forClass(SeriesChangedEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().seriesId()).isEqualTo(seriesId);
    }

    @Test
    @DisplayName("포스트 생성 실패 - 시리즈 없음")
    void createPost_SeriesNotFound() {
        // given
        Long seriesId = 1L;
        PostCreateRequest request = new PostCreateRequest(seriesId, "Title", List.of());
        given(seriesRepository.findById(seriesId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.createPost(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("포스트 수정 성공")
    void updatePost_Success() {
        // given
        Long postId = 1L;
        Long seriesId = 1L;
        Post post = Post.builder().id(postId).series(Series.builder().id(seriesId).build()).build();
        PostUpdateRequest request = new PostUpdateRequest("Updated Title", null, null, null);
        request.normalize();

        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(postRepository.save(any(Post.class))).willReturn(post);

        // when
        postService.updatePost(postId, request);

        // then
        assertThat(post.getTitle()).isEqualTo(request.getTitle());
        verify(postRepository).save(post);

        ArgumentCaptor<PostChangedEvent> eventCaptor = ArgumentCaptor.forClass(PostChangedEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().postId()).isEqualTo(postId);
    }

    @Test
    @DisplayName("포스트 수정 실패 - 포스트 없음")
    void updatePost_NotFound() {
        // given
        Long postId = 1L;
        PostUpdateRequest request = new PostUpdateRequest("Title", null, null, null);
        given(postRepository.findById(postId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.updatePost(postId, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("포스트 삭제 성공")
    void deletePost_Success() {
        // given
        Long postId = 1L;
        Long seriesId = 1L;
        Post post = Post.builder().id(postId).series(Series.builder().id(seriesId).build()).build();
        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(mediaService.findMediaByPostId(postId)).willReturn(List.of());

        // when
        postService.deletePost(postId);

        // then
        verify(postRepository).delete(post);

        ArgumentCaptor<PostChangedEvent> eventCaptor = ArgumentCaptor.forClass(PostChangedEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().postId()).isEqualTo(postId);
    }

    @Test
    @DisplayName("작가의 포스트 목록 조회 성공")
    void getPostsByWriter_Success() {
        // given
        Long accountId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        given(accountRepository.existsNonDeletedById(accountId)).willReturn(true);
        given(postRepository.findAllByWriterId(eq(accountId), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of()));

        // when
        Page<PostSummary> result = postService.getPostsByWriter(accountId, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("작가의 포스트 목록 조회 실패 - 계정 없음")
    void getPostsByWriter_AccountNotFound() {
        // given
        Long accountId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        given(accountRepository.existsNonDeletedById(accountId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> postService.getPostsByWriter(accountId, pageable))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
