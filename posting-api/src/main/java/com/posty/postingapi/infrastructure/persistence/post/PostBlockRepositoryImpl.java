package com.posty.postingapi.infrastructure.persistence.post;

import com.posty.postingapi.domain.account.QAccount;
import com.posty.postingapi.domain.post.PostBlock;
import com.posty.postingapi.domain.post.PostBlockRepositoryCustom;
import com.posty.postingapi.domain.post.QPost;
import com.posty.postingapi.domain.post.QPostBlock;
import com.posty.postingapi.infrastructure.persistence.BaseQuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PostBlockRepositoryImpl extends BaseQuerydslRepositorySupport implements PostBlockRepositoryCustom {

    public PostBlockRepositoryImpl() {
        super(PostBlock.class);
    }

    @Override
    public List<Long> findDistinctWriterIdsBySeriesId(Long seriesId) {
        QPostBlock postBlock = QPostBlock.postBlock;
        QPost post = QPost.post;
        QAccount writer = QAccount.account;

        return from(postBlock)
                .join(postBlock.post, post)
                .join(postBlock.writer, writer)
                .where(post.series.id.eq(seriesId))
                .select(writer.id)
                .distinct()
                .fetch();
    }

    @Override
    public List<Long> findDistinctWriterIdsByPostId(Long postId) {
        QPostBlock postBlock = QPostBlock.postBlock;
        QAccount writer = QAccount.account;

        return from(postBlock)
                .join(postBlock.writer, writer)
                .where(postBlock.post.id.eq(postId))
                .select(writer.id)
                .distinct()
                .fetch();
    }
}
