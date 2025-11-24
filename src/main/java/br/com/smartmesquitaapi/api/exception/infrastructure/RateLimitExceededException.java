package br.com.smartmesquitaapi.api.exception.infrastructure;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Exceção lançada quando o limite de requisições é excedido
 */
@Getter
public class RateLimitExceededException extends InfrastructureException {

    private final long remaining;
    private final long resetTime;

    public RateLimitExceededException(String message) {
        this(message, 0, 0);
    }

    public RateLimitExceededException(String message, long remaining, long resetTime) {
        super(message, HttpStatus.TOO_MANY_REQUESTS);
        this.remaining = remaining;
        this.resetTime = resetTime;
    }
}