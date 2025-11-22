package com.posty.postingapi.dto.post;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class PostCreateRequest {

    @NotNull
    private Long seriesId;

    @NotEmpty
    @Size(min = 1, max = 32)
    private String title;

    @NotEmpty
    private List<PostBlockCreateRequest> blocks;

    public void normalize() {
        if (title != null) {
            title = title.trim();
        }

        if (blocks != null) {
            blocks = blocks.stream()
                    .filter(block -> block != null)
                    .peek(PostBlockCreateRequest::normalize)
                    .toList();
        }
    }
}
