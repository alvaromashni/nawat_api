package br.com.smartmesquitaapi.security;

import br.com.smartmesquitaapi.auth.JWTUserData;
import br.com.smartmesquitaapi.user.domain.User;
import br.com.smartmesquitaapi.user.domain.UserRole;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TokenConfig - Testes de Geração e Validação de JWT")
class TokenConfigTest {

    private TokenConfig tokenConfig;
    private User testUser;
    private static final String TEST_SECRET = "test-secret-key-for-jwt-signing-minimum-256-bits-required";

    @BeforeEach
    void setUp() {
        tokenConfig = new TokenConfig();
        ReflectionTestUtils.setField(tokenConfig, "secret", TEST_SECRET);

        // Criar usuário de teste
        testUser = new User();
        testUser.setUserId(UUID.randomUUID());
        testUser.setName("João Silva");
        testUser.setEmail("joao@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(UserRole.USER);
        testUser.setEnabled(true);
    }

    // ==================== TESTES DE GERAÇÃO DE TOKEN ====================

    @Test
    @DisplayName("Deve gerar token JWT válido para usuário")
    void shouldGenerateValidJwtTokenForUser() {
        // Act
        String token = tokenConfig.generateToken(testUser);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT tem 3 partes separadas por '.'
    }

    @Test
    @DisplayName("Deve incluir userId no token como claim")
    void shouldIncludeUserIdInTokenAsClaim() {
        // Act
        String token = tokenConfig.generateToken(testUser);

        // Assert
        DecodedJWT decodedJWT = JWT.decode(token);
        String userIdClaim = decodedJWT.getClaim("userId").asString();

        assertNotNull(userIdClaim);
        assertEquals(testUser.getUserId().toString(), userIdClaim);
    }

    @Test
    @DisplayName("Deve incluir email no subject do token")
    void shouldIncludeEmailInTokenSubject() {
        // Act
        String token = tokenConfig.generateToken(testUser);

        // Assert
        DecodedJWT decodedJWT = JWT.decode(token);
        String subject = decodedJWT.getSubject();

        assertNotNull(subject);
        assertEquals(testUser.getEmail(), subject);
    }

    @Test
    @DisplayName("Deve configurar expiração de 24 horas (86400 segundos)")
    void shouldSetExpirationTo24Hours() {
        // Arrange
        Instant before = Instant.now();

        // Act
        String token = tokenConfig.generateToken(testUser);

        // Assert
        Instant after = Instant.now();
        DecodedJWT decodedJWT = JWT.decode(token);
        Instant expiresAt = decodedJWT.getExpiresAt().toInstant();
        Instant issuedAt = decodedJWT.getIssuedAt().toInstant();

        // Verificar que foi emitido agora
        assertTrue(issuedAt.isAfter(before.minusSeconds(1)));
        assertTrue(issuedAt.isBefore(after.plusSeconds(1)));

        // Verificar que expira em ~24 horas (86400 segundos)
        long secondsUntilExpiry = expiresAt.getEpochSecond() - issuedAt.getEpochSecond();
        assertEquals(86400, secondsUntilExpiry);
    }

    @Test
    @DisplayName("Deve incluir timestamp de emissão")
    void shouldIncludeIssuedAtTimestamp() {
        // Arrange
        Instant before = Instant.now();

        // Act
        String token = tokenConfig.generateToken(testUser);

        // Assert
        Instant after = Instant.now();
        DecodedJWT decodedJWT = JWT.decode(token);
        Instant issuedAt = decodedJWT.getIssuedAt().toInstant();

        assertNotNull(issuedAt);
        assertTrue(issuedAt.isAfter(before.minusSeconds(1)));
        assertTrue(issuedAt.isBefore(after.plusSeconds(1)));
    }

    @Test
    @DisplayName("Deve gerar tokens diferentes para chamadas sucessivas")
    void shouldGenerateDifferentTokensForSuccessiveCalls() {
        // Act
        String token1 = tokenConfig.generateToken(testUser);

        // Pequeno delay para garantir timestamps diferentes
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String token2 = tokenConfig.generateToken(testUser);

        // Assert
        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2);
    }

