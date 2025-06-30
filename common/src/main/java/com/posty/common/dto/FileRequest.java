package com.posty.common.dto;

import com.posty.common.domain.post.MediaType;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class FileRequest {

    private MediaType mediaType;

    private String originUrl;
}
