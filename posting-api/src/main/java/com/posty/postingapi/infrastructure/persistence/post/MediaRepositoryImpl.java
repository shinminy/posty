package com.posty.postingapi.infrastructure.persistence.post;

import com.posty.postingapi.domain.post.*;
import com.posty.postingapi.infrastructure.persistence.BaseQuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MediaRepositoryImpl extends BaseQuerydslRepositorySupport implements MediaRepositoryCustom {

    public MediaRepositoryImpl() {
        super(Media.class);
    }

    @Override
    public List<Media> findMediaWithUploadFailures(int maxUploadAttemptCount) {
        QMedia media = QMedia.media;

        return from(media)
                .where(
                        media.status.eq(MediaStatus.UPLOAD_FAILED),
                        media.uploadAttemptCount.lt(maxUploadAttemptCount)
                )
                .fetch();
    }

    @Override
    public List<Media> findMediaWithDeletionFailures(int maxDeletionAttemptCount) {
        QMedia media = QMedia.media;

        return from(media)
                .where(
                        media.status.eq(MediaStatus.DELETION_FAILED),
                        media.deleteAttemptCount.lt(maxDeletionAttemptCount)
                )
                .fetch();
    }

    @Override
    public List<Media> findMediaBySeriesId(long seriesId) {
        QPost post = QPost.post;
        QPostBlock postBlock = QPostBlock.postBlock;
        QMedia media = QMedia.media;

        return from(post)
                .join(post.blocks, postBlock)
                .join(postBlock.media, media)
                .where(
                        post.series.id.eq(seriesId)
                )
                .select(media)
                .fetch();
    }

    @Override
    public List<Media> findMediaByPostId(long postId) {
        QPostBlock postBlock = QPostBlock.postBlock;
        QMedia media = QMedia.media;

        return from(postBlock)
                .join(postBlock.media, media)
                .where(
                        postBlock.post.id.eq(postId),
                        postBlock.contentType.eq(ContentType.MEDIA)
                )
                .select(media)
                .fetch();
    }
}
