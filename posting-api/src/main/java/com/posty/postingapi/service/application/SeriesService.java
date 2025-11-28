package com.posty.postingapi.service.application;

import com.posty.postingapi.domain.account.Account;
import com.posty.postingapi.domain.account.AccountRepository;
import com.posty.postingapi.domain.comment.CommentRepository;
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
import com.posty.postingapi.mq.MediaEventPublisher;
import com.posty.postingapi.properties.PaginationConfig;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SeriesService {

    private final SeriesRepository seriesRepository;
    private final PostRepository postRepository;
    private final AccountRepository accountRepository;
    private final CommentRepository commentRepository;

    private final WriterCacheManager writerCacheManager;

    private final MediaService mediaService;
    private final MediaEventPublisher mediaEventPublisher;

    private final int defaultPage;
    private final int defaultPageSize;

    public SeriesService(
            SeriesRepository seriesRepository,
            PostRepository postRepository,
            AccountRepository accountRepository,
            CommentRepository commentRepository,
            WriterCacheManager writerCacheManager,
            MediaService mediaService,
            MediaEventPublisher mediaEventPublisher,
            PaginationConfig paginationConfig
    ) {
        this.seriesRepository = seriesRepository;
        this.postRepository = postRepository;
        this.accountRepository = accountRepository;
        this.commentRepository = commentRepository;

        this.writerCacheManager = writerCacheManager;

        this.mediaService = mediaService;
        this.mediaEventPublisher = mediaEventPublisher;

        defaultPage = paginationConfig.getDefaultPage();
        defaultPageSize = paginationConfig.getDefaultSize();
    }

    private Series findSeriesById(Long seriesId) {
        return seriesRepository.findById(seriesId)
                .orElseThrow(() -> new ResourceNotFoundException("Series", seriesId));
    }

    public SeriesDetailResponse getSeriesDetail(Long seriesId, Pageable pageable) {
        Series series = findSeriesById(seriesId);

        List<String> writers = writerCacheManager.loadWritersOfSeries(seriesId);

        Page<Post> postData = postRepository.findAllBySeriesId(seriesId, pageable);
        Page<PostSummary> posts = postData.map(PostMapper::toPostSummary);

        return SeriesMapper.toSeriesDetailResponse(series, writers, posts);
    }

    public SeriesDetailResponse createSeries(SeriesCreateRequest request) {
        request.normalize();

        List<Long> managerIds = request.getManagerIds();
        List<Account> managers = accountRepository.findNonDeletedByIdIn(managerIds);
        if (managers.isEmpty()) {
            throw new ResourceNotFoundException("Account", managerIds);
        }

        Series series = SeriesMapper.toEntity(request, new HashSet(managers));
        Series saved = seriesRepository.save(series);

        Page<PostSummary> emptyPosts = new PageImpl<>(
                new ArrayList<>(),
                PageRequest.of(defaultPage, defaultPageSize, Sort.by(Sort.Direction.DESC, "id")),
                0
        );

        return SeriesMapper.toSeriesDetailResponse(saved, new ArrayList<>(), emptyPosts);
    }

    public void updateSeries(Long seriesId, SeriesUpdateRequest request) {
        Series series = findSeriesById(seriesId);

        request.normalize();

        List<Long> managerIds = request.getManagerIds();
        Set<Account> managers;
        if (managerIds == null) {
            managers = series.getManagers();
        } else {
            List<Account> managerList = accountRepository.findNonDeletedByIdIn(managerIds);
            if (managerList.isEmpty()) {
                throw new ResourceNotFoundException("Account", managerIds);
            }

            managers = new HashSet<>(managerList);
        }

        series.updateInfo(request, managers);
        seriesRepository.save(series);

        writerCacheManager.clearWritersOfSeries(seriesId);
    }

    public void deleteSeries(Long seriesId) {
        Series series = findSeriesById(seriesId);

        List<Media> mediaList = mediaService.findMediaBySeriesId(seriesId);

        commentRepository.deleteAllBySeriesId(seriesId);
        seriesRepository.delete(series);

        writerCacheManager.clearWritersOfSeries(seriesId);

        List<Media> waitingMediaList = mediaService.deleteOrPrepareMediaForDeletion(mediaList);
        waitingMediaList.forEach(mediaEventPublisher::publishMediaDelete);
    }

    public Page<SeriesSummary> getSeriesByManager(Long accountId, Pageable pageable) {
        if (!accountRepository.existsNonDeletedById(accountId)) {
            throw new ResourceNotFoundException("Account", accountId);
        }

        Page<Series> series = seriesRepository.findByManagersId(accountId, pageable);
        return series.map(SeriesMapper::toSeriesSummary);
    }
}
