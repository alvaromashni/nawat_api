package br.com.smartmesquitaapi.service.pix.exception;

/**
 * Rate limit excedido
 */
public class RateLimitExceededException extends PixException {
    public RateLimitExceededException(String message) {
        super(message);
    }
}
