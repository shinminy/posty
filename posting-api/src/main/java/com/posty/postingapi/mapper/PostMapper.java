package com.posty.postingapi.mapper;

import com.posty.postingapi.domain.post.Post;
import com.posty.postingapi.domain.series.Series;
import com.posty.postingapi.dto.PostBlockResponse;
import com.posty.postingapi.dto.PostCreateRequest;
import com.posty.postingapi.dto.PostDetailResponse;
import com.posty.postingapi.dto.PostSummary;

import java.util.List;

public class PostMapper {

    public static PostDetailResponse toPostDetailResponse(Post entity, List<String> writers, List<PostBlockResponse> blocks) {
        return new PostDetailResponse(
                entity.getId(),
                entity.getTitle(),
                writers,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                blocks
        );
    }

    public static PostSummary toPostSummary(Post entity) {
        return new PostSummary(
                entity.getId(),
                entity.getTitle(),
                entity.getCreatedAt()
        );
    }

    public static Post toEntity(PostCreateRequest request, Series series) {
        return Post.builder()
                .series(series)
                .title(request.getTitle())
                .build();
    }
}
