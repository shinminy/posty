package com.posty.postingapi.config;

import com.posty.postingapi.properties.TimeProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Getter
@Setter
@Configuration
public class TimeConfig {

    private String zoneId;

    public TimeConfig(TimeProperties timeProperties) {
        zoneId = timeProperties.getZoneId();
    }

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of(zoneId));
    }
}
