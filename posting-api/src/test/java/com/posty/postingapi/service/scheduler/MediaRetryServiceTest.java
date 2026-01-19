package com.posty.postingapi.service.scheduler;

import com.posty.postingapi.domain.post.Media;
import com.posty.postingapi.domain.post.event.MediaChangedEvent;
import com.posty.postingapi.properties.SchedulerProperties;
import com.posty.postingapi.service.application.MediaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MediaRetryServiceTest {

    private static final int MAX_UPLOAD_ATTEMPT_COUNT = 2;
    private static final int MAX_DELETION_ATTEMPT_COUNT = 2;

    @Mock
    private MediaService mediaService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private SchedulerProperties schedulerProperties;

    private MediaRetryService mediaRetryService;

    @BeforeEach
    void setUp() {
        var mediaProps = mock(SchedulerProperties.MediaSchedulerProperties.class);
        var retryProps = mock(SchedulerProperties.MediaSchedulerProperties.MediaRetryProperties.class);
        var uploadProps = mock(SchedulerProperties.MediaSchedulerProperties.MediaRetryProperties.MediaUploadRetryProperties.class);
        var deleteProps = mock(SchedulerProperties.MediaSchedulerProperties.MediaRetryProperties.MediaDeleteRetryProperties.class);

        given(schedulerProperties.getMedia()).willReturn(mediaProps);
        given(mediaProps.getRetry()).willReturn(retryProps);
        given(retryProps.getUpload()).willReturn(uploadProps);
        given(uploadProps.getMaxAttemptCount()).willReturn(MAX_UPLOAD_ATTEMPT_COUNT);
        given(retryProps.getDelete()).willReturn(deleteProps);
        given(deleteProps.getMaxAttemptCount()).willReturn(MAX_DELETION_ATTEMPT_COUNT);

        mediaRetryService = new MediaRetryService(mediaService, eventPublisher, schedulerProperties);
    }

    @Test
    @DisplayName("미디어 업로드 재시도 - 실패한 업로드들에 대해 이벤트 발행")
    void retryFailedUploads_PublishEvents() {
        // given
        Media media = mock(Media.class);
        when(media.getId()).thenReturn(100L);
        when(mediaService.prepareMediaForUploadRetry(MAX_UPLOAD_ATTEMPT_COUNT)).thenReturn(List.of(media));

        // when
        List<Long> result = mediaRetryService.retryFailedUploads();

        // then
        assertThat(result).containsExactly(100L);
        verify(eventPublisher, times(1)).publishEvent(any(MediaChangedEvent.class));
    }

    @Test
    @DisplayName("미디어 삭제 재시도 - 실패한 삭제들에 대해 이벤트 발행")
    void retryFailedDeletions_PublishEvents() {
        // given
        Media media = mock(Media.class);
        when(media.getId()).thenReturn(100L);
        when(mediaService.prepareMediaForDeletionRetry(MAX_DELETION_ATTEMPT_COUNT)).thenReturn(List.of(media));

        // when
        List<Long> result = mediaRetryService.retryFailedDeletions();

        // then
        assertThat(result).containsExactly(100L);
        verify(eventPublisher, times(1)).publishEvent(any(MediaChangedEvent.class));
    }
}
