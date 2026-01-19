package com.posty.postingapi.controller;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.posty.postingapi.dto.account.AccountCreateRequest;
import com.posty.postingapi.dto.account.AccountDeleteResponse;
import com.posty.postingapi.dto.account.AccountDetailResponse;
import com.posty.postingapi.dto.account.AccountUpdateRequest;
import com.posty.postingapi.properties.ApiProperties;
import com.posty.postingapi.service.application.AccountService;
import com.posty.postingapi.support.TestSecurityConfig;
import com.posty.postingapi.support.TestTimeConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@Import({TestSecurityConfig.class, TestTimeConfig.class, ApiProperties.class})
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("계정 조회 API 성공")
    void getAccount_Success() throws Exception {
        // given
        Long accountId = 1L;
        AccountDetailResponse response = new AccountDetailResponse(
                accountId, "test@example.com", "tester", "+82-10-1234-5678",
                null, null, null, null, null
        );
        given(accountService.getAccountDetail(accountId)).willReturn(response);

        // when & then
        mockMvc.perform(get("/accounts/{accountId}", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("tester"));
    }

    @Test
    @DisplayName("계정 생성 API 성공")
    void createAccount_Success() throws Exception {
        // given
        AccountCreateRequest request = new AccountCreateRequest(
                "new@example.com", "password123", "newName", "+82-10-1234-5678"
        );
        AccountDetailResponse response = new AccountDetailResponse(
                1L, "new@example.com", "newName", "+82-10-1234-5678",
                null, null, null, null, null
        );
        given(accountService.createAccount(any(AccountCreateRequest.class))).willReturn(response);

        ObjectMapper testMapper = objectMapper.copy();
        testMapper.configure(MapperFeature.USE_ANNOTATIONS, false);

        // when & then
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("new@example.com"));
    }

    @Test
    @DisplayName("계정 수정 API 성공")
    void updateAccount_Success() throws Exception {
        // given
        Long accountId = 1L;
        AccountUpdateRequest request = new AccountUpdateRequest(
                "newPassword", "newName", "+82-10-8765-4321"
        );

        // when & then
        mockMvc.perform(patch("/accounts/{accountId}", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(accountService).updateAccount(any(Long.class), any(AccountUpdateRequest.class));
    }

    @Test
    @DisplayName("계정 삭제 API 성공")
    void deleteAccount_Success() throws Exception {
        // given
        Long accountId = 1L;
        LocalDateTime scheduledTime = LocalDateTime.now().plusDays(7);
        AccountDeleteResponse response = new AccountDeleteResponse(scheduledTime);
        given(accountService.scheduleAccountDeletion(accountId)).willReturn(response);

        // when & then
        mockMvc.perform(delete("/accounts/{accountId}", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scheduledAt").exists());

        verify(accountService).scheduleAccountDeletion(accountId);
    }
}
