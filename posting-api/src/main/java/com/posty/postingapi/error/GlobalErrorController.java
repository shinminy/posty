package com.posty.postingapi.error;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.util.Optional;

@RestController
@RequestMapping("/error")
public class GlobalErrorController implements ErrorController {

    private final Clock clock;

    public GlobalErrorController(Clock clock) {
        this.clock = clock;
    }

    @RequestMapping
    public ResponseEntity<ErrorResponse> handleError(HttpServletRequest request) {
        ErrorResponse errorResponse = Optional.ofNullable(request.getAttribute("errorResponse"))
                .map(attr -> (ErrorResponse) attr)
                .orElseGet(() -> new ErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "An unexpected error occurred. Please try again later.",
                        request.getRequestURI(),
                        clock
                ));

        return ResponseEntity.status(errorResponse.getStatus()).body(errorResponse);
    }
}
