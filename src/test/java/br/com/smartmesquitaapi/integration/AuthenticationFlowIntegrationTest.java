package br.com.smartmesquitaapi.integration;

import br.com.smartmesquitaapi.auth.AuthService;
import br.com.smartmesquitaapi.auth.RefreshToken;
import br.com.smartmesquitaapi.auth.RefreshTokenRepository;
import br.com.smartmesquitaapi.auth.dto.request.BankDetailsRequest;
import br.com.smartmesquitaapi.auth.dto.request.LoginRequest;
import br.com.smartmesquitaapi.auth.dto.request.RegisterUserRequest;
import br.com.smartmesquitaapi.auth.dto.response.AuthResponse;
import br.com.smartmesquitaapi.security.TokenConfig;
import br.com.smartmesquitaapi.user.UserRepository;
import br.com.smartmesquitaapi.user.domain.BankDetails;
import br.com.smartmesquitaapi.user.domain.PixKeyType;
import br.com.smartmesquitaapi.user.domain.User;
import br.com.smartmesquitaapi.user.domain.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Teste de integração end-to-end do fluxo completo de autenticação
 * Testa: Registro -> Login -> Refresh Token -> Verificação de Token
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Fluxo de Integração - Autenticação Completa")
class AuthenticationFlowIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private TokenConfig tokenConfig;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // Limpar dados antes de cada teste
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ==================== TESTES DE FLUXO COMPLETO ====================

    @Test
    @DisplayName("Deve completar fluxo completo: Registro -> Login -> Refresh Token")
    void shouldCompleteFullAuthenticationFlow() {
        // ===== FASE 1: REGISTRO =====
        RegisterUserRequest registerRequest = new RegisterUserRequest();
        registerRequest.setName("Maria Silva");
        registerRequest.setEmail("maria@example.com");
        registerRequest.setPassword("senha123");
        registerRequest.setRole(UserRole.USER);

        // Act - Registrar
        AuthResponse registerResponse = authService.register(registerRequest);

        // Assert - Verificar resposta de registro
        assertNotNull(registerResponse);
        assertNotNull(registerResponse.getToken());
        assertNotNull(registerResponse.getRefreshToken());
        assertEquals("Bearer", registerResponse.getType());
        assertEquals("Maria Silva", registerResponse.getUser().getName());
        assertEquals("maria@example.com", registerResponse.getUser().getEmail());
        assertEquals(UserRole.USER, registerResponse.getUser().getRole());
        assertTrue(registerResponse.getUser().getIsActive());

        // Verificar que o usuário foi salvo no banco
        Optional<User> savedUser = userRepository.findByEmail("maria@example.com");
        assertTrue(savedUser.isPresent());
        assertEquals("Maria Silva", savedUser.get().getName());

        // Verificar que o refresh token foi salvo
        Optional<RefreshToken> savedRefreshToken = refreshTokenRepository.findByToken(registerResponse.getRefreshToken());
        assertTrue(savedRefreshToken.isPresent());
        assertEquals(savedUser.get().getUserId(), savedRefreshToken.get().getUser().getUserId());

        // ===== FASE 2: LOGIN =====
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("maria@example.com");
        loginRequest.setPassword("senha123");

        // Act - Login
        AuthResponse loginResponse = authService.login(loginRequest);

        // Assert - Verificar resposta de login
        assertNotNull(loginResponse);
        assertNotNull(loginResponse.getToken());
        assertNotNull(loginResponse.getRefreshToken());
        assertNotEquals(registerResponse.getToken(), loginResponse.getToken()); // Novo token
        assertEquals("maria@example.com", loginResponse.getUser().getEmail());

        // ===== FASE 3: REFRESH TOKEN =====
        String currentRefreshToken = loginResponse.getRefreshToken();

        // Act - Renovar tokens
        AuthResponse refreshResponse = authService.processRefreshToken(currentRefreshToken);

        // Assert - Verificar resposta de refresh
        assertNotNull(refreshResponse);
        assertNotNull(refreshResponse.getToken());
        assertNotNull(refreshResponse.getRefreshToken());
        assertNotEquals(loginResponse.getToken(), refreshResponse.getToken()); // Novo JWT
        assertNotEquals(currentRefreshToken, refreshResponse.getRefreshToken()); // Novo refresh token

        // Verificar que o refresh token antigo foi removido
        Optional<RefreshToken> oldRefreshToken = refreshTokenRepository.findByToken(currentRefreshToken);
        assertFalse(oldRefreshToken.isPresent());

        // Verificar que o novo refresh token foi salvo
        Optional<RefreshToken> newRefreshToken = refreshTokenRepository.findByToken(refreshResponse.getRefreshToken());
        assertTrue(newRefreshToken.isPresent());

        // ===== FASE 4: VALIDAÇÃO DE TOKEN =====
        // Verificar que todos os tokens JWT gerados são válidos
        assertTrue(tokenConfig.validateToken(registerResponse.getToken()).isPresent());
        assertTrue(tokenConfig.validateToken(loginResponse.getToken()).isPresent());
        assertTrue(tokenConfig.validateToken(refreshResponse.getToken()).isPresent());
    }

    @Test
    @DisplayName("Deve registrar usuário com dados bancários completos")
    void shouldRegisterUserWithCompleteBankDetails() {
        // Arrange
        BankDetailsRequest bankDetails = new BankDetailsRequest();
        bankDetails.setPixKey("comercio@example.com");
        bankDetails.setPixKeyType(PixKeyType.EMAIL);
        bankDetails.setBankName("Caixa Econômica Federal");
        bankDetails.setAccountHolder("Comércio LTDA");
        bankDetails.setCnpj("12345678000199");
        bankDetails.setAccountNumber("98765-4");

        RegisterUserRequest registerRequest = new RegisterUserRequest();
        registerRequest.setName("Comércio LTDA");
        registerRequest.setEmail("comercio@example.com");
        registerRequest.setPassword("senhaSegura123");
        registerRequest.setRole(UserRole.USER);
        registerRequest.setBankDetails(bankDetails);

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.getUser().getHasPixKey());

        // Verificar no banco de dados
        Optional<User> savedUser = userRepository.findByEmail("comercio@example.com");
        assertTrue(savedUser.isPresent());

        BankDetails savedBankDetails = savedUser.get().getBankDetails();
        assertNotNull(savedBankDetails);
        assertEquals("comercio@example.com", savedBankDetails.getPixKey());
        assertEquals(PixKeyType.EMAIL, savedBankDetails.getPixKeyType());
        assertEquals("Caixa Econômica Federal", savedBankDetails.getBankName());
        assertEquals("Comércio LTDA", savedBankDetails.getAccountHolder());
        assertEquals("12345678000199", savedBankDetails.getCnpj());
        assertEquals("98765-4", savedBankDetails.getAccountNumber());
        assertFalse(savedBankDetails.getIsVerified()); // Não verificado por padrão
    }

    @Test
    @DisplayName("Deve falhar login com senha incorreta")
    void shouldFailLoginWithIncorrectPassword() {
        // Arrange - Registrar usuário primeiro
        RegisterUserRequest registerRequest = new RegisterUserRequest();
        registerRequest.setName("Pedro Santos");
        registerRequest.setEmail("pedro@example.com");
        registerRequest.setPassword("senhaCorreta");
        registerRequest.setRole(UserRole.USER);

        authService.register(registerRequest);

        // Tentar login com senha errada
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("pedro@example.com");
        loginRequest.setPassword("senhaErrada");

        // Act & Assert
        assertThrows(Exception.class, () -> {
            authService.login(loginRequest);
        });
    }

    @Test
    @DisplayName("Deve impedir registro de email duplicado")
    void shouldPreventDuplicateEmailRegistration() {
        // Arrange - Registrar primeiro usuário
        RegisterUserRequest firstRequest = new RegisterUserRequest();
        firstRequest.setName("Primeiro Usuário");
        firstRequest.setEmail("duplicado@example.com");
        firstRequest.setPassword("senha123");
        firstRequest.setRole(UserRole.USER);

        authService.register(firstRequest);

        // Tentar registrar com mesmo email
        RegisterUserRequest duplicateRequest = new RegisterUserRequest();
        duplicateRequest.setName("Segundo Usuário");
        duplicateRequest.setEmail("duplicado@example.com");
        duplicateRequest.setPassword("outraSenha");
        duplicateRequest.setRole(UserRole.USER);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            authService.register(duplicateRequest);
        });
    }

    @Test
    @DisplayName("Deve criar refresh token com expiração de 30 dias")
    void shouldCreateRefreshTokenWith30DaysExpiration() {
        // Arrange
        RegisterUserRequest registerRequest = new RegisterUserRequest();
        registerRequest.setName("Ana Costa");
        registerRequest.setEmail("ana@example.com");
        registerRequest.setPassword("senha123");
        registerRequest.setRole(UserRole.USER);

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        Optional<RefreshToken> savedToken = refreshTokenRepository.findByToken(response.getRefreshToken());
        assertTrue(savedToken.isPresent());

        Instant expiryDate = savedToken.get().getExpiryDate();
        long daysUntilExpiry = Duration.between(Instant.now(), expiryDate).toDays();

        // Deve expirar em aproximadamente 30 dias
        assertTrue(daysUntilExpiry >= 29 && daysUntilExpiry <= 30);
    }

    @Test
    @DisplayName("Deve invalidar refresh token antigo ao gerar novo")
    void shouldInvalidateOldRefreshTokenWhenGeneratingNew() {
        // Arrange - Registrar e fazer login
        RegisterUserRequest registerRequest = new RegisterUserRequest();
        registerRequest.setName("Carlos Eduardo");
        registerRequest.setEmail("carlos@example.com");
        registerRequest.setPassword("senha123");
        registerRequest.setRole(UserRole.USER);

        AuthResponse registerResponse = authService.register(registerRequest);
        String oldRefreshToken = registerResponse.getRefreshToken();

        // Act - Processar refresh token
        AuthResponse refreshResponse = authService.processRefreshToken(oldRefreshToken);

        // Assert - Token antigo não deve mais existir
        Optional<RefreshToken> oldToken = refreshTokenRepository.findByToken(oldRefreshToken);
        assertFalse(oldToken.isPresent());

        // Token novo deve existir
        Optional<RefreshToken> newToken = refreshTokenRepository.findByToken(refreshResponse.getRefreshToken());
        assertTrue(newToken.isPresent());
    }

    @Test
    @DisplayName("Deve gerar tokens JWT válidos e decodificáveis")
    void shouldGenerateValidAndDecodableJwtTokens() {
        // Arrange
        RegisterUserRequest registerRequest = new RegisterUserRequest();
        registerRequest.setName("Laura Mendes");
        registerRequest.setEmail("laura@example.com");
        registerRequest.setPassword("senha123");
        registerRequest.setRole(UserRole.USER);

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert - Token deve ser válido
        var decodedToken = tokenConfig.validateToken(response.getToken());
        assertTrue(decodedToken.isPresent());
        assertEquals("laura@example.com", decodedToken.get().getEmail());

        // Verificar que o token tem o formato JWT correto (3 partes separadas por pontos)
        String[] tokenParts = response.getToken().split("\\.");
        assertEquals(3, tokenParts.length);
    }

    @Test
    @DisplayName("Deve permitir múltiplos logins simultâneos (diferentes refresh tokens)")
    void shouldAllowMultipleSimultaneousLogins() {
        // Arrange - Registrar usuário
        RegisterUserRequest registerRequest = new RegisterUserRequest();
        registerRequest.setName("Roberto Alves");
        registerRequest.setEmail("roberto@example.com");
        registerRequest.setPassword("senha123");
        registerRequest.setRole(UserRole.USER);

        authService.register(registerRequest);

        // Act - Fazer múltiplos logins
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("roberto@example.com");
        loginRequest.setPassword("senha123");

        AuthResponse login1 = authService.login(loginRequest);
        AuthResponse login2 = authService.login(loginRequest);
        AuthResponse login3 = authService.login(loginRequest);

        // Assert - Todos devem ter tokens diferentes
        assertNotEquals(login1.getRefreshToken(), login2.getRefreshToken());
        assertNotEquals(login2.getRefreshToken(), login3.getRefreshToken());
        assertNotEquals(login1.getRefreshToken(), login3.getRefreshToken());

        // Todos os refresh tokens devem ser válidos
        assertTrue(refreshTokenRepository.findByToken(login1.getRefreshToken()).isPresent());
        assertTrue(refreshTokenRepository.findByToken(login2.getRefreshToken()).isPresent());
        assertTrue(refreshTokenRepository.findByToken(login3.getRefreshToken()).isPresent());
    }

    @Test
    @DisplayName("Deve criptografar senha ao registrar usuário")
    void shouldEncryptPasswordWhenRegisteringUser() {
        // Arrange
        RegisterUserRequest registerRequest = new RegisterUserRequest();
        registerRequest.setName("Fernanda Lima");
        registerRequest.setEmail("fernanda@example.com");
        registerRequest.setPassword("minhaSenha123");
        registerRequest.setRole(UserRole.USER);

        // Act
        authService.register(registerRequest);

        // Assert
        Optional<User> savedUser = userRepository.findByEmail("fernanda@example.com");
        assertTrue(savedUser.isPresent());

        // Senha deve estar criptografada (não em texto plano)
        assertNotEquals("minhaSenha123", savedUser.get().getPassword());

        // Deve ser possível verificar com BCrypt
        assertTrue(passwordEncoder.matches("minhaSenha123", savedUser.get().getPassword()));
    }

    @Test
    @DisplayName("Deve criar usuários com diferentes roles (USER e ADMIN)")
    void shouldCreateUsersWithDifferentRoles() {
        // Arrange & Act - Criar usuário comum
        RegisterUserRequest userRequest = new RegisterUserRequest();
        userRequest.setName("Usuário Comum");
        userRequest.setEmail("user@example.com");
        userRequest.setPassword("senha123");
        userRequest.setRole(UserRole.USER);

        AuthResponse userResponse = authService.register(userRequest);

        // Arrange & Act - Criar usuário admin
        RegisterUserRequest adminRequest = new RegisterUserRequest();
        adminRequest.setName("Administrador");
        adminRequest.setEmail("admin@example.com");
        adminRequest.setPassword("senha123");
        adminRequest.setRole(UserRole.ADMIN);

        AuthResponse adminResponse = authService.register(adminRequest);

        // Assert
        assertEquals(UserRole.USER, userResponse.getUser().getRole());
        assertEquals(UserRole.ADMIN, adminResponse.getUser().getRole());

        // Verificar no banco
        Optional<User> savedUser = userRepository.findByEmail("user@example.com");
        Optional<User> savedAdmin = userRepository.findByEmail("admin@example.com");

        assertTrue(savedUser.isPresent());
        assertTrue(savedAdmin.isPresent());
        assertEquals(UserRole.USER, savedUser.get().getRole());
        assertEquals(UserRole.ADMIN, savedAdmin.get().getRole());
    }

    @Test
    @DisplayName("Deve manter sessão válida após múltiplos refreshes")
    void shouldMaintainValidSessionAfterMultipleRefreshes() {
        // Arrange - Registrar usuário
        RegisterUserRequest registerRequest = new RegisterUserRequest();
        registerRequest.setName("Sessão Teste");
        registerRequest.setEmail("sessao@example.com");
        registerRequest.setPassword("senha123");
        registerRequest.setRole(UserRole.USER);

        AuthResponse initialResponse = authService.register(registerRequest);
        String currentRefreshToken = initialResponse.getRefreshToken();

        // Act - Fazer múltiplos refreshes
        AuthResponse refresh1 = authService.processRefreshToken(currentRefreshToken);
        AuthResponse refresh2 = authService.processRefreshToken(refresh1.getRefreshToken());
        AuthResponse refresh3 = authService.processRefreshToken(refresh2.getRefreshToken());

        // Assert - Todos os tokens devem ser válidos
        assertTrue(tokenConfig.validateToken(refresh1.getToken()).isPresent());
        assertTrue(tokenConfig.validateToken(refresh2.getToken()).isPresent());
        assertTrue(tokenConfig.validateToken(refresh3.getToken()).isPresent());

        // Email deve ser o mesmo em todos
        assertEquals("sessao@example.com", tokenConfig.validateToken(refresh1.getToken()).get().getEmail());
        assertEquals("sessao@example.com", tokenConfig.validateToken(refresh2.getToken()).get().getEmail());
        assertEquals("sessao@example.com", tokenConfig.validateToken(refresh3.getToken()).get().getEmail());
    }
}
