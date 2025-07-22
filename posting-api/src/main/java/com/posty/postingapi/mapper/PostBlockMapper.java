package com.posty.postingapi.mapper;

import com.posty.postingapi.domain.account.Account;
import com.posty.postingapi.domain.post.*;
import com.posty.postingapi.dto.post.*;

public class PostBlockMapper {

    public static PostBlockResponse toPostBlockResponse(PostBlock entity) {
        ContentResponse content;
        if (entity.getContentType() == ContentType.TEXT) {
            content = new TextContentResponse(entity.getTextContent());
        } else {
            Media media = entity.getMedia();
            content = new MediaContentResponse(media.getMediaType(), media.getStoredUrl(), media.getStatus());
        }

        return new PostBlockResponse(
                entity.getId(),
                entity.getOrderNo(),
                AccountMapper.toAccountSummary(entity.getWriter()),
                content,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static PostBlock toEntity(PostBlockRequest request, Post post, Account writer) {
        ContentRequest content = request.getContent();
        ContentType contentType = content.getType();

        PostBlock.PostBlockBuilder postBlockBuilder = PostBlock.builder()
                .post(post)
                .orderNo(request.getOrderNo())
                .writer(writer)
                .contentType(contentType);

        if (contentType == ContentType.TEXT) {
            TextContentRequest textContent = (TextContentRequest) content;

            return postBlockBuilder
                    .textContent(textContent.getText())
                    .build();
        } else {
            MediaContentRequest mediaContent = (MediaContentRequest) content;
            Media media = Media.builder()
                    .mediaType(mediaContent.getMediaType())
                    .originUrl(mediaContent.getOriginMediaUrl())
                    .status(MediaStatus.WAITING_UPLOAD)
                    .uploadAttemptCount(0)
                    .deleteAttemptCount(0)
                    .build();

            return postBlockBuilder
                    .media(media)
                    .build();
        }
    }
}
