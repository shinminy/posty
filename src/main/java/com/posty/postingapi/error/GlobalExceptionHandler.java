package com.posty.postingapi.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({ MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class, ConstraintViolationException.class })
    public ResponseEntity<ErrorResponse> handleBadRequestException(Exception e, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid or missing query parameter.",
                request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST,
                "The request contains invalid parameters.",
                request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException e, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
                HttpStatus.NOT_FOUND,
                e.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
                HttpStatus.NOT_FOUND,
                "The requested endpoint does not exist.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest request) {
        log.error("Unhandled exception", e);

        ErrorResponse response = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An internal server error has occurred. Please try again later.",
                request.getRequestURI()
        );
        return ResponseEntity.internalServerError().body(response);
    }
}
