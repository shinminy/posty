package com.posty.postingapi.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "time")
public class TimeConfig {

    private String zoneId;

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of(zoneId));
    }
}
