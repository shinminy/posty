package com.posty.postingapi.controller;

import com.posty.postingapi.aspect.ResponseLogging;
import com.posty.postingapi.config.OpenApiConfig;
import com.posty.postingapi.dto.auth.*;
import com.posty.postingapi.error.CommonErrorResponses;
import com.posty.postingapi.service.application.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "인증 API",
        description = "로그인, 로그아웃, 토큰 갱신 등 인증 관련 API" +
                " (***" + OpenApiConfig.API_KEY_SCHEME_NAME + "*** 필요)"
)
@CommonErrorResponses
@ResponseLogging
@Validated
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "로그인", description = "로그인합니다.")
    @ApiResponse(responseCode = "200", description = "OK")
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @Operation(summary = "로그아웃", description = "로그아웃합니다.")
    @ApiResponse(responseCode = "204", description = "No Content")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "액세스 토큰 갱신", description = "액세스 토큰을 새로 발급합니다.")
    @ApiResponse(responseCode = "200", description = "OK")
    @PostMapping("/refresh")
    public RefreshResponse refreshAccessToken(@RequestBody RefreshRequest request) {
        return authService.refreshAccessToken(request.refreshToken());
    }

    @Operation(summary = "이메일 인증 코드 발송", description = "이메일로 인증 코드를 발송합니다.")
    @ApiResponse(responseCode = "204", description = "No Content")
    @PostMapping("/email/send")
    public ResponseEntity<Void> sendEmailVerificationCode(@RequestBody EmailVerificationSendRequest request) {
        authService.sendVerificationCodeByEmail(request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "이메일 인증 코드 검증", description = "이메일로 수신한 인증 코드를 검증합니다.")
    @ApiResponse(responseCode = "204", description = "No Content")
    @PostMapping("/email/verify")
    public ResponseEntity<Void> verifyEmailVerificationCode(@RequestBody EmailVerificationVerifyRequest request) {
        authService.verifyVerificationCodeByEmail(request);
        return ResponseEntity.noContent().build();
    }
}
