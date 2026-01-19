package com.posty.postingapi.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "scheduler")
public class SchedulerProperties {

    private AccountSchedulerProperties account = new AccountSchedulerProperties();
    private MediaSchedulerProperties media = new MediaSchedulerProperties();

    @Getter
    @Setter
    public static class AccountSchedulerProperties {

        private AccountDeletionProperties deletion = new AccountDeletionProperties();

        @Getter
        @Setter
        public static class AccountDeletionProperties {

            private int gracePeriodDays;
            private int batchSize;
        }
    }

    @Getter
    @Setter
    public static class MediaSchedulerProperties {

        private MediaRetryProperties retry = new MediaRetryProperties();

        @Getter
        @Setter
        public static class MediaRetryProperties {

            private MediaUploadRetryProperties upload = new MediaUploadRetryProperties();
            private MediaDeleteRetryProperties delete = new MediaDeleteRetryProperties();

            @Getter
            @Setter
            public static class MediaUploadRetryProperties {

                private int maxAttemptCount;
            }

            @Getter
            @Setter
            public static class MediaDeleteRetryProperties {

                private int maxAttemptCount;
            }
        }
    }
}
