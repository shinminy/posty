package com.posty.postingapi.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;

@Slf4j
public class FileCommunicationException extends RuntimeException {

    public FileCommunicationException(HttpMethod method, Throwable cause) {
        super(String.format("[%s] File API error", method.name()), cause);
    }

    public FileCommunicationException(HttpMethod method, HttpStatusCode status, String body) {
        super(String.format("[%s] File API error: [%s] %s", method.name(), status, body));
    }

    public FileCommunicationException(HttpMethod method, HttpStatusCode status, String body, Throwable cause) {
        super(String.format("[%s] File API error: [%s] %s", method.name(), status, body), cause);
    }
}
