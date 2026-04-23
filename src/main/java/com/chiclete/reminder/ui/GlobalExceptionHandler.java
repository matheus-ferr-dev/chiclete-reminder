package com.chiclete.reminder.ui;

import com.chiclete.reminder.service.exception.EmailAlreadyExistsException;
import com.chiclete.reminder.service.exception.ReminderNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleEmailAlreadyExists(
        EmailAlreadyExistsException ex,
        HttpServletRequest request
    ) {
        return error(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(ReminderNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleReminderNotFound(
        ReminderNotFoundException ex,
        HttpServletRequest request
    ) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(
        BadCredentialsException ex,
        HttpServletRequest request
    ) {
        return error(HttpStatus.UNAUTHORIZED, "Credenciais invalidas.", request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
        MethodArgumentNotValidException ex,
        HttpServletRequest request
    ) {
        String message = ex.getBindingResult().getAllErrors().stream()
            .findFirst()
            .map(err -> err.getDefaultMessage() == null ? "Dados invalidos." : err.getDefaultMessage())
            .orElse("Dados invalidos.");
        return error(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
        IllegalArgumentException ex,
        HttpServletRequest request
    ) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message, String path) {
        return ResponseEntity.status(status).body(Map.of(
            "timestamp", Instant.now().toString(),
            "status", status.value(),
            "message", message,
            "path", path
        ));
    }
}
