package com.posty.postingapi.security.config;

import com.posty.postingapi.aspect.ResponseLogger;
import com.posty.postingapi.controller.AccountController;
import com.posty.postingapi.error.GlobalExceptionHandler;
import com.posty.postingapi.properties.ApiProperties;
import com.posty.postingapi.security.apikey.ApiKeyRepository;
import com.posty.postingapi.security.jwt.JwtTokenProvider;
import com.posty.postingapi.service.application.AccountService;
import com.posty.postingapi.support.TestTimeConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@Import({SecurityConfig.class, ApiProperties.class, ResponseLogger.class, GlobalExceptionHandler.class, TestTimeConfig.class})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApiKeyRepository apiKeyRepository;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private AccountService accountService;

    @Autowired
    private ApiProperties apiProperties;

    @Test
    @DisplayName("API 키 인증 성공")
    void apiKey_Success() throws Exception {
        // given
        String apiKey = "valid-api-key";
        String hashedKey = DigestUtils.sha512Hex(apiKey);
        given(apiKeyRepository.isValid(hashedKey)).willReturn(true);
        
        // JWT 필터 통과를 위해 유효한 토큰 설정 (ApiKeyFilter가 먼저 실행됨)
        String token = "valid-token";
        given(jwtTokenProvider.validateToken(token)).willReturn(true);
        Claims claims = Jwts.claims().subject("1").build();
        given(jwtTokenProvider.getClaims(token)).willReturn(claims);

        // when & then
        mockMvc.perform(get("/account/1")
                        .header(apiProperties.getKeyHeaderName(), apiKey)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("API 키 누락 시 401 에러")
    void apiKey_Missing() throws Exception {
        // when & then
        mockMvc.perform(get("/account/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("잘못된 API 키 사용 시 401 에러")
    void apiKey_Invalid() throws Exception {
        // given
        String invalidKey = "invalid-key";
        given(apiKeyRepository.isValid(anyString())).willReturn(false);

        // when & then
        mockMvc.perform(get("/account/1")
                        .header(apiProperties.getKeyHeaderName(), invalidKey))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("JWT 토큰 인증 성공")
    void jwt_Success() throws Exception {
        // given
        String apiKey = "valid-api-key";
        given(apiKeyRepository.isValid(anyString())).willReturn(true);

        String token = "valid-token";
        given(jwtTokenProvider.validateToken(token)).willReturn(true);
        Claims claims = Jwts.claims().subject("1").build();
        given(jwtTokenProvider.getClaims(token)).willReturn(claims);

        // when & then
        mockMvc.perform(get("/account/1")
                        .header(apiProperties.getKeyHeaderName(), apiKey)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("JWT 토큰 누락 시 401 에러")
    void jwt_Missing() throws Exception {
        // given
        String apiKey = "valid-api-key";
        given(apiKeyRepository.isValid(anyString())).willReturn(true);

        // when & then
        mockMvc.perform(get("/account/1")
                        .header(apiProperties.getKeyHeaderName(), apiKey))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("잘못된 JWT 토큰 사용 시 401 에러")
    void jwt_Invalid() throws Exception {
        // given
        String apiKey = "valid-api-key";
        given(apiKeyRepository.isValid(anyString())).willReturn(true);

        String invalidToken = "invalid-token";
        given(jwtTokenProvider.validateToken(invalidToken)).willReturn(false);

        // when & then
        mockMvc.perform(get("/account/1")
                        .header(apiProperties.getKeyHeaderName(), apiKey)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("필터 제외 경로 (/auth/*) - API 키는 필요하지만 JWT는 불필요")
    void filter_ExcludeAuthPath() throws Exception {
        // given
        String apiKey = "valid-api-key";
        given(apiKeyRepository.isValid(anyString())).willReturn(true);

        // when & then
        mockMvc.perform(get("/auth/login")
                        .header(apiProperties.getKeyHeaderName(), apiKey))
                .andExpect(status().isNotFound()); // 컨트롤러에 GET /auth/login이 없어서 404가 뜨는 것이 정상 (필터는 통과함)
    }

    @Test
    @DisplayName("필터 제외 경로 (/docs/*) - API 키와 JWT 모두 불필요")
    void filter_ExcludeDocsPath() throws Exception {
        // when & then
        mockMvc.perform(get("/docs/index.html"))
                .andExpect(status().isNotFound()); // 필터를 통과하여 존재하지 않는 경로로 도달
    }
}
