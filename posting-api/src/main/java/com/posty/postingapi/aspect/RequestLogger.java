package com.posty.postingapi.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.posty.postingapi.properties.ApiProperties;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Aspect
@Component
public class RequestLogger {

    private final ApiProperties apiProperties;

    private final ObjectMapper objectMapper;

    public RequestLogger(
            ApiProperties apiProperties,
            ObjectMapper objectMapper
    ) {
        this.apiProperties = apiProperties;

        this.objectMapper = objectMapper;
    }

    @Pointcut("execution(* com.posty.postingapi.controller.*.*(..))")
    public void controllerMethods() {}

    @Before("controllerMethods()")
    public void logRequestInfo(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            log.warn("No ServletRequestAttributes found in RequestContextHolder");
            return;
        }
        HttpServletRequest servletRequest = attributes.getRequest();

        String requestId = UUID.randomUUID().toString();
        servletRequest.setAttribute(apiProperties.getRequestIdName(), requestId);

        String body = "No body found";

        Object bodyObject = Arrays.stream(joinPoint.getArgs())
                .filter(arg -> arg != null && isDto(arg))
                .findFirst()
                .orElse(null);

        if (bodyObject != null) {
            try {
                body = objectMapper.writeValueAsString(bodyObject);
            } catch (JsonProcessingException e) {
                body = "Serialization failed";
            }
        } else if (servletRequest instanceof ContentCachingRequestWrapper wrapper) {
            byte[] content = wrapper.getContentAsByteArray();
            if (content.length > 0) {
                try {
                    String rawBody = new String(content, wrapper.getCharacterEncoding());
                    body = maskSensitiveRawString(rawBody);
                } catch (UnsupportedEncodingException e) {
                    body = "Failed to decode request body";
                    log.warn("{}", body, e);
                }
            }
        }

        log.info(
                "\n\n[Request info]\nRequest ID: {}\nRequest Method: {}\nRequest URL: {}\nRequest Params: {}\nClient IP: {}\n{}: {}\nRequest Body: {}\n",
                requestId,
                servletRequest.getMethod(),
                getRequestUrl(servletRequest),
                servletRequest.getParameterMap(),
                getClientIp(servletRequest),
                apiProperties.getKeyHeaderName(),
                servletRequest.getHeader(apiProperties.getKeyHeaderName()),
                body
        );
    }

    private boolean isDto(Object arg) {
        String packageName = arg.getClass().getPackageName();
        return packageName.startsWith("com.posty.postingapi.dto");
    }

    private String maskSensitiveRawString(String raw) {
        return raw.replaceAll("(\"password\"\\s*:\\s*)\"[^\"]+\"", "$1\"****\"");
    }

    private String getRequestUrl(HttpServletRequest request) {
        return ServletUriComponentsBuilder.fromRequest(request)
                .replaceQuery(null)
                .build()
                .toUriString();
    }

    private String getClientIp(HttpServletRequest request) {
        // 프록시를 거친 경우를 확인하여 거치지 않았다면 X-Forwarded-For 헤더가 없으므로 remoteAddr를 바로 가져옴
        String clientIp = Optional.ofNullable(request.getHeader(apiProperties.getXffHeaderName()))
                .filter(StringUtils::isNotEmpty)
                .orElse(request.getRemoteAddr());

        // IPv6를 IPv4로 변환 (로컬 개발 환경에서는 IPv6 주소가 나타날 수 있음)
        if ("0:0:0:0:0:0:0:1".equals(clientIp)) {
            return "127.0.0.1";
        }

        return clientIp;
    }
}
