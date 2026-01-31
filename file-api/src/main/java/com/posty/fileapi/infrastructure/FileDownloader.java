package com.posty.fileapi.infrastructure;

import com.posty.fileapi.properties.DownloadConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Slf4j
@Component
public class FileDownloader {

    private final int connectTimeout;
    private final int readTimeout;

    public FileDownloader(DownloadConfig downloadConfig) {
        connectTimeout = downloadConfig.getConnectTimeout();
        readTimeout = downloadConfig.getReadTimeout();
    }

    public void download(URL downloadUrl, Path targetPath) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) downloadUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(connectTimeout);
        connection.setReadTimeout(readTimeout);

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("File download failed with response code=" + responseCode);
        }

        try (InputStream inputStream = connection.getInputStream()) {
            long size = Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.debug("Downloaded file to {} with size: {}", targetPath, size);
        } finally {
            connection.disconnect();
        }
    }
}
