package com.posty.postingapi.service.event;

import com.posty.postingapi.domain.post.event.PostChangedEvent;
import com.posty.postingapi.infrastructure.cache.WriterCacheManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class PostEventListenerTest {

    @Mock
    WriterCacheManager writerCacheManager;

    PostEventListener handler;

    @BeforeEach
    void setUp() {
        handler = new PostEventListener(writerCacheManager);
    }

    @Test
    @DisplayName("포스트 변경 이벤트 처리 - 포스트 작가 목록 캐시 무효화")
    void handlePostChangedEvent_ClearPostWritersCache() {
        // given
        PostChangedEvent event = new PostChangedEvent(1L, 10L);

        // when
        handler.handle(event);

        // then
        verify(writerCacheManager).clearWritersOfPosts(event.postId(), event.seriesId());
    }
}
