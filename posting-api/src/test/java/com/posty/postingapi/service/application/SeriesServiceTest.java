package com.posty.postingapi.service.application;

import com.posty.postingapi.domain.account.Account;
import com.posty.postingapi.domain.account.AccountRepository;
import com.posty.postingapi.domain.comment.CommentRepository;
import com.posty.postingapi.domain.post.PostRepository;
import com.posty.postingapi.domain.series.Series;
import com.posty.postingapi.domain.series.SeriesRepository;
import com.posty.postingapi.dto.series.SeriesCreateRequest;
import com.posty.postingapi.dto.series.SeriesDetailResponse;
import com.posty.postingapi.dto.series.SeriesUpdateRequest;
import com.posty.postingapi.dto.series.SeriesSummary;
import com.posty.postingapi.error.ResourceNotFoundException;
import com.posty.postingapi.infrastructure.cache.WriterCacheManager;
import com.posty.postingapi.mq.MediaEventPublisher;
import com.posty.postingapi.properties.PaginationProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SeriesServiceTest {

    @Mock
    private SeriesRepository seriesRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private WriterCacheManager writerCacheManager;

    @Mock
    private MediaService mediaService;

    @Mock
    private MediaEventPublisher mediaEventPublisher;

    @Mock
    private PaginationProperties paginationProperties;

    private SeriesService seriesService;

    @BeforeEach
    void setUp() {
        given(paginationProperties.getDefaultPage()).willReturn(0);
        given(paginationProperties.getDefaultSize()).willReturn(10);

        seriesService = new SeriesService(
                seriesRepository, postRepository, accountRepository, commentRepository,
                writerCacheManager, mediaService, mediaEventPublisher,
                paginationProperties
        );
    }

    @Test
    @DisplayName("시리즈 상세 조회 성공")
    void getSeriesDetail_Success() {
        // given
        Long seriesId = 1L;
        Series series = Series.builder().id(seriesId).title("Series Title").build();
        List<String> writers = List.of("Writer");
        Pageable pageable = PageRequest.of(0, 10);
        Page<com.posty.postingapi.domain.post.Post> postData = new PageImpl<>(Collections.emptyList());

        given(seriesRepository.findById(seriesId)).willReturn(Optional.of(series));
        given(writerCacheManager.loadWritersOfSeries(seriesId)).willReturn(writers);
        given(postRepository.findAllBySeriesId(eq(seriesId), any(Pageable.class))).willReturn(postData);

        // when
        SeriesDetailResponse response = seriesService.getSeriesDetail(seriesId, pageable);

        // then
        assertThat(response.getId()).isEqualTo(seriesId);
        assertThat(response.getTitle()).isEqualTo("Series Title");
        assertThat(response.getWriters()).containsExactly("Writer");
    }

    @Test
    @DisplayName("시리즈 조회 실패 - 존재하지 않는 시리즈")
    void getSeriesDetail_NotFound() {
        // given
        Long seriesId = 1L;
        given(seriesRepository.findById(seriesId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> seriesService.getSeriesDetail(seriesId, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("시리즈 생성 성공")
    void createSeries_Success() {
        // given
        Long managerId = 1L;
        SeriesCreateRequest request = new SeriesCreateRequest("Series Title", "Description", List.of(managerId));
        Account manager = Account.builder().id(managerId).name("Manager").build();

        given(accountRepository.findNonDeletedByIdIn(List.of(managerId))).willReturn(List.of(manager));
        given(seriesRepository.save(any(Series.class)))
                .willAnswer(invocation -> invocation.<Series>getArgument(0));

        // when
        SeriesDetailResponse response = seriesService.createSeries(request);

        // then
        assertThat(response.getTitle()).isEqualTo("Series Title");
        verify(seriesRepository).save(any(Series.class));
    }

    @Test
    @DisplayName("시리즈 생성 실패 - 관리자 계정 없음")
    void createSeries_AccountNotFound() {
        // given
        Long managerId = 1L;
        SeriesCreateRequest request = new SeriesCreateRequest("Title", "Desc", List.of(managerId));

        given(accountRepository.findNonDeletedByIdIn(anyList())).willReturn(Collections.emptyList());

        // when & then
        assertThatThrownBy(() -> seriesService.createSeries(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("시리즈 수정 성공")
    void updateSeries_Success() {
        // given
        Long seriesId = 1L;
        Series series = Series.builder().id(seriesId).title("Old Title").build();
        SeriesUpdateRequest request = new SeriesUpdateRequest("New Title", "New Desc", List.of(1L));
        Account manager = Account.builder().id(1L).build();

        given(seriesRepository.findById(seriesId)).willReturn(Optional.of(series));
        given(accountRepository.findNonDeletedByIdIn(List.of(1L))).willReturn(List.of(manager));

        // when
        seriesService.updateSeries(seriesId, request);

        // then
        assertThat(series.getTitle()).isEqualTo("New Title");
        verify(seriesRepository).save(series);
        verify(writerCacheManager).clearWritersOfSeries(seriesId);
    }

    @Test
    @DisplayName("시리즈 수정 실패 - 시리즈 없음")
    void updateSeries_NotFound() {
        // given
        Long seriesId = 1L;
        SeriesUpdateRequest request = new SeriesUpdateRequest("Title", "Desc", List.of(1L));
        given(seriesRepository.findById(seriesId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> seriesService.updateSeries(seriesId, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("시리즈 수정 실패 - 관리자 계정 없음")
    void updateSeries_AccountNotFound() {
        // given
        Long seriesId = 1L;
        Series series = Series.builder().id(seriesId).build();
        SeriesUpdateRequest request = new SeriesUpdateRequest("Title", "Desc", List.of(999L));

        given(seriesRepository.findById(seriesId)).willReturn(Optional.of(series));
        given(accountRepository.findNonDeletedByIdIn(anyList())).willReturn(Collections.emptyList());

        // when & then
        assertThatThrownBy(() -> seriesService.updateSeries(seriesId, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("시리즈 삭제 성공")
    void deleteSeries_Success() {
        // given
        Long seriesId = 1L;
        Series series = Series.builder().id(seriesId).build();
        given(seriesRepository.findById(seriesId)).willReturn(Optional.of(series));
        given(mediaService.findMediaBySeriesId(seriesId)).willReturn(List.of());

        // when
        seriesService.deleteSeries(seriesId);

        // then
        verify(seriesRepository).delete(series);
        verify(writerCacheManager).clearWritersOfSeries(seriesId);
    }

    @Test
    @DisplayName("계정별 시리즈 목록 조회 성공")
    void getSeriesByManager_Success() {
        // given
        Long accountId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Series series = Series.builder().id(1L).title("Series").build();
        Page<Series> seriesPage = new PageImpl<>(List.of(series), pageable, 1);

        given(accountRepository.existsNonDeletedById(accountId)).willReturn(true);
        given(seriesRepository.findByManagersId(accountId, pageable)).willReturn(seriesPage);

        // when
        Page<SeriesSummary> result = seriesService.getSeriesByManager(accountId, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Series");
    }

    @Test
    @DisplayName("계정별 시리즈 목록 조회 실패 - 존재하지 않는 계정")
    void getSeriesByManager_AccountNotFound() {
        // given
        Long accountId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        given(accountRepository.existsNonDeletedById(accountId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> seriesService.getSeriesByManager(accountId, pageable))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
