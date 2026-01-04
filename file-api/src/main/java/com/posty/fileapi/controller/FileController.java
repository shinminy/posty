package com.posty.fileapi.controller;

import com.posty.common.dto.FileUploadRequest;
import com.posty.common.dto.FileUploadResponse;
import com.posty.fileapi.dto.FileData;
import com.posty.fileapi.properties.ApiConfig;
import com.posty.fileapi.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;

@Slf4j
@RestController
public class FileController {

    private final FileService fileService;

    private final String externalUrl;

    public FileController(FileService fileService, ApiConfig apiConfig) {
        this.fileService = fileService;

        externalUrl = apiConfig.getExternalUrl();
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<byte[]> getFile(@PathVariable String fileName) {
        FileData fileData;
        try {
            fileData = fileService.getFile(fileName);
        } catch (FileNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }

        String contentType = fileData.contentType() == null ? "application/octet-stream" : fileData.contentType();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(fileData.bytes());
    }

    @PostMapping
    public ResponseEntity<?> upload(@RequestBody FileUploadRequest request) {
        String fileName;
        try {
            fileName = fileService.storeFile(request.getMediaType(), request.getOriginUrl());
        } catch (MalformedURLException e) {
            String message = "Invalid URL!";
            log.error("{}", message, e);
            return ResponseEntity.badRequest().body(message);
        } catch (IllegalArgumentException e) {
            String message = e.getMessage();
            log.error("{}", message, e);
            return ResponseEntity.badRequest().body(message);
        } catch (IOException e) {
            String message = "Failed to save file...";
            log.error("{}", message, e);
            return ResponseEntity.internalServerError().body(message);
        }

        URI location = ServletUriComponentsBuilder
                .fromUriString(externalUrl)
                .pathSegment(fileName)
                .build()
                .toUri();
        return ResponseEntity.created(location).body(new FileUploadResponse(location.toString(), fileName));
    }

    @DeleteMapping("/{fileName}")
    public ResponseEntity<?> deleteFile(@PathVariable String fileName) {
        try {
            fileService.deleteFile(fileName);
        } catch (FileNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            String message = "Failed to delete file...";
            log.error("{}", message, e);
            return ResponseEntity.internalServerError().body(message);
        }

        return ResponseEntity.noContent().build();
    }
}
