package br.com.smartmesquitaapi.ratelimit.keygenerators;

import br.com.smartmesquitaapi.ratelimit.RateLimitType;
import org.springframework.stereotype.Component;

@Component
public class GlobalKeyGenerator implements RateLimitKeyGenerator {

    @Override
    public String generateKey(String baseKey) {
        return baseKey;
    }

    @Override
    public boolean supports(RateLimitType type) {
        return type ==RateLimitType.GLOBAL;
    }
}
