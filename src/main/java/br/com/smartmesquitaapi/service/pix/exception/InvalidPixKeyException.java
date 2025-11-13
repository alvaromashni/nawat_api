package br.com.smartmesquitaapi.service.pix.exception;

/**
 * Formato de chave PIX inválido
 */
public class InvalidPixKeyException extends PixException {
    public InvalidPixKeyException(String message) {
        super(message);
    }
}
