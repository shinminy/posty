package com.posty.postingapi.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(final String resourceName) {
        super(resourceName + " not found");
    }

    public ResourceNotFoundException(final String resourceName, final long resourceId) {
        super(resourceName + " not found with ID = " + resourceId);
    }

    public ResourceNotFoundException(final String resourceName, final List<Long> resourceIds) {
        super(resourceName + " not found with IDs = " + resourceIds);
    }
}
