package br.com.smartmesquitaapi.service;

import br.com.smartmesquitaapi.domain.user.BankDetails;
import br.com.smartmesquitaapi.domain.user.User;
import br.com.smartmesquitaapi.domain.user.UserRepository;
import br.com.smartmesquitaapi.infrastructure.security.TokenConfig;
import br.com.smartmesquitaapi.infrastructure.security.dto.AuthResponse;
import br.com.smartmesquitaapi.infrastructure.security.dto.LoginRequest;
import br.com.smartmesquitaapi.infrastructure.security.dto.RegisterUserRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenConfig tokenConfig;
    private final AuthenticationManager authenticationManager;

    /**
     * Registrar novo usuário
     */
    @Transactional
    public AuthResponse register(RegisterUserRequest request) {
        log.info("Registrando novo usuário: {}", request.getEmail());

        // Validar se email já existe
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email já cadastrado no sistema");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setEnabled(true);

        if (request.getBankDetails() != null) {
            BankDetails bankDetails = new BankDetails();
            bankDetails.setPixKey(request.getBankDetails().getPixKey());
            bankDetails.setPixKeyType(request.getBankDetails().getPixKeyType());
            bankDetails.setBankName(request.getBankDetails().getBankName());
            bankDetails.setAccountHolder(request.getBankDetails().getAccountHolder());
            bankDetails.setCnpj(request.getBankDetails().getCnpj());
            bankDetails.setAccountNumber(request.getBankDetails().getAccountNumber());
            bankDetails.setIsVerified(false);

            user.setBankDetails(bankDetails);
        }

        user = userRepository.save(user);

        String token = tokenConfig.generateToken(user);

        log.info("Usuário registrado com sucesso: {} (ID: {})", user.getEmail(), user.getUserId());

        return buildAuthResponse(token, user);
    }

    /**
     * Login de usuário
     */
    public AuthResponse login(LoginRequest request) {
        log.info("Tentativa de login: {}", request.getEmail());


        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = (User) userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Credenciais inválidas"));

        if (!user.getIsEnabled()) {
            throw new UserInactiveException("Usuário inativo");
        }

        String token = tokenConfig.generateToken(user);

        log.info("Login realizado com sucesso: {}", user.getEmail());

        return buildAuthResponse(token, user);
    }

    /**
     * Constrói o response de autenticação
     */
    private AuthResponse buildAuthResponse(String token, User user) {
        AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                .id(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .isActive(user.getIsEnabled())
                .hasPixKey(user.getBankDetails() != null && user.getBankDetails().getPixKey() != null)
                .build();

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .user(userInfo)
                .build();
    }

    // ========== EXCEPTIONS ==========

    public static class EmailAlreadyExistsException extends RuntimeException {
        public EmailAlreadyExistsException(String message) {
            super(message);
        }
    }

    public static class InvalidCredentialsException extends RuntimeException {
        public InvalidCredentialsException(String message) {
            super(message);
        }
    }

    public static class UserInactiveException extends RuntimeException {
        public UserInactiveException(String message) {
            super(message);
        }
    }
}
