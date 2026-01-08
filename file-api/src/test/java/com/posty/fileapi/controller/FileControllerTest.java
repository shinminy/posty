package com.posty.fileapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.posty.common.domain.post.MediaType;
import com.posty.common.dto.FileUploadRequest;
import com.posty.fileapi.dto.FileData;
import com.posty.fileapi.properties.ApiConfig;
import com.posty.fileapi.service.FileService;
import com.posty.fileapi.support.TestSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
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
    @DisplayName("파일 조회 성공")
    void getFile_Success() throws Exception {
        // given
        String fileName = "test.txt";
        byte[] content = "test content".getBytes();
        FileData fileData = new FileData("text/plain", content);
        given(fileService.getFile(fileName)).willReturn(fileData);

        // when & then
        mockMvc.perform(get("/{fileName}", fileName))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "text/plain"))
                .andExpect(content().bytes(content));
    }

    @Test
    @DisplayName("파일 조회 실패 - 파일 없음")
    void getFile_NotFound() throws Exception {
        // given
        String fileName = "nonexistent.txt";
        given(fileService.getFile(fileName)).willThrow(new FileNotFoundException());

        // when & then
        mockMvc.perform(get("/{fileName}", fileName))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("파일 조회 실패 - 서버 에러")
    void getFile_InternalServerError() throws Exception {
        // given
        String fileName = "error.txt";
        given(fileService.getFile(fileName)).willThrow(new IOException());

        // when & then
        mockMvc.perform(get("/{fileName}", fileName))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("파일 업로드 성공")
    void upload_Success() throws Exception {
        // given
        FileUploadRequest request = new FileUploadRequest(MediaType.IMAGE, "http://example.com/image.jpg");
        String fileName = "stored-image.jpg";
        given(fileService.storeFile(request.getMediaType(), request.getOriginUrl())).willReturn(fileName);

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
    @DisplayName("파일 업로드 실패 - 잘못된 URL")
    void upload_MalformedUrl() throws Exception {
        // given
        FileUploadRequest request = new FileUploadRequest(MediaType.IMAGE, "invalid-url");
        given(fileService.storeFile(request.getMediaType(), request.getOriginUrl()))
                .willThrow(new MalformedURLException());

        // when & then
        mockMvc.perform(post("/")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid URL!"));
    }

    @Test
    @DisplayName("파일 업로드 실패 - 잘못된 인자 (MIME 타입 등)")
    void upload_IllegalArgument() throws Exception {
        // given
        FileUploadRequest request = new FileUploadRequest(MediaType.IMAGE, "http://example.com/bad.txt");
        given(fileService.storeFile(request.getMediaType(), request.getOriginUrl()))
                .willThrow(new IllegalArgumentException("Invalid MIME type!"));

        // when & then
        mockMvc.perform(post("/")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid MIME type!"));
    }

    @Test
    @DisplayName("파일 업로드 실패 - 서버 에러")
    void upload_InternalServerError() throws Exception {
        // given
        FileUploadRequest request = new FileUploadRequest(MediaType.IMAGE, "http://example.com/image.jpg");
        given(fileService.storeFile(request.getMediaType(), request.getOriginUrl()))
                .willThrow(new IOException());

        // when & then
        mockMvc.perform(post("/")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to save file..."));
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
    @DisplayName("파일 삭제 실패 - 파일 없음")
    void deleteFile_NotFound() throws Exception {
        // given
        String fileName = "nonexistent.txt";
        doThrow(new FileNotFoundException()).when(fileService).deleteFile(fileName);

        // when & then
        mockMvc.perform(delete("/{fileName}", fileName))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("파일 삭제 실패 - 서버 에러")
    void deleteFile_InternalServerError() throws Exception {
        // given
        String fileName = "error.txt";
        doThrow(new IOException()).when(fileService).deleteFile(fileName);

        // when & then
        mockMvc.perform(delete("/{fileName}", fileName))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to delete file..."));
    }
}
