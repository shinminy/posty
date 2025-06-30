package com.posty.postingapi.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.posty.postingapi.properties.ApiConfig;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@Slf4j
@ControllerAdvice
public class ResponseLogger implements ResponseBodyAdvice<Object> {

    private final ApiConfig apiConfig;

    private final ObjectMapper objectMapper;

    public ResponseLogger(ApiConfig apiConfig, ObjectMapper objectMapper) {
        this.apiConfig = apiConfig;

        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return returnType.getDeclaringClass().isAnnotationPresent(ResponseLogging.class)
                || returnType.getMethod().isAnnotationPresent(ResponseLogging.class);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
        String requestId = (String) servletRequest.getAttribute(apiConfig.getRequestIdName());

        if (StringUtils.isBlank(requestId)) {
            return body;
        }

        String statusAsString;
        if (response instanceof ServletServerHttpResponse servletResponse) {
            int statusCode = servletResponse.getServletResponse().getStatus();
            HttpStatus status = HttpStatus.resolve(statusCode);
            statusAsString = status == null ? String.valueOf(statusCode) : String.valueOf(status);
        } else {
            statusAsString = "HTTP status unavailable (non-ServletServerHttpResponse)";
        }

        String bodyAsString;
        if (body == null) {
            bodyAsString = "No body found";
        } else {
            try {
                bodyAsString = objectMapper.writeValueAsString(body);
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize response body", e);
                bodyAsString = String.valueOf(body);
            }
        }

        log.info(
                "\n\n[Response Info]\nRequest ID: {}\nResponse Status: {}\nResponse Body: {}\n",
                requestId == null ? "UNKNOWN" : requestId,
                statusAsString,
                bodyAsString
        );

        return body;
    }
}
