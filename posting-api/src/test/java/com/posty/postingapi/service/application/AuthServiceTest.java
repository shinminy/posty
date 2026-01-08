package com.posty.postingapi.service.application;

import com.posty.postingapi.domain.account.Account;
import com.posty.postingapi.domain.account.AccountRepository;
import com.posty.postingapi.dto.auth.EmailVerificationSendRequest;
import com.posty.postingapi.dto.auth.EmailVerificationVerifyRequest;
import com.posty.postingapi.dto.auth.LoginRequest;
import com.posty.postingapi.dto.auth.LoginResponse;
import com.posty.postingapi.error.InvalidAuthenticationException;
import com.posty.postingapi.error.ResourceNotFoundException;
import com.posty.postingapi.error.TooManyRequestsException;
import com.posty.postingapi.error.VerificationFailedException;
import com.posty.postingapi.infrastructure.cache.RefreshTokenManager;
import com.posty.postingapi.infrastructure.cache.VerificationCacheManager;
import com.posty.postingapi.infrastructure.mail.MailTemplate;
import com.posty.postingapi.infrastructure.mail.MailTemplateLoader;
import com.posty.postingapi.infrastructure.mail.SmtpEmailSender;
import com.posty.postingapi.properties.TimeToLiveProperties;
import com.posty.postingapi.security.jwt.JwtTokenProvider;
import com.posty.postingapi.support.TestTimeConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AccountRepository accountRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    
    @Mock
    private RefreshTokenManager refreshTokenManager;
    
    @Mock
    private VerificationCacheManager verificationCacheManager;
    
    @Mock
    private SmtpEmailSender emailSender;
    
    @Mock
    private MailTemplateLoader mailTemplateLoader;
    
    @Mock
    private TimeToLiveProperties timeToLiveProperties;

    private Clock clock;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        MailTemplate mockTemplate = new MailTemplate("Subject", "Body {{expiresIn}}");
        given(mailTemplateLoader.loadVerificationCodeTemplate()).willReturn(mockTemplate);
        
        var verificationProperties = mock(TimeToLiveProperties.VerificationProperties.class);
        given(timeToLiveProperties.getVerification()).willReturn(verificationProperties);
        given(verificationProperties.getCode()).willReturn(Duration.ofMinutes(5));

        clock = new TestTimeConfig().testClock();

        authService = new AuthService(
                accountRepository, passwordEncoder, jwtTokenProvider,
                refreshTokenManager, verificationCacheManager, emailSender,
                clock, mailTemplateLoader, timeToLiveProperties
        );
    }

    @Test
    @DisplayName("로그인 성공")
    void login_Success() {
        // given
        String email = "test@example.com";
        String password = "password";
        LoginRequest request = new LoginRequest(email, password);
        
        Account account = Account.builder()
                .id(1L)
                .email(email)
                .password("hashedPassword")
                .name("tester")
                .build();

        given(accountRepository.findNonDeletedByEmail(email)).willReturn(Optional.of(account));
        given(passwordEncoder.matches(password, "hashedPassword")).willReturn(true);
        given(jwtTokenProvider.createAccessToken(1L, "tester")).willReturn("accessToken");
        given(jwtTokenProvider.createRefreshToken()).willReturn("refreshToken");
        given(jwtTokenProvider.getAccessTokenExpiry()).willReturn(Duration.ofMinutes(30));

        // when
        LoginResponse response = authService.login(request);

        // then
        assertThat(response.accessToken()).isEqualTo("accessToken");
        assertThat(response.refreshToken()).isEqualTo("refreshToken");
    }

    @Test
    @DisplayName("로그인 실패 - 계정 없음")
    void login_AccountNotFound() {
        // given
        String email = "nonexistent@example.com";
        LoginRequest request = new LoginRequest(email, "password");

        given(accountRepository.findNonDeletedByEmail(email)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호")
    void login_InvalidPassword() {
        // given
        String email = "test@example.com";
        String password = "wrongPassword";
        LoginRequest request = new LoginRequest(email, password);
        
        Account account = Account.builder()
                .id(1L)
                .email(email)
                .password("hashedPassword")
                .build();

        given(accountRepository.findNonDeletedByEmail(email)).willReturn(Optional.of(account));
        given(passwordEncoder.matches(password, "hashedPassword")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout_Success() {
        // given
        String refreshToken = "refreshToken";

        // when
        authService.logout(refreshToken);

        // then
        org.mockito.Mockito.verify(refreshTokenManager).clearRefreshToken(refreshToken);
    }

    @Test
    @DisplayName("토큰 갱신 성공")
    void refreshAccessToken_Success() {
        // given
        String refreshToken = "refreshToken";
        Long accountId = 1L;
        Account account = Account.builder()
                .id(accountId)
                .name("tester")
                .build();

        given(refreshTokenManager.loadAccountIdByRefreshToken(refreshToken)).willReturn(Optional.of(accountId));
        given(accountRepository.findNonDeletedById(accountId)).willReturn(Optional.of(account));
        given(jwtTokenProvider.createAccessToken(accountId, "tester")).willReturn("newAccessToken");
        given(jwtTokenProvider.getAccessTokenExpiry()).willReturn(Duration.ofMinutes(30));

        // when
        com.posty.postingapi.dto.auth.RefreshResponse response = authService.refreshAccessToken(refreshToken);

        // then
        assertThat(response.accessToken()).isEqualTo("newAccessToken");
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 잘못된 리프레시 토큰")
    void refreshAccessToken_InvalidToken() {
        // given
        String refreshToken = "invalidToken";
        given(refreshTokenManager.loadAccountIdByRefreshToken(refreshToken)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.refreshAccessToken(refreshToken))
                .isInstanceOf(InvalidAuthenticationException.class);
    }

    @Test
    @DisplayName("이메일 인증 코드 발송 성공")
    void sendVerificationCodeByEmail_Success() {
        // given
        String email = "test@example.com";
        EmailVerificationSendRequest request = new EmailVerificationSendRequest(email);
        given(verificationCacheManager.tryConsumeEmailVerificationQuota(email)).willReturn(true);

        // when
        authService.sendVerificationCodeByEmail(request);

        // then
        org.mockito.Mockito.verify(verificationCacheManager).saveEmailCode(eq(email), anyString());
        org.mockito.Mockito.verify(emailSender).sendHtml(eq(email), anyString(), anyString());
    }

    @Test
    @DisplayName("이메일 인증 코드 발송 실패 - 쿼터 초과")
    void sendVerificationCodeByEmail_TooManyRequests() {
        // given
        String email = "test@example.com";
        EmailVerificationSendRequest request = new EmailVerificationSendRequest(email);
        given(verificationCacheManager.tryConsumeEmailVerificationQuota(email)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.sendVerificationCodeByEmail(request))
                .isInstanceOf(TooManyRequestsException.class);
    }

    @Test
    @DisplayName("이메일 인증 코드 검증 성공")
    void verifyVerificationCodeByEmail_Success() {
        // given
        String email = "test@example.com";
        String code = "123456";
        EmailVerificationVerifyRequest request = new EmailVerificationVerifyRequest(email, code);
        given(verificationCacheManager.getEmailCode(email)).willReturn(code);

        // when
        authService.verifyVerificationCodeByEmail(request);

        // then
        org.mockito.Mockito.verify(verificationCacheManager).clearEmailCode(email);
        org.mockito.Mockito.verify(verificationCacheManager).markEmailVerified(email);
    }

    @Test
    @DisplayName("이메일 인증 코드 검증 실패 - 코드 만료 또는 요청 안함")
    void verifyVerificationCodeByEmail_ExpiredOrNotRequested() {
        // given
        String email = "test@example.com";
        EmailVerificationVerifyRequest request = new EmailVerificationVerifyRequest(email, "123456");
        given(verificationCacheManager.getEmailCode(email)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> authService.verifyVerificationCodeByEmail(request))
                .isInstanceOf(VerificationFailedException.class);
    }

    @Test
    @DisplayName("이메일 인증 코드 검증 실패 - 코드 불일치")
    void verifyVerificationCodeByEmail_CodeMismatch() {
        // given
        String email = "test@example.com";
        EmailVerificationVerifyRequest request = new EmailVerificationVerifyRequest(email, "wrong");
        given(verificationCacheManager.getEmailCode(email)).willReturn("correct");

        // when & then
        assertThatThrownBy(() -> authService.verifyVerificationCodeByEmail(request))
                .isInstanceOf(VerificationFailedException.class);
    }
}
