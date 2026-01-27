package com.posty.fileapi.dto;

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
