package com.posty.postingapi.infrastructure.mail;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "mail")
public class MailProperties {

    private FromProperties from = new FromProperties();

    @Getter
    @Setter
    public static class FromProperties {

        private String noReply;
    }
}
