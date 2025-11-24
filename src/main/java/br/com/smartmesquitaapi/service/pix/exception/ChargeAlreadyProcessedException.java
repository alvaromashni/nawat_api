package br.com.smartmesquitaapi.service.pix.exception;

import org.springframework.http.HttpStatus;

/**
 * Cobrança já processada
 */
public class ChargeAlreadyProcessedException extends PixException {
    public ChargeAlreadyProcessedException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
