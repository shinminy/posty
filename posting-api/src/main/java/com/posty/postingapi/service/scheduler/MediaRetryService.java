package com.posty.postingapi.service.scheduler;

import com.posty.postingapi.config.SchedulerConfig;
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

    @Transactional
    public void retryFailedUploads() {
        List<Media> failedMedia = mediaRepository.findFailedMediaForRetry(maxRetryCount);
        for (Media media : failedMedia) {
            Long mediaId = media.getId();

            mediaRepository.save(media.pending());
            mediaEventPublisher.publishMediaUpload(mediaId);

            log.debug("Re-sent media {} to upload queue", mediaId);
        }
    }
}
