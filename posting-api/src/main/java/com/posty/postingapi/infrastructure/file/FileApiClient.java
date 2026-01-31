package com.posty.postingapi.infrastructure.file;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.posty.postingapi.error.FileCommunicationException;
import com.posty.postingapi.properties.MediaProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
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

    public FileApiClient(RestTemplate restTemplate, ObjectMapper objectMapper, MediaProperties mediaProperties) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;

        fileApiUrl = mediaProperties.getFileApiUrl();
        fileApiToken = mediaProperties.getFileApiToken();
    }

    public FileUploadResponse upload(FileUploadRequest request) throws JsonProcessingException {
        HttpMethod method = HttpMethod.POST;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(fileApiToken);

        HttpEntity<FileUploadRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(
                    fileApiUrl,
                    method,
                    entity,
                    String.class
            );
        } catch (HttpStatusCodeException e) {
            throw new FileCommunicationException(method, e.getStatusCode(), e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new FileCommunicationException(method, e);
        }

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new FileCommunicationException(method, response.getStatusCode(), response.getBody());
        }

        return objectMapper.readValue(response.getBody(), FileUploadResponse.class);
    }

    public void delete(String fileName) {
        URI uri = UriComponentsBuilder
                .fromUriString(fileApiUrl)
                .pathSegment(fileName)
                .build()
                .toUri();

        HttpMethod method = HttpMethod.DELETE;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(fileApiToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(
                    uri,
                    method,
                    entity,
                    String.class
            );
        } catch (HttpStatusCodeException e) {
            throw new FileCommunicationException(method, e.getStatusCode(), e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new FileCommunicationException(method, e);
        }

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new FileCommunicationException(method, response.getStatusCode(), response.getBody());
        }
    }
}
