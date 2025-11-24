package br.com.smartmesquitaapi.api.exception.auth;

import org.springframework.http.HttpStatus;

/**
 * Exceção lançada quando se tenta registrar um usuário com email já existente
 */
public class EmailAlreadyExistsException extends AuthenticationException {

    public EmailAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}