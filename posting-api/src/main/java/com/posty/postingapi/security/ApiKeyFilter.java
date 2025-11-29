package com.posty.postingapi.security;

import com.posty.postingapi.properties.ApiProperties;
import com.posty.postingapi.security.apikey.ApiKeyRepository;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    private final ApiProperties apiProperties;
    private final ApiKeyRepository apiKeyRepository;

    public ApiKeyFilter(ApiProperties apiProperties, ApiKeyRepository apiKeyRepository) {
        this.apiProperties = apiProperties;
        this.apiKeyRepository = apiKeyRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        //"/docs/**", "/v3/api-docs/**", "/swagger-ui/**", "/webjars/**", "/favicon*.png"
        return path.startsWith("/docs/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String apiKey = request.getHeader(apiProperties.getKeyHeaderName());

        if (StringUtils.isEmpty(apiKey)) {
            fail(response, apiKey, "Missing API key");
            return;
        }

        String hashedKey = DigestUtils.sha512Hex(apiKey);

        if (!apiKeyRepository.isValid(hashedKey)) {
            fail(response, apiKey, "Invalid or expired API key");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void fail(HttpServletResponse response, String apiKey, String message) throws IOException {
        log.info("Wrong API key! [{}] {} {}", HttpServletResponse.SC_UNAUTHORIZED, message, apiKey);
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
    }
}
