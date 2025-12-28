package com.posty.postingapi.service.application;

import com.posty.postingapi.common.formatter.DurationKoreanFormatter;
import com.posty.postingapi.domain.account.Account;
import com.posty.postingapi.domain.account.AccountRepository;
import com.posty.postingapi.dto.auth.*;
import com.posty.postingapi.error.InvalidAuthenticationException;
import com.posty.postingapi.error.ResourceNotFoundException;
import com.posty.postingapi.error.TooManyRequestsException;
import com.posty.postingapi.error.VerificationFailedException;
import com.posty.postingapi.infrastructure.cache.RefreshTokenManager;
import com.posty.postingapi.properties.TimeToLiveProperties;
import com.posty.postingapi.infrastructure.cache.VerificationCacheManager;
import com.posty.postingapi.infrastructure.mail.MailTemplate;
import com.posty.postingapi.infrastructure.mail.MailTemplateLoader;
import com.posty.postingapi.infrastructure.mail.SmtpEmailSender;
import com.posty.postingapi.security.jwt.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service
public class AuthService {

    private final AccountRepository accountRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenManager refreshTokenManager;

    private final VerificationCacheManager verificationCacheManager;

    private final SmtpEmailSender emailSender;

    private final Clock clock;

    private final MailTemplate emailVerificationTemplate;

    public AuthService(
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            RefreshTokenManager refreshTokenManager,
            VerificationCacheManager verificationCacheManager,
            SmtpEmailSender emailSender,
            Clock clock,
            MailTemplateLoader mailTemplateLoader,
            TimeToLiveProperties timeToLiveProperties
    ) {
        this.accountRepository = accountRepository;

        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenManager = refreshTokenManager;
        this.verificationCacheManager = verificationCacheManager;
        this.emailSender = emailSender;

        this.clock = clock;

        MailTemplate originEmailTemplate = mailTemplateLoader.loadVerificationCodeTemplate();
        Duration verificationCodeTtl = timeToLiveProperties.getVerification().getCode();
        String emailBody = originEmailTemplate.body()
                .replace("{{expiresIn}}", DurationKoreanFormatter.format(verificationCodeTtl));
        emailVerificationTemplate = new MailTemplate(originEmailTemplate.subject(), emailBody);
    }

    public LoginResponse login(LoginRequest request) {
        request.normalize();

        String email = request.getEmail();
        Account account = accountRepository.findNonDeletedByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Account"));

        if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            throw new ResourceNotFoundException("Account");
        }

        String accessToken = jwtTokenProvider.createAccessToken(account.getId(), account.getName());
        String refreshToken = jwtTokenProvider.createRefreshToken();

        refreshTokenManager.saveRefreshToken(refreshToken, account.getId(), jwtTokenProvider.getRefreshTokenExpiry());

        account.updateLastLogin(LocalDateTime.now(clock));
        accountRepository.save(account);

        Instant accessTokenExpiresAt = Instant.now(clock).plus(jwtTokenProvider.getAccessTokenExpiry());

        return new LoginResponse(accessToken, refreshToken, accessTokenExpiresAt);
    }

    public void logout(String refreshToken) {
        refreshTokenManager.clearRefreshToken(refreshToken);
    }

    public RefreshResponse refreshAccessToken(String refreshToken) {
        Long accountId = refreshTokenManager.loadAccountIdByRefreshToken(refreshToken)
                .orElseThrow(InvalidAuthenticationException::new);

        Account account = accountRepository.findNonDeletedById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account"));

        String newAccessToken = jwtTokenProvider.createAccessToken(account.getId(), account.getName());
        Instant accessTokenExpiresAt = Instant.now(clock).plus(jwtTokenProvider.getAccessTokenExpiry());

        return new RefreshResponse(newAccessToken, accessTokenExpiresAt);
    }

    public void sendVerificationCodeByEmail(EmailVerificationSendRequest request) {
        request.normalize();

        String email = request.getEmail();

        if (!verificationCacheManager.tryConsumeEmailVerificationQuota(email)) {
            throw new TooManyRequestsException();
        }

        String verificationCode = String.format("%06d", new Random().nextInt(1_000_000));

        verificationCacheManager.saveEmailCode(email, verificationCode);

        String htmlBody = emailVerificationTemplate.body()
                .replace("{{verificationCode}}", verificationCode);
        emailSender.sendHtml(email, emailVerificationTemplate.subject(), htmlBody);
    }

    public void verifyVerificationCodeByEmail(EmailVerificationVerifyRequest request) {
        request.normalize();

        String email = request.getEmail();

        String saved = verificationCacheManager.getEmailCode(email);
        if (saved == null) {
            log.debug("Email verification failed (expired or not requested). email={}", email);
            throw new VerificationFailedException();
        }

        if (!saved.equals(request.getVerificationCode())) {
            log.debug("Email verification failed (code mismatch). email={}", email);
            throw new VerificationFailedException();
        }

        verificationCacheManager.clearEmailCode(email);
        verificationCacheManager.markEmailVerified(email);
    }
}
