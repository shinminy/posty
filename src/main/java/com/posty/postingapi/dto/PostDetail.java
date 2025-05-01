package com.posty.postingapi.dto;

import com.posty.postingapi.domain.post.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
public class PostDetail {

    private Long id;

    private String title;

    @Schema(description = "해당 포스트 작성에 참여한 작성자 목록")
    private List<String> writers;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<PostBlockDetail> blocks;

    public PostDetail(Long id, String title, List<String> writers, LocalDateTime createdAt, LocalDateTime updatedAt, List<PostBlockDetail> blocks) {
        this.id = id;
        this.title = title;
        this.writers = writers;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.blocks = blocks;
    }

    public PostDetail(Post post, List<String> writers, List<PostBlockDetail> blocks) {
        this(post.getId(), post.getTitle(), writers, post.getCreatedAt(), post.getUpdatedAt(), blocks);
    }
}
