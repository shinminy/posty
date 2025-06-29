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

    public List<Media> findFailedMediaForRetry(int maxRetryCount) {
        QMedia qMedia = QMedia.media;

        return queryFactory
                .selectFrom(qMedia)
                .where(
                        qMedia.status.eq(MediaStatus.FAILED),
                        qMedia.tryCount.lt(maxRetryCount)
                )
                .fetch();
    }
}
