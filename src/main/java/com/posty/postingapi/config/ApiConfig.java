package com.posty.postingapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "api")
public class ApiConfig {

    private String keyHeaderName;
    private String xffHeaderName;

    public String getKeyHeaderName() {
        return keyHeaderName;
    }

    public void setKeyHeaderName(String keyHeaderName) {
        this.keyHeaderName = keyHeaderName;
    }

    public String getXffHeaderName() {
        return xffHeaderName;
    }

    public void setXffHeaderName(String xffHeaderName) {
        this.xffHeaderName = xffHeaderName;
    }
}
