package com.posty.postingapi.infrastructure.file;

import com.posty.postingapi.config.MediaConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Component
public class FileUploader {

    // TODO: 추후 파일 업로드 API 구현 시 제거
    private final Path uploadPath;

    public FileUploader(MediaConfig mediaConfig) {
        String uploadDir = mediaConfig.getUpload().getFileServer().getUploadPath();
        uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create upload directory...", e);
            }
        }
    }

    // TODO: 파일 서버 세팅 및 업로드 API 구현 시 해당 메서드 사용
    public String upload(Path filePath, URL uploadUrl) throws IOException {
        log.debug("Uploading {} to {}", filePath, uploadUrl);

        HttpURLConnection connection = (HttpURLConnection) uploadUrl.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");

        try (InputStream inputStream = Files.newInputStream(filePath)) {
            inputStream.transferTo(connection.getOutputStream());
        }

        int responseCode = connection.getResponseCode();
        if (responseCode < HttpURLConnection.HTTP_MULT_CHOICE) {
            log.debug("Uploaded file to {} with response code={}", uploadUrl, responseCode);
            // todo: 추후 응답 형태에 따라 수정 필요
            return connection.getResponseMessage();
        } else {
            throw new IOException("Upload failed with response code=" + responseCode);
        }
    }

    public String upload(Path filePath, String mediaDirName, String fileExtension) throws IOException {
        String fileName = System.currentTimeMillis() + "-" + UUID.randomUUID() + fileExtension;
        Path uploadMediaPath = uploadPath.resolve(mediaDirName);
        if (!Files.exists(uploadMediaPath)) {
            try {
                Files.createDirectories(uploadMediaPath);
            } catch (IOException e) {
                throw new IOException("Failed to create upload media directory... directory: " + uploadMediaPath);
            }
        }

        Files.copy(filePath, uploadMediaPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        //return uploadUrl + "/" + mediaDirName +  "/" + fileName;
        return null;
    }
}
