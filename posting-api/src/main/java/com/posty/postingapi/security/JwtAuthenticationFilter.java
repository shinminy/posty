package com.posty.postingapi.security;

import com.posty.postingapi.properties.ApiProperties;
import com.posty.postingapi.security.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    private final String authTypeKey;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, ApiProperties apiProperties) {
        this.jwtTokenProvider = jwtTokenProvider;

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

        if (StringUtils.isEmpty(token)) {
            fail(request, response, token, "Missing access token");
            return;
        }

        if (!jwtTokenProvider.validateToken(token)) {
            fail(request, response, token, "Invalid or expired access token");
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
        if (bearer != null && bearer.startsWith(BEARER_PREFIX)) {
            return bearer.substring(7);
        }
        return null;
    }

    private void fail(HttpServletRequest request, HttpServletResponse response, String token, String message) throws IOException {
        log.info("Wrong access token! [{}] {} {}", HttpServletResponse.SC_UNAUTHORIZED, message, token);

        request.setAttribute(authTypeKey, AuthType.JWT);
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
    }
}
