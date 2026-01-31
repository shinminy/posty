package com.posty.fileapi.controller;

import com.posty.fileapi.dto.FileUploadRequest;
import com.posty.fileapi.dto.FileUploadResponse;
import com.posty.fileapi.properties.ApiConfig;
import com.posty.fileapi.service.FileService;
import com.posty.fileapi.service.FileStreamResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Slf4j
@RestController
@Validated
public class FileController {

    private final FileService fileService;

    private final String externalUrl;

    public FileController(FileService fileService, ApiConfig apiConfig) {
        this.fileService = fileService;

        externalUrl = apiConfig.getExternalUrl();
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<StreamingResponseBody> getFile(
            @PathVariable @NotBlank String fileName,
            @RequestHeader(value = HttpHeaders.RANGE, required = false) String range
    ) {
        FileStreamResult result = fileService.getFileStream(fileName, range);

        ResponseEntity.BodyBuilder builder = result.partial()
                ? ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                : ResponseEntity.ok();

        if (result.contentRange() != null) {
            builder.header(HttpHeaders.CONTENT_RANGE, result.contentRange());
        }

        return builder
                .header(HttpHeaders.CONTENT_TYPE, result.contentType())
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(result.contentLength()))
                .body(result.body());
    }

    @PostMapping
    public ResponseEntity<FileUploadResponse> upload(@Valid @RequestBody FileUploadRequest request) {
        String fileName = fileService.storeFile(request.mediaType(), request.originUrl());

        URI location = ServletUriComponentsBuilder
                .fromUriString(externalUrl)
                .pathSegment(fileName)
                .build()
                .toUri();

        return ResponseEntity
                .created(location)
                .body(new FileUploadResponse(location.toString(), fileName));
    }

    @DeleteMapping("/{fileName}")
    public ResponseEntity<Void> deleteFile(@PathVariable @NotBlank String fileName) {
        fileService.deleteFile(fileName);
        return ResponseEntity.noContent().build();
    }
}
