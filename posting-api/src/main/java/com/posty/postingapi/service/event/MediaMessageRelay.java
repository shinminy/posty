package com.posty.postingapi.service.event;

import com.posty.postingapi.domain.post.event.MediaChangedEvent;
import com.posty.postingapi.infrastructure.mq.MediaEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class MediaMessageRelay {

    private final MediaEventPublisher mediaEventPublisher;

    public MediaMessageRelay(MediaEventPublisher mediaEventPublisher) {
        this.mediaEventPublisher = mediaEventPublisher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(MediaChangedEvent event) {
        switch (event.changeType()) {
            case CREATED -> mediaEventPublisher.publishMediaUpload(event.mediaId());
            case DELETED -> mediaEventPublisher.publishMediaDelete(event.mediaId());
        }
    }
}
