package com.posty.postingapi.aspect;

import com.posty.postingapi.config.ApiConfig;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Aspect
@Component
public class RequestLoggingAspect {

    private final HttpServletRequest request;

    private final ApiConfig apiConfig;

    public RequestLoggingAspect(HttpServletRequest request, ApiConfig apiConfig) {
        this.request = request;
        this.apiConfig = apiConfig;
    }

    @Pointcut("execution(* com.posty.postingapi.controller.*.*(..))")
    public void controllerMethods() {}

    @Before("controllerMethods()")
    public void logRequestInfo() {
        final String apiKeyHeaderName = apiConfig.getKeyHeaderName();

        log.info("\n\n[Request info]\nRequest URL: {}\nRequest Method: {}\n{}: {}\nClient IP: {}\nRequest Params: {}\n",
                request.getRequestURL().toString(), request.getMethod(), apiKeyHeaderName,
                request.getHeader(apiKeyHeaderName), getClientIp(request), request.getParameterMap());
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

    @AfterReturning(value = "controllerMethods()", returning = "response")
    public void logResponseInfo(Object response) {
        log.info("\n\n[Response Body]\n{}\n", response);
    }
}
