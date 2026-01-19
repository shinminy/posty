package com.posty.postingapi.scheduler;

import com.posty.postingapi.service.scheduler.MediaRetryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;


@Slf4j
@Component
public class MediaScheduler {

    private final MediaRetryService mediaRetryService;

    public MediaScheduler(MediaRetryService mediaRetryService) {
        this.mediaRetryService = mediaRetryService;
    }

    @Scheduled(cron = "${scheduler.media.retry.upload.cron}")
    public void runMediaUploadRetry() {
        List<Long> mediaIds = mediaRetryService.retryFailedUploads();

        log.info("{} media upload retry completed.", mediaIds.size());
        if (!mediaIds.isEmpty()) {
            log.debug("Re-published IDs: {}", mediaIds);
        }
    }

    @Scheduled(cron = "${scheduler.media.retry.delete.cron}")
    public void runMediaDeletionRetry() {
        List<Long> mediaIds = mediaRetryService.retryFailedDeletions();

        log.info("{} media deletion retry completed.", mediaIds.size());
        if (!mediaIds.isEmpty()) {
            log.debug("Re-published IDs: {}", mediaIds);
        }
    }
}
