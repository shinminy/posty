package com.posty.postingapi.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class TooManyRequestsException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Too many requests. Please try again later.";

    public TooManyRequestsException() {
        super(DEFAULT_MESSAGE);
    }
}
