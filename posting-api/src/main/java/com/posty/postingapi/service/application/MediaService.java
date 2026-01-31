package com.posty.postingapi.service.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.posty.postingapi.domain.post.Media;
import com.posty.postingapi.domain.post.MediaRepository;
import com.posty.postingapi.domain.post.MediaStatus;
import com.posty.postingapi.domain.post.MediaType;
import com.posty.postingapi.error.InvalidMediaException;
import com.posty.postingapi.error.InvalidMediaStatusException;
import com.posty.postingapi.error.ResourceNotFoundException;
import com.posty.postingapi.infrastructure.file.FileApiClient;
import com.posty.postingapi.infrastructure.file.FileUploadRequest;
import com.posty.postingapi.infrastructure.file.FileUploadResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.utils.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class MediaService {

    private final MediaRepository mediaRepository;
    private final FileApiClient fileApiClient;
    private final Clock clock;

    public MediaService(
            MediaRepository mediaRepository,
            FileApiClient fileApiClient,
            Clock clock
    ) {
        this.mediaRepository = mediaRepository;
        this.fileApiClient = fileApiClient;
        this.clock = clock;
    }

    @Transactional
    public List<Media> prepareMediaForUploadRetry(int maxUploadAttemptCount) {
        List<Media> failedMediaList = mediaRepository.findMediaWithUploadFailures(maxUploadAttemptCount);
        return prepareMediaForUpload(failedMediaList);
    }

    @Transactional
    public List<Media> prepareMediaForUpload(List<Media> mediaList) {
        List<Media> toRetry = mediaList.stream()
                .filter(media -> media.getStatus() == MediaStatus.UPLOAD_FAILED)
                .toList();

        if (toRetry.isEmpty()) {
            return List.of();
        }

        toRetry.forEach(Media::waitingUpload);
        return mediaRepository.saveAll(toRetry);
    }

    @Transactional
    public List<Media> prepareMediaForDeletionRetry(int maxDeletionAttemptCount) {
        List<Media> failedMediaList = mediaRepository.findMediaWithDeletionFailures(maxDeletionAttemptCount);
        return deleteOrPrepareMediaForDeletion(failedMediaList);
    }

    @Transactional
    public List<Media> deleteOrPrepareMediaForDeletion(List<Media> mediaList) {
        if (mediaList.isEmpty()) {
            return mediaList;
        }

        List<Media> toDelete = new ArrayList<>();
        List<Media> toRetry = new ArrayList<>();

        for (Media media : mediaList) {
            if (media.getStatus() == MediaStatus.WAITING_DELETION) {
                continue;
            }

            if (isDeletableImmediately(media)) {
                toDelete.add(media);
            } else {
                media.waitingDeletion();
                toRetry.add(media);
            }
        }

        if (!toDelete.isEmpty()) {
            mediaRepository.deleteAll(toDelete);
        }

        if (toRetry.isEmpty()) {
            return toRetry;
        }

        return mediaRepository.saveAll(toRetry);
    }

    private boolean isDeletableImmediately(Media media) {
        MediaStatus status = media.getStatus();
        return status == MediaStatus.WAITING_UPLOAD || status == MediaStatus.UPLOAD_FAILED;
    }

    public List<Media> findMediaBySeriesId(long seriesId) {
        return mediaRepository.findMediaBySeriesId(seriesId);
    }

    public List<Media> findMediaByPostId(long postId) {
        return mediaRepository.findMediaByPostId(postId);
    }

    @Transactional
    public void uploadMediaFile(Long mediaId) throws JsonProcessingException {
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new ResourceNotFoundException("Media", mediaId));

        MediaStatus status = media.getStatus();
        if (status != MediaStatus.WAITING_UPLOAD) {
            throw new InvalidMediaStatusException(mediaId, status);
        }

        MediaType mediaType = media.getMediaType();
        String originUrl = media.getOriginUrl();
        FileUploadRequest request = new FileUploadRequest(mediaType, originUrl);

        FileUploadResponse response;
        response = fileApiClient.upload(request);

        media.uploaded(response.storedUrl(), response.storedFilename(), LocalDateTime.now(clock));
        mediaRepository.save(media);
    }

    @Transactional
    public void failToUploadMedia(Long mediaId) {
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new ResourceNotFoundException("Media", mediaId));

        media.uploadFailed(LocalDateTime.now(clock));
        mediaRepository.save(media);
    }

    @Transactional
    public void deleteMediaFile(Long mediaId) {
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new ResourceNotFoundException("Media", mediaId));

        MediaStatus status = media.getStatus();
        if (status != MediaStatus.WAITING_DELETION) {
            throw new InvalidMediaStatusException(mediaId, status);
        }

        String fileName = media.getStoredFilename();
        if (StringUtils.isBlank(fileName)) {
            throw new InvalidMediaException("Filename is empty (ID: " + mediaId + ")");
        }

        fileApiClient.delete(fileName);

        mediaRepository.delete(media);
    }

    @Transactional
    public void failToDeleteMedia(Long mediaId) {
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new ResourceNotFoundException("Media", mediaId));

        media.deletionFailed(LocalDateTime.now(clock));
        mediaRepository.save(media);
    }
}
