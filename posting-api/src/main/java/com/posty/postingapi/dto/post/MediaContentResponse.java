package com.posty.postingapi.dto.post;

import com.posty.postingapi.domain.post.ContentType;
import com.posty.postingapi.domain.post.MediaStatus;
import com.posty.postingapi.domain.post.MediaType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class MediaContentResponse extends ContentResponse {

    private MediaType mediaType;

    private String storedMediaUrl;

    @Schema(description = "미디어 파일 업로드 상태")
    private MediaStatus status;

    public MediaContentResponse(MediaType mediaType, String storedMediaUrl, MediaStatus status) {
        super(ContentType.MEDIA);
        this.mediaType = mediaType;
        this.storedMediaUrl = storedMediaUrl;
        this.status = status;
    }
}
