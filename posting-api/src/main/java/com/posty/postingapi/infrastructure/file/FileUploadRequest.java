package com.posty.postingapi.infrastructure.file;

import com.posty.postingapi.domain.post.MediaType;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class FileUploadRequest {

    private MediaType mediaType;

    private String originUrl;
}
