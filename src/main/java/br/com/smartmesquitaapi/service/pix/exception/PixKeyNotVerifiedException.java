package br.com.smartmesquitaapi.service.pix.exception;

import org.springframework.http.HttpStatus;

/**
 * Exceção lançada quando a chave PIX não foi verificada
 */
public class PixKeyNotVerifiedException extends PixException {
    public PixKeyNotVerifiedException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
