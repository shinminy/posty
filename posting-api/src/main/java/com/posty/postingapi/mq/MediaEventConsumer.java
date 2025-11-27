package com.posty.postingapi.mq;

import com.posty.postingapi.service.application.MediaService;
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

    @JmsListener(destination = "${media.upload-queue-name}")
    public void consumeMediaUpload(Long mediaId) {
        log.debug("Received media upload request for media {}", mediaId);
        if (mediaService.uploadMediaFile(mediaId)) {
            log.debug("Media {} upload succeeded!", mediaId);
        } else {
            log.debug("Media {} upload failed...", mediaId);
        }
    }

    @JmsListener(destination = "${media.delete-queue-name}")
    public void consumeMediaDelete(Long mediaId) {
        log.debug("Received media delete request for media {}", mediaId);
        if (mediaService.deleteMediaFile(mediaId)) {
            log.debug("Media {} deletion succeeded!", mediaId);
        } else {
            log.debug("Media {} deletion failed...", mediaId);
        }
    }
}
