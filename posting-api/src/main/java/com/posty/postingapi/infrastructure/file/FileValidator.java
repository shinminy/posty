package com.posty.postingapi.infrastructure.file;

import com.posty.postingapi.domain.post.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Component
public class FileValidator {

    private final ClamAVScanner clamAVScanner;

    private final Tika tika;

    public FileValidator(ClamAVScanner clamAVScanner) {
        this.clamAVScanner = clamAVScanner;

        tika = new Tika();
    }

    public boolean isValidMimeType(URL url, MediaType expected) throws IOException {
        String detectedType = tika.detect(url);
        log.debug("Detected type is {}", detectedType);

        switch (expected) {
            case IMAGE:
                return detectedType.startsWith("image/");
            case VIDEO:
                return detectedType.startsWith("video/");
            case AUDIO:
                return detectedType.startsWith("audio/");
            default:
                return false;
        }
    }

    public boolean isValidSize(Path filePath, long maxFileSize) throws IOException {
        long size = Files.size(filePath);
        log.debug("File size is {}", size);

        return size > 0 || size < maxFileSize;
    }

    public boolean isMaliciousFile(Path filePath) {
        try {
            return !clamAVScanner.scanFile(filePath.toString());
        } catch (Exception e) {
            throw new RuntimeException("ClamAV scan failed", e);
        }
    }
}
