package br.com.smartmesquitaapi.api.interceptor;

import br.com.smartmesquitaapi.config.RateLimiterConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiterConfig rateLimiterConfig;

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {

        String clientKey = rateLimiterConfig.getClientKey(request);

        if (!rateLimiterConfig.tryConsume(clientKey)){
            log.warn("Rate limit excedido - IP: {} | URI: {}", clientKey, request.getRequestURI());

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("""
                    {
                      "timestamp": "%s",
                      "status": 429,
                      "error": "Too Many Requests",
                      "message": "Limite de requisições excedido. Tente novamente mais tarde.",
                      "path": "%s"
                    }
                    """.formatted(
                            java.time.LocalDateTime.now().toString(),
                            request.getRequestURI()
                    ));
            return false;
        }
        return true;
    }
}
