package vn.aequitas.coreplatform.web.advice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vn.aequitas.coreplatform.application.common.exception.DuplicateNameException;
import vn.aequitas.coreplatform.application.common.exception.NotFoundException;

import java.util.stream.Collectors;

/**
 * Central exception-to-HTTP mapping, the counterpart of the .NET
 * {@code ErrorHandlingMiddleware}. Emits a {@link ErrorResponse} body.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Duplicate data -> 409 (mirrors {@code DuplicateNameException}). */
    @ExceptionHandler(DuplicateNameException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateNameException ex) {
        return build(HttpStatus.CONFLICT, message(ex, "Duplicate data error."));
    }

    /** Bean-validation failure on a request body -> 400 (replaces FluentValidation's {@code ValidationException}). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(GlobalExceptionHandler::formatFieldError)
                .collect(Collectors.joining(System.lineSeparator()));
        String message = "Validation failed: " + System.lineSeparator() + details;
        return build(HttpStatus.BAD_REQUEST, message);
    }

    /** Invalid argument -> 400 (mirrors {@code ArgumentException}). */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, message(ex, "Invalid argument."));
    }

    /** Missing record -> 404 (mirrors {@code KeyNotFoundException}). */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, message(ex, "Record not found."));
    }

    /** Anything else -> 500. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unhandled exception caught by advice.", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
    }

    private static String formatFieldError(FieldError error) {
        return " -- " + error.getField() + ": " + error.getDefaultMessage();
    }

    private static String message(Exception ex, String fallback) {
        return ex.getMessage() != null ? ex.getMessage() : fallback;
    }

    private static ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(message, status.value()));
    }
}
