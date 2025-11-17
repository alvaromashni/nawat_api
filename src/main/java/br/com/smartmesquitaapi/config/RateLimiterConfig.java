package br.com.smartmesquitaapi.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
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

    /**
     * Cria um bucket com limite padrão: 100 requisições por hora
     */
    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.classic(
                100,
                Refill.intervally(100, Duration.ofHours(1))
        );
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
    //revisar essa função acima

    /**
     * Cria um bucket customizado
     */
    private Bucket createBucket(int capacity, Duration period) {
        Bandwidth limit = Bandwidth.classic(
                capacity,
                Refill.intervally(capacity, period)
        );
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Resolve o bucket para uma chave (IP ou userID)
     */
    public Bucket resolveBucket(String key) {
        return cache.computeIfAbsent(key, k -> createNewBucket());
    }

    /** Tenta consumir 1 token do bucket
     * @return true se permitido, false se rate limit excedido
     */
    public boolean tryConsume(String key){
        Bucket bucket = resolveBucket(key);
        return bucket.tryConsume(1);
    }

    /**
     * Tenta consumir N tokens do bucket
     */
    public boolean tryConsume(String key, long tokens) {
        Bucket bucket = resolveBucket(key);
        return bucket.tryConsume(tokens);
    }

    /**
     * Extrai chave de rate limiting do request (IP do cliente)
     */
    public String getClientKey(HttpServletRequest request){

        String xForwardedFor = request.getHeader("X-Forwarded-For");

        if (xForwardedFor != null && !xForwardedFor.isEmpty()){
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * Limpa o cache periodicamente (evita memory leak)
     * Chame este méto.do via @Scheduled ou manualmente
     */
    public void clearCache(){
        log.info("Limpando cache de rate limiter. Tamanho atual: {}", cache.size());
        cache.clear();
    }

    /**
     * Retorna número de tokens disponíveis para uma chave
     */
    public long getAvailableTokens(String key) {
        Bucket bucket = resolveBucket(key);
        return bucket.getAvailableTokens();
    }

}
