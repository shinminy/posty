package com.posty.postingapi.error;

import com.posty.postingapi.domain.post.MediaStatus;

public class InvalidMediaStatusException extends RuntimeException {

    public InvalidMediaStatusException(Long mediaId, MediaStatus status) {
        super("Invalid media status (ID: " + mediaId + ", status: " + status + ")");
    }
}
