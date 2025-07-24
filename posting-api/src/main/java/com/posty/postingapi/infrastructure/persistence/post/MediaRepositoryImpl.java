package com.posty.postingapi.infrastructure.persistence.post;

import com.posty.postingapi.domain.post.*;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MediaRepositoryImpl implements MediaRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public MediaRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public List<Media> findMediaWithUploadFailures(int maxUploadAttemptCount) {
        QMedia qMedia = QMedia.media;

        return queryFactory
                .selectFrom(qMedia)
                .where(
                        qMedia.status.eq(MediaStatus.UPLOAD_FAILED),
                        qMedia.uploadAttemptCount.lt(maxUploadAttemptCount)
                )
                .fetch();
    }

    @Override
    public List<Media> findMediaWithDeletionFailures(int maxDeletionAttemptCount) {
        QMedia qMedia = QMedia.media;

        return queryFactory
                .selectFrom(qMedia)
                .where(
                        qMedia.status.eq(MediaStatus.DELETION_FAILED),
                        qMedia.deleteAttemptCount.lt(maxDeletionAttemptCount)
                )
                .fetch();
    }

    @Override
    public List<Media> findMediaBySeriesId(long seriesId) {
        QPost qPost = QPost.post;
        QPostBlock qPostBlock = QPostBlock.postBlock;
        QMedia qMedia = QMedia.media;

        return queryFactory
                .select(qMedia)
                .from(qPost)
                .join(qPost.blocks, qPostBlock)
                .join(qPostBlock.media, qMedia)
                .where(
                        qPost.series.id.eq(seriesId)
                )
                .fetch();
    }

    @Override
    public List<Media> findMediaByPostId(long postId) {
        QPostBlock qPostBlock = QPostBlock.postBlock;
        QMedia qMedia = QMedia.media;

        return queryFactory
                .select(qMedia)
                .from(qPostBlock)
                .join(qPostBlock.media, qMedia)
                .where(
                        qPostBlock.post.id.eq(postId),
                        qPostBlock.contentType.eq(ContentType.MEDIA)
                )
                .fetch();
    }
}
