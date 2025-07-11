package com.posty.fileapi.controller;

import com.posty.common.dto.FileRequest;
import com.posty.common.dto.FileResponse;
import com.posty.fileapi.dto.FileData;
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
@RequestMapping("/file")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
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
    public ResponseEntity<?> upload(@RequestBody FileRequest fileRequest) {
        String fileName;
        try {
            fileName = fileService.storeFile(fileRequest.getMediaType(), fileRequest.getOriginUrl());
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

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .pathSegment(fileName)
                .build()
                .toUri();
        return ResponseEntity.created(location).body(new FileResponse(location.toString()));
    }
}
