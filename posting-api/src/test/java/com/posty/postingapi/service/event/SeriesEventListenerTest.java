package com.posty.postingapi.service.event;

import com.posty.postingapi.domain.series.event.SeriesChangedEvent;
import com.posty.postingapi.infrastructure.cache.WriterCacheManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class SeriesEventListenerTest {

    @Mock
    WriterCacheManager writerCacheManager;

    SeriesEventListener handler;

    @BeforeEach
    void setUp() {
        handler = new SeriesEventListener(writerCacheManager);
    }

    @Test
    @DisplayName("시리즈 변경 이벤트 처리 - 시리즈 작가 목록 캐시 무효화")
    void handleSeriesChangedEvent_ClearSeriesWritersCache() {
        // given
        SeriesChangedEvent event = new SeriesChangedEvent(1L);

        // when
        handler.handle(event);

        // then
        verify(writerCacheManager).clearWritersOfSeries(event.seriesId());
    }
}
