package com.posty.postingapi.dto;

import com.posty.postingapi.domain.post.PostBlock;
import com.posty.postingapi.domain.post.PostBlockType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class PostBlockResponse {

    private Long id;

    @Schema(description = "블록 순서 (번호)")
    private Integer order;

    private AccountSummary writer;

    private PostBlockType blockType;

    @Schema(description = "blockType이 TEXT면 HTML, 이외에는 URL")
    private String content;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public PostBlockResponse(Long id, Integer order, AccountSummary writer, PostBlockType blockType, String content, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.order = order;
        this.writer = writer;
        this.blockType = blockType;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public PostBlockResponse(PostBlock block) {
        this(
                block.getId(),
                block.getOrderNo(),
                new AccountSummary(block.getWriter()),
                block.getBlockType(), PostBlockType.TEXT == block.getBlockType() ? block.getContent() : block.getMediaUrl(),
                block.getCreatedAt(),
                block.getUpdatedAt()
        );
    }
}
