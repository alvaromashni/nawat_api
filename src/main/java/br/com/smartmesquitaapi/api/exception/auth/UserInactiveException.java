package br.com.smartmesquitaapi.api.exception.auth;

import org.springframework.http.HttpStatus;

/**
 * Exceção lançada quando um usuário inativo tenta realizar uma operação
 */
public class UserInactiveException extends AuthenticationException {

    public UserInactiveException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}