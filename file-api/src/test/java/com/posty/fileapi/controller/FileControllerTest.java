package com.posty.fileapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.posty.fileapi.dto.FileUploadRequest;
import com.posty.fileapi.dto.MediaType;
import com.posty.fileapi.properties.ApiConfig;
import com.posty.fileapi.service.FileService;
import com.posty.fileapi.support.TestSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileController.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, ApiConfig.class})
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileService fileService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("파일 전체 조회 성공")
    void getFile_FullContent_Success() throws Exception {
        // given
        String fileName = "full-file.txt";
        byte[] contentBytes = "This is full content of the file.".getBytes();

        Resource resource = new ByteArrayResource(contentBytes) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };

        given(fileService.getFileResource(fileName)).willReturn(resource);

        // when & then
        mockMvc.perform(get("/{fileName}", fileName))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "text/plain"))
                .andExpect(content().bytes(contentBytes));
    }

    @Test
    @DisplayName("파일 조회 성공")
    void getFile_Success() throws Exception {
        // given
        String fileName = "test.txt";
        String content = "test content";
        byte[] contentBytes = content.getBytes();

        Resource resource = new ByteArrayResource(contentBytes) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };

        given(fileService.getFileResource(fileName)).willReturn(resource);

        // when & then
        mockMvc.perform(get("/{fileName}", fileName))
                .andExpect(status().isOk())
                .andExpect(content().string(content));
    }

    @Test
    @DisplayName("파일 조회 - Range 요청 시 부분 응답(206)")
    void getFile_RangeRequest() throws Exception {
        // given
        String fileName = "video.mp4";
        byte[] content = new byte[1000];

        Resource resource = new ByteArrayResource(content) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };

        given(fileService.getFileResource(fileName)).willReturn(resource);

        // when & then
        mockMvc.perform(get("/{fileName}", fileName)
                        .header(HttpHeaders.RANGE, "bytes=100-199"))
                .andExpect(status().isPartialContent())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "video/mp4"))
                .andExpect(header().string(HttpHeaders.CONTENT_RANGE, "bytes 100-199/1000"))
                .andExpect(content().bytes(
                        java.util.Arrays.copyOfRange(content, 100, 200)
                ));
    }

    @Test
    @DisplayName("파일 조회 실패 - PathVariable 제약 조건 위반")
    void getFile_InvalidPathVariable_BadRequest() throws Exception {
        // given
        String shortFileName = " ";

        // when & then
        mockMvc.perform(get("/{fileName}", shortFileName))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid request")));
    }

    @Test
    @DisplayName("파일 업로드 성공")
    void upload_Success() throws Exception {
        // given
        FileUploadRequest request = new FileUploadRequest(MediaType.IMAGE, "http://example.com/image.jpg");
        String fileName = "stored-image.jpg";
        given(fileService.storeFile(request.mediaType(), request.originUrl())).willReturn(fileName);

        // when & then
        mockMvc.perform(post("/")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "https://example.com/stored-image.jpg"))
                .andExpect(jsonPath("$.storedUrl").value("https://example.com/stored-image.jpg"))
                .andExpect(jsonPath("$.storedFilename").value(fileName));
    }

    @Test
    @DisplayName("파일 업로드 실패 - 필수값 누락")
    void upload_NullField_BadRequest() throws Exception {
        // given
        FileUploadRequest request = new FileUploadRequest(null, "https://example.com/test.jpg");

        // when & then
        mockMvc.perform(post("/")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid request")));
    }

    @Test
    @DisplayName("파일 업로드 실패 - 유효하지 않은 URL 형식")
    void upload_InvalidUrl_BadRequest() throws Exception {
        // given
        FileUploadRequest request = new FileUploadRequest(MediaType.IMAGE, "not-a-url");

        // when & then
        mockMvc.perform(post("/")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid request")));
    }

    @Test
    @DisplayName("파일 삭제 성공")
    void deleteFile_Success() throws Exception {
        // given
        String fileName = "delete-me.txt";

        // when & then
        mockMvc.perform(delete("/{fileName}", fileName))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("파일 삭제 실패 - PathVariable 제약 조건 위반")
    void deleteFile_InvalidPathVariable_BadRequest() throws Exception {
        // given
        String shortFileName = " ";

        // when & then
        mockMvc.perform(delete("/{fileName}", shortFileName))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid request")));
    }
}
