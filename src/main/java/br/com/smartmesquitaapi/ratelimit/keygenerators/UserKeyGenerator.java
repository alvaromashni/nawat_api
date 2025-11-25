package br.com.smartmesquitaapi.ratelimit.keygenerators;

import br.com.smartmesquitaapi.ratelimit.RateLimitType;
import br.com.smartmesquitaapi.user.service.UserContextService;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserKeyGenerator implements RateLimitKeyGenerator {

    private final UserContextService userContextService;

    public UserKeyGenerator(UserContextService userContextService) {
        this.userContextService = userContextService;
    }

    @Override
    public boolean supports(RateLimitType type) {
        return type == RateLimitType.USER;
    }

    @Override
    public String generateKey(String baseKey) {
        UUID userId = userContextService.getCurrentUserId();
        return userId != null ? baseKey + ":" + userId : baseKey + ":anonymous";
    }
}
