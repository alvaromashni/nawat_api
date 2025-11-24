package br.com.smartmesquitaapi.service.pix;

import br.com.smartmesquitaapi.api.exception.auth.UserInactiveException;
import br.com.smartmesquitaapi.domain.pix.PixCharge;
import br.com.smartmesquitaapi.domain.pix.PixChargeRepository;
import br.com.smartmesquitaapi.domain.pix.PixChargeStatus;
import br.com.smartmesquitaapi.domain.user.BankDetails;
import br.com.smartmesquitaapi.domain.user.PixKeyType;
import br.com.smartmesquitaapi.domain.user.User;
import br.com.smartmesquitaapi.domain.user.UserRepository;
import br.com.smartmesquitaapi.service.pix.dto.CreatePixChargeRequest;
import br.com.smartmesquitaapi.service.pix.dto.CreatePixChargeResponse;
import br.com.smartmesquitaapi.service.pix.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PixChargeServiceTest {

    @Mock
    private PixChargeRepository pixChargeRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PixChargeService pixChargeService;

    private UUID userId;
    private User user;
    private CreatePixChargeRequest request;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        // Mock user com chave PIX válida e NOME definido
        BankDetails bankDetails = new BankDetails();
        bankDetails.setPixKey("test@example.com");
        bankDetails.setPixKeyType(PixKeyType.EMAIL);
        bankDetails.setIsVerified(true);
        bankDetails.setAccountHolder("Test User");

        user = new User();
        user.setUserId(userId);
        user.setName("Test Mesquita"); // IMPORTANTE: nome é obrigatório
        user.setEmail("test@test.com");
        user.setEnabled(true);
        user.setBankDetails(bankDetails);

        // Mock request válido
        request = new CreatePixChargeRequest();
        request.setAmountCents(5000);
        request.setIdempotencyKey(UUID.randomUUID().toString());
        request.setExpiresMinutes(10);
    }

    @Test
    void shouldCreatePixChargeSuccessfully() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(pixChargeRepository.findByUserIdAndIdempotencyKey(any(), any()))
                .thenReturn(Optional.empty());
        when(pixChargeRepository.existsByTxid(any())).thenReturn(false);
        when(pixChargeRepository.countByUserAndStatus(any(), any())).thenReturn(0L);
        when(pixChargeRepository.save(any(PixCharge.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        CreatePixChargeResponse response = pixChargeService.createPixCharge(
                userId, request, "127.0.0.1"
        );

        // Assert
        assertNotNull(response);
        assertNotNull(response.getTxid());
        assertNotNull(response.getQrPayload());
        assertNotNull(response.getQrImageBase64());
        assertNotNull(response.getExpiresAt());
        assertEquals(5000, response.getAmountCents());

        verify(pixChargeRepository, times(1)).save(any(PixCharge.class));
    }

    @Test
    void shouldReturnExistingChargeWhenIdempotencyKeyExists() {
        // Arrange
        PixCharge existingCharge = new PixCharge();
        existingCharge.setPixChargeId(UUID.randomUUID());
        existingCharge.setUser(user);
        existingCharge.setTxid("EXISTING123");
        existingCharge.setAmountCents(5000);
        existingCharge.setQrPayload("00020126...");
        existingCharge.setQrImageBase64("base64data");
        existingCharge.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        existingCharge.setStatus(PixChargeStatus.PENDING);

        when(pixChargeRepository.findByUserIdAndIdempotencyKey(userId, request.getIdempotencyKey()))
                .thenReturn(Optional.of(existingCharge));

        // Act
        CreatePixChargeResponse response = pixChargeService.createPixCharge(
                userId, request, "127.0.0.1"
        );

        // Assert
        assertNotNull(response);
        assertEquals("EXISTING123", response.getTxid());
        verify(pixChargeRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> {
            pixChargeService.createPixCharge(userId, request, "127.0.0.1");
        });
    }

    @Test
    void shouldThrowExceptionWhenUserInactive() {
        // Arrange
        user.setEnabled(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(pixChargeRepository.findByUserIdAndIdempotencyKey(any(), any()))
                .thenReturn(Optional.empty()); // Adicionar este mock

        // Act & Assert
        UserInactiveException exception = assertThrows(UserInactiveException.class, () -> {
            pixChargeService.createPixCharge(userId, request, "127.0.0.1");
        });

        assertEquals("Usuário inativo", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenPixKeyNotVerified() {
        // Arrange
        user.getBankDetails().setIsVerified(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(pixChargeRepository.findByUserIdAndIdempotencyKey(any(), any()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(PixKeyNotVerifiedException.class, () -> {
            pixChargeService.createPixCharge(userId, request, "127.0.0.1");
        });
    }

    @Test
    void shouldThrowExceptionWhenAmountTooLow() {
        // Arrange
        request.setAmountCents(50); // Menos que o mínimo (100)

        // Act & Assert
        InvalidAmountException exception = assertThrows(InvalidAmountException.class, () -> {
            pixChargeService.createPixCharge(userId, request, "127.0.0.1");
        });

        assertTrue(exception.getMessage().contains("Valor mínimo"));
    }

    @Test
    void shouldThrowExceptionWhenAmountTooHigh() {
        // Arrange
        request.setAmountCents(2000000); // Mais que o máximo (1000000)

        // Act & Assert
        InvalidAmountException exception = assertThrows(InvalidAmountException.class, () -> {
            pixChargeService.createPixCharge(userId, request, "127.0.0.1");
        });

        assertTrue(exception.getMessage().contains("Valor máximo"));
    }

    @Test
    void shouldThrowExceptionWhenRateLimitExceeded() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(pixChargeRepository.findByUserIdAndIdempotencyKey(any(), any()))
                .thenReturn(Optional.empty());
        when(pixChargeRepository.countByUserAndStatus(userId, PixChargeStatus.PENDING))
                .thenReturn(150L); // Acima do limite

        // Act & Assert
        RateLimitExceededPixException exception = assertThrows(RateLimitExceededPixException.class, () -> {
            pixChargeService.createPixCharge(userId, request, "127.0.0.1");
        });

        assertTrue(exception.getMessage().contains("Limite"));
    }

    @Test
    void shouldThrowExceptionWhenIdempotencyKeyIsBlank() {
        // Arrange
        request.setIdempotencyKey("");

        // Act & Assert
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            pixChargeService.createPixCharge(userId, request, "127.0.0.1");
        });

        assertEquals("IdempotencyKey é obrigatória", exception.getMessage());
    }
}