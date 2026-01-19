package com.posty.postingapi.service.event;

import com.posty.postingapi.domain.series.event.SeriesChangedEvent;
import com.posty.postingapi.infrastructure.cache.WriterCacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class SeriesEventListener {

    private final WriterCacheManager writerCacheManager;

    public SeriesEventListener(WriterCacheManager writerCacheManager) {
        this.writerCacheManager = writerCacheManager;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(SeriesChangedEvent event) {
        writerCacheManager.clearWritersOfSeries(event.seriesId());
    }
}
