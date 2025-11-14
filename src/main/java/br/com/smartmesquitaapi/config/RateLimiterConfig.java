package br.com.smartmesquitaapi.config;

import io.github.bucket4j.Bucket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate Limiter usando Bucket4j ("Token" Bucket Algorithm)
 * Limita número de requisições por IP/'user'
 */
@Component
@Slf4j
public class RateLimiterConfig {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

}
