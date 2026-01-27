package com.posty.fileapi.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class FileUploadResponse {

    private String storedUrl;

    private String storedFilename;
}
