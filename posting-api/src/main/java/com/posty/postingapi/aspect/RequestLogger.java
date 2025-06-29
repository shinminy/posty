package com.posty.postingapi.aspect;

import com.posty.postingapi.config.ApiConfig;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.UnsupportedEncodingException;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Aspect
@Component
public class RequestLogger {

    private final ApiConfig apiConfig;

    public RequestLogger(ApiConfig apiConfig) {
        this.apiConfig = apiConfig;
    }

    @Pointcut("execution(* com.posty.postingapi.controller.*.*(..))")
    public void controllerMethods() {}

    @Before("controllerMethods()")
    public void logRequestInfo() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            log.warn("No ServletRequestAttributes found in RequestContextHolder");
            return;
        }
        HttpServletRequest servletRequest = attributes.getRequest();

        String requestId = UUID.randomUUID().toString();
        servletRequest.setAttribute(apiConfig.getRequestIdName(), requestId);

        String body;
        if (servletRequest instanceof ContentCachingRequestWrapper wrapper) {
            byte[] content = wrapper.getContentAsByteArray();
            if (content.length == 0) {
                body = "No body found";
            } else {
                try {
                    body = new String(content, wrapper.getCharacterEncoding());
                } catch (UnsupportedEncodingException e) {
                    body = "Failed to decode request body";
                    log.warn("{}", body, e);
                }
            }
        } else {
            body = "Not cacheable";
        }

        log.info(
                "\n\n[Request info]\nRequest ID: {}\nRequest Method: {}\nRequest URL: {}\nRequest Params: {}\nClient IP: {}\n{}: {}\nRequest Body: {}\n",
                requestId,
                servletRequest.getMethod(),
                servletRequest.getRequestURL().toString(),
                servletRequest.getParameterMap(),
                getClientIp(servletRequest),
                apiConfig.getKeyHeaderName(),
                servletRequest.getHeader(apiConfig.getKeyHeaderName()),
                body
        );
    }

    private String getClientIp(HttpServletRequest request) {
        // 프록시를 거친 경우를 확인하여 거치지 않았다면 X-Forwarded-For 헤더가 없으므로 remoteAddr를 바로 가져옴
        String clientIp = Optional.ofNullable(request.getHeader(apiConfig.getXffHeaderName()))
                .filter(StringUtils::isNotEmpty)
                .orElse(request.getRemoteAddr());

        // IPv6를 IPv4로 변환 (로컬 개발 환경에서는 IPv6 주소가 나타날 수 있음)
        if ("0:0:0:0:0:0:0:1".equals(clientIp)) {
            return "127.0.0.1";
        }

        return clientIp;
    }
}
