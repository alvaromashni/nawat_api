package br.com.smartmesquitaapi.api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Exceção base para todas as exceções de domínio da aplicação.
 * Permite mapear exceções específicas para status HTTP apropriados.
 */
@Getter
public abstract class ApplicationException extends RuntimeException {

    private final HttpStatus httpStatus;

    protected ApplicationException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    protected ApplicationException(String message, Throwable cause, HttpStatus httpStatus) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    /**
     * Método auxiliar para criar exceções com status padrão
     */
    protected ApplicationException(String message) {
        this(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}