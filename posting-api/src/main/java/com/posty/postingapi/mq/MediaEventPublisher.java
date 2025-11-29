package com.posty.postingapi.mq;

import com.posty.postingapi.domain.post.Media;
import com.posty.postingapi.properties.MediaProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MediaEventPublisher {

    private final JmsTemplate jmsTemplate;

    private final String uploadQueueName;
    private final String deleteQueueName;

    public MediaEventPublisher(JmsTemplate jmsTemplate, MediaProperties mediaProperties) {
        this.jmsTemplate = jmsTemplate;

        uploadQueueName = mediaProperties.getUploadQueueName();
        deleteQueueName = mediaProperties.getDeleteQueueName();
    }

    public void publishMediaUpload(Media media) {
        jmsTemplate.convertAndSend(uploadQueueName, media.getId());
    }

    public void publishMediaDelete(Media media) {
        jmsTemplate.convertAndSend(deleteQueueName, media.getId());
    }
}
