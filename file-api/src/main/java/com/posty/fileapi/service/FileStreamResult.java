package com.posty.fileapi.service;

import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public record FileStreamResult(
        StreamingResponseBody body,
        long contentLength,
        String contentType,
        String contentRange,
        boolean partial
) {
}
