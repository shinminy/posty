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
        Object statusObj = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object uriObj = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

        int status = statusObj != null ? Integer.parseInt(statusObj.toString()) : 500;

        HttpStatus httpStatus = HttpStatus.resolve(status);
        String message = switch (status) {
            case 400 -> "The request could not be processed due to invalid syntax.";
            case 401 -> "Authentication is required and has failed or has not yet been provided.";
            case 403 -> "You do not have permission to access the requested resource.";
            case 404 -> "The requested resource could not be found on the server.";
            case 405 -> "The HTTP method used is not allowed for the requested resource.";
            default -> "An unexpected error occurred. Please try again later.";
        };
        String path = uriObj != null ? uriObj.toString() : "unknown";

        ErrorResponse errorResponse = new ErrorResponse(
                httpStatus != null ? httpStatus : HttpStatus.INTERNAL_SERVER_ERROR,
                message,
                path
        );

        return ResponseEntity.status(status).body(errorResponse);
    }
}
