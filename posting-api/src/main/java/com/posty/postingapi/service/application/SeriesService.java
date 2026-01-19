package com.posty.postingapi.service.application;

import com.posty.postingapi.domain.account.Account;
import com.posty.postingapi.domain.account.AccountRepository;
import com.posty.postingapi.domain.comment.CommentRepository;
import com.posty.postingapi.domain.post.event.MediaChangedEvent;
import com.posty.postingapi.domain.series.event.SeriesChangedEvent;
import com.posty.postingapi.dto.series.SeriesSummary;
import com.posty.postingapi.infrastructure.cache.WriterCacheManager;
import com.posty.postingapi.domain.post.*;
import com.posty.postingapi.domain.series.Series;
import com.posty.postingapi.domain.series.SeriesRepository;
import com.posty.postingapi.dto.series.SeriesCreateRequest;
import com.posty.postingapi.dto.series.SeriesDetailResponse;
import com.posty.postingapi.dto.post.PostSummary;
import com.posty.postingapi.dto.series.SeriesUpdateRequest;
import com.posty.postingapi.error.ResourceNotFoundException;
import com.posty.postingapi.mapper.PostMapper;
import com.posty.postingapi.mapper.SeriesMapper;
import com.posty.postingapi.properties.PaginationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class SeriesService {

    private final SeriesRepository seriesRepository;
    private final PostRepository postRepository;
    private final AccountRepository accountRepository;
    private final CommentRepository commentRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final WriterCacheManager writerCacheManager;

    private final MediaService mediaService;

    private final int defaultPage;
    private final int defaultPageSize;

    public SeriesService(
            SeriesRepository seriesRepository,
            PostRepository postRepository,
            AccountRepository accountRepository,
            CommentRepository commentRepository,
            ApplicationEventPublisher applicationEventPublisher,
            WriterCacheManager writerCacheManager,
            MediaService mediaService,
            PaginationProperties paginationProperties
    ) {
        this.seriesRepository = seriesRepository;
        this.postRepository = postRepository;
        this.accountRepository = accountRepository;
        this.commentRepository = commentRepository;

        this.applicationEventPublisher = applicationEventPublisher;

        this.writerCacheManager = writerCacheManager;

        this.mediaService = mediaService;

        defaultPage = paginationProperties.getDefaultPage();
        defaultPageSize = paginationProperties.getDefaultSize();
    }

    private Series findSeriesById(Long seriesId) {
        return seriesRepository.findById(seriesId)
                .orElseThrow(() -> new ResourceNotFoundException("Series", seriesId));
    }

    private void publishMediaDeletedEvents(List<Media> mediaList) {
        mediaList.stream()
                .map(media -> new MediaChangedEvent(
                        media.getId(),
                        MediaChangedEvent.MediaChangeType.DELETED
                ))
                .forEach(applicationEventPublisher::publishEvent);
    }

    private void deleteMediaAndPublishEvents(List<Media> mediaList) {
        List<Media> waitingMediaList = mediaService.deleteOrPrepareMediaForDeletion(mediaList);
        publishMediaDeletedEvents(waitingMediaList);
    }

    public SeriesDetailResponse getSeriesDetail(Long seriesId, Pageable pageable) {
        Series series = findSeriesById(seriesId);

        List<String> writers = writerCacheManager.loadWritersOfSeries(seriesId);

        Page<PostSummary> posts = postRepository.findAllBySeriesId(seriesId, pageable)
                .map(PostMapper::toPostSummary);

        return SeriesMapper.toSeriesDetailResponse(series, writers, posts);
    }

    @Transactional
    public SeriesDetailResponse createSeries(SeriesCreateRequest request) {
        request.normalize();

        Set<Account> managers = findManagersByIds(request.getManagerIds());

        Series series = SeriesMapper.toEntity(request, managers);
        Series saved = seriesRepository.save(series);

        return SeriesMapper.toSeriesDetailResponse(
                saved,
                new ArrayList<>(),
                getEmptyPostPage()
        );
    }

    private Set<Account> findManagersByIds(List<Long> managerIds) {
        List<Account> managerList = accountRepository.findNonDeletedByIdIn(managerIds);
        if (managerList.isEmpty()) {
            throw new ResourceNotFoundException("Account", managerIds);
        }

        return new HashSet<>(managerList);
    }

    private Page<PostSummary> getEmptyPostPage() {
        return new PageImpl<>(
                new ArrayList<>(),
                PageRequest.of(defaultPage, defaultPageSize, Sort.by(Sort.Direction.DESC, "id")),
                0
        );
    }

    @Transactional
    public void updateSeries(Long seriesId, SeriesUpdateRequest request) {
        Series series = findSeriesById(seriesId);
        request.normalize();

        List<Long> managerIds = request.getManagerIds();
        Set<Account> managers = managerIds == null
                ? series.getManagers()
                : findManagersByIds(managerIds);

        series.updateInfo(request, managers);
        seriesRepository.save(series);

        applicationEventPublisher.publishEvent(new SeriesChangedEvent(seriesId));
    }

    @Transactional
    public void deleteSeries(Long seriesId) {
        Series series = findSeriesById(seriesId);

        List<Media> mediaList = mediaService.findMediaBySeriesId(seriesId);

        commentRepository.deleteAllBySeriesId(seriesId);
        seriesRepository.delete(series);

        applicationEventPublisher.publishEvent(new SeriesChangedEvent(seriesId));
        deleteMediaAndPublishEvents(mediaList);
    }

    public Page<SeriesSummary> getSeriesByManager(Long accountId, Pageable pageable) {
        if (!accountRepository.existsNonDeletedById(accountId)) {
            throw new ResourceNotFoundException("Account", accountId);
        }

        Page<Series> series = seriesRepository.findByManagersId(accountId, pageable);
        return series.map(SeriesMapper::toSeriesSummary);
    }
}
