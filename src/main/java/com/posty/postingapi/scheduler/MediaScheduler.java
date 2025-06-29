package com.posty.postingapi.scheduler;

import com.posty.postingapi.service.scheduler.MediaRetryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class MediaScheduler {

    private final MediaRetryService mediaRetryService;

    public MediaScheduler(MediaRetryService mediaRetryService) {
        this.mediaRetryService = mediaRetryService;
    }

    @Scheduled(cron = "${scheduler.media.retry.cron}")
    public void runMediaRetry() {
        log.info("Media retry scheduler started...");
        mediaRetryService.retryFailedUploads();
        log.info("Media retry scheduler finished!");
    }
}
