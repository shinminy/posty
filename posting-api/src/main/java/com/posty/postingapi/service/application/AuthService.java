package com.posty.postingapi.service.application;

import com.posty.postingapi.domain.account.Account;
import com.posty.postingapi.domain.account.AccountRepository;
import com.posty.postingapi.dto.auth.LoginRequest;
import com.posty.postingapi.dto.auth.LoginResponse;
import com.posty.postingapi.dto.auth.RefreshResponse;
import com.posty.postingapi.error.ResourceNotFoundException;
import com.posty.postingapi.infrastructure.cache.RefreshTokenManager;
import com.posty.postingapi.security.jwt.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;

@Slf4j
@Service
public class AuthService {

    private final AccountRepository accountRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenManager refreshTokenManager;

    private final Clock clock;

    public AuthService(
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            RefreshTokenManager refreshTokenManager,
            Clock clock
    ) {
        this.accountRepository = accountRepository;

        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenManager = refreshTokenManager;

        this.clock = clock;
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
        Long accountId = refreshTokenManager.loadAccountIdByRefreshToken(refreshToken);
        Account account = accountRepository.findNonDeletedById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account"));

        String newAccessToken = jwtTokenProvider.createAccessToken(account.getId(), account.getName());
        Instant accessTokenExpiresAt = Instant.now(clock).plus(jwtTokenProvider.getAccessTokenExpiry());

        return new RefreshResponse(newAccessToken, accessTokenExpiresAt);
    }
}
