package com.posty.fileapi.infrastructure;

import com.posty.fileapi.error.FileIOException;
import com.posty.fileapi.properties.ValidationConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Slf4j
@Component
public class FileValidator {

    private final ClamAVScanner clamAVScanner;

    private final long maxSize;

    private final Tika tika;

    public FileValidator(ClamAVScanner clamAVScanner, ValidationConfig validationConfig) {
        this.clamAVScanner = clamAVScanner;

        maxSize = validationConfig.getMaxSize();

        tika = new Tika();
    }

    public String detectContentType(Path filePath) {
        try {
            return tika.detect(filePath);
        } catch (IOException e) {
            log.error("Failed to read {} for MIME detection", filePath, e);
            throw new FileIOException("Failed to detect mime type");
        }
    }

    public Optional<String> getDotExtensionIfValidMimeType(URL url, MimeMediaType expected) {
        String detectedType;
        try {
            detectedType = tika.detect(url);
        } catch (IOException e) {
            log.error("Failed to read {} for MIME detection", url.toString(), e);
            throw new FileIOException("Failed to detect mime type");
        }
        log.debug("Detected type is {}", detectedType);

        if (!expected.matches(detectedType)) {
            return Optional.empty();
        }

        MimeTypes allTypes = MimeTypes.getDefaultMimeTypes();
        MimeType mimeType;
        try {
            mimeType = allTypes.forName(detectedType);
        } catch (MimeTypeException e) {
            log.error("Failed to get extension from {}", detectedType, e);
            return Optional.empty();
        }

        return Optional.of(mimeType.getExtension());
    }

    public boolean isValidSize(Path filePath) {
        long size;
        try {
            size = Files.size(filePath);
            log.debug("File size is {}", size);
        } catch (IOException e) {
            log.error("Failed to read size of {}", filePath, e);
            throw new FileIOException("Failed to read file size");
        }

        return size > 0 || size < maxSize;
    }

    public boolean isMaliciousFile(Path filePath) {
        try {
            return !clamAVScanner.scanFile(filePath.toString());
        } catch (Exception e) {
            throw new RuntimeException("ClamAV scan failed", e);
        }
    }
}
