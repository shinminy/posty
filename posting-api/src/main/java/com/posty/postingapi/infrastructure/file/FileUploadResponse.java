package com.posty.postingapi.infrastructure.file;

public record FileUploadResponse(
        String storedUrl,
        String storedFilename
) {
}
