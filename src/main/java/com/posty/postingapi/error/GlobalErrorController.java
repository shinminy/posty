package com.posty.postingapi.error;

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
            errorResponse = new ErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "An unexpected error occurred. Please try again later.",
                    request.getRequestURI()
            );
        }

        return ResponseEntity.status(errorResponse.getStatus()).body(errorResponse);
    }
}
