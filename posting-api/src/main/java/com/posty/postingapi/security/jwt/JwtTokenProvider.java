package com.posty.postingapi.security.jwt;

import com.posty.postingapi.properties.SecurityProperties;
import io.jsonwebtoken.*;
import lombok.Getter;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private final SecretKey key;

    @Getter
    private final Duration accessTokenExpiry;
    @Getter
    private final Duration refreshTokenExpiry;

    public JwtTokenProvider(SecurityProperties securityProperties) {
        this.key = Jwts.SIG.HS256.key().build();

        SecurityProperties.jwtProperties jwtProperties = securityProperties.getJwt();
        accessTokenExpiry = jwtProperties.getAccessExpiry();
        refreshTokenExpiry = jwtProperties.getRefreshExpiry();
    }

    public String createAccessToken(Long accountId, String accountName) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(String.valueOf(accountId))
                .claim("name", accountName)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessTokenExpiry)))
                .id(UUID.randomUUID().toString())
                .signWith(key)
                .compact();
    }

    public String createRefreshToken() {
        Instant now = Instant.now();

        return Jwts.builder()
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(refreshTokenExpiry)))
                .id(UUID.randomUUID().toString())
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims getClaims(String token) {
        Jws<Claims> jws = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
        return jws.getPayload();
    }
}
