package com.posty.postingapi.service.application;

import com.posty.common.domain.post.MediaType;
import com.posty.common.dto.FileRequest;
import com.posty.common.dto.FileResponse;
import com.posty.postingapi.domain.post.Media;
import com.posty.postingapi.domain.post.MediaRepository;
import com.posty.postingapi.infrastructure.file.FileUploader;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;

@Slf4j
@Service
public class MediaService {

    private final MediaRepository mediaRepository;
    private final FileUploader fileUploader;
    private final Clock clock;

    public MediaService(
            MediaRepository mediaRepository,
            FileUploader fileUploader,
            Clock clock
    ) {
        this.mediaRepository = mediaRepository;
        this.fileUploader = fileUploader;
        this.clock = clock;
    }

    @Transactional
    public boolean upload(Long mediaId) {
        Media media = mediaRepository.findById(mediaId).orElse(null);
        if (media == null) {
            log.error("Media not found (ID: {})", mediaId);
            return false;
        }

        MediaType mediaType = media.getMediaType();
        String originUrl = media.getOriginUrl();

        try {
            FileRequest request = new FileRequest(mediaType, originUrl);
            FileResponse response = fileUploader.upload(request);
            if (response == null) {
                failMedia(media);
                return false;
            }

            successMedia(media, response.getStoredUrl());
            return true;
        } catch (Exception e) {
            log.error("Error occurred when uploading media with ID={}", mediaId, e);
            failMedia(media);
            return false;
        }
    }

    private Media successMedia(Media media, String storedUrl) {
        return mediaRepository.save(media.succeeded(storedUrl, LocalDateTime.now(clock)));
    }

    private Media failMedia(Media media) {
        return mediaRepository.save(media.failed(LocalDateTime.now(clock)));
    }
}
