package com.posty.postingapi.infrastructure.file;

import com.posty.common.dto.FileRequest;
import com.posty.common.dto.FileResponse;
import com.posty.postingapi.properties.MediaConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class FileUploader {

    private final RestTemplate restTemplate;

    private final String uploadUrl;
    private final String uploadToken;

    public FileUploader(RestTemplate restTemplate, MediaConfig mediaConfig) {
        this.restTemplate = restTemplate;

        uploadUrl = mediaConfig.getUploadUrl();
        uploadToken = mediaConfig.getUploadToken();
    }

    public FileResponse upload(FileRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(uploadToken);

        HttpEntity<FileRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<FileResponse> response = restTemplate.postForEntity(uploadUrl, entity, FileResponse.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            return null;
        }
    }
}
