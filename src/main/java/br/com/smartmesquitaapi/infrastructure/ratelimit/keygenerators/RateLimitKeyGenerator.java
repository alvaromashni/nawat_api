package br.com.smartmesquitaapi.infrastructure.ratelimit.keygenerators;

import br.com.smartmesquitaapi.infrastructure.ratelimit.RateLimitType;

public interface RateLimitKeyGenerator {

    boolean supports(RateLimitType type);
    String generateKey(String baseKey);

}
