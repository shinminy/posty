package com.posty.postingapi.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class AlreadyProcessedException extends RuntimeException {

    public AlreadyProcessedException(String action, String target) {
        super(action + " already processed for: " + target);
    }
}
