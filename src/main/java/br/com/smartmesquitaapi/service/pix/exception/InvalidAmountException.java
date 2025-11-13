package br.com.smartmesquitaapi.service.pix.exception;

/**
 * Valor inválido
 */
public class InvalidAmountException extends PixException {
    public InvalidAmountException(String message) {
        super(message);
    }
}
