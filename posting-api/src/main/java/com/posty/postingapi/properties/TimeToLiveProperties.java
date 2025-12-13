package com.posty.postingapi.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "time-to-live")
public class TimeToLiveProperties {

    private Duration accountNameCache;
    private VerificationProperties verification = new VerificationProperties();

    @Getter
    @Setter
    public static class VerificationProperties {

        private Duration rateLimit;
        private Duration code;
        private Duration verified;
    }
}
