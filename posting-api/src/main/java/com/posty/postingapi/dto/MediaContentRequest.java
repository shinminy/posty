package com.posty.postingapi.dto;

import com.posty.postingapi.domain.post.ContentType;
import com.posty.postingapi.domain.post.MediaType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class MediaContentRequest extends ContentRequest {

    @NotNull
    private MediaType mediaType;

    @NotEmpty
    private String originMediaUrl;

    public MediaContentRequest(MediaType mediaType, String originMediaUrl) {
        super(ContentType.MEDIA);
        this.mediaType = mediaType;
        this.originMediaUrl = originMediaUrl;
    }

    @Override
    public void normalize() {
        if (originMediaUrl != null) {
            originMediaUrl = originMediaUrl.trim().toLowerCase();
        }
    }
}
