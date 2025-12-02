package com.posty.postingapi.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "api")
public class ApiProperties {

    private String requestIdName;
    private String keyHeaderName;
    private String xffHeaderName;

    private String authTypeKey;
}
