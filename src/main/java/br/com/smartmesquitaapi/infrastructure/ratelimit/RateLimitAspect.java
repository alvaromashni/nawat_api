package br.com.smartmesquitaapi.infrastructure.ratelimit;

import br.com.smartmesquitaapi.api.exception.infrastructure.RateLimitExceededException;
import br.com.smartmesquitaapi.service.UserContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.util.UUID;

/**
 * Aspect que intercepta métodos anotados com @RateLimit
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitAspect {

    private final RateLimitService rateLimitService;
    private final UserContextService userContextService;

    @Around("@annotation(br.com.smartmesquitaapi.infrastructure.ratelimit.RateLimit)")
    public Object checkRateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        RateLimit rateLimit = signature.getMethod().getAnnotation(RateLimit.class);

        String key = buildRateLimitKey(rateLimit);

        if (rateLimitService.isBanned(key)) {
            throw new RateLimitExceededException(
                    "Você foi temporariamente bloqueado. Tente novamente mais tarde."
            );
        }

        Duration windowDuration = Duration.of(rateLimit.duration(), rateLimit.unit().toChronoUnit());
        boolean allowed = rateLimitService.allowRequest(key, rateLimit.limit(), windowDuration);

        if (!allowed) {
            long remaining = rateLimitService.getRemainingRequests(key, rateLimit.limit());
            long resetTime = rateLimitService.getResetTimeSeconds(key);

            throw new RateLimitExceededException(
                    String.format("Limite de requisições excedido. Tente novamente em %d segundos.", resetTime),
                    remaining,
                    resetTime
            );
        }

        addRateLimitHeaders(key, rateLimit.limit());

        return joinPoint.proceed();
    }

    /**
     * Constrói a chave do rate limit baseado no tipo
     */
    private String buildRateLimitKey(RateLimit rateLimit) {
        String baseKey = rateLimit.key();

        switch (rateLimit.type()) {
            case USER:
                UUID userId = userContextService.getCurrentUserId();
                return userId != null ? baseKey + ":" + userId : baseKey + ":anonymous";

            case IP:
                String ip = userContextService.getClientIp();
                return baseKey + ":" + ip;

            case USER_AND_IP:
                UUID userId2 = userContextService.getCurrentUserId();
                String ip2 = userContextService.getClientIp();
                return baseKey + ":" + (userId2 != null ? userId2 : "anonymous") + ":" + ip2;

            case GLOBAL:
            default:
                return baseKey;
        }
    }

    /**
     * Adiciona headers de rate limit na response
     */
    private void addRateLimitHeaders(String key, int maxRequests) {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                long remaining = rateLimitService.getRemainingRequests(key, maxRequests);
                long resetTime = rateLimitService.getResetTimeSeconds(key);

                var response = attributes.getResponse();
                if (response != null) {
                    response.setHeader("X-RateLimit-Limit", String.valueOf(maxRequests));
                    response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
                    response.setHeader("X-RateLimit-Reset", String.valueOf(resetTime));
                }
            }
        } catch (Exception e) {
            log.debug("Could not add rate limit headers", e);
        }
    }
}