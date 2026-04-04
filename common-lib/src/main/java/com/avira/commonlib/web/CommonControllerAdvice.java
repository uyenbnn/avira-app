package com.avira.commonlib.web;

import com.avira.commonlib.exception.ApiErrorResponse;
import com.avira.commonlib.exception.ConflictException;
import com.avira.commonlib.exception.ForbiddenException;
import com.avira.commonlib.exception.NotFoundException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class CommonControllerAdvice {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(NotFoundException ex, WebRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(ConflictException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiErrorResponse> handleForbidden(ForbiddenException ex, WebRequest request) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String message, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        ApiErrorResponse body = new ApiErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                Instant.now()
        );
        return ResponseEntity.status(status).body(body);
    }
}
