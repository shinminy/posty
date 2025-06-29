package com.posty.postingapi.dto;

import com.posty.postingapi.domain.post.ContentType;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class TextContentRequest extends ContentRequest {

    @NotEmpty
    private String text;

    public TextContentRequest(String text) {
        super(ContentType.TEXT);
        this.text = text;
    }

    @Override
    public void normalize() {
        if (text != null) {
            text = text.trim().toLowerCase();
        }
    }
}
