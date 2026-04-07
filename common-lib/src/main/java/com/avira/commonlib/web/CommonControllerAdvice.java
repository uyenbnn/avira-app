package com.avira.commonlib.web;

import com.avira.commonlib.exception.ApiErrorResponse;
import com.avira.commonlib.exception.ConflictException;
import com.avira.commonlib.exception.ForbiddenException;
import com.avira.commonlib.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Instant;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class CommonControllerAdvice {

    private static final Logger log = LoggerFactory.getLogger(CommonControllerAdvice.class);

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

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ApiErrorResponse> handleWebClientResponseException(
            WebClientResponseException ex, WebRequest request) {
        log.warn("External service request failed with status {}: {}",
                ex.getStatusCode(), ex.getResponseBodyAsString());
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String message = extractErrorMessage(ex);
        return build(status, message, request);
    }

    private String extractErrorMessage(WebClientResponseException ex) {
        String responseBody = ex.getResponseBodyAsString();
        if (responseBody == null || responseBody.isEmpty()) {
            return ex.getStatusCode().toString();
        }
        // Attempt to extract message from JSON error response
        if (responseBody.contains("\"message\"")) {
            try {
                // Simple extraction: look for "message": "..." pattern
                int start = responseBody.indexOf("\"message\":");
                if (start > 0) {
                    int quoteStart = responseBody.indexOf("\"", start + 10);
                    int quoteEnd = responseBody.indexOf("\"", quoteStart + 1);
                    if (quoteStart > 0 && quoteEnd > quoteStart) {
                        return responseBody.substring(quoteStart + 1, quoteEnd);
                    }
                }
            } catch (Exception e) {
                log.debug("Failed to extract error message from response body", e);
            }
        }
        // Fallback to HTTP status reason
        return ex.getStatusCode().toString();
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
