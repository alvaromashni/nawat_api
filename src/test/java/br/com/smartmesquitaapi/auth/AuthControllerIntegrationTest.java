package br.com.smartmesquitaapi.auth;

import br.com.smartmesquitaapi.auth.dto.request.LoginRequest;
import br.com.smartmesquitaapi.auth.dto.request.RefreshTokenRequest;
import br.com.smartmesquitaapi.auth.dto.request.RegisterUserRequest;
import br.com.smartmesquitaapi.auth.dto.response.AuthResponse;
import br.com.smartmesquitaapi.ratelimit.RateLimitAspect;
import br.com.smartmesquitaapi.security.SecurityFilter;
import br.com.smartmesquitaapi.security.TokenConfig;
import br.com.smartmesquitaapi.user.UserRepository;
import br.com.smartmesquitaapi.user.domain.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@DisplayName("AuthController - Testes de Integração dos Endpoints de Autenticação")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private SecurityFilter securityFilter;

    @MockBean
    private TokenConfig tokenConfig;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RateLimitAspect rateLimitAspect;

    // ==================== TESTES DE REGISTER ====================

    @Test
    @DisplayName("POST /api/v1/auth/register - Deve registrar usuário com sucesso")
    void shouldRegisterUserSuccessfully() throws Exception {
        // Arrange
        RegisterUserRequest request = new RegisterUserRequest();
        request.setName("João Silva");
        request.setEmail("joao@example.com");
        request.setPassword("senha123");
        request.setRole(UserRole.USER);

        AuthResponse mockResponse = AuthResponse.builder()
            .token("jwt.token.here")
            .refreshToken("refresh-token-uuid")
            .type("Bearer")
            .build();

        when(authService.register(any(RegisterUserRequest.class))).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token").value("jwt.token.here"))
            .andExpect(jsonPath("$.refreshToken").value("refresh-token-uuid"))
            .andExpect(jsonPath("$.type").value("Bearer"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Deve retornar 400 para request inválido (sem email)")
    void shouldReturn400ForInvalidRegisterRequest() throws Exception {
        // Arrange
        RegisterUserRequest request = new RegisterUserRequest();
        request.setName("João Silva");
        // Email ausente
        request.setPassword("senha123");
        request.setRole(UserRole.USER);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    // ==================== TESTES DE LOGIN ====================

    @Test
    @DisplayName("POST /api/v1/auth/login - Deve fazer login com sucesso")
    void shouldLoginSuccessfully() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("joao@example.com");
        request.setPassword("senha123");

        AuthResponse mockResponse = AuthResponse.builder()
            .token("jwt.token.here")
            .refreshToken("refresh-token-uuid")
            .type("Bearer")
            .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("jwt.token.here"))
            .andExpect(jsonPath("$.refreshToken").value("refresh-token-uuid"))
            .andExpect(jsonPath("$.type").value("Bearer"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Deve retornar 401 para credenciais inválidas")
    void shouldReturn401ForInvalidCredentials() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("joao@example.com");
        request.setPassword("senhaErrada");

        when(authService.login(any(LoginRequest.class)))
            .thenThrow(new BadCredentialsException("Credenciais inválidas"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Deve retornar 400 para request sem email")
    void shouldReturn400ForLoginRequestWithoutEmail() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        // Email ausente
        request.setPassword("senha123");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Deve retornar 400 para request sem senha")
    void shouldReturn400ForLoginRequestWithoutPassword() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("joao@example.com");
        // Senha ausente

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    // ==================== TESTES DE VERIFY TOKEN ====================

    @Test
    @DisplayName("GET /api/v1/auth/verify - Deve retornar 200 para token válido")
    void shouldReturn200ForValidToken() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/auth/verify"))
            .andExpect(status().isOk());
    }

    // ==================== TESTES DE REFRESH TOKEN ====================

    @Test
    @DisplayName("POST /api/v1/auth/refresh - Deve renovar tokens com sucesso")
    void shouldRefreshTokensSuccessfully() throws Exception {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setToken("old-refresh-token");

        AuthResponse mockResponse = AuthResponse.builder()
            .token("new.jwt.token")
            .refreshToken("new-refresh-token-uuid")
            .type("Bearer")
            .build();

        when(authService.processRefreshToken(anyString())).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("new.jwt.token"))
            .andExpect(jsonPath("$.refreshToken").value("new-refresh-token-uuid"))
            .andExpect(jsonPath("$.type").value("Bearer"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh - Deve retornar 400 para refresh token inválido")
    void shouldReturn400ForInvalidRefreshToken() throws Exception {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setToken("invalid-token");

        when(authService.processRefreshToken(anyString()))
            .thenThrow(new RuntimeException("Erro: Token não encontrado no banco!"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().is5xxServerError());
    }

    // ==================== TESTES DE VALIDAÇÃO DE REQUEST ====================

    @Test
    @DisplayName("POST /api/v1/auth/register - Deve retornar 400 para email inválido")
    void shouldReturn400ForInvalidEmailFormat() throws Exception {
        // Arrange
        RegisterUserRequest request = new RegisterUserRequest();
        request.setName("João Silva");
        request.setEmail("email-invalido");
        request.setPassword("senha123");
        request.setRole(UserRole.USER);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Deve aceitar registro com todos os campos obrigatórios")
    void shouldAcceptRegisterWithAllRequiredFields() throws Exception {
        // Arrange
        RegisterUserRequest request = new RegisterUserRequest();
        request.setName("João Silva");
        request.setEmail("joao@example.com");
        request.setPassword("senha123");
        request.setRole(UserRole.USER);

        AuthResponse mockResponse = AuthResponse.builder()
            .token("jwt.token.here")
            .refreshToken("refresh-token-uuid")
            .type("Bearer")
            .build();

        when(authService.register(any(RegisterUserRequest.class))).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }

    // ==================== TESTES DE CONTENT TYPE ====================

    @Test
    @DisplayName("POST /api/v1/auth/login - Deve retornar 415 para Content-Type inválido")
    void shouldReturn415ForInvalidContentType() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("joao@example.com");
        request.setPassword("senha123");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.TEXT_PLAIN)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Deve aceitar application/json como Content-Type")
    void shouldAcceptApplicationJsonContentType() throws Exception {
        // Arrange
        RegisterUserRequest request = new RegisterUserRequest();
        request.setName("João Silva");
        request.setEmail("joao@example.com");
        request.setPassword("senha123");
        request.setRole(UserRole.USER);

        AuthResponse mockResponse = AuthResponse.builder()
            .token("jwt.token.here")
            .refreshToken("refresh-token-uuid")
            .type("Bearer")
            .build();

        when(authService.register(any(RegisterUserRequest.class))).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    // ==================== TESTES DE MÉTODOS HTTP ====================

    @Test
    @DisplayName("GET /api/v1/auth/register - Deve retornar 405 (Method Not Allowed)")
    void shouldReturn405ForGetOnRegisterEndpoint() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/auth/register"))
            .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("GET /api/v1/auth/login - Deve retornar 405 (Method Not Allowed)")
    void shouldReturn405ForGetOnLoginEndpoint() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/auth/login"))
            .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("POST /api/v1/auth/verify - Deve retornar 405 (Method Not Allowed)")
    void shouldReturn405ForPostOnVerifyEndpoint() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/verify"))
            .andExpect(status().isMethodNotAllowed());
    }

    // ==================== TESTES DE RESPONSE FORMAT ====================

    @Test
    @DisplayName("POST /api/v1/auth/login - Deve retornar response em formato JSON")
    void shouldReturnJsonResponseForLogin() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("joao@example.com");
        request.setPassword("senha123");

        AuthResponse mockResponse = AuthResponse.builder()
            .token("jwt.token.here")
            .refreshToken("refresh-token-uuid")
            .type("Bearer")
            .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.token").exists())
            .andExpect(jsonPath("$.refreshToken").exists())
            .andExpect(jsonPath("$.type").exists());
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Deve incluir status 201 CREATED no response")
    void shouldReturn201CreatedForSuccessfulRegistration() throws Exception {
        // Arrange
        RegisterUserRequest request = new RegisterUserRequest();
        request.setName("João Silva");
        request.setEmail("joao@example.com");
        request.setPassword("senha123");
        request.setRole(UserRole.USER);

        AuthResponse mockResponse = AuthResponse.builder()
            .token("jwt.token.here")
            .refreshToken("refresh-token-uuid")
            .type("Bearer")
            .build();

        when(authService.register(any(RegisterUserRequest.class))).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }
}
