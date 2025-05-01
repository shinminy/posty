package com.posty.postingapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
@ConfigurationProperties(prefix = "time")
public class TimeConfig {

    private String zoneId;

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of(zoneId));
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }
}
