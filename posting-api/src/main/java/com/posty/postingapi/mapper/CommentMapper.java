package com.posty.postingapi.mapper;

import com.posty.postingapi.domain.comment.Comment;
import com.posty.postingapi.dto.account.AccountSummary;
import com.posty.postingapi.dto.comment.CommentDetailResponse;
import com.posty.postingapi.dto.post.PostSummary;

public class CommentMapper {

    public static CommentDetailResponse toCommentDetailResponse(Comment entity, PostSummary post, AccountSummary writer) {
        return new CommentDetailResponse(
                entity.getId(),
                post,
                entity.getContent(),
                writer,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
