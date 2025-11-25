package br.com.smartmesquitaapi.ratelimit.keygenerators;

import br.com.smartmesquitaapi.ratelimit.RateLimitType;

public interface RateLimitKeyGenerator {

    boolean supports(RateLimitType type);
    String generateKey(String baseKey);

}
