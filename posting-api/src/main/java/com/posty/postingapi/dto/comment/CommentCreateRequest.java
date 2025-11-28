package com.posty.postingapi.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class CommentCreateRequest {

    @NotNull
    private Long postId;

    @NotEmpty
    @Size(min = 1, max = 1000)
    private String content;

    @Schema(description = "계정(Account) ID")
    @NotNull
    private Long writerId;

    public void normalize() {
        if (content != null) {
            content = content.trim();
        }
    }
}
