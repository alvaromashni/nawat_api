package br.com.smartmesquitaapi.service.pix.exception;

import org.springframework.http.HttpStatus;

/**
 * Usuário não encontrado
 */
public class UserNotFoundException extends PixException {
    public UserNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
