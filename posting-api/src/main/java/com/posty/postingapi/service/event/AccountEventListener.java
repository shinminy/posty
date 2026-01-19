package com.posty.postingapi.service.event;

import com.posty.postingapi.domain.account.event.AccountChangedEvent;
import com.posty.postingapi.infrastructure.cache.WriterCacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class AccountEventListener {

    private final WriterCacheManager writerCacheManager;

    public AccountEventListener(WriterCacheManager writerCacheManager) {
        this.writerCacheManager = writerCacheManager;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(AccountChangedEvent event) {
        writerCacheManager.clearAccountName(event.accountId());
    }
}
