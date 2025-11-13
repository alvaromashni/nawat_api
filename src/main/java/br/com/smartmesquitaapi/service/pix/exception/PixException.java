package br.com.smartmesquitaapi.service.pix.exception;

/**
 * Exceção base para erros relacionados a PIX
 */
public class PixException extends RuntimeException {

    public PixException(String message){
        super(message);
    }

    public PixException(String message, Throwable cause){
        super(message, cause);
    }

}
