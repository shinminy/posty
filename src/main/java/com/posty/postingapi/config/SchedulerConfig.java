package com.posty.postingapi.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "scheduler")
public class SchedulerConfig {

    private Account account = new Account();

    @Getter
    @Setter
    public static class Account {

        private Deletion deletion = new Deletion();

        @Getter
        @Setter
        public static class Deletion {

            private int gracePeriodDays;
        }
    }
}
