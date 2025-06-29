package com.posty.postingapi.service.application;

import com.posty.postingapi.common.FileNameUtil;
import com.posty.postingapi.config.MediaConfig;
import com.posty.postingapi.domain.post.Media;
import com.posty.postingapi.domain.post.MediaRepository;
import com.posty.postingapi.domain.post.MediaType;
import com.posty.postingapi.error.ResourceNotFoundException;
import com.posty.postingapi.infrastructure.file.FileDownloader;
import com.posty.postingapi.infrastructure.file.FileUploader;
import com.posty.postingapi.infrastructure.file.FileValidator;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class MediaService {

    private final MediaRepository mediaRepository;
    private final Clock clock;
    private final FileDownloader fileDownloader;
    private final FileValidator fileValidator;
    private final FileUploader fileUploader;

    private final Path tempPath;
    private final long maxFileSize;
    private final Path uploadPath;
    private final String uploadUrl;
    private final int downloadConnectTimeout;
    private final int downloadReadTimeout;

    public MediaService(
            MediaRepository mediaRepository,
            Clock clock,
            FileDownloader fileDownloader,
            FileValidator fileValidator,
            FileUploader fileUploader,
            MediaConfig mediaConfig
    ) {
        this.mediaRepository = mediaRepository;
        this.clock = clock;
        this.fileDownloader = fileDownloader;
        this.fileValidator = fileValidator;
        this.fileUploader = fileUploader;

        tempPath = Paths.get(mediaConfig.getTempPath());
        if (!Files.exists(tempPath)) {
            try {
                Files.createDirectories(tempPath);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create temp directory...", e);
            }
        }
        maxFileSize = mediaConfig.getMaxSize() * 1024 * 1024;
        MediaConfig.UploadConfig.FileServerConfig fileServerConfig = mediaConfig.getUpload().getFileServer();
        String uploadDir = fileServerConfig.getUploadPath();
        uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create upload directory...", e);
            }
        }
        uploadUrl = fileServerConfig.getBaseUrl() + uploadDir;
        MediaConfig.DownloadConfig downloadConfig = mediaConfig.getDownload();
        downloadConnectTimeout = downloadConfig.getConnectTimeout();
        downloadReadTimeout = downloadConfig.getReadTimeout();
    }

    @Transactional
    public void upload(Long mediaId) {
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new ResourceNotFoundException("Media", mediaId));

        String originUrl = media.getOriginUrl();
        MediaType mediaType = media.getMediaType();

        String mediaDirName = mediaType.name().toLowerCase();
        Path tempMediaPath = tempPath.resolve(mediaDirName);
        if (!Files.exists(tempMediaPath)) {
            try {
                Files.createDirectories(tempMediaPath);
            } catch (IOException e) {
                log.error("Failed to create media directory... (ID: {}, directory: {})", mediaId, tempMediaPath, e);
                return;
            }
        }

        URL downloadUrl;
        try {
            downloadUrl = new URL(originUrl);
        } catch (MalformedURLException e) {
            log.error("Invalid URL (ID: {}, URL: {})", mediaId, originUrl, e);
            return;
        }

        String fileExtension = FileNameUtil.extractExtension(downloadUrl);
        String tempFileName = mediaId + "-" + System.currentTimeMillis() + fileExtension;
        Path tempFilePath = tempMediaPath.resolve(tempFileName);

        try {
            if (!fileValidator.isValidMimeType(downloadUrl, mediaType)) {
                throw new RuntimeException("Invalid MIME type!");
            }

            fileDownloader.download(downloadUrl, tempFilePath, downloadConnectTimeout, downloadReadTimeout);

            if (!fileValidator.isValidSize(tempFilePath, maxFileSize)) {
                throw new RuntimeException("Invalid file size!");
            }

            // TODO: 서버 저장 공간 문제 해결 후 악성 파일 검사 기능 활성화
            //  if (fileValidator.isMaliciousFile(tempFilePath)) {
            //      Files.deleteIfExists(tempFilePath);
            //      throw new RuntimeException("Malicious file!");
            //  }
            if (fileValidator.isMaliciousFile(tempFilePath)) {
                throw new RuntimeException("Malicious file!");
            }

            // TODO: 파일 서버 세팅 및 업로드 API 구현 시 해당 메서드 사용
            //  String storedUrl = fileUploader.upload(tempFilePath, new URL(storedUrl));
            String storedUrl = fileUploader.upload(tempFilePath, mediaDirName, fileExtension);

            mediaRepository.save(media.succeeded(storedUrl, LocalDateTime.now(clock)));
        } catch (Exception e) {
            log.error("Failed to upload media (ID: {}, URL: {})", mediaId, originUrl, e);
            mediaRepository.save(media.failed(LocalDateTime.now(clock)));
        }

        try {
            Files.deleteIfExists(tempFilePath);
        } catch (IOException e) {
            log.error("Failed to delete temporary file (ID: {}, temporary file: {})", mediaId, tempFilePath, e);
        }
    }
}
