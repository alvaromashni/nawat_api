package br.com.smartmesquitaapi.ratelimit.keygenerators;

import br.com.smartmesquitaapi.ratelimit.RateLimitType;
import br.com.smartmesquitaapi.user.service.UserContextService;
import org.springframework.stereotype.Component;

@Component
class IpKeyGenerator implements RateLimitKeyGenerator {

    private final UserContextService userContextService;

    IpKeyGenerator(UserContextService userContextService) {
        this.userContextService = userContextService;
    }

    @Override
    public boolean supports(RateLimitType type) {
        return type == RateLimitType.IP;
    }

    @Override
    public String generateKey(String baseKey) {
        String ip = userContextService.getClientIp();
        return baseKey + ":" + ip;    }
}
