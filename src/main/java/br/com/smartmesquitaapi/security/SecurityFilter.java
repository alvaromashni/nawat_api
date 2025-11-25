package br.com.smartmesquitaapi.security;

import br.com.smartmesquitaapi.auth.JWTUserData;
import br.com.smartmesquitaapi.user.domain.User;
import br.com.smartmesquitaapi.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenConfig tokenConfig;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try{
            String authorizesHeader = request.getHeader("Authorization");

            if (Strings.isNotEmpty(authorizesHeader) && authorizesHeader.startsWith("Bearer ")) {
                String token = authorizesHeader.substring("Bearer ".length());
                log.info(">>> Token recebido: {}...", token.substring(0, Math.min(20, token.length())));

                Optional<JWTUserData> optUser = tokenConfig.validateToken(token);

                if (optUser.isPresent()) {
                    JWTUserData userData = optUser.get();
                    log.info(">>> Token válido para userId: {}", userData.userId());

                    UUID userId = userData.userId();
                    Optional<User> userOpt = userRepository.findById(userId);

                    if (userOpt.isPresent()) {
                        User user = userOpt.get();

                        log.info(">>> Usuário encontrado: {} | Role: {} | Enabled: {} | Authorities: {}",
                                user.getEmail(), user.getRole(), user.isEnabled(), user.getAuthorities());

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        user,
                                        null,
                                        user.getAuthorities()
                                );

                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        log.info(">>> Autenticação criada e setada no contexto para: {}", user.getEmail());
                    } else {
                        log.warn("User não encontrado no banco: {}", userId);
                    }
                } else {
                    log.warn(">>> Token inválido ou expirado!");
                }
            }
            filterChain.doFilter(request, response);
        } catch(Exception e){
            System.out.println(">>> ERRO NO FILTRO DE SEGURANÇA: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}