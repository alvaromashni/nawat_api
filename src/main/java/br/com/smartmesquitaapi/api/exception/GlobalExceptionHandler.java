package br.com.smartmesquitaapi.api.exception;

import br.com.smartmesquitaapi.api.dto.error.ErrorResponse;
import br.com.smartmesquitaapi.api.exception.auth.AuthenticationException;
import br.com.smartmesquitaapi.api.exception.infrastructure.RateLimitExceededException;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Handler global de exceções
 * Agora simplificado com suporte à hierarquia de exceções
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handler genérico para todas as ApplicationException
     * Usa o HttpStatus definido na própria exceção
     */
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> handleApplicationException(ApplicationException ex) {
        return buildErrorResponse(ex.getHttpStatus(), ex.getMessage());
    }

    /**
     * Handler específico para exceções de autenticação
     * (pode adicionar lógica extra como logging de tentativas de login)
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        return buildErrorResponse(ex.getHttpStatus(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Erros de validação")
                .details(errors)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno do servidor: " + ex.getMessage()
        );
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimit(RateLimitExceededException ex){
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("X-RateLimit-Retry-After-Seconds", String.valueOf(ex.getResetTime()))
                .header("X-RateLimit-Remaining", String.valueOf(ex.getRemaining()))
                .body(new ErrorResponse(ex.getMessage()));
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status,  String message) {
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .build();

        return ResponseEntity.status(status).body(response);
    }
}

