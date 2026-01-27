package com.posty.postingapi.infrastructure.file;

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
