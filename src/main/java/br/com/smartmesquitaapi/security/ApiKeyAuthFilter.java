package br.com.smartmesquitaapi.security;

import br.com.smartmesquitaapi.apikey.domain.TotemKey;
import br.com.smartmesquitaapi.apikey.repository.TotemKeyRepository;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final TotemKeyRepository totemKeyRepository;

    public ApiKeyAuthFilter(TotemKeyRepository totemKeyRepository) {
        this.totemKeyRepository = totemKeyRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String apiKeyHeader = request.getHeader("X-API-KEY");

        if (apiKeyHeader != null && SecurityContextHolder.getContext().getAuthentication() == null){

            var totemOptional = totemKeyRepository.findByKeyValueAndIsActiveTrue(apiKeyHeader);

            if (totemOptional.isPresent()){
                TotemKey totem = totemOptional.get();

                var authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_TOTEM"));
                var auth = new UsernamePasswordAuthenticationToken(
                        totem.getOrganization(),
                        null,
                        authorities
                );

                SecurityContextHolder.getContext().setAuthentication(auth);

            }
        }
        filterChain.doFilter(request, response);
    }
}
