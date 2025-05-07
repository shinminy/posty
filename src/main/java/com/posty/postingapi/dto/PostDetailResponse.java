package com.posty.postingapi.dto;

import com.posty.postingapi.domain.post.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class PostDetailResponse {

    private Long id;

    private String title;

    @Schema(description = "해당 포스트 작성에 참여한 작성자 목록")
    private List<String> writers;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<PostBlockResponse> blocks;

    public PostDetailResponse(Post post, List<String> writers, List<PostBlockResponse> blocks) {
        this(post.getId(), post.getTitle(), writers, post.getCreatedAt(), post.getUpdatedAt(), blocks);
    }
}
