package br.com.smartmesquitaapi.service.pix.exception;

/**
 * Chave PIX não cadastrada
 */
public class PixKeyNotFoundException extends PixException {
    public PixKeyNotFoundException(String message) {
        super(message);
    }
}
