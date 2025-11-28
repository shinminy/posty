package com.posty.postingapi.service.application;

import com.posty.postingapi.domain.account.Account;
import com.posty.postingapi.domain.account.AccountRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    private final WriterCacheManager writerCacheManager;

    private final MediaService mediaService;
    private final MediaEventPublisher mediaEventPublisher;

    public SeriesService(
            SeriesRepository seriesRepository,
            PostRepository postRepository,
            AccountRepository accountRepository,
            WriterCacheManager writerCacheManager,
            MediaService mediaService,
            MediaEventPublisher mediaEventPublisher
    ) {
        this.seriesRepository = seriesRepository;
        this.postRepository = postRepository;
        this.accountRepository = accountRepository;

        this.writerCacheManager = writerCacheManager;

        this.mediaService = mediaService;
        this.mediaEventPublisher = mediaEventPublisher;
    }

    private Series findSeriesById(Long seriesId) {
        return seriesRepository.findById(seriesId)
                .orElseThrow(() -> new ResourceNotFoundException("Series", seriesId));
    }

    public SeriesDetailResponse getSeriesDetail(Long seriesId, int page, int size) {
        Series series = findSeriesById(seriesId);

        List<String> writers = writerCacheManager.loadWritersOfSeries(seriesId);

        PageRequest pageable = PageRequest.of(page-1, size);
        Page<Post> postData = postRepository.findAllBySeriesId(seriesId, pageable);

        List<PostSummary> posts = postData.stream()
                .map(PostMapper::toPostSummary)
                .toList();

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

        return SeriesMapper.toSeriesDetailResponse(saved, new ArrayList<>(), new ArrayList<>());
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

        seriesRepository.delete(series);

        writerCacheManager.clearWritersOfSeries(seriesId);

        List<Media> waitingMediaList = mediaService.deleteOrPrepareMediaForDeletion(mediaList);
        waitingMediaList.forEach(mediaEventPublisher::publishMediaDelete);
    }
}
