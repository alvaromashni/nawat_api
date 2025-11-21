package br.com.smartmesquitaapi.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.support.PropertiesLoaderSupport;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
public class RateLimitService {

    private final RedisTemplate<Object, Object> redisTemplate;


    public RateLimitService(RedisTemplate<Object, Object> redisTemplate, PropertiesLoaderSupport propertiesLoaderSupport) {
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
