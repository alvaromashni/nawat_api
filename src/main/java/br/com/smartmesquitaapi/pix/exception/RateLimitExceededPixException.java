package br.com.smartmesquitaapi.pix.exception;

import org.springframework.http.HttpStatus;

/**
 * Exceção lançada quando o usuário excede o limite de cobranças PIX por hora
 * Diferente do RateLimitExceededException de infraestrutura, esta é uma regra de negócio
 */
public class RateLimitExceededPixException extends PixException {

    public RateLimitExceededPixException(String message) {
        super(message, HttpStatus.TOO_MANY_REQUESTS);
    }
}