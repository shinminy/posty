package com.posty.postingapi.dto.post;

import com.posty.postingapi.domain.post.ContentType;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class TextContentResponse extends ContentResponse {

    @NotEmpty
    private String text;

    public TextContentResponse(String text) {
        super(ContentType.TEXT);
        this.text = text;
    }
}
