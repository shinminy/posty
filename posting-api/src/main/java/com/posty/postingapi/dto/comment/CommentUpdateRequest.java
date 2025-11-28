package com.posty.postingapi.dto.comment;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class CommentUpdateRequest {

    @NotEmpty
    @Size(min = 1, max = 1000)
    private String content;

    public void normalize() {
        if (content != null) {
            content = content.trim();
        }
    }
}
