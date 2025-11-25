package br.com.smartmesquitaapi.pix.exception;

import org.springframework.http.HttpStatus;

/**
 * Cobrança não encontrada
 */
public class ChargeNotFoundException extends PixException {
    public ChargeNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
