package com.posty.postingapi.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.posty.postingapi.error.ErrorResponse;
import com.posty.postingapi.properties.ApiProperties;
import com.posty.postingapi.security.AuthType;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Clock;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;
    private final Clock clock;

    private final String authTypeKey;

    public CustomAuthenticationEntryPoint(
            ObjectMapper objectMapper,
            Clock clock,
            ApiProperties apiProperties
    ) {
        this.objectMapper = objectMapper;
        this.clock = clock;

        this.authTypeKey = apiProperties.getAuthTypeKey();
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        AuthType authType = (AuthType) request.getAttribute(authTypeKey);

        String message = switch (authType) {
            case API_KEY -> "Please provide a valid API key.";
            case JWT -> "Please provide a valid access token.";
        };

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED,
                message,
                request.getRequestURI(),
                clock
        );

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
