package com.posty.postingapi.infrastructure.mq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.posty.postingapi.error.FileCommunicationException;
import com.posty.postingapi.error.InvalidMediaException;
import com.posty.postingapi.error.InvalidMediaStatusException;
import com.posty.postingapi.error.ResourceNotFoundException;
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

        try {
            mediaService.uploadMediaFile(mediaId);
            log.debug("Media {} upload succeeded!", mediaId);
            return;

        } catch(ResourceNotFoundException | InvalidMediaStatusException e) {
            log.error("{}", e.getMessage(), e);
        } catch (FileCommunicationException | JsonProcessingException e) {
            log.error("{}", e.getMessage(), e);
            mediaService.failToUploadMedia(mediaId);
        }
        log.debug("Media {} upload failed...", mediaId);
    }

    @JmsListener(destination = "${media.delete-queue-name}")
    public void consumeMediaDelete(Long mediaId) {
        log.debug("Received media delete request for media {}", mediaId);

        try {
            mediaService.deleteMediaFile(mediaId);
            log.debug("Media {} deletion succeeded!", mediaId);
            return;

        } catch(ResourceNotFoundException | InvalidMediaStatusException e) {
            log.error("{}", e.getMessage(), e);
        } catch (InvalidMediaException | FileCommunicationException e) {
            log.error("{}", e.getMessage(), e);
            mediaService.failToDeleteMedia(mediaId);
        }
        log.debug("Media {} deletion failed...", mediaId);
    }
}
