package com.posty.postingapi.infrastructure.persistence.post;

import com.posty.postingapi.domain.post.*;
import com.posty.postingapi.infrastructure.persistence.BaseQuerydslRepositorySupport;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PostRepositoryImpl extends BaseQuerydslRepositorySupport implements PostRepositoryCustom {

    public PostRepositoryImpl() {
        super(Post.class);
    }

    @Override
    public Page<Post> findAllByWriterId(Long writerId, Pageable pageable) {
        QPost post = QPost.post;
        QPostBlock postBlock = QPostBlock.postBlock;

        JPQLQuery<Post> query = from(post)
                .distinct()
                .join(post.blocks, postBlock)
                .where(postBlock.writer.id.eq(writerId));

        @SuppressWarnings("ConstantConditions")
        List<Post> posts = getQuerydsl().applyPagination(pageable, query).fetch();

        long total = query.fetchCount();

        return new PageImpl<>(posts, pageable, total);
    }
}
