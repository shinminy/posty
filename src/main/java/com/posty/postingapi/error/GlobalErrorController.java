package com.posty.postingapi.error;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/error")
public class GlobalErrorController implements ErrorController {

    @RequestMapping
    public ResponseEntity<ErrorResponse> handleError(HttpServletRequest request) {
        ErrorResponse errorResponse = (ErrorResponse) request.getAttribute("errorResponse");

        if (errorResponse == null) {
            final HttpStatus unauthorizedStatus = HttpStatus.UNAUTHORIZED;

            Object statusObj = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
            if (statusObj instanceof Integer && Integer.parseInt(statusObj.toString()) == unauthorizedStatus.value()) {
                errorResponse = new ErrorResponse(
                        unauthorizedStatus,
                        "Please provide a valid API key.",
                        request.getRequestURI()
                );
            } else {
                errorResponse = new ErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "An unexpected error occurred. Please try again later.",
                        request.getRequestURI()
                );
            }
        }

        return ResponseEntity.status(errorResponse.getStatus()).body(errorResponse);
    }
}
