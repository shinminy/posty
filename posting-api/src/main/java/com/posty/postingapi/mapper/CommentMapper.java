package com.posty.postingapi.mapper;

import com.posty.postingapi.domain.account.Account;
import com.posty.postingapi.domain.comment.Comment;
import com.posty.postingapi.domain.post.Post;
import com.posty.postingapi.dto.account.AccountSummary;
import com.posty.postingapi.dto.comment.CommentCreateRequest;
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

    public static Comment toEntity(CommentCreateRequest request, Post post, Account writer) {
        return Comment.builder()
                .post(post)
                .content(request.getContent())
                .writer(writer)
                .build();
    }
}
