package com.posty.postingapi.domain.post.event;

public record PostChangedEvent(Long postId, Long seriesId) {
}
