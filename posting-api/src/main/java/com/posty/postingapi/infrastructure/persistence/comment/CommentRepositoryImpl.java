package com.posty.postingapi.infrastructure.persistence.comment;

import com.posty.postingapi.domain.comment.Comment;
import com.posty.postingapi.domain.comment.CommentRepositoryCustom;
import com.posty.postingapi.domain.comment.QComment;
import com.posty.postingapi.domain.post.QPost;
import com.posty.postingapi.infrastructure.persistence.BaseQuerydslRepositorySupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class CommentRepositoryImpl extends BaseQuerydslRepositorySupport implements CommentRepositoryCustom {

    public CommentRepositoryImpl() {
        super(Comment.class);
    }

    @Override
    @Transactional
    public long deleteAllBySeriesId(Long seriesId) {
        QComment comment = QComment.comment;
        QPost post = QPost.post;

        List<Long> commentIds = from(comment)
                .join(comment.post, post)
                .where(post.series.id.eq(seriesId))
                .select(comment.id)
                .fetch();

        if (commentIds.isEmpty()) {
            return 0;
        }

        return delete(comment)
                .where(comment.id.in(commentIds))
                .execute();
    }
}
