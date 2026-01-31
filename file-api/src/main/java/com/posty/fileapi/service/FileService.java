package com.posty.fileapi.service;

import com.posty.fileapi.common.FileNameParts;
import com.posty.fileapi.common.FileNameUtil;
import com.posty.fileapi.common.UUIDUtil;
import com.posty.fileapi.dto.MediaType;
import com.posty.fileapi.error.FileIOException;
import com.posty.fileapi.error.InvalidFileException;
import com.posty.fileapi.error.InvalidURLException;
import com.posty.fileapi.error.StoredFileNotFoundException;
import com.posty.fileapi.infrastructure.MimeMediaType;
import com.posty.fileapi.properties.DirConfig;
import com.posty.fileapi.infrastructure.FileDownloader;
import com.posty.fileapi.infrastructure.FileValidator;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.SeekableByteChannel;
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
        Files.createDirectories(tempPath);
        Files.createDirectories(basePath);
    }

    public FileStreamResult getFileStream(String fileName, String rangeHeader) {
        Path filePath = basePath.resolve(fileName);
        if (!Files.exists(filePath)) {
            throw new StoredFileNotFoundException(fileName);
        }

        long totalSize;
        try {
            totalSize = Files.size(filePath);
        } catch (IOException e) {
            log.error("Failed to read size of {}", fileName, e);
            throw new FileIOException("Failed to read file size");
        }
        ByteRange range = ByteRange.parse(rangeHeader, totalSize);

        StreamingResponseBody body = output -> {
            try (SeekableByteChannel channel = Files.newByteChannel(filePath)) {
                channel.position(range.start());

                long remaining = range.length();
                byte[] buffer = new byte[8192];

                while (remaining > 0) {
                    int read = channel.read(java.nio.ByteBuffer.wrap(
                            buffer, 0, (int) Math.min(buffer.length, remaining)
                    ));
                    if (read == -1) break;

                    output.write(buffer, 0, read);
                    remaining -= read;
                }
            } catch (IOException e) {
                String message = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
                if (e instanceof org.apache.catalina.connector.ClientAbortException ||
                        message.contains("broken pipe") ||
                        message.contains("connection reset")) {
                    return;
                }

                log.error("File streaming of {} failed.", filePath, e);
                throw e;
            }
        };

        String contentType = fileValidator.detectContentType(filePath);
        boolean partial = range.partial();

        return new FileStreamResult(
                body,
                range.length(),
                contentType,
                partial ? range.toContentRangeHeader() : null,
                partial
        );
    }

    public String storeFile(MediaType mediaType, String originUrl) {
        URL downloadUrl;
        try {
            downloadUrl = new URL(originUrl);
        } catch (MalformedURLException e) {
            log.error("Failed to parse url {}", originUrl, e);
            throw new InvalidURLException();
        }

        String dotExtension;
        dotExtension = fileValidator.getDotExtensionIfValidMimeType(downloadUrl, MimeMediaType.from(mediaType))
                .orElseThrow(() -> new InvalidFileException("Invalid MIME type!"));

        FileNameParts fileNameParts = FileNameUtil.parseFileNameFromUrl(downloadUrl);
        String tempFileName = fileNameParts.name() + "-" + System.currentTimeMillis() + dotExtension;
        Path tempFilePath = tempPath.resolve(tempFileName);

        try {
            fileDownloader.download(downloadUrl, tempFilePath);
        } catch (IOException e) {
            log.error("Failed to download file from {}", downloadUrl, e);
            throw new FileIOException("Failed to download file from url");
        }

        if (!fileValidator.isValidSize(tempFilePath)) {
            throw new InvalidFileException("Invalid file size!");
        }

        if (fileValidator.isMaliciousFile(tempFilePath)) {
            throw new InvalidFileException("Malicious file!");
        }

        String fileName = System.currentTimeMillis() + UUIDUtil.getUUIDWithoutDash() + dotExtension;
        Path targetPath = basePath.resolve(fileName);

        try {
            Files.copy(tempFilePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("File {} has been stored!", fileName);
        } catch (IOException e) {
            log.error("Failed to store {}!", tempFileName, e);
            throw new FileIOException("Failed to store file");
        }

        try {
            Files.deleteIfExists(tempFilePath);
        } catch (IOException e) {
            log.error("Failed to delete temporary file (temporary file: {})", tempFilePath, e);
        }

        return fileName;
    }

    public void deleteFile(String fileName) {
        Path filePath = basePath.resolve(fileName);
        if (!Files.exists(filePath)) {
            throw new StoredFileNotFoundException(fileName);
        }

        try {
            Files.delete(filePath);
            log.info("File {} has been deleted!", fileName);
        } catch (IOException e) {
            log.error("Failed to delete {}!", fileName, e);
            throw new FileIOException("Failed to delete file");
        }
    }
}
