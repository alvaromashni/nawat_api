package br.com.smartmesquitaapi.auth;

import br.com.smartmesquitaapi.api.exception.auth.EmailAlreadyExistsException;
import br.com.smartmesquitaapi.api.exception.auth.InvalidCredentialsException;
import br.com.smartmesquitaapi.api.exception.auth.UserInactiveException;
import br.com.smartmesquitaapi.auth.dto.request.UserInfo;
import br.com.smartmesquitaapi.pix.exception.UserNotFoundException;
import br.com.smartmesquitaapi.user.domain.BankDetails;
import br.com.smartmesquitaapi.user.domain.User;
import br.com.smartmesquitaapi.user.UserRepository;
import br.com.smartmesquitaapi.security.TokenConfig;
import br.com.smartmesquitaapi.auth.dto.response.AuthResponse;
import br.com.smartmesquitaapi.auth.dto.request.LoginRequest;
import br.com.smartmesquitaapi.auth.dto.request.RegisterUserRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenConfig tokenConfig;
    private final AuthenticationManager authenticationManager;

    /**
     * Registrar novo usuário
     */
    @Transactional
    public AuthResponse register(RegisterUserRequest request) {
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
        RefreshToken refreshToken = createRefreshToken(user.getEmail());

        return buildAuthResponse(token, refreshToken.getToken(), user);
    }


    public AuthResponse login(LoginRequest request) {

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new InvalidCredentialsException("Credenciais inválidas"));
            if (!user.getIsEnabled()) {
                throw new UserInactiveException("Usuário inativo");
            }
            String token = tokenConfig.generateToken(user);
            RefreshToken refreshToken = createRefreshToken(user.getEmail());

            return buildAuthResponse(token, refreshToken.getToken(), user);
    }

    private AuthResponse buildAuthResponse(String token, String refreshToken, User user) {
        UserInfo userInfo = UserInfo.builder()
                .id(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .isActive(user.getIsEnabled())
                .hasPixKey(user.getBankDetails() != null && user.getBankDetails().getPixKey() != null)
                .build();

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .type("Bearer")
                .user(userInfo)
                .build();
    }

    public RefreshToken createRefreshToken(String email){

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException("Erro: Usuário não encontrado no banco!"));

        RefreshToken refreshToken = new RefreshToken();

        Instant init = Instant.now();
        Duration durationTime = Duration.ofDays(30);

        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(init.plus(durationTime));
        refreshTokenRepository.save(refreshToken);

        return refreshToken;
    }

    public AuthResponse processRefreshToken(String token){

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token).orElseThrow(
                () -> new RuntimeException("Erro: Token não encontrado no banco!")
        );

        verifyExpirationToken(refreshToken);

        User user = refreshToken.getUser();
        RefreshToken newToken = createRefreshToken(user.getEmail());
        String newAccessToken = tokenConfig.generateToken(user);

        refreshTokenRepository.delete(refreshToken);
        return buildAuthResponse(newAccessToken, newToken.getToken(), user);
    }

    public RefreshToken verifyExpirationToken(RefreshToken token){

        if (token.getExpiryDate().isBefore(Instant.now())){
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token expirado, por favor refaça o login.");
        }
        return token;
    }
}
