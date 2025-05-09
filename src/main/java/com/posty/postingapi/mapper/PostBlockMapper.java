package com.posty.postingapi.mapper;

import com.posty.postingapi.domain.post.PostBlock;
import com.posty.postingapi.domain.post.PostBlockType;
import com.posty.postingapi.dto.PostBlockResponse;

public class PostBlockMapper {

    public static PostBlockResponse toPostBlockResponse(PostBlock entity) {
        return new PostBlockResponse(
                entity.getId(),
                entity.getOrderNo(),
                AccountMapper.toAccountSummary(entity.getWriter()),
                entity.getBlockType(), PostBlockType.TEXT == entity.getBlockType() ? entity.getContent() : entity.getMediaUrl(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
