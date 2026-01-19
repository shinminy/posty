package com.posty.postingapi.service.event;

import com.posty.postingapi.domain.post.event.MediaChangedEvent;
import com.posty.postingapi.infrastructure.mq.MediaEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class MediaMessageRelayTest {

    @Mock
    MediaEventPublisher mediaEventPublisher;

    MediaMessageRelay handler;

    @BeforeEach
    void setUp() {
        handler = new MediaMessageRelay(mediaEventPublisher);
    }

    @Test
    @DisplayName("미디어 변경 이벤트 처리 - 생성 시 업로드 메시지 발행")
    void handleMediaChangedEvent_PublishUploadMessage() {
        // given
        Long mediaId = 100L;
        MediaChangedEvent event = new MediaChangedEvent(mediaId, MediaChangedEvent.MediaChangeType.CREATED);

        // when
        handler.handle(event);

        // then
        verify(mediaEventPublisher).publishMediaUpload(mediaId);
    }

    @Test
    @DisplayName("미디어 변경 이벤트 처리 - 삭제 시 삭제 메시지 발행")
    void handleMediaChangedEvent_PublishDeleteMessage() {
        // given
        Long mediaId = 100L;
        MediaChangedEvent event = new MediaChangedEvent(mediaId, MediaChangedEvent.MediaChangeType.DELETED);

        // when
        handler.handle(event);

        // then
        verify(mediaEventPublisher).publishMediaDelete(mediaId);
    }
}
