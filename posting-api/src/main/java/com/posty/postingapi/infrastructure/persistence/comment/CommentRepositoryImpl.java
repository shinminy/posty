package com.posty.postingapi.infrastructure.persistence.comment;

import com.posty.postingapi.domain.comment.CommentRepositoryCustom;
import com.posty.postingapi.domain.comment.QComment;
import com.posty.postingapi.domain.post.QPost;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class CommentRepositoryImpl implements CommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public CommentRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    @Transactional
    public long deleteAllBySeriesId(Long seriesId) {
        QComment comment = QComment.comment;
        QPost post = QPost.post;

        List<Long> commentIds = queryFactory
                .select(comment.id)
                .from(comment)
                .join(comment.post, post)
                .where(post.series.id.eq(seriesId))
                .fetch();

        if (commentIds.isEmpty()) {
            return 0;
        }

        return queryFactory
                .delete(comment)
                .where(comment.id.in(commentIds))
                .execute();
    }
}
