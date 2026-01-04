package com.posty.fileapi.service;

import com.posty.common.domain.post.MediaType;
import com.posty.fileapi.common.FileNameParts;
import com.posty.fileapi.common.FileNameUtil;
import com.posty.fileapi.common.UUIDUtil;
import com.posty.fileapi.properties.DirConfig;
import com.posty.fileapi.dto.FileData;
import com.posty.fileapi.infrastructure.FileDownloader;
import com.posty.fileapi.infrastructure.FileValidator;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.utils.StringUtils;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Slf4j
@Service
public class FileService {

    private final FileValidator fileValidator;
    private final FileDownloader fileDownloader;

    private final Path tempPath;
    private final Path basePath;

    public FileService(
            FileValidator fileValidator,
            FileDownloader fileDownloader,
            DirConfig dirConfig
    ) {
        this.fileValidator = fileValidator;
        this.fileDownloader = fileDownloader;

        tempPath = Paths.get(dirConfig.getTemp());
        basePath = Paths.get(dirConfig.getBase());
    }

    @PostConstruct
    public void init() throws IOException {
        createDir(tempPath);
        createDir(basePath);
    }

    private void createDir(Path path) throws IOException {
        Files.createDirectories(path);
    }

    public FileData getFile(String fileName) throws IOException {
        Path filePath = basePath.resolve(fileName);
        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("File not found: " + fileName);
        }

        String contentType = fileValidator.detectContentType(filePath);
        byte[] bytes = Files.readAllBytes(filePath);

        return new FileData(contentType, bytes);
    }

    public String storeFile(MediaType mediaType, String originUrl) throws IOException {
        URL downloadUrl = new URL(originUrl);

        String dotExtension = fileValidator.getDotExtensionIfValidMimeType(downloadUrl, mediaType);
        if (StringUtils.isBlank(dotExtension)) {
            throw new IllegalArgumentException("Invalid MIME type!");
        }

        FileNameParts fileNameParts = FileNameUtil.parseFileNameFromUrl(downloadUrl);
        String tempFileName = fileNameParts.name() + "-" + System.currentTimeMillis() + dotExtension;
        Path tempFilePath = tempPath.resolve(tempFileName);

        fileDownloader.download(downloadUrl, tempFilePath);

        if (!fileValidator.isValidSize(tempFilePath)) {
            throw new IllegalArgumentException("Invalid file size!");
        }

        if (fileValidator.isMaliciousFile(tempFilePath)) {
            throw new IllegalArgumentException("Malicious file!");
        }

        String fileName = System.currentTimeMillis() + UUIDUtil.getUUIDWithoutDash() + dotExtension;
        Path targetPath = basePath.resolve(fileName);

        Files.copy(tempFilePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        log.info("File {} has been stored!", fileName);

        try {
            Files.deleteIfExists(tempFilePath);
        } catch (IOException e) {
            log.error("Failed to delete temporary file (temporary file: {})", tempFilePath, e);
        }

        return fileName;
    }

    public void deleteFile(String fileName) throws IOException {
        Path filePath = basePath.resolve(fileName);
        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("File not found: " + fileName);
        }

        Files.delete(filePath);
        log.info("File {} has been deleted!", fileName);
    }
}
