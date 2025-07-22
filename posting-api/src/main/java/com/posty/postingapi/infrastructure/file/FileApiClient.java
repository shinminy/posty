package com.posty.postingapi.infrastructure.file;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.posty.common.dto.FileUploadRequest;
import com.posty.common.dto.FileUploadResponse;
import com.posty.postingapi.properties.MediaConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Slf4j
@Component
public class FileApiClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final String fileApiUrl;
    private final String fileApiToken;

    public FileApiClient(RestTemplate restTemplate, ObjectMapper objectMapper, MediaConfig mediaConfig) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;

        fileApiUrl = mediaConfig.getFileApiUrl();
        fileApiToken = mediaConfig.getFileApiToken();
    }

    public FileUploadResponse upload(FileUploadRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(fileApiToken);

        HttpEntity<FileUploadRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                fileApiUrl,
                HttpMethod.POST,
                entity,
                String.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            try {
                return objectMapper.readValue(response.getBody(), FileUploadResponse.class);
            } catch (JsonProcessingException e) {
                log.error("Failed to parse response", e);
                return null;
            }
        } else {
            log.error("Failed to upload! ([{}] {})", response.getStatusCode(), response.getBody());
            return null;
        }
    }

    public boolean delete(String fileName) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(fileApiUrl)
                .pathSegment(fileName)
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(fileApiToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                uri,
                HttpMethod.DELETE,
                entity,
                String.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            return true;
        } else {
            log.error("Failed to delete! ([{}] {})", response.getStatusCode(), response.getBody());
            return false;
        }
    }
}