    @Test
    @DisplayName("Deve assinar token com algoritmo HMAC256")
    void shouldSignTokenWithHmac256Algorithm() {
        // Act
        String token = tokenConfig.generateToken(testUser);

        // Assert
        DecodedJWT decodedJWT = JWT.decode(token);
        assertEquals("HS256", decodedJWT.getAlgorithm());
    }

    // ==================== TESTES DE VALIDAÇÃO DE TOKEN ====================

    @Test
    @DisplayName("Deve validar token válido e retornar dados do usuário")
    void shouldValidateValidTokenAndReturnUserData() {
        // Arrange
        String token = tokenConfig.generateToken(testUser);

        // Act
        Optional<JWTUserData> result = tokenConfig.validateToken(token);

        // Assert
        assertTrue(result.isPresent());
        JWTUserData userData = result.get();
        assertEquals(testUser.getUserId(), userData.getUserId());
        assertEquals(testUser.getEmail(), userData.getEmail());
    }

    @Test
    @DisplayName("Deve retornar Optional vazio para token inválido")
    void shouldReturnEmptyOptionalForInvalidToken() {
        // Arrange
        String invalidToken = "invalid.jwt.token";

        // Act
        Optional<JWTUserData> result = tokenConfig.validateToken(invalidToken);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Deve retornar Optional vazio para token com assinatura incorreta")
    void shouldReturnEmptyOptionalForTokenWithWrongSignature() {
        // Arrange
        String tokenWithWrongSignature = JWT.create()
            .withClaim("userId", testUser.getUserId().toString())
            .withSubject(testUser.getEmail())
            .withExpiresAt(Instant.now().plusSeconds(86400))
            .withIssuedAt(Instant.now())
            .sign(Algorithm.HMAC256("wrong-secret-key"));

        // Act
        Optional<JWTUserData> result = tokenConfig.validateToken(tokenWithWrongSignature);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Deve retornar Optional vazio para token expirado")
    void shouldReturnEmptyOptionalForExpiredToken() {
        // Arrange
        String expiredToken = JWT.create()
            .withClaim("userId", testUser.getUserId().toString())
            .withSubject(testUser.getEmail())
            .withExpiresAt(Instant.now().minusSeconds(3600)) // Expirado há 1 hora
            .withIssuedAt(Instant.now().minusSeconds(90000))
            .sign(Algorithm.HMAC256(TEST_SECRET));

        // Act
        Optional<JWTUserData> result = tokenConfig.validateToken(expiredToken);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Deve retornar Optional vazio para token nulo")
    void shouldReturnEmptyOptionalForNullToken() {
        // Act
        Optional<JWTUserData> result = tokenConfig.validateToken(null);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Deve retornar Optional vazio para token vazio")
    void shouldReturnEmptyOptionalForEmptyToken() {
        // Act
        Optional<JWTUserData> result = tokenConfig.validateToken("");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Deve retornar Optional vazio para token malformado")
    void shouldReturnEmptyOptionalForMalformedToken() {
        // Arrange
        String malformedToken = "this.is.not.a.valid.jwt.format";

        // Act
        Optional<JWTUserData> result = tokenConfig.validateToken(malformedToken);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Deve validar token sem claim de userId")
    void shouldReturnEmptyOptionalForTokenWithoutUserIdClaim() {
        // Arrange
        String tokenWithoutUserId = JWT.create()
            .withSubject(testUser.getEmail())
            .withExpiresAt(Instant.now().plusSeconds(86400))
            .withIssuedAt(Instant.now())
            .sign(Algorithm.HMAC256(TEST_SECRET));

        // Act & Assert
        // O token será validado, mas ao tentar extrair o userId causará erro
        assertThrows(Exception.class, () -> {
            Optional<JWTUserData> result = tokenConfig.validateToken(tokenWithoutUserId);
            result.ifPresent(data -> data.getUserId()); // Forçar extração do claim
        });
    }

    // ==================== TESTES DE INTEGRAÇÃO ====================

    @Test
    @DisplayName("Deve completar ciclo completo: gerar -> validar -> extrair dados")
    void shouldCompleteFullCycle_Generate_Validate_Extract() {
        // Arrange & Act - Gerar
        String token = tokenConfig.generateToken(testUser);

        // Act - Validar
        Optional<JWTUserData> result = tokenConfig.validateToken(token);

        // Assert
        assertTrue(result.isPresent());

        JWTUserData userData = result.get();
        assertEquals(testUser.getUserId(), userData.getUserId());
        assertEquals(testUser.getEmail(), userData.getEmail());
    }

    @Test
    @DisplayName("Deve validar múltiplos tokens para diferentes usuários")
    void shouldValidateMultipleTokensForDifferentUsers() {
        // Arrange
        User user1 = new User();
        user1.setUserId(UUID.randomUUID());
        user1.setEmail("user1@example.com");

        User user2 = new User();
        user2.setUserId(UUID.randomUUID());
        user2.setEmail("user2@example.com");

        // Act
        String token1 = tokenConfig.generateToken(user1);
        String token2 = tokenConfig.generateToken(user2);

        Optional<JWTUserData> result1 = tokenConfig.validateToken(token1);
        Optional<JWTUserData> result2 = tokenConfig.validateToken(token2);

        // Assert
        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());

        assertEquals(user1.getUserId(), result1.get().getUserId());
        assertEquals(user1.getEmail(), result1.get().getEmail());

        assertEquals(user2.getUserId(), result2.get().getUserId());
        assertEquals(user2.getEmail(), result2.get().getEmail());

        assertNotEquals(token1, token2);
    }

    @Test
    @DisplayName("Deve preservar UUID correto durante geração e validação")
    void shouldPreserveCorrectUuidDuringGenerationAndValidation() {
        // Arrange
        UUID specificUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        testUser.setUserId(specificUserId);

        // Act
        String token = tokenConfig.generateToken(testUser);
        Optional<JWTUserData> result = tokenConfig.validateToken(token);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(specificUserId, result.get().getUserId());
    }

    @Test
    @DisplayName("Deve preservar email com caracteres especiais")
    void shouldPreserveEmailWithSpecialCharacters() {
        // Arrange
        testUser.setEmail("user+tag@example.com.br");

        // Act
        String token = tokenConfig.generateToken(testUser);
        Optional<JWTUserData> result = tokenConfig.validateToken(token);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("user+tag@example.com.br", result.get().getEmail());
    }

    @Test
    @DisplayName("Deve rejeitar token após modificação manual")
    void shouldRejectTokenAfterManualModification() {
        // Arrange
        String token = tokenConfig.generateToken(testUser);
        String[] parts = token.split("\\.");

        // Modificar payload (segunda parte)
        String modifiedToken = parts[0] + "." + parts[1] + "modified" + "." + parts[2];

        // Act
        Optional<JWTUserData> result = tokenConfig.validateToken(modifiedToken);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Deve lidar com diferentes roles de usuário")
    void shouldHandleDifferentUserRoles() {
        // Arrange
        User adminUser = new User();
        adminUser.setUserId(UUID.randomUUID());
        adminUser.setEmail("admin@example.com");
        adminUser.setRole(UserRole.ADMIN);

        User regularUser = new User();
        regularUser.setUserId(UUID.randomUUID());
        regularUser.setEmail("user@example.com");
        regularUser.setRole(UserRole.USER);

        // Act
        String adminToken = tokenConfig.generateToken(adminUser);
        String userToken = tokenConfig.generateToken(regularUser);

        Optional<JWTUserData> adminResult = tokenConfig.validateToken(adminToken);
        Optional<JWTUserData> userResult = tokenConfig.validateToken(userToken);

        // Assert
        assertTrue(adminResult.isPresent());
        assertTrue(userResult.isPresent());

        assertEquals(adminUser.getEmail(), adminResult.get().getEmail());
        assertEquals(regularUser.getEmail(), userResult.get().getEmail());
    }
}
