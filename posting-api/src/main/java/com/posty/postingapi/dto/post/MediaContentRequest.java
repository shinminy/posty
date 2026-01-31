package com.posty.postingapi.dto.post;

import com.posty.postingapi.domain.post.ContentType;
import com.posty.postingapi.domain.post.MediaType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
@ToString(callSuper = true)
public class MediaContentRequest extends ContentRequest {

    @NotNull
    private MediaType mediaType;

    @Schema(description = "10MB 미만 업로드 가능")
    @NotBlank
    @URL
    private String originMediaUrl;

    public MediaContentRequest(MediaType mediaType, String originMediaUrl) {
        super(ContentType.MEDIA);
        this.mediaType = mediaType;
        this.originMediaUrl = originMediaUrl;
    }

    @Override
    public void normalize() {
    }
}
