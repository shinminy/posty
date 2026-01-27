package com.posty.postingapi.service.application;

import com.posty.postingapi.domain.post.Media;
import com.posty.postingapi.domain.post.MediaRepository;
import com.posty.postingapi.domain.post.MediaStatus;
import com.posty.postingapi.domain.post.MediaType;
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
    public boolean uploadMediaFile(Long mediaId) {
        Media media = mediaRepository.findById(mediaId).orElse(null);
        if (media == null) {
            log.error("Media not found (ID: {})", mediaId);
            return false;
        }

        MediaStatus status = media.getStatus();
        if (status != MediaStatus.WAITING_UPLOAD) {
            log.error("Invalid media status (ID: {}, status: {})", mediaId, status);
            return false;
        }

        MediaType mediaType = media.getMediaType();
        String originUrl = media.getOriginUrl();

        try {
            FileUploadRequest request = new FileUploadRequest(mediaType, originUrl);
            FileUploadResponse response = fileApiClient.upload(request);
            if (response == null) {
                failToUploadMedia(media);
                return false;
            }

            successToUploadMedia(media, response.getStoredUrl(), response.getStoredFilename());
            return true;
        } catch (Exception e) {
            log.error("Error occurred when uploading media file with ID={}", mediaId, e);
            failToUploadMedia(media);
            return false;
        }
    }

    private void successToUploadMedia(Media media, String storedUrl, String storedFilename) {
        media.uploaded(storedUrl, storedFilename, LocalDateTime.now(clock));
        mediaRepository.save(media);
    }

    private void failToUploadMedia(Media media) {
        media.uploadFailed(LocalDateTime.now(clock));
        mediaRepository.save(media);
    }

    @Transactional
    public boolean deleteMediaFile(Long mediaId) {
        Media media = mediaRepository.findById(mediaId).orElse(null);
        if (media == null) {
            log.error("Media not found (ID: {})", mediaId);
            return false;
        }

        MediaStatus status = media.getStatus();
        if (status != MediaStatus.WAITING_DELETION) {
            log.error("Invalid media status (ID: {}, status: {})", mediaId, status);
            return false;
        }

        String fileName = media.getStoredFilename();
        if (StringUtils.isBlank(fileName)) {
            log.error("Filename is empty (ID: {})", mediaId);
            return false;
        }

        try {
            boolean result = fileApiClient.delete(fileName);
            if (!result) {
                failToDeleteMedia(media);
                return false;
            }

            successToDeleteMedia(media);
            return true;
        } catch (Exception e) {
            log.error("Error occurred when deleting media file with ID={}", mediaId, e);
            failToDeleteMedia(media);
            return false;
        }
    }

    private void successToDeleteMedia(Media media) {
        mediaRepository.delete(media);
    }

    private void failToDeleteMedia(Media media) {
        media.deletionFailed(LocalDateTime.now(clock));
        mediaRepository.save(media);
    }
}
