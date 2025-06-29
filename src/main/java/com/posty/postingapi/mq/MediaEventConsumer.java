package com.posty.postingapi.mq;

import com.posty.postingapi.service.application.MediaService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MediaEventConsumer {

    private final MediaService mediaService;

    public MediaEventConsumer(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @Transactional
    @JmsListener(destination = "${media.upload.queue-name}")
    public void consumeMediaUpload(Long mediaId) {
        log.debug("Received media upload request for media {}", mediaId);
        mediaService.upload(mediaId);
    }
}
