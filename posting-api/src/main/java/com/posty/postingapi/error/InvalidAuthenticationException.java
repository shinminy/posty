package com.posty.postingapi.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidAuthenticationException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Authentication information is invalid or expired.";

    public InvalidAuthenticationException() {
        super(DEFAULT_MESSAGE);
    }
}
