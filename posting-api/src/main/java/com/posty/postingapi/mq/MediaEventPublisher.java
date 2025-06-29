package com.posty.postingapi.mq;

import com.posty.postingapi.config.MediaConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MediaEventPublisher {

    private final JmsTemplate jmsTemplate;

    private final String uploadQueueName;

    public MediaEventPublisher(JmsTemplate jmsTemplate, MediaConfig mediaConfig) {
        this.jmsTemplate = jmsTemplate;

        uploadQueueName = mediaConfig.getUpload().getQueueName();
    }

    public void publishMediaUpload(Long mediaId) {
        jmsTemplate.convertAndSend(uploadQueueName, mediaId);
    }
}
