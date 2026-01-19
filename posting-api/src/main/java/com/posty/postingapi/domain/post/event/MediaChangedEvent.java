package com.posty.postingapi.domain.post.event;

public record MediaChangedEvent(
        Long mediaId,
        MediaChangeType changeType
) {
    public enum MediaChangeType {
        CREATED,
        DELETED
    }
}
