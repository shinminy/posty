package com.posty.fileapi.infrastructure;

import com.posty.fileapi.properties.ValidationConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

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

    public String detectContentType(Path filePath) throws IOException {
        return tika.detect(filePath);
    }

    public String getDotExtensionIfValidMimeType(URL url, MimeMediaType expected) throws IOException {
        String detectedType = tika.detect(url);
        log.debug("Detected type is {}", detectedType);

        if (!expected.matches(detectedType)) {
            return null;
        }

        MimeTypes allTypes = MimeTypes.getDefaultMimeTypes();
        try {
            return allTypes.forName(detectedType).getExtension();
        } catch (MimeTypeException e) {
            log.error("Failed to get extension from {}", detectedType, e);
            return null;
        }
    }

    public boolean isValidSize(Path filePath) throws IOException {
        long size = Files.size(filePath);
        log.debug("File size is {}", size);

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
