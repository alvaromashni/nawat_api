package br.com.smartmesquitaapi.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimitService - Testes de Rate Limiting com Redis")
class RateLimitServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    // ==================== TESTES DE ALLOW REQUEST ====================

    @Test
    @DisplayName("Deve permitir primeira requisição")
    void shouldAllowFirstRequest() {
        // Arrange
        String key = "user:123:endpoint";
        int maxRequests = 10;
        Duration windowDuration = Duration.ofMinutes(1);

        when(valueOperations.increment("rateLimit:" + key)).thenReturn(1L);
        when(redisTemplate.getExpire("rateLimit:" + key)).thenReturn(-1L);

        // Act
        boolean allowed = rateLimitService.allowRequest(key, maxRequests, windowDuration);

        // Assert
        assertTrue(allowed);
        verify(valueOperations).increment("rateLimit:" + key);
        verify(redisTemplate).expire("rateLimit:" + key, windowDuration);
    }

    @Test
    @DisplayName("Deve permitir requisições dentro do limite")
    void shouldAllowRequestsWithinLimit() {
        // Arrange
        String key = "user:123:endpoint";
        int maxRequests = 10;
        Duration windowDuration = Duration.ofMinutes(1);

        when(valueOperations.increment("rateLimit:" + key)).thenReturn(5L);
        when(redisTemplate.getExpire("rateLimit:" + key)).thenReturn(30L);

        // Act
        boolean allowed = rateLimitService.allowRequest(key, maxRequests, windowDuration);

        // Assert
        assertTrue(allowed);
        verify(valueOperations).increment("rateLimit:" + key);
        verify(redisTemplate, never()).expire(anyString(), any(Duration.class));
    }

    @Test
    @DisplayName("Deve permitir requisição exatamente no limite")
    void shouldAllowRequestExactlyAtLimit() {
        // Arrange
        String key = "user:123:endpoint";
        int maxRequests = 10;
        Duration windowDuration = Duration.ofMinutes(1);

        when(valueOperations.increment("rateLimit:" + key)).thenReturn(10L);
        when(redisTemplate.getExpire("rateLimit:" + key)).thenReturn(30L);

        // Act
        boolean allowed = rateLimitService.allowRequest(key, maxRequests, windowDuration);

        // Assert
        assertTrue(allowed);
    }

    @Test
    @DisplayName("Deve bloquear requisição quando exceder o limite")
    void shouldBlockRequestWhenExceedingLimit() {
        // Arrange
        String key = "user:123:endpoint";
        int maxRequests = 10;
        Duration windowDuration = Duration.ofMinutes(1);

        when(valueOperations.increment("rateLimit:" + key)).thenReturn(11L);
        when(redisTemplate.getExpire("rateLimit:" + key)).thenReturn(30L);

        // Act
        boolean allowed = rateLimitService.allowRequest(key, maxRequests, windowDuration);

        // Assert
        assertFalse(allowed);
        verify(valueOperations).increment("rateLimit:" + key);
    }

    @Test
    @DisplayName("Deve bloquear requisições muito acima do limite")
    void shouldBlockRequestsFarAboveLimit() {
        // Arrange
        String key = "user:123:endpoint";
        int maxRequests = 10;
        Duration windowDuration = Duration.ofMinutes(1);

        when(valueOperations.increment("rateLimit:" + key)).thenReturn(50L);
        when(redisTemplate.getExpire("rateLimit:" + key)).thenReturn(30L);

        // Act
        boolean allowed = rateLimitService.allowRequest(key, maxRequests, windowDuration);

        // Assert
        assertFalse(allowed);
    }

    @Test
    @DisplayName("Deve configurar expiração quando contador é 1")
    void shouldSetExpirationWhenCountIsOne() {
        // Arrange
        String key = "user:123:endpoint";
        int maxRequests = 10;
        Duration windowDuration = Duration.ofMinutes(5);

        when(valueOperations.increment("rateLimit:" + key)).thenReturn(1L);
        when(redisTemplate.getExpire("rateLimit:" + key)).thenReturn(-1L);

        // Act
        rateLimitService.allowRequest(key, maxRequests, windowDuration);

        // Assert
        verify(redisTemplate).expire("rateLimit:" + key, windowDuration);
    }

    @Test
    @DisplayName("Deve configurar expiração quando TTL é -1 (sem expiração)")
    void shouldSetExpirationWhenTtlIsMinusOne() {
        // Arrange
        String key = "user:123:endpoint";
        int maxRequests = 10;
        Duration windowDuration = Duration.ofMinutes(5);

        when(valueOperations.increment("rateLimit:" + key)).thenReturn(3L);
        when(redisTemplate.getExpire("rateLimit:" + key)).thenReturn(-1L);

        // Act
        rateLimitService.allowRequest(key, maxRequests, windowDuration);

        // Assert
        verify(redisTemplate).expire("rateLimit:" + key, windowDuration);
    }

    @Test
    @DisplayName("Deve permitir requisição quando Redis retorna null (fail-open)")
    void shouldAllowRequestWhenRedisReturnsNull() {
        // Arrange
        String key = "user:123:endpoint";
        int maxRequests = 10;
        Duration windowDuration = Duration.ofMinutes(1);

        when(valueOperations.increment("rateLimit:" + key)).thenReturn(null);

        // Act
        boolean allowed = rateLimitService.allowRequest(key, maxRequests, windowDuration);

        // Assert
        assertTrue(allowed);
    }

    @Test
    @DisplayName("Deve permitir requisição quando Redis lança exceção (fail-open)")
    void shouldAllowRequestWhenRedisThrowsException() {
        // Arrange
        String key = "user:123:endpoint";
        int maxRequests = 10;
        Duration windowDuration = Duration.ofMinutes(1);

        when(valueOperations.increment("rateLimit:" + key))
            .thenThrow(new RuntimeException("Redis connection error"));

        // Act
        boolean allowed = rateLimitService.allowRequest(key, maxRequests, windowDuration);

        // Assert
        assertTrue(allowed);
    }

    @Test
    @DisplayName("Deve adicionar prefixo 'rateLimit:' à chave do Redis")
    void shouldAddRateLimitPrefixToRedisKey() {
        // Arrange
        String key = "user:123:endpoint";
        int maxRequests = 10;
        Duration windowDuration = Duration.ofMinutes(1);

        when(valueOperations.increment(anyString())).thenReturn(1L);
        when(redisTemplate.getExpire(anyString())).thenReturn(-1L);

        // Act
        rateLimitService.allowRequest(key, maxRequests, windowDuration);

        // Assert
        verify(valueOperations).increment("rateLimit:" + key);
        verify(redisTemplate).getExpire("rateLimit:" + key);
    }

    // ==================== TESTES DE GET REMAINING REQUESTS ====================

    @Test
    @DisplayName("Deve retornar requisições restantes corretas")
    void shouldReturnCorrectRemainingRequests() {
        // Arrange
        String key = "user:123:endpoint";
        int maxRequests = 10;

        when(valueOperations.get("ratelimit:" + key)).thenReturn(3L);

        // Act
        long remaining = rateLimitService.getRemainingRequests(key, maxRequests);

        // Assert
        assertEquals(7L, remaining);
        verify(valueOperations).get("ratelimit:" + key);
    }

    @Test
    @DisplayName("Deve retornar max requests quando não há contador no Redis")
    void shouldReturnMaxRequestsWhenNoCounterInRedis() {
        // Arrange
        String key = "user:123:endpoint";
        int maxRequests = 10;

        when(valueOperations.get("ratelimit:" + key)).thenReturn(null);

        // Act
        long remaining = rateLimitService.getRemainingRequests(key, maxRequests);

        // Assert
        assertEquals(10L, remaining);
    }

    @Test
    @DisplayName("Deve retornar 0 quando limite já foi excedido")
    void shouldReturnZeroWhenLimitAlreadyExceeded() {
        // Arrange
        String key = "user:123:endpoint";
        int maxRequests = 10;

        when(valueOperations.get("ratelimit:" + key)).thenReturn(15L);

        // Act
        long remaining = rateLimitService.getRemainingRequests(key, maxRequests);

        // Assert
        assertEquals(0L, remaining);
    }

    @Test
    @DisplayName("Deve retornar 0 quando contador está exatamente no limite")
    void shouldReturnZeroWhenCounterIsExactlyAtLimit() {
        // Arrange
        String key = "user:123:endpoint";
        int maxRequests = 10;

        when(valueOperations.get("ratelimit:" + key)).thenReturn(10L);

        // Act
        long remaining = rateLimitService.getRemainingRequests(key, maxRequests);

        // Assert
        assertEquals(0L, remaining);
    }

    @Test
    @DisplayName("Deve retornar max requests quando Redis lança exceção")
    void shouldReturnMaxRequestsWhenRedisThrowsExceptionInGetRemaining() {
        // Arrange
        String key = "user:123:endpoint";
        int maxRequests = 10;

        when(valueOperations.get("ratelimit:" + key))
            .thenThrow(new RuntimeException("Redis error"));

        // Act
        long remaining = rateLimitService.getRemainingRequests(key, maxRequests);

        // Assert
        assertEquals(10L, remaining);
    }

    // ==================== TESTES DE GET RESET TIME ====================

    @Test
    @DisplayName("Deve retornar tempo de reset correto em segundos")
    void shouldReturnCorrectResetTimeInSeconds() {
        // Arrange
        String key = "user:123:endpoint";

        when(redisTemplate.getExpire("ratelimit:" + key, TimeUnit.SECONDS)).thenReturn(45L);

        // Act
        long resetTime = rateLimitService.getResetTimeSeconds(key);

        // Assert
        assertEquals(45L, resetTime);
        verify(redisTemplate).getExpire("ratelimit:" + key, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("Deve retornar 0 quando chave não existe no Redis")
    void shouldReturnZeroWhenKeyDoesNotExist() {
        // Arrange
        String key = "user:123:endpoint";

        when(redisTemplate.getExpire("ratelimit:" + key, TimeUnit.SECONDS)).thenReturn(null);

        // Act
        long resetTime = rateLimitService.getResetTimeSeconds(key);

        // Assert
        assertEquals(0L, resetTime);
    }

    @Test
    @DisplayName("Deve retornar 0 quando TTL é negativo")
    void shouldReturnZeroWhenTtlIsNegative() {
        // Arrange
        String key = "user:123:endpoint";

        when(redisTemplate.getExpire("ratelimit:" + key, TimeUnit.SECONDS)).thenReturn(-1L);

        // Act
        long resetTime = rateLimitService.getResetTimeSeconds(key);

        // Assert
        assertEquals(0L, resetTime);
    }

    @Test
    @DisplayName("Deve retornar 0 quando Redis lança exceção em getResetTime")
    void shouldReturnZeroWhenRedisThrowsExceptionInGetResetTime() {
        // Arrange
        String key = "user:123:endpoint";

        when(redisTemplate.getExpire("ratelimit:" + key, TimeUnit.SECONDS))
            .thenThrow(new RuntimeException("Redis error"));

        // Act
        long resetTime = rateLimitService.getResetTimeSeconds(key);

        // Assert
        assertEquals(0L, resetTime);
    }

    // ==================== TESTES DE BAN ====================

    @Test
    @DisplayName("Deve banir chave por duração especificada")
    void shouldBanKeyForSpecifiedDuration() {
        // Arrange
        String key = "user:123";
        Duration banDuration = Duration.ofHours(24);

        doNothing().when(valueOperations).set(anyString(), anyString(), any(Duration.class));

        // Act
        rateLimitService.ban(key, banDuration);

        // Assert
        verify(valueOperations).set("banned:" + key, "banned", banDuration);
    }

    @Test
    @DisplayName("Deve adicionar prefixo 'banned:' ao banir chave")
    void shouldAddBannedPrefixWhenBanningKey() {
        // Arrange
        String key = "user:123";
        Duration banDuration = Duration.ofHours(1);

        doNothing().when(valueOperations).set(anyString(), anyString(), any(Duration.class));

        // Act
        rateLimitService.ban(key, banDuration);

        // Assert
        verify(valueOperations).set(eq("banned:" + key), eq("banned"), eq(banDuration));
    }

    @Test
    @DisplayName("Deve lidar com exceção ao banir chave")
    void shouldHandleExceptionWhenBanningKey() {
        // Arrange
        String key = "user:123";
        Duration banDuration = Duration.ofHours(1);

        doThrow(new RuntimeException("Redis error"))
            .when(valueOperations).set(anyString(), anyString(), any(Duration.class));

        // Act & Assert - Não deve lançar exceção
        assertDoesNotThrow(() -> rateLimitService.ban(key, banDuration));
    }

    @Test
    @DisplayName("Deve banir chave por diferentes durações")
    void shouldBanKeyForDifferentDurations() {
        // Arrange
        String key1 = "user:123";
        String key2 = "user:456";
        Duration duration1 = Duration.ofMinutes(30);
        Duration duration2 = Duration.ofDays(7);

        doNothing().when(valueOperations).set(anyString(), anyString(), any(Duration.class));

        // Act
        rateLimitService.ban(key1, duration1);
        rateLimitService.ban(key2, duration2);

        // Assert
        verify(valueOperations).set("banned:" + key1, "banned", duration1);
        verify(valueOperations).set("banned:" + key2, "banned", duration2);
    }

    // ==================== TESTES DE IS BANNED ====================

    @Test
    @DisplayName("Deve retornar true quando chave está banida")
    void shouldReturnTrueWhenKeyIsBanned() {
        // Arrange
        String key = "user:123";

        when(redisTemplate.hasKey("banned:" + key)).thenReturn(true);

        // Act
        boolean isBanned = rateLimitService.isBanned(key);

        // Assert
        assertTrue(isBanned);
        verify(redisTemplate).hasKey("banned:" + key);
    }

    @Test
    @DisplayName("Deve retornar false quando chave não está banida")
    void shouldReturnFalseWhenKeyIsNotBanned() {
        // Arrange
        String key = "user:123";

        when(redisTemplate.hasKey("banned:" + key)).thenReturn(false);

        // Act
        boolean isBanned = rateLimitService.isBanned(key);

        // Assert
        assertFalse(isBanned);
    }

    @Test
    @DisplayName("Deve retornar false quando Redis lança exceção ao verificar ban")
    void shouldReturnFalseWhenRedisThrowsExceptionInIsBanned() {
        // Arrange
        String key = "user:123";

        when(redisTemplate.hasKey("banned:" + key))
            .thenThrow(new RuntimeException("Redis error"));

        // Act
        boolean isBanned = rateLimitService.isBanned(key);

        // Assert
        assertFalse(isBanned);
    }

    @Test
    @DisplayName("Deve adicionar prefixo 'banned:' ao verificar ban")
    void shouldAddBannedPrefixWhenCheckingBan() {
        // Arrange
        String key = "user:123";

        when(redisTemplate.hasKey(anyString())).thenReturn(false);

        // Act
        rateLimitService.isBanned(key);

        // Assert
        verify(redisTemplate).hasKey("banned:" + key);
    }

    // ==================== TESTES DE INTEGRAÇÃO ====================

    @Test
    @DisplayName("Deve simular sequência de requisições até atingir limite")
    void shouldSimulateRequestSequenceUntilLimit() {
        // Arrange
        String key = "user:123:endpoint";
        int maxRequests = 3;
        Duration windowDuration = Duration.ofMinutes(1);

        when(valueOperations.increment("rateLimit:" + key))
            .thenReturn(1L)
            .thenReturn(2L)
            .thenReturn(3L)
            .thenReturn(4L);
        when(redisTemplate.getExpire("rateLimit:" + key)).thenReturn(-1L, 55L, 50L, 45L);

        // Act & Assert
        assertTrue(rateLimitService.allowRequest(key, maxRequests, windowDuration)); // 1ª
        assertTrue(rateLimitService.allowRequest(key, maxRequests, windowDuration)); // 2ª
        assertTrue(rateLimitService.allowRequest(key, maxRequests, windowDuration)); // 3ª (limite)
        assertFalse(rateLimitService.allowRequest(key, maxRequests, windowDuration)); // 4ª (bloqueada)
    }

    @Test
    @DisplayName("Deve gerenciar diferentes chaves independentemente")
    void shouldManageDifferentKeysIndependently() {
        // Arrange
        String key1 = "user:123:endpoint";
        String key2 = "user:456:endpoint";
        int maxRequests = 5;
        Duration windowDuration = Duration.ofMinutes(1);

        when(valueOperations.increment("rateLimit:" + key1)).thenReturn(3L);
        when(valueOperations.increment("rateLimit:" + key2)).thenReturn(1L);
        when(redisTemplate.getExpire(anyString())).thenReturn(30L);

        // Act
        boolean allowed1 = rateLimitService.allowRequest(key1, maxRequests, windowDuration);
        boolean allowed2 = rateLimitService.allowRequest(key2, maxRequests, windowDuration);

        // Assert
        assertTrue(allowed1);
        assertTrue(allowed2);
        verify(valueOperations).increment("rateLimit:" + key1);
        verify(valueOperations).increment("rateLimit:" + key2);
    }

    @Test
    @DisplayName("Deve combinar allowRequest e getRemainingRequests corretamente")
    void shouldCombineAllowRequestAndGetRemainingRequestsCorrectly() {
        // Arrange
        String key = "user:123:endpoint";
        int maxRequests = 10;

        when(valueOperations.increment("rateLimit:" + key)).thenReturn(4L);
        when(redisTemplate.getExpire("rateLimit:" + key)).thenReturn(30L);
        when(valueOperations.get("ratelimit:" + key)).thenReturn(4L);

        // Act
        boolean allowed = rateLimitService.allowRequest(key, maxRequests, Duration.ofMinutes(1));
        long remaining = rateLimitService.getRemainingRequests(key, maxRequests);

        // Assert
        assertTrue(allowed);
        assertEquals(6L, remaining);
    }

    @Test
    @DisplayName("Deve integrar ban com allowRequest")
    void shouldIntegrateBanWithAllowRequest() {
        // Arrange
        String key = "user:123";

        when(redisTemplate.hasKey("banned:" + key)).thenReturn(true);
        doNothing().when(valueOperations).set(anyString(), anyString(), any(Duration.class));

        // Act
        rateLimitService.ban(key, Duration.ofHours(1));
        boolean isBanned = rateLimitService.isBanned(key);

        // Assert
        assertTrue(isBanned);
        verify(valueOperations).set("banned:" + key, "banned", Duration.ofHours(1));
    }

    @Test
    @DisplayName("Deve lidar com múltiplas janelas de tempo diferentes")
    void shouldHandleMultipleDifferentTimeWindows() {
        // Arrange
        String key = "user:123:endpoint";

        when(valueOperations.increment("rateLimit:" + key)).thenReturn(1L, 2L);
        when(redisTemplate.getExpire("rateLimit:" + key)).thenReturn(-1L);

        // Act
        boolean allowed1 = rateLimitService.allowRequest(key, 10, Duration.ofMinutes(1));
        boolean allowed2 = rateLimitService.allowRequest(key, 100, Duration.ofHours(1));

        // Assert
        assertTrue(allowed1);
        assertTrue(allowed2);
        verify(redisTemplate).expire("rateLimit:" + key, Duration.ofMinutes(1));
        verify(redisTemplate).expire("rateLimit:" + key, Duration.ofHours(1));
    }
}
