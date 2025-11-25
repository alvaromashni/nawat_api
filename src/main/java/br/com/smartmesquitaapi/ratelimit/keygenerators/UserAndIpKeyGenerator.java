package br.com.smartmesquitaapi.ratelimit.keygenerators;

import br.com.smartmesquitaapi.ratelimit.RateLimitType;
import br.com.smartmesquitaapi.user.service.UserContextService;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserAndIpKeyGenerator implements RateLimitKeyGenerator {

    private final UserContextService userContextService;

    public UserAndIpKeyGenerator(UserContextService userContextService) {
        this.userContextService = userContextService;
    }

    @Override
    public boolean supports(RateLimitType type) {
        return type == RateLimitType.USER_AND_IP;
    }

    @Override
    public String generateKey(String baseKey) {
        UUID userId2 = userContextService.getCurrentUserId();
        String ip2 = userContextService.getClientIp();
        return baseKey + ":" + (userId2 != null ? userId2 : "anonymous") + ":" + ip2;
    }
}
