package com.posty.postingapi.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class PostBlockRequest {

    @NotNull
    @Min(1)
    private Integer orderNo;

    @Schema(description = "계정(Account) ID")
    @NotNull
    private Long writerId;

    @NotNull
    @Schema(oneOf = { TextContentRequest.class, MediaContentRequest.class })
    private ContentRequest content;

    public void normalize() {
        if (content != null) {
            content.normalize();
        }
    }
}
