package com.posty.fileapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

public record FileUploadRequest(
        @NotNull MediaType mediaType,
        @NotBlank @URL String originUrl
) {
}
