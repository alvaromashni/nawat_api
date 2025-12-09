package br.com.smartmesquitaapi.auth;

import br.com.smartmesquitaapi.api.exception.auth.EmailAlreadyExistsException;
import br.com.smartmesquitaapi.api.exception.auth.InvalidCredentialsException;
import br.com.smartmesquitaapi.api.exception.auth.UserInactiveException;
import br.com.smartmesquitaapi.auth.dto.request.LoginRequest;
import br.com.smartmesquitaapi.auth.dto.request.RegisterUserRequest;
import br.com.smartmesquitaapi.auth.dto.response.AuthResponse;
import br.com.smartmesquitaapi.organization.domain.Mosque;
import br.com.smartmesquitaapi.organization.dto.MosqueDto;
import br.com.smartmesquitaapi.organization.mapper.OrganizationMapper;
import br.com.smartmesquitaapi.pix.exception.UserNotFoundException;
import br.com.smartmesquitaapi.security.TokenConfig;
import br.com.smartmesquitaapi.user.UserRepository;
import br.com.smartmesquitaapi.user.domain.BankDetails;
import br.com.smartmesquitaapi.user.domain.PixKeyType;
import br.com.smartmesquitaapi.user.domain.User;
import br.com.smartmesquitaapi.user.domain.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService - Testes de Autenticação")
class AuthServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenConfig tokenConfig;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private OrganizationMapper organizationMapper;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private Mosque testMosque;
    private RegisterUserRequest registerRequest;
    private LoginRequest loginRequest;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        // Configurar organização de teste
        testMosque = new Mosque();
        testMosque.setOrgName("Mesquita Teste");
        testMosque.setImaName("Ima Teste");

        BankDetails bankDetails = new BankDetails();
        bankDetails.setPixKey("joao@example.com");
        bankDetails.setPixKeyType(PixKeyType.EMAIL);
        bankDetails.setIsVerified(true);
        testMosque.setBankDetails(bankDetails);

        // Configurar usuário de teste
        testUser = new User();
        testUser.setUserId(UUID.randomUUID());
        testUser.setName("João Silva");
        testUser.setEmail("joao@example.com");
        testUser.setPassword("encodedPassword123");
        testUser.setRole(UserRole.USER);
        testUser.setEnabled(true);
        testUser.setOrganization(testMosque);

        // Configurar request de registro
        registerRequest = new RegisterUserRequest();
        registerRequest.setName("João Silva");
        registerRequest.setEmail("joao@example.com");
        registerRequest.setPassword("senha123");
        registerRequest.setOrganization(null); // Sem organização por padrão

        // Configurar request de login
        loginRequest = new LoginRequest();
        loginRequest.setEmail("joao@example.com");
        loginRequest.setPassword("senha123");

        // Configurar refresh token
        refreshToken = new RefreshToken();
        refreshToken.setRefreshTokenId(UUID.randomUUID());
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUser(testUser);
        refreshToken.setExpiryDate(Instant.now().plus(30, ChronoUnit.DAYS));
    }

    // ==================== TESTES DE REGISTRO ====================

    @Test
    @DisplayName("Deve registrar novo usuário com sucesso")
    void shouldRegisterNewUserSuccessfully() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword123");
        when(organizationMapper.toEntity(any())).thenReturn(null);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(tokenConfig.generateToken(any(User.class))).thenReturn("jwt.token.here");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt.token.here", response.getToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("Bearer", response.getType());
        assertEquals("João Silva", response.getUser().getName());
        assertEquals("joao@example.com", response.getUser().getEmail());
        assertEquals(UserRole.USER, response.getUser().getRole());
        assertTrue(response.getUser().getIsActive());

        verify(userRepository).existsByEmail("joao@example.com");
        verify(passwordEncoder).encode("senha123");
        verify(userRepository).save(any(User.class));
        verify(tokenConfig).generateToken(any(User.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Deve registrar usuário com organização (Mesquita)")
    void shouldRegisterUserWithOrganization() {
        // Arrange
        MosqueDto mosqueDto = new MosqueDto();
        mosqueDto.setOrgName("Mesquita Central");
        mosqueDto.setImaName("Ima João");

        BankDetails bankDetails = new BankDetails();
        bankDetails.setPixKey("joao@example.com");
        bankDetails.setPixKeyType(PixKeyType.EMAIL);
        bankDetails.setBankName("Banco do Brasil");
        bankDetails.setAccountHolder("João Silva");
        bankDetails.setCnpj("12345678000199");
        bankDetails.setAccountNumber("12345-6");
        mosqueDto.setBankDetails(bankDetails);

        registerRequest.setOrganization(mosqueDto);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword123");
        when(organizationMapper.toEntity(any())).thenReturn(testMosque);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setUserId(UUID.randomUUID());
            savedUser.setOrganization(testMosque);
            return savedUser;
        });
        when(tokenConfig.generateToken(any(User.class))).thenReturn("jwt.token.here");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.getUser().getHasPixKey());
        assertEquals(UserRole.ORG_OWNER, response.getUser().getRole());

        verify(organizationMapper).toEntity(mosqueDto);
        verify(userRepository).save(argThat(user ->
            user.getOrganization() != null &&
            user.getRole() == UserRole.ORG_OWNER
        ));
    }

    @Test
    @DisplayName("Deve lançar exceção ao registrar email já existente")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        EmailAlreadyExistsException exception = assertThrows(
            EmailAlreadyExistsException.class,
            () -> authService.register(registerRequest)
        );

        assertEquals("Email já cadastrado no sistema", exception.getMessage());
        verify(userRepository).existsByEmail("joao@example.com");
        verify(userRepository, never()).save(any(User.class));
        verify(tokenConfig, never()).generateToken(any(User.class));
    }

    @Test
    @DisplayName("Deve criar usuário habilitado por padrão no registro")
    void shouldCreateEnabledUserByDefault() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenConfig.generateToken(any(User.class))).thenReturn("jwt.token.here");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        // Act
        authService.register(registerRequest);

        // Assert
        verify(userRepository).save(argThat(user -> user.getIsEnabled()));
    }

    // ==================== TESTES DE LOGIN ====================

    @Test
    @DisplayName("Deve fazer login com credenciais válidas")
    void shouldLoginWithValidCredentials() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(null);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(tokenConfig.generateToken(any(User.class))).thenReturn("jwt.token.here");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt.token.here", response.getToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("Bearer", response.getType());
        assertEquals(testUser.getEmail(), response.getUser().getEmail());

        verify(authenticationManager).authenticate(
            argThat(auth ->
                auth.getPrincipal().equals("joao@example.com") &&
                auth.getCredentials().equals("senha123")
            )
        );
        verify(userRepository).findByEmail("joao@example.com");
        verify(tokenConfig).generateToken(testUser);
    }

    @Test
    @DisplayName("Deve lançar exceção ao fazer login com credenciais inválidas")
    void shouldThrowExceptionWhenLoginWithInvalidCredentials() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        assertThrows(
            BadCredentialsException.class,
            () -> authService.login(loginRequest)
        );

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByEmail(anyString());
        verify(tokenConfig, never()).generateToken(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao fazer login de usuário inativo")
    void shouldThrowExceptionWhenUserIsInactive() {
        // Arrange
        testUser.setEnabled(false);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(null);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        // Act & Assert
        UserInactiveException exception = assertThrows(
            UserInactiveException.class,
            () -> authService.login(loginRequest)
        );

        assertEquals("Usuário inativo", exception.getMessage());
        verify(tokenConfig, never()).generateToken(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não for encontrado após autenticação")
    void shouldThrowExceptionWhenUserNotFoundAfterAuth() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(null);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        InvalidCredentialsException exception = assertThrows(
            InvalidCredentialsException.class,
            () -> authService.login(loginRequest)
        );

        assertEquals("Credenciais inválidas", exception.getMessage());
    }

    // ==================== TESTES DE REFRESH TOKEN ====================

    @Test
    @DisplayName("Deve criar refresh token com expiração de 30 dias")
    void shouldCreateRefreshTokenWith30DaysExpiration() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RefreshToken createdToken = authService.createRefreshToken(testUser.getEmail());

        // Assert
        assertNotNull(createdToken);
        assertNotNull(createdToken.getToken());
        assertNotNull(createdToken.getExpiryDate());
        assertEquals(testUser, createdToken.getUser());

        // Verificar que expira em aproximadamente 30 dias
        long daysUntilExpiry = ChronoUnit.DAYS.between(Instant.now(), createdToken.getExpiryDate());
        assertTrue(daysUntilExpiry >= 29 && daysUntilExpiry <= 30);

        verify(userRepository).findByEmail(testUser.getEmail());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar refresh token para usuário inexistente")
    void shouldThrowExceptionWhenCreatingRefreshTokenForNonExistentUser() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(
            UserNotFoundException.class,
            () -> authService.createRefreshToken("naoexiste@example.com")
        );

        assertEquals("Erro: Usuário não encontrado no banco!", exception.getMessage());
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Deve processar refresh token válido e retornar novos tokens")
    void shouldProcessValidRefreshTokenAndReturnNewTokens() {
        // Arrange
        String oldRefreshToken = "old-refresh-token-uuid";
        refreshToken.setToken(oldRefreshToken);

        when(refreshTokenRepository.findByToken(oldRefreshToken)).thenReturn(Optional.of(refreshToken));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(tokenConfig.generateToken(any(User.class))).thenReturn("new.jwt.token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> {
            RefreshToken token = invocation.getArgument(0);
            token.setToken(UUID.randomUUID().toString());
            return token;
        });

        // Act
        AuthResponse response = authService.processRefreshToken(oldRefreshToken);

        // Assert
        assertNotNull(response);
        assertEquals("new.jwt.token", response.getToken());
        assertNotNull(response.getRefreshToken());
        assertNotEquals(oldRefreshToken, response.getRefreshToken());

        verify(refreshTokenRepository).findByToken(oldRefreshToken);
        verify(refreshTokenRepository).delete(refreshToken);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
        verify(tokenConfig).generateToken(testUser);
    }

    @Test
    @DisplayName("Deve lançar exceção ao processar refresh token inexistente")
    void shouldThrowExceptionWhenRefreshTokenNotFound() {
        // Arrange
        String invalidToken = "invalid-token";
        when(refreshTokenRepository.findByToken(invalidToken)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> authService.processRefreshToken(invalidToken)
        );

        assertEquals("Erro: Token não encontrado no banco!", exception.getMessage());
        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Deve lançar exceção e deletar refresh token expirado")
    void shouldThrowExceptionAndDeleteExpiredRefreshToken() {
        // Arrange
        refreshToken.setExpiryDate(Instant.now().minus(1, ChronoUnit.DAYS));
        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.of(refreshToken));

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> authService.processRefreshToken(refreshToken.getToken())
        );

        assertEquals("Refresh token expirado, por favor refaça o login.", exception.getMessage());
        verify(refreshTokenRepository).delete(refreshToken);
    }

    @Test
    @DisplayName("Deve verificar token expirado corretamente")
    void shouldVerifyExpiredTokenCorrectly() {
        // Arrange
        refreshToken.setExpiryDate(Instant.now().minus(1, ChronoUnit.HOURS));

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> authService.verifyExpirationToken(refreshToken)
        );

        assertEquals("Refresh token expirado, por favor refaça o login.", exception.getMessage());
        verify(refreshTokenRepository).delete(refreshToken);
    }

    @Test
    @DisplayName("Deve retornar token quando não expirado")
    void shouldReturnTokenWhenNotExpired() {
        // Arrange
        refreshToken.setExpiryDate(Instant.now().plus(10, ChronoUnit.DAYS));

        // Act
        RefreshToken result = authService.verifyExpirationToken(refreshToken);

        // Assert
        assertNotNull(result);
        assertEquals(refreshToken, result);
        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
    }

    // ==================== TESTES DE INTEGRAÇÃO DE FLUXO ====================

    @Test
    @DisplayName("Deve completar fluxo completo: registro -> login -> refresh token")
    void shouldCompleteFullAuthenticationFlow() {
        // Arrange - Registro
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(tokenConfig.generateToken(any(User.class))).thenReturn("jwt.token.1");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        // Act - Registro
        AuthResponse registerResponse = authService.register(registerRequest);

        // Assert - Registro
        assertNotNull(registerResponse);
        assertEquals("jwt.token.1", registerResponse.getToken());

        // Arrange - Login
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(null);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(tokenConfig.generateToken(any(User.class))).thenReturn("jwt.token.2");

        // Act - Login
        AuthResponse loginResponse = authService.login(loginRequest);

        // Assert - Login
        assertNotNull(loginResponse);
        assertEquals("jwt.token.2", loginResponse.getToken());

        // Arrange - Refresh Token
        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.of(refreshToken));
        when(tokenConfig.generateToken(any(User.class))).thenReturn("jwt.token.3");

        // Act - Refresh Token
        AuthResponse refreshResponse = authService.processRefreshToken(refreshToken.getToken());

        // Assert - Refresh Token
        assertNotNull(refreshResponse);
        assertEquals("jwt.token.3", refreshResponse.getToken());
    }

    @Test
    @DisplayName("Deve incluir informação de PIX key no AuthResponse")
    void shouldIncludePixKeyInfoInAuthResponse() {
        // Arrange
        BankDetails bankDetails = new BankDetails();
        bankDetails.setPixKey("joao@example.com");
        bankDetails.setPixKeyType(PixKeyType.EMAIL);
        bankDetails.setIsVerified(true);
        testMosque.setBankDetails(bankDetails);
        testUser.setOrganization(testMosque);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(null);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(tokenConfig.generateToken(any(User.class))).thenReturn("jwt.token.here");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.getUser().getHasPixKey());
    }

    @Test
    @DisplayName("Deve indicar ausência de PIX key quando usuário não tem organização")
    void shouldIndicateNoPixKeyWhenUserHasNoOrganization() {
        // Arrange
        testUser.setOrganization(null);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(null);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(tokenConfig.generateToken(any(User.class))).thenReturn("jwt.token.here");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertFalse(response.getUser().getHasPixKey());
    }
}
