package br.com.smartmesquitaapi.ratelimit;

import br.com.smartmesquitaapi.api.exception.infrastructure.RateLimitExceededException;
import br.com.smartmesquitaapi.ratelimit.annotations.RateLimit;
import br.com.smartmesquitaapi.ratelimit.keygenerators.RateLimitKeyGenerator;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.util.List;

/**
 * Aspect que intercepta métodos anotados com @RateLimit
 */
@Aspect
@Component
@Slf4j
public class RateLimitAspect {

    private final RateLimitService rateLimitService;
    private final List<RateLimitKeyGenerator> keyGenerators;

    public RateLimitAspect(RateLimitService rateLimitService, List<RateLimitKeyGenerator> keyGenerators) {
        this.rateLimitService = rateLimitService;
        this.keyGenerators = keyGenerators;
    }

    @Around("@annotation(br.com.smartmesquitaapi.ratelimit.annotations.RateLimit)")
    public Object checkRateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            log.info("=== RATELIMIT: Iniciando verificação de rate limit");
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            RateLimit rateLimit = signature.getMethod().getAnnotation(RateLimit.class);

            String baseKey = buildBaseKey(joinPoint, rateLimit);
            log.info("=== RATELIMIT: Base key: {}", baseKey);
            String key = buildRateLimitKey(rateLimit, baseKey);
            log.info("=== RATELIMIT: Rate limit key: {}", key);
        } catch (Exception e) {
            log.error("=== RATELIMIT: Erro durante verificação de rate limit: {}", e.getMessage(), e);
            throw e;
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        RateLimit rateLimit = signature.getMethod().getAnnotation(RateLimit.class);

        String baseKey = buildBaseKey(joinPoint, rateLimit);
        String key = buildRateLimitKey(rateLimit, baseKey);

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

    private String buildBaseKey(JoinPoint joinPoint, RateLimit rateLimit){

        if (!rateLimit.key().isEmpty()){
            return rateLimit.key();
        }
        return joinPoint.getSignature().toShortString();
    }

    private String buildRateLimitKey(RateLimit rateLimit, String basekey) {

        for (RateLimitKeyGenerator generator : keyGenerators){
            if (generator.supports(rateLimit.type())){
                return generator.generateKey(basekey);
            }
        }
        return basekey;
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