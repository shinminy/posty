package com.posty.postingapi.service.event;

import com.posty.postingapi.domain.account.event.AccountChangedEvent;
import com.posty.postingapi.infrastructure.cache.WriterCacheManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AccountEventListenerTest {

    @Mock
    WriterCacheManager writerCacheManager;

    AccountEventListener handler;

    @BeforeEach
    void setUp() {
        handler = new AccountEventListener(writerCacheManager);
    }

    @Test
    @DisplayName("계정 변경 이벤트 처리 - 계정 이름 캐시 무효화")
    void handleAccountChangedEvent_ClearAccountNameCache() {
        // given
        AccountChangedEvent event = new AccountChangedEvent(100L);

        // when
        handler.handle(event);

        // then
        verify(writerCacheManager).clearAccountName(event.accountId());
    }
}
