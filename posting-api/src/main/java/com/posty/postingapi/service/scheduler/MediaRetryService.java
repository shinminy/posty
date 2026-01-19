package com.posty.postingapi.service.scheduler;

import com.posty.postingapi.domain.post.event.MediaChangedEvent;
import com.posty.postingapi.properties.SchedulerProperties;
import com.posty.postingapi.domain.post.*;
import com.posty.postingapi.service.application.MediaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class MediaRetryService {

    private final MediaService mediaService;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final int maxUploadAttemptCount;
    private final int maxDeletionAttemptCount;

    public MediaRetryService(
            MediaService mediaService,
            ApplicationEventPublisher applicationEventPublisher,
            SchedulerProperties schedulerProperties
    ) {
        this.mediaService = mediaService;

        this.applicationEventPublisher = applicationEventPublisher;

        SchedulerProperties.MediaSchedulerProperties.MediaRetryProperties retryProperties = schedulerProperties.getMedia().getRetry();
        maxUploadAttemptCount = retryProperties.getUpload().getMaxAttemptCount();
        maxDeletionAttemptCount = retryProperties.getDelete().getMaxAttemptCount();
    }

    @Transactional
    public List<Long> retryFailedUploads() {
        List<Media> mediaList = mediaService.prepareMediaForUploadRetry(maxUploadAttemptCount);
        List<Long> mediaIds = mediaList.stream().map(Media::getId).toList();

        mediaIds.stream()
                .map(id -> new MediaChangedEvent(
                        id,
                        MediaChangedEvent.MediaChangeType.CREATED
                ))
                .forEach(applicationEventPublisher::publishEvent);

        return mediaIds;
    }

    @Transactional
    public List<Long> retryFailedDeletions() {
        List<Media> mediaList = mediaService.prepareMediaForDeletionRetry(maxDeletionAttemptCount);
        List<Long> mediaIds = mediaList.stream().map(Media::getId).toList();

        mediaIds.stream()
                .map(id -> new MediaChangedEvent(
                        id,
                        MediaChangedEvent.MediaChangeType.DELETED
                ))
                .forEach(applicationEventPublisher::publishEvent);

        return mediaIds;
    }
}
