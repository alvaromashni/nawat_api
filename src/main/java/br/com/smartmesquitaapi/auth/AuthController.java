package br.com.smartmesquitaapi.auth;

import br.com.smartmesquitaapi.auth.dto.request.RefreshTokenRequest;
import br.com.smartmesquitaapi.ratelimit.RateLimitType;
import br.com.smartmesquitaapi.ratelimit.annotations.RateLimit;
import br.com.smartmesquitaapi.auth.dto.request.LoginRequest;
import br.com.smartmesquitaapi.auth.dto.response.AuthResponse;
import br.com.smartmesquitaapi.auth.dto.request.RegisterUserRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        log.info("POST /api/auth/register - Email: {} | Role: {}", request.getEmail(), request.getRole());

        AuthResponse response = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @RateLimit(limit = 5, duration = 60, unit = TimeUnit.SECONDS, type = RateLimitType.IP)
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            log.info("=== CONTROLLER: POST /api/v1/auth/login - Email: {}", request.getEmail());

            AuthResponse response = authService.login(request);

            log.info("=== CONTROLLER: Login response criado com sucesso");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("=== CONTROLLER: Erro capturado no controller: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<Void> verifyToken() {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> createNewRefreshToken(@RequestBody RefreshTokenRequest request){

        AuthResponse response = authService.processRefreshToken(request.getToken());
        return ResponseEntity.ok(response);
    }
}
