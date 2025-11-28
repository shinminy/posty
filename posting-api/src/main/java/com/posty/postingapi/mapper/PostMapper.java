package com.posty.postingapi.mapper;

import com.posty.postingapi.domain.post.Post;
import com.posty.postingapi.domain.series.Series;
import com.posty.postingapi.dto.post.PostBlockResponse;
import com.posty.postingapi.dto.post.PostCreateRequest;
import com.posty.postingapi.dto.post.PostDetailResponse;
import com.posty.postingapi.dto.post.PostSummary;
import org.springframework.data.domain.Page;

import java.util.List;

public class PostMapper {

    public static PostDetailResponse toPostDetailResponse(Post entity, List<String> writers, Page<PostBlockResponse> blocks) {
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
