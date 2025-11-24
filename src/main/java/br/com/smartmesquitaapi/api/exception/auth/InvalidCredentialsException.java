package br.com.smartmesquitaapi.api.exception.auth;

import org.springframework.http.HttpStatus;

/**
 * Exceção lançada quando as credenciais fornecidas são inválidas
 */
public class InvalidCredentialsException extends AuthenticationException {

    public InvalidCredentialsException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}