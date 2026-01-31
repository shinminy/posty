package com.posty.fileapi.dto;

public record FileUploadResponse(
        String storedUrl,
        String storedFilename
) {
}
