package com.posty.postingapi.infrastructure.persistence.post;

import com.posty.postingapi.domain.account.QAccount;
import com.posty.postingapi.domain.post.PostBlock;
import com.posty.postingapi.domain.post.PostBlockRepositoryCustom;
import com.posty.postingapi.domain.post.QPost;
import com.posty.postingapi.domain.post.QPostBlock;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

    @Override
    public Page<PostBlock> findPageByPostIdOrderByOrderNo(Long postId, Pageable pageable) {
        QPostBlock postBlock = QPostBlock.postBlock;

        List<PostBlock> content = queryFactory
                .selectFrom(postBlock)
                .where(postBlock.post.id.eq(postId))
                .orderBy(postBlock.orderNo.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(postBlock.count())
                .from(postBlock)
                .where(postBlock.post.id.eq(postId))
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0L : total);
    }
}
