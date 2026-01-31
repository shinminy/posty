package com.posty.fileapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.posty.fileapi.dto.FileUploadRequest;
import com.posty.fileapi.dto.MediaType;
import com.posty.fileapi.properties.ApiConfig;
import com.posty.fileapi.service.FileService;
import com.posty.fileapi.service.FileStreamResult;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
        String content = "This is full content of the file.";
        byte[] contentBytes = content.getBytes();

        StreamingResponseBody body = output -> output.write(contentBytes);
        FileStreamResult result = new FileStreamResult(
                body, contentBytes.length, "text/plain", null, false
        );

        given(fileService.getFileStream(eq(fileName), eq(null))).willReturn(result);

        // when
        MvcResult mvcResult = mockMvc.perform(get("/{fileName}", fileName))
                .andExpect(request().asyncStarted())
                .andReturn();

        // then
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "text/plain"))
                .andExpect(header().string(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentBytes.length)))
                .andExpect(header().doesNotExist(HttpHeaders.CONTENT_RANGE))
                .andExpect(content().bytes(contentBytes));
    }

    @Test
    @DisplayName("파일 조회 성공 - 스트리밍 확인")
    void getFile_Success() throws Exception {
        // given
        String fileName = "test.txt";
        String content = "test content";

        StreamingResponseBody body = output -> output.write(content.getBytes());
        FileStreamResult result = new FileStreamResult(
                body, content.length(), "text/plain", null, false
        );

        given(fileService.getFileStream(eq(fileName), any())).willReturn(result);

        // when
        MvcResult mvcResult = mockMvc.perform(get("/{fileName}", fileName))
                .andExpect(request().asyncStarted())
                .andReturn();

        // then
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "text/plain"))
                .andExpect(header().string(HttpHeaders.CONTENT_LENGTH, String.valueOf(content.length())))
                .andExpect(content().string(content));
    }

    @Test
    @DisplayName("파일 조회 - Range 요청 시 부분 응답(206) 반환")
    void getFile_RangeRequest() throws Exception {
        // given
        String fileName = "video.mp4";
        String rangeHeader = "bytes=100-199";

        StreamingResponseBody body = output -> {}; // 바디는 비워둠
        FileStreamResult result = new FileStreamResult(
                body, 100, "video/mp4", "bytes 100-199/1000", true
        );

        given(fileService.getFileStream(fileName, rangeHeader)).willReturn(result);

        // when
        MvcResult mvcResult = mockMvc.perform(get("/{fileName}", fileName)
                        .header(HttpHeaders.RANGE, rangeHeader))
                .andExpect(request().asyncStarted())
                .andReturn();

        // then
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isPartialContent())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "video/mp4"))
                .andExpect(header().string(HttpHeaders.CONTENT_RANGE, "bytes 100-199/1000"))
                .andExpect(header().string(HttpHeaders.CONTENT_LENGTH, "100"));
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
