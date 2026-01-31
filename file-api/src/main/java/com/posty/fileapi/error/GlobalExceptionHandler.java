package com.posty.fileapi.error;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

import java.io.IOException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public void handleAsyncRequestNotUsableException(AsyncRequestNotUsableException e) {
        log.debug("Streaming response closed by client.");
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class
    })
    public ResponseEntity<String> handleBadRequest(Exception e) {
        String message;
        if (e instanceof MethodArgumentNotValidException notValidException) {
            message = "Invalid request: "
                    + notValidException.getBindingResult().getFieldErrors().stream()
                    .map(fieldError -> String.format("%s (%s)", fieldError.getField(), fieldError.getDefaultMessage()))
                    .collect(Collectors.joining(", "));
        } else if (e instanceof ConstraintViolationException violationException) {
            message = "Invalid request: "
                    + violationException.getConstraintViolations().stream()
                    .map(violation -> {
                        String path = violation.getPropertyPath().toString();
                        String fieldName = path.contains(".")
                                ? path.substring(path.lastIndexOf('.') + 1)
                                : path;
                        return String.format("%s (%s)", fieldName, violation.getMessage());
                    })
                    .collect(Collectors.joining(", "));
        } else {
            message = "Invalid request";
        }

        log.error("{}", message, e);

        return ResponseEntity
                .badRequest()
                .body(message);
    }

    @ExceptionHandler({
            InvalidFileException.class,
            InvalidURLException.class
    })
    public ResponseEntity<String> handleCustomBadRequest(RuntimeException e) {
        log.error("{}", e.getMessage(), e);
        return ResponseEntity
                .badRequest()
                .body(e.getMessage());
    }

    @ExceptionHandler(StoredFileNotFoundException.class)
    public ResponseEntity<Void> handleFileNotFound(StoredFileNotFoundException e) {
        log.error("{}", e.getMessage(), e);
        return ResponseEntity
                .notFound()
                .build();
    }

    @ExceptionHandler(InvalidRangeException.class)
    public ResponseEntity<String> handleInvalidRange(InvalidRangeException e) {
        log.error("{}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                .body(e.getMessage());
    }

    @ExceptionHandler(FileIOException.class)
    public ResponseEntity<Void> handleFileRead(FileIOException e) {
        log.error("{}", e.getMessage(), e);
        return ResponseEntity
                .internalServerError()
                .build();
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<String> handleIOException(IOException e) {
        log.error("{}", e.getMessage(), e);
        return ResponseEntity
                .internalServerError()
                .build();
    }
}
