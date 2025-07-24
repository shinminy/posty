package com.posty.postingapi.domain.post;

import java.util.List;

public interface MediaRepositoryCustom {

    List<Media> findMediaWithUploadFailures(int maxUploadAttemptCount);
    List<Media> findMediaWithDeletionFailures(int maxDeletionAttemptCount);
    List<Media> findMediaBySeriesId(long seriesId);
    List<Media> findMediaByPostId(long postId);
}
