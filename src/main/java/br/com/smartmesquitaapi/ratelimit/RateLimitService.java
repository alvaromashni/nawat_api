package br.com.smartmesquitaapi.ratelimit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.support.PropertiesLoaderSupport;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RateLimitService {

    private final RedisTemplate<String, Object> redisTemplate;


    public RateLimitService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Verifica se request pode ser processado (Token Bucket)
     *
     * @param key Identificador único (ex: "userId:endpoint" ou "ip:endpoint")
     * @param maxRequests Número máximo de requests
     * @param windowDuration Duração da janela
     * @return true se pode processar, false se atingiu limite
     */
    public boolean allowRequest(String key, int maxRequests, Duration windowDuration) {
        String redisKey = "rateLimit:" + key;

        try {
            Long currentCount = redisTemplate.opsForValue().increment(redisKey);
            if (currentCount == null){
                log.error("O 'Redis increment' retornou null para chave: {}", redisKey);
                return true;
            }

            Long ttl = redisTemplate.getExpire(redisKey);

            if (currentCount == 1 || ttl == -1){
                redisTemplate.expire(redisKey, windowDuration);
            }

            boolean allowed = currentCount <= maxRequests;

            if (!allowed){
                log.warn("Rate limit excedido para chave: {} | Count: {} | Max: {}",
                        key, currentCount, maxRequests);
            }
            return allowed;
        } catch (Exception e) {
            log.error("Verificando erro de 'rate limit' para chave: {}", key, e);
            return true;
        }
    }

    public long getRemainingRequests(String key, int maxRequests) {
        String redisKey = "ratelimit:" + key;

        try {
            Long currentCount = (Long) redisTemplate.opsForValue().get(redisKey);
            if (currentCount == null) {
                return maxRequests;
            }
            return Math.max(0, maxRequests - currentCount);
        } catch (Exception e) {
            log.error("Error getting remaining requests for key: {}", key, e);
            return maxRequests;
        }
    }


    public long getResetTimeSeconds(String key) {
        String redisKey = "ratelimit:" + key;

        try {
            Long ttl = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
            return ttl != null && ttl > 0 ? ttl : 0;
        } catch (Exception e) {
            log.error("Error getting reset time for key: {}", key, e);
            return 0;
        }
    }

    public void ban(String key, Duration duration){
        String redisKey = "banned:" + key;

        try {
            redisTemplate.opsForValue().set(redisKey, "banned", duration);
            log.warn(" Chave banida: {} pelo tempo de: {}", key, duration);
        } catch(Exception e){
            log.error("Erro de chave banida: {}", key, e);
        }
    }

    public boolean isBanned(String key){
        String redisKey = "banned:" + key;

        try {
            return redisTemplate.hasKey(redisKey);
        } catch (Exception e){
            log.error("Erro ao checar se a chave {} foi banida", key, e);
            return false;
        }
    }
}
