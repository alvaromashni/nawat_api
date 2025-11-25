package br.com.smartmesquitaapi.pix.exception;

/**
 * Chave PIX não cadastrada
 */
public class PixKeyNotFoundException extends PixException {
    public PixKeyNotFoundException(String message) {
        super(message);
    }
}
