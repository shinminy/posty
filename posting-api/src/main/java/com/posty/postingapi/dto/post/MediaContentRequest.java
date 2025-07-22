package com.posty.postingapi.dto.post;

import com.posty.common.domain.post.MediaType;
import com.posty.postingapi.domain.post.ContentType;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "10MB 미만 업로드 가능")
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
            originMediaUrl = originMediaUrl.trim();
        }
    }
}
