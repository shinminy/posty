package com.posty.postingapi.security.apikey;

import com.posty.postingapi.properties.ApiProperties;
import com.posty.postingapi.security.AuthType;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class ApiKeyFilter extends OncePerRequestFilter {

    private final ApiKeyRepository apiKeyRepository;

    private final String apiKeyHeaderName;
    private final String authTypeKey;

    public ApiKeyFilter(ApiKeyRepository apiKeyRepository, ApiProperties apiProperties) {
        this.apiKeyRepository = apiKeyRepository;

        apiKeyHeaderName = apiProperties.getKeyHeaderName();
        authTypeKey = apiProperties.getAuthTypeKey();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.startsWith("/docs/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String apiKey = request.getHeader(apiKeyHeaderName);

        if (StringUtils.isEmpty(apiKey)) {
            fail(request, response, apiKey, "Missing API key");
            return;
        }

        String hashedKey = DigestUtils.sha512Hex(apiKey);

        if (!apiKeyRepository.isValid(hashedKey)) {
            fail(request, response, apiKey, "Invalid or expired API key");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void fail(HttpServletRequest request, HttpServletResponse response, String apiKey, String message) throws IOException {
        log.info("Wrong API key! [{}] {} {}", HttpServletResponse.SC_UNAUTHORIZED, message, apiKey);

        request.setAttribute(authTypeKey, AuthType.API_KEY);
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
    }
}
