package com.posty.postingapi.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class PostUpdateRequest {

    @Size(min = 1, max = 32)
    private String title;

    @Schema(description = "새로 추가한 블록 목록")
    private List<PostBlockCreateRequest> newBlocks;

    @Schema(description = "수정한 블록 목록")
    private List<PostBlockUpdateRequest> updatedBlocks;

    @Schema(description = "삭제할 블록의 id 목록")
    private List<Long> deletedBlockIds;

    public void normalize() {
        if (title != null) {
            title = title.trim();
        }

        if (newBlocks != null) {
            newBlocks = newBlocks.stream()
                    .filter(block -> block != null)
                    .peek(PostBlockCreateRequest::normalize)
                    .toList();
        }

        if (updatedBlocks != null) {
            updatedBlocks = updatedBlocks.stream()
                    .filter(block -> block != null)
                    .peek(PostBlockUpdateRequest::normalize)
                    .toList();
        }

        if (deletedBlockIds != null) {
            deletedBlockIds = deletedBlockIds.stream()
                    .filter(id -> id != null)
                    .toList();
        }
    }
}
