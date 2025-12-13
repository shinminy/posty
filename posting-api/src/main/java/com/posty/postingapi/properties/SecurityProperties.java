package com.posty.postingapi.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    private jwtProperties jwt = new jwtProperties();

    @Getter
    @Setter
    public static class jwtProperties {

        private Duration accessExpiry;
        private Duration refreshExpiry;
    }
}
