package br.com.smartmesquitaapi.service.pix.exception;

/**
 * Request inválido
 */
public class InvalidRequestException extends PixException {
    public InvalidRequestException(String message) {
        super(message);
    }
}
