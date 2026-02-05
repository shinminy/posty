package com.posty.fileapi.service;

import com.posty.fileapi.dto.MediaType;
import com.posty.fileapi.error.InvalidFileException;
import com.posty.fileapi.error.StoredFileNotFoundException;
import com.posty.fileapi.infrastructure.FileDownloader;
import com.posty.fileapi.infrastructure.FileValidator;
import com.posty.fileapi.infrastructure.MimeMediaType;
import com.posty.fileapi.properties.DirConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private FileValidator fileValidator;

    @Mock
    private FileDownloader fileDownloader;

    @Mock
    private DirConfig dirConfig;

    @TempDir
    Path tempFolder;

    private FileService fileService;
    private Path basePath;
    private Path tempPath;

    @BeforeEach
    void setUp() throws IOException {
        basePath = tempFolder.resolve("base");
        tempPath = tempFolder.resolve("temp");
        Files.createDirectories(basePath);
        Files.createDirectories(tempPath);

        given(dirConfig.getBase()).willReturn(basePath.toString());
        given(dirConfig.getTemp()).willReturn(tempPath.toString());

        fileService = new FileService(fileValidator, fileDownloader, dirConfig);
    }

    @Test
    @DisplayName("파일 조회 성공 - Resource 반환")
    void getFileResource_Success() throws Exception {
        // given
        String fileName = "test.txt";
        Path filePath = basePath.resolve(fileName);
        Files.write(filePath, "test content".getBytes());

        // when
        Resource resource = fileService.getFileResource(fileName);

        // then
        assertThat(resource).isNotNull();
        assertThat(resource.exists()).isTrue();
        assertThat(resource.getFilename()).isEqualTo(fileName);
    }

    @Test
    @DisplayName("파일 조회 실패 - 파일 없음")
    void getFileResource_NotFound() {
        // when & then
        assertThatThrownBy(() -> fileService.getFileResource("nonexistent.txt"))
                .isInstanceOf(StoredFileNotFoundException.class);
    }

    @Test
    @DisplayName("파일 저장 성공")
    void storeFile_Success() throws IOException {
        // given
        MediaType mediaType = MediaType.IMAGE;
        String originUrl = "https://example.com/image.jpg";
        given(fileValidator.getDotExtensionIfValidMimeType(any(URL.class), eq(MimeMediaType.from(mediaType))))
                .willReturn(Optional.of(".jpg"));
        given(fileValidator.isValidSize(any(Path.class))).willReturn(true);
        given(fileValidator.isMaliciousFile(any(Path.class))).willReturn(false);

        // download 시 임시 파일 생성을 흉내냄
        doAnswer(invocation -> {
            Path targetPath = invocation.getArgument(1);
            Files.write(targetPath, "dummy image content".getBytes());
            return null;
        }).when(fileDownloader).download(any(URL.class), any(Path.class));

        // when
        String fileName = fileService.storeFile(mediaType, originUrl);

        // then
        assertThat(fileName).isNotNull();
        assertThat(fileName).endsWith(".jpg");
        assertThat(Files.exists(basePath.resolve(fileName))).isTrue();
    }

    @Test
    @DisplayName("파일 저장 실패 - 잘못된 MIME 타입")
    void storeFile_InvalidMimeType() throws IOException {
        // given
        MediaType mediaType = MediaType.IMAGE;
        String originUrl = "https://example.com/not-image.txt";
        given(fileValidator.getDotExtensionIfValidMimeType(any(URL.class), eq(MimeMediaType.from(mediaType))))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> fileService.storeFile(mediaType, originUrl))
                .isInstanceOf(InvalidFileException.class)
                .hasMessage("Invalid MIME type!");
    }

    @Test
    @DisplayName("파일 저장 실패 - 크기 초과")
    void storeFile_InvalidSize() throws IOException {
        // given
        MediaType mediaType = MediaType.IMAGE;
        String originUrl = "https://example.com/large.jpg";
        given(fileValidator.getDotExtensionIfValidMimeType(any(URL.class), eq(MimeMediaType.from(mediaType))))
                .willReturn(Optional.of(".jpg"));
        given(fileValidator.isValidSize(any(Path.class))).willReturn(false);

        doAnswer(invocation -> {
            Path targetPath = invocation.getArgument(1);
            Files.write(targetPath, "large content".getBytes());
            return null;
        }).when(fileDownloader).download(any(URL.class), any(Path.class));

        // when & then
        assertThatThrownBy(() -> fileService.storeFile(mediaType, originUrl))
                .isInstanceOf(InvalidFileException.class)
                .hasMessage("Invalid file size!");
    }

    @Test
    @DisplayName("파일 저장 실패 - 악성 파일")
    void storeFile_MaliciousFile() throws IOException {
        // given
        MediaType mediaType = MediaType.IMAGE;
        String originUrl = "https://example.com/virus.jpg";
        given(fileValidator.getDotExtensionIfValidMimeType(any(URL.class), eq(MimeMediaType.from(mediaType))))
                .willReturn(Optional.of(".jpg"));
        given(fileValidator.isValidSize(any(Path.class))).willReturn(true);
        given(fileValidator.isMaliciousFile(any(Path.class))).willReturn(true);

        doAnswer(invocation -> {
            Path targetPath = invocation.getArgument(1);
            Files.write(targetPath, "malicious content".getBytes());
            return null;
        }).when(fileDownloader).download(any(URL.class), any(Path.class));

        // when & then
        assertThatThrownBy(() -> fileService.storeFile(mediaType, originUrl))
                .isInstanceOf(InvalidFileException.class)
                .hasMessage("Malicious file!");
    }

    @Test
    @DisplayName("파일 삭제 성공")
    void deleteFile_Success() throws IOException {
        // given
        String fileName = "delete-me.txt";
        Path filePath = basePath.resolve(fileName);
        Files.write(filePath, "to be deleted".getBytes());

        // when
        fileService.deleteFile(fileName);

        // then
        assertThat(Files.exists(filePath)).isFalse();
    }

    @Test
    @DisplayName("파일 삭제 실패 - 파일 없음")
    void deleteFile_NotFound() {
        // when & then
        assertThatThrownBy(() -> fileService.deleteFile("nonexistent.txt"))
                .isInstanceOf(StoredFileNotFoundException.class);
    }
}
