package com.posty.postingapi.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class DuplicateAccountException extends RuntimeException {

    public DuplicateAccountException(final String message) {
        super(message);
    }
}
