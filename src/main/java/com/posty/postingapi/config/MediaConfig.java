package com.posty.postingapi.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "media")
public class MediaConfig {

    private String tempPath;
    private long maxSize;
    private UploadConfig upload = new UploadConfig();
    private DownloadConfig download = new DownloadConfig();
    private ClamAVConfig clamav = new ClamAVConfig();

    @Getter
    @Setter
    public static class UploadConfig {

        private String queueName;
        private FileServerConfig fileServer = new FileServerConfig();

        @Getter
        @Setter
        public static class FileServerConfig {

            private String baseUrl;
            private String uploadPath;
        }
    }

    @Getter
    @Setter
    public static class DownloadConfig {

        private int connectTimeout;
        private int readTimeout;
    }

    @Getter
    @Setter
    public static class ClamAVConfig {

        private String host;
        private int port;
    }
}
