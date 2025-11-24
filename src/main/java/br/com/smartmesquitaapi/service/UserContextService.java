package br.com.smartmesquitaapi.service;

import br.com.smartmesquitaapi.domain.user.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

@Service
@Slf4j
public class UserContextService {

    public UUID getCurrentUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof User) {
                User user = (User) auth.getPrincipal();
                return user.getUserId();
            }
        } catch (Exception e) {
            log.debug("Could not get user ID from authentication", e);
        }
        return null;
    }

    public String getClientIp() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();

                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("X-Real-IP");
                }
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }

                if (ip != null && ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }

                return ip != null ? ip : "unknown";
            }
        } catch (Exception e) {
            log.debug("Could not get client IP", e);
        }
        return "unknown";
    }

}
