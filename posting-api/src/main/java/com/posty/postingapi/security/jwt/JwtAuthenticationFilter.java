package com.posty.postingapi.security.jwt;

import com.posty.postingapi.properties.ApiProperties;
import com.posty.postingapi.security.AuthType;
import com.posty.postingapi.security.config.CustomAuthenticationEntryPoint;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final int BEARER_PREFIX_LEN = BEARER_PREFIX.length();

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    private final String apiKeyHeaderName;
    private final String authTypeKey;

    public JwtAuthenticationFilter(
            JwtTokenProvider jwtTokenProvider,
            CustomAuthenticationEntryPoint authenticationEntryPoint,
            ApiProperties apiProperties
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationEntryPoint = authenticationEntryPoint;

        apiKeyHeaderName = apiProperties.getKeyHeaderName();
        authTypeKey = apiProperties.getAuthTypeKey();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.startsWith("/docs/")
                || path.startsWith("/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        if (!StringUtils.hasText(token)) {
            fail(request, response, "Missing access token");
            return;
        }

        if (!jwtTokenProvider.validateToken(token)) {
            fail(request, response, "Invalid or expired access token");
            return;
        }

        Claims claims = jwtTokenProvider.getClaims(token);
        Long accountId = Long.valueOf(claims.getSubject());

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(accountId, null, null);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader(HttpHeaders.AUTHORIZATION);
        return StringUtils.hasText(bearer) && bearer.startsWith(BEARER_PREFIX)
                ? bearer.substring(BEARER_PREFIX_LEN)
                : null;
    }

    private void fail(HttpServletRequest request, HttpServletResponse response, String message) throws ServletException, IOException {
        String apiKey = request.getHeader(apiKeyHeaderName);
        log.info("[{}] Wrong access token! ({})", apiKey, message);

        request.setAttribute(authTypeKey, AuthType.JWT);
        authenticationEntryPoint.commence(request, response, new InsufficientAuthenticationException(message));
    }
}
