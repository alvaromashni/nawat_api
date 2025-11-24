package br.com.smartmesquitaapi.api.exception.auth;

import br.com.smartmesquitaapi.api.exception.ApplicationException;
import org.springframework.http.HttpStatus;

/**
 * Exceção base para erros relacionados à autenticação e autorização
 */
public abstract class AuthenticationException extends ApplicationException {

    protected AuthenticationException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }

    protected AuthenticationException(String message, Throwable cause, HttpStatus httpStatus) {
        super(message, cause, httpStatus);
    }
}