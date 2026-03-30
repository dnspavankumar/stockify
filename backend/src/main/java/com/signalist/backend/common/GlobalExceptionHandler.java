package com.signalist.backend.common;

import java.time.Instant;
import java.util.stream.Collectors;

import com.mongodb.MongoTimeoutException;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.UncategorizedMongoDbException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(ApiException exception, HttpServletRequest request) {
        return buildResponse(exception.getStatus(), exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return buildResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateKeyException(
            DuplicateKeyException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.CONFLICT, duplicateKeyMessage(request.getRequestURI()), request.getRequestURI());
    }

    @ExceptionHandler({DataAccessResourceFailureException.class, UncategorizedMongoDbException.class, MongoTimeoutException.class})
    public ResponseEntity<ApiErrorResponse> handleDatabaseConnectivityException(
            Exception exception,
            HttpServletRequest request
    ) {
        if (!isMongoConnectivityIssue(exception)) {
            return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage(), request.getRequestURI());
        }

        return buildResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Database is unavailable. Check MONGODB_URI or start MongoDB before using authentication.",
                request.getRequestURI()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnhandledException(Exception exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage(), request.getRequestURI());
    }

    private boolean isMongoConnectivityIssue(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof DataAccessResourceFailureException
                    || current instanceof MongoTimeoutException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private String duplicateKeyMessage(String path) {
        if ("/api/auth/sign-up".equals(path)) {
            return "An account with this email already exists";
        }
        return "A record with this value already exists";
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(HttpStatus status, String message, String path) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path
        ));
    }
}
