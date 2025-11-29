package com.posty.postingapi.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "media")
public class MediaProperties {

    private String fileApiUrl;
    private String fileApiToken;
    private String uploadQueueName;
    private String deleteQueueName;
}
