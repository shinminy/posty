package com.posty.postingapi.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "media")
public class MediaConfig {

    private String uploadQueueName;
    private String uploadUrl;
    private String uploadToken;
}
