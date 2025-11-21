package br.com.smartmesquitaapi.infrastructure.ratelimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Anotação para aplicar rate limiting em endpoints
 *
 * Uso:
 * @RateLimit(key = "create-pix", limit = 10, duration = 1, unit = TimeUnit.MINUTES)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    String key();

    int limit() default 10;

    long duration() default 1;

    TimeUnit unit() default TimeUnit.MINUTES;

    /**
     * Tipo de rate limiting:
     * - USER: Por usuário (userId)
     * - IP: Por endereço IP
     * - USER_AND_IP: Combinação
     * - GLOBAL: Global (todos compartilham)
     */
    RateLimitType type() default RateLimitType.USER;

    enum RateLimitType {
        USER,
        IP,
        USER_AND_IP,
        GLOBAL
    }

}
