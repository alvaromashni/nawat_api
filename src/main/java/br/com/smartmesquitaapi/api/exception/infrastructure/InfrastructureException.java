package br.com.smartmesquitaapi.api.exception.infrastructure;

import br.com.smartmesquitaapi.api.exception.ApplicationException;
import org.springframework.http.HttpStatus;

/**
 * Exceção base para erros relacionados à infraestrutura
 * (cache, rate limiting, serviços externos, etc.)
 */
public abstract class InfrastructureException extends ApplicationException {

    protected InfrastructureException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }

    protected InfrastructureException(String message, Throwable cause, HttpStatus httpStatus) {
        super(message, cause, httpStatus);
    }
}