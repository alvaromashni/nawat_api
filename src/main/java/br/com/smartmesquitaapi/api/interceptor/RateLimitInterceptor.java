package br.com.smartmesquitaapi.api.interceptor;

import br.com.smartmesquitaapi.config.RateLimiterConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

        }
    }



}
