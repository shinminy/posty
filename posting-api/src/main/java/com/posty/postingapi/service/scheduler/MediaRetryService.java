package com.posty.postingapi.service.scheduler;

import com.posty.postingapi.properties.SchedulerConfig;
import com.posty.postingapi.domain.post.*;
import com.posty.postingapi.mq.MediaEventPublisher;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class MediaRetryService {

    private final MediaRepository mediaRepository;

    private final MediaEventPublisher mediaEventPublisher;

    private final int maxRetryCount;

    public MediaRetryService(
            MediaRepository mediaRepository,
            MediaEventPublisher mediaEventPublisher,
            SchedulerConfig schedulerConfig
    ) {
        this.mediaRepository = mediaRepository;

        this.mediaEventPublisher = mediaEventPublisher;

        maxRetryCount = schedulerConfig.getMedia().getRetry().getMaxCount();
    }

    public void retryFailedUploads() {
        List<Media> failedMedia = mediaRepository.findFailedMediaForRetry(maxRetryCount);

        List<Long> pendingMediaIds = pendingMedia(failedMedia).stream()
                .map(Media::getId)
                .toList();

        for (Long mediaId : pendingMediaIds) {
            mediaEventPublisher.publishMediaUpload(mediaId);
        }
        log.debug("Re-sent media to upload queue: {}", pendingMediaIds);
    }

    @Transactional
    public List<Media> pendingMedia(List<Media> failedMedia) {
        return failedMedia.stream()
                .map(Media::pending)
                .map(mediaRepository::save)
                .toList();
    }
}
