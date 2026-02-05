package com.posty.fileapi.controller;

import com.posty.fileapi.dto.FileUploadRequest;
import com.posty.fileapi.dto.FileUploadResponse;
import com.posty.fileapi.error.FileIOException;
import com.posty.fileapi.properties.ApiConfig;
import com.posty.fileapi.service.FileService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
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

    // 게이트웨이(Nginx 등)가 없는 환경(로컬/테스트)에서 발생하는 favicon 요청을 막기 위한 fallback 처리
    @GetMapping("/favicon.ico")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void favicon() {
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<ResourceRegion> getFile(
            @PathVariable @NotBlank String fileName,
            @RequestHeader HttpHeaders headers
    ) {
        Resource resource = fileService.getFileResource(fileName);

        long contentLength;
        try {
            contentLength = resource.contentLength();
        } catch (IOException e) {
            throw new FileIOException("Failed to read file size");
        }

        MediaType mediaType = MediaTypeFactory
                .getMediaType(resource)
                .orElse(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM);

        if (headers.getRange().isEmpty()) {
            ResourceRegion region = new ResourceRegion(resource, 0, contentLength);
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(region);
        }

        HttpRange range = headers.getRange().get(0);
        long start = range.getRangeStart(contentLength);
        long end = range.getRangeEnd(contentLength);
        long rangeLength = end - start + 1;

        ResourceRegion region = new ResourceRegion(resource, start, rangeLength);

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(mediaType)
                .body(region);
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
