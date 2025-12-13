package com.posty.postingapi.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class VerificationFailedException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Invalid or expired verification code.";

    public VerificationFailedException() {
        super(DEFAULT_MESSAGE);
    }
}
