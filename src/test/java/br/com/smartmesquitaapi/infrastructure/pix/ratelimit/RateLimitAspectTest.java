package br.com.smartmesquitaapi.infrastructure.pix.ratelimit;

import br.com.smartmesquitaapi.api.exception.infrastructure.RateLimitExceededException;
import br.com.smartmesquitaapi.infrastructure.ratelimit.RateLimitAspect;
import br.com.smartmesquitaapi.infrastructure.ratelimit.RateLimitService;
import br.com.smartmesquitaapi.infrastructure.ratelimit.RateLimitType;
import br.com.smartmesquitaapi.infrastructure.ratelimit.annotations.RateLimit;
import br.com.smartmesquitaapi.infrastructure.ratelimit.keygenerators.RateLimitKeyGenerator;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitAspectTest {

    @Mock
    private RateLimitService rateLimitService;

    @Mock
    private RateLimitKeyGenerator keyGenerator; // O nosso Strategy falso

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature signature;

    @Mock
    private RateLimit rateLimitAnnotation;

    private RateLimitAspect rateLimitAspect;

    @BeforeEach
    void setUp() {
        // Configuramos o Aspecto injetando manualmente a lista contendo nosso Mock
        // Isso simula o comportamento do Spring injetando as Strategies
        List<RateLimitKeyGenerator> keyGenerators = Collections.singletonList(keyGenerator);
        rateLimitAspect = new RateLimitAspect(rateLimitService, keyGenerators);
    }

    @Test
    @DisplayName("Deve permitir a execução quando o limite não for excedido")
    void shouldProceedWhenRateLimitIsNotExceeded() throws Throwable {
        // GIVEN (DADO QUE)
        mockAnnotationSetup("minha-chave");

        // O gerador diz que suporta este tipo e gera uma chave de teste
        when(keyGenerator.supports(any())).thenReturn(true);
        when(keyGenerator.generateKey(anyString())).thenReturn("chave-final:123");

        // O serviço diz que ainda tem tokens disponíveis (true)
        when(rateLimitService.allowRequest(anyString(), anyInt(), any())).thenReturn(true);

        // WHEN (QUANDO)
        rateLimitAspect.checkRateLimit(joinPoint);

        // THEN (ENTÃO)
        // Verificamos se o méto.do original foi chamado
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    @DisplayName("Deve lançar exceção quando o limite for excedido")
    void shouldThrowExceptionWhenRateLimitIsExceeded() {
        // GIVEN
        mockAnnotationSetup("minha-chave");

        when(keyGenerator.supports(any())).thenReturn(true);
        when(keyGenerator.generateKey(anyString())).thenReturn("chave-final:123");

        // O serviço diz que NÃO tem tokens (false)
        when(rateLimitService.allowRequest(anyString(), anyInt(), any())).thenReturn(false);

        // Configuramos os dados para a exceção
        when(rateLimitService.getRemainingRequests(anyString(), anyInt())).thenReturn(0L);
        when(rateLimitService.getResetTimeSeconds(anyString())).thenReturn(60L);

        // WHEN & THEN
        assertThrows(RateLimitExceededException.class, () -> {
            rateLimitAspect.checkRateLimit(joinPoint);
        });

        // Garante que o méto.do original NUNCA foi chamado
        try {
            verify(joinPoint, never()).proceed();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    // Méto.do auxiliar para configurar a "magia" da reflexão do AspectJ
    private void mockAnnotationSetup(String keyName) {
        when(joinPoint.getSignature()).thenReturn(signature);

        // Mockando um méto.do fake apenas para retornar a anotação
        Method method = mock(Method.class);
        when(signature.getMethod()).thenReturn(method);
        when(method.getAnnotation(RateLimit.class)).thenReturn(rateLimitAnnotation);

        when(rateLimitAnnotation.unit()).thenReturn(TimeUnit.SECONDS);

        // Configurando valores da anotação
        when(rateLimitAnnotation.key()).thenReturn(keyName);
        when(rateLimitAnnotation.limit()).thenReturn(10);
        when(rateLimitAnnotation.duration()).thenReturn(60L);
         when(rateLimitAnnotation.type()).thenReturn(RateLimitType.IP); // Se necessário
    }
}
