package com.posty.postingapi.service.application;

import com.posty.common.domain.post.MediaType;
import com.posty.common.dto.FileUploadRequest;
import com.posty.common.dto.FileUploadResponse;
import com.posty.postingapi.domain.post.Media;
import com.posty.postingapi.domain.post.MediaRepository;
import com.posty.postingapi.domain.post.MediaStatus;
import com.posty.postingapi.domain.post.PostBlockRepository;
import com.posty.postingapi.infrastructure.file.FileApiClient;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.utils.StringUtils;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MediaService {

    private final EntityManager entityManager;

    private final PostBlockRepository postBlockRepository;
    private final MediaRepository mediaRepository;
    private final FileApiClient fileApiClient;
    private final Clock clock;

    public MediaService(
            EntityManager entityManager,
            PostBlockRepository postBlockRepository,
            MediaRepository mediaRepository,
            FileApiClient fileApiClient,
            Clock clock
    ) {
        this.entityManager = entityManager;

        this.postBlockRepository = postBlockRepository;
        this.mediaRepository = mediaRepository;
        this.fileApiClient = fileApiClient;
        this.clock = clock;
    }

    @Transactional
    public List<Media> prepareMediaForUploadRetry(int maxUploadAttemptCount) {
        List<Media> failedMediaList = mediaRepository.findMediaWithUploadFailures(maxUploadAttemptCount);
        return prepareMediaForUpload(failedMediaList);
    }

    public List<Media> prepareMediaForUpload(List<Media> mediaList) {
        if (mediaList.isEmpty()) {
            return mediaList;
        }

        List<Media> waitingList = mediaList.stream()
                .filter(media -> media.getStatus() == MediaStatus.UPLOAD_FAILED)
                .map(Media::waitingUpload)
                .toList();
        if (waitingList.isEmpty()) {
            return waitingList;
        }

        return mediaRepository.saveAll(waitingList);
    }

    @Transactional
    public List<Media> prepareMediaForDeletionRetry(int maxDeletionAttemptCount) {
        List<Media> failedMediaList = mediaRepository.findMediaWithDeletionFailures(maxDeletionAttemptCount);
        return deleteOrPrepareMediaForDeletion(failedMediaList);
    }

    public List<Media> deleteOrPrepareMediaForDeletion(List<Media> mediaList) {
        if (mediaList.isEmpty()) {
            return mediaList;
        }

        Map<Boolean, List<Media>> partitioned = mediaList.stream()
                .filter(media -> media.getStatus() != MediaStatus.WAITING_DELETION)
                .collect(Collectors.partitioningBy(media -> {
                    MediaStatus status = media.getStatus();
                    return status == MediaStatus.WAITING_UPLOAD || status == MediaStatus.UPLOAD_FAILED;
                }));

        List<Media> deletableList = partitioned.get(true);

        if (!deletableList.isEmpty()) {
            mediaRepository.deleteAll(deletableList);
        }

        List<Media> waitingList = partitioned.get(false).stream()
                .map(Media::waitingDeletion)
                .toList();
        if (waitingList.isEmpty()) {
            return waitingList;
        }

        return mediaRepository.saveAll(waitingList);
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
        mediaRepository.save(media.uploaded(storedUrl, storedFilename, LocalDateTime.now(clock)));
    }

    private void failToUploadMedia(Media media) {
        mediaRepository.save(media.uploadFailed(LocalDateTime.now(clock)));
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
        mediaRepository.save(media.deletionFailed(LocalDateTime.now(clock)));
    }
}
