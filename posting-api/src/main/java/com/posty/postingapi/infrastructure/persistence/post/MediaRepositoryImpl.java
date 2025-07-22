package com.posty.postingapi.infrastructure.persistence.post;

import com.posty.postingapi.domain.post.Media;
import com.posty.postingapi.domain.post.MediaRepositoryCustom;
import com.posty.postingapi.domain.post.MediaStatus;
import com.posty.postingapi.domain.post.QMedia;
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
}
