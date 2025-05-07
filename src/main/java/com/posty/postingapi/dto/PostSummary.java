package com.posty.postingapi.dto;

import com.posty.postingapi.domain.post.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class PostSummary {

    private Long id;

    @Schema(description = "포스트 순서 (번호)")
    private Integer order;

    private String title;

    private LocalDateTime createdAt;

    public PostSummary(Long id, Integer order, String title, LocalDateTime createdAt) {
        this.id = id;
        this.order = order;
        this.title = title;
        this.createdAt = createdAt;
    }

    public PostSummary(Post post) {
        this.id = post.getId();
        this.order = post.getOrderNo();
        this.title = post.getTitle();
        this.createdAt = post.getCreatedAt();
    }
}
