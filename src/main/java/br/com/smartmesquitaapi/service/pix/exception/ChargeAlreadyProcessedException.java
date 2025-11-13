package br.com.smartmesquitaapi.service.pix.exception;

/**
 * Cobrança já processada
 */
public class ChargeAlreadyProcessedException extends PixException {
    public ChargeAlreadyProcessedException(String message) {
        super(message);
    }
}
