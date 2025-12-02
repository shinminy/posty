package com.posty.postingapi.error;

import com.posty.postingapi.properties.ApiProperties;
import com.posty.postingapi.security.AuthType;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;

@RestController
@RequestMapping("/error")
public class GlobalErrorController implements ErrorController {

    private final Clock clock;
    private final String authTypeKey;

    public GlobalErrorController(Clock clock, ApiProperties apiProperties) {
        this.clock = clock;
        authTypeKey = apiProperties.getAuthTypeKey();
    }

    @RequestMapping
    public ResponseEntity<ErrorResponse> handleError(HttpServletRequest request) {
        ErrorResponse errorResponse = (ErrorResponse) request.getAttribute("errorResponse");

        if (errorResponse == null) {
            final HttpStatus unauthorizedStatus = HttpStatus.UNAUTHORIZED;

            Object statusObj = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
            if (statusObj instanceof Integer && Integer.parseInt(statusObj.toString()) == unauthorizedStatus.value()) {
                AuthType authType = (AuthType) request.getAttribute(authTypeKey);
                String message;
                if (authType == AuthType.API_KEY) {
                    message = "Please provide a valid API key.";
                } else if (authType == AuthType.JWT) {
                    message = "Please provide a valid access token.";
                } else {
                    message = "Unauthorized access.";
                }

                errorResponse = new ErrorResponse(
                        unauthorizedStatus,
                        message,
                        request.getRequestURI(),
                        clock
                );
            } else {
                errorResponse = new ErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "An unexpected error occurred. Please try again later.",
                        request.getRequestURI(),
                        clock
                );
            }
        }

        return ResponseEntity.status(errorResponse.getStatus()).body(errorResponse);
    }
}
