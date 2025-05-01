package com.posty.postingapi.domain.post;

import com.posty.postingapi.domain.account.QAccount;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PostBlockRepositoryImpl implements PostBlockRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public PostBlockRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public List<String> findDistinctWriterNamesBySeriesId(Long seriesId) {
        QPostBlock postBlock = QPostBlock.postBlock;
        QPost post = QPost.post;
        QAccount writer = QAccount.account;

        return queryFactory
                .select(writer.name)
                .distinct()
                .from(postBlock)
                .join(postBlock.post, post)
                .join(postBlock.writer, writer)
                .where(post.series.id.eq(seriesId))
                .orderBy(writer.name.asc())
                .fetch();
    }

    @Override
    public List<String> findDistinctWriterNamesByPostId(Long postId) {
        QPostBlock postBlock = QPostBlock.postBlock;
        QAccount writer = QAccount.account;

        return queryFactory
                .select(writer.name)
                .distinct()
                .from(postBlock)
                .join(postBlock.writer, writer)
                .where(postBlock.post.id.eq(postId))
                .orderBy(writer.name.asc())
                .fetch();
    }
}
