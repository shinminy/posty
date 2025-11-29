package com.posty.postingapi.service.scheduler;

import com.posty.postingapi.properties.SchedulerProperties;
import com.posty.postingapi.domain.post.*;
import com.posty.postingapi.mq.MediaEventPublisher;
import com.posty.postingapi.service.application.MediaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class MediaRetryService {

    private final MediaService mediaService;
    private final MediaEventPublisher mediaEventPublisher;

    private final int maxUploadAttemptCount;
    private final int maxDeletionAttemptCount;

    public MediaRetryService(
            MediaService mediaService,
            MediaEventPublisher mediaEventPublisher,
            SchedulerProperties schedulerProperties
    ) {
        this.mediaService = mediaService;
        this.mediaEventPublisher = mediaEventPublisher;

        SchedulerProperties.MediaSchedulerConfig.MediaRetryConfig retryConfig = schedulerProperties.getMedia().getRetry();
        maxUploadAttemptCount = retryConfig.getUpload().getMaxAttemptCount();
        maxDeletionAttemptCount = retryConfig.getDelete().getMaxAttemptCount();
    }

    public void retryFailedUploads() {
        List<Media> preparedList = mediaService.prepareMediaForUploadRetry(maxUploadAttemptCount);

        List<Long> publishedIds = preparedList.stream()
                .peek(mediaEventPublisher::publishMediaUpload)
                .map(Media::getId)
                .toList();

        log.debug("Re-sent media to upload queue: {}", publishedIds);
    }

    public void retryFailedDeletions() {
        List<Media> preparedList = mediaService.prepareMediaForDeletionRetry(maxDeletionAttemptCount);

        List<Long> publishedIds = preparedList.stream()
                .peek(mediaEventPublisher::publishMediaDelete)
                .map(Media::getId)
                .toList();

        log.debug("Re-sent media to deletion queue: {}", publishedIds);
    }
}
