package com.posty.postingapi.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "scheduler")
public class SchedulerConfig {

    private AccountSchedulerConfig account = new AccountSchedulerConfig();
    private MediaSchedulerConfig media = new MediaSchedulerConfig();

    @Getter
    @Setter
    public static class AccountSchedulerConfig {

        private AccountDeletionConfig deletion = new AccountDeletionConfig();

        @Getter
        @Setter
        public static class AccountDeletionConfig {

            private int gracePeriodDays;
        }
    }

    @Getter
    @Setter
    public static class MediaSchedulerConfig {

        private MediaRetryConfig retry = new MediaRetryConfig();

        @Getter
        @Setter
        public static class MediaRetryConfig {

            private MediaUploadRetryConfig upload = new MediaUploadRetryConfig();
            private MediaDeleteRetryConfig delete = new MediaDeleteRetryConfig();

            @Getter
            @Setter
            public static class MediaUploadRetryConfig {

                private int maxAttemptCount;
            }

            @Getter
            @Setter
            public static class MediaDeleteRetryConfig {

                private int maxAttemptCount;
            }
        }
    }
}
