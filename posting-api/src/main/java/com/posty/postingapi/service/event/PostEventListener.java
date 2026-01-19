package com.posty.postingapi.service.event;

import com.posty.postingapi.domain.post.event.PostChangedEvent;
import com.posty.postingapi.infrastructure.cache.WriterCacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PostEventListener {

    private final WriterCacheManager writerCacheManager;

    public PostEventListener(WriterCacheManager writerCacheManager) {
        this.writerCacheManager = writerCacheManager;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PostChangedEvent event) {
        writerCacheManager.clearWritersOfPosts(event.postId(), event.seriesId());
    }
}
