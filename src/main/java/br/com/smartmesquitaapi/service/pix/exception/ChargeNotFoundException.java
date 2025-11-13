package br.com.smartmesquitaapi.service.pix.exception;


/**
 * Cobrança não encontrada
 */
public class ChargeNotFoundException extends PixException {
    public ChargeNotFoundException(String message) {
        super(message);
    }
}
