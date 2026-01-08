package com.posty.postingapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.posty.postingapi.dto.auth.*;
import com.posty.postingapi.properties.ApiProperties;
import com.posty.postingapi.service.application.AuthService;
import com.posty.postingapi.support.TestSecurityConfig;
import com.posty.postingapi.support.TestTimeConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({TestSecurityConfig.class, TestTimeConfig.class, ApiProperties.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("로그인 API 성공")
    void login_Success() throws Exception {
        // given
        String requestBody = "{\"email\":\"test@example.com\",\"password\":\"password123456\"}";
        LoginResponse response = new LoginResponse("access-token", "refresh-token", Instant.now());
        given(authService.login(any(LoginRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("로그아웃 API 성공")
    void logout_Success() throws Exception {
        // given
        LogoutRequest request = new LogoutRequest("refresh-token");

        // when & then
        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(authService).logout("refresh-token");
    }

    @Test
    @DisplayName("토큰 갱신 API 성공")
    void refreshAccessToken_Success() throws Exception {
        // given
        RefreshRequest request = new RefreshRequest("refresh-token");
        RefreshResponse response = new RefreshResponse("new-access-token", Instant.now());
        given(authService.refreshAccessToken("refresh-token")).willReturn(response);

        // when & then
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"));
    }

    @Test
    @DisplayName("이메일 인증 코드 발송 API 성공")
    void sendEmailVerificationCode_Success() throws Exception {
        // given
        EmailVerificationSendRequest request = new EmailVerificationSendRequest("test@example.com");

        // when & then
        mockMvc.perform(post("/auth/email/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(authService).sendVerificationCodeByEmail(any(EmailVerificationSendRequest.class));
    }

    @Test
    @DisplayName("이메일 인증 코드 검증 API 성공")
    void verifyEmailVerificationCode_Success() throws Exception {
        // given
        EmailVerificationVerifyRequest request = new EmailVerificationVerifyRequest("test@example.com", "123456");

        // when & then
        mockMvc.perform(post("/auth/email/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(authService).verifyVerificationCodeByEmail(any(EmailVerificationVerifyRequest.class));
    }
}
