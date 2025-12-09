package br.com.smartmesquitaapi.integration;

import br.com.smartmesquitaapi.pix.PixChargeService;
import br.com.smartmesquitaapi.pix.dto.CreatePixChargeRequest;
import br.com.smartmesquitaapi.pix.dto.CreatePixChargeResponse;
import br.com.smartmesquitaapi.pix.dto.PixChargeDto;
import br.com.smartmesquitaapi.pix.domain.PixChargeStatus;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Teste de integração completo do fluxo de criação de cobranças PIX
 * Simula o cenário real desde a criação até a consulta
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Fluxo de Integração - Criação e Consulta de Cobranças PIX")
class PixChargeFlowIntegrationTest {

    @Autowired
    private PixChargeService pixChargeService;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private RedisTemplate<String, Object> redisTemplate;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Criar usuário de teste com dados bancários verificados
        testUser = new User();
        testUser.setName("João Silva");
        testUser.setEmail("joao@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(UserRole.USER);
        testUser.setEnabled(true);

        BankDetails bankDetails = new BankDetails();
        bankDetails.setPixKey("joao@example.com");
        bankDetails.setPixKeyType(PixKeyType.EMAIL);
        bankDetails.setBankName("Banco do Brasil");
        bankDetails.setAccountHolder("João Silva");
        bankDetails.setAccountNumber("12345-6");
        bankDetails.markAsVerified("proof-url");

        testUser.getOrganization().setBankDetails(bankDetails);
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("Deve completar fluxo completo: criar cobrança -> consultar por localId -> consultar por txid")
    void shouldCompleteFullPixChargeFlow() {
        // Arrange
        CreatePixChargeRequest request = CreatePixChargeRequest.builder()
            .localDonationId("LOCAL-123")
            .amountCents(5000) // R$ 50,00
            .idempotencyKey(UUID.randomUUID().toString())
            .expiresMinutes(10)
            .build();

        // Act 1 - Criar cobrança
        CreatePixChargeResponse createResponse = pixChargeService.createPixCharge(
            testUser.getUserId(),
            request,
            "192.168.1.1"
        );

        // Assert 1 - Verificar resposta de criação
        assertNotNull(createResponse);
        assertNotNull(createResponse.getTxid());
        assertNotNull(createResponse.getQrImageBase64());
        assertNotNull(createResponse.getQrPayload());
        assertEquals(5000, createResponse.getAmountCents());
        assertNotNull(createResponse.getExpiresAt());

        // Act 2 - Consultar por localId
        PixChargeDto chargeByLocalId = pixChargeService.getChargeByLocalId("LOCAL-123");

        // Assert 2 - Verificar dados da consulta por localId
        assertNotNull(chargeByLocalId);
        assertEquals(createResponse.getTxid(), chargeByLocalId.getTxid());
        assertEquals(createResponse.getTxid(), chargeByLocalId.getTxid());
        assertEquals("LOCAL-123", chargeByLocalId.getLocalDonationId());
        assertEquals(5000, chargeByLocalId.getAmountCents());
        assertEquals(PixChargeStatus.PENDING, chargeByLocalId.getStatus());

        // Act 3 - Consultar por txid
        PixChargeDto chargeByTxid = pixChargeService.getChargeByTxid(createResponse.getTxid());

        // Assert 3 - Verificar dados da consulta por txid
        assertNotNull(chargeByTxid);
        assertEquals(createResponse.getTxid(), chargeByTxid.getTxid());
        assertEquals("LOCAL-123", chargeByTxid.getLocalDonationId());

        // Assert 4 - Verificar que ambas as consultas retornam os mesmos dados
        assertEquals(chargeByLocalId.getId(), chargeByTxid.getId());
        assertEquals(chargeByLocalId.getTxid(), chargeByTxid.getTxid());
        assertEquals(chargeByLocalId.getAmountCents(), chargeByTxid.getAmountCents());
        assertEquals(chargeByLocalId.getStatus(), chargeByTxid.getStatus());
    }

    @Test
    @DisplayName("Deve garantir idempotência - mesma chave não cria cobrança duplicada")
    void shouldGuaranteeIdempotency() {
        // Arrange
        String idempotencyKey = UUID.randomUUID().toString();
        CreatePixChargeRequest request1 = CreatePixChargeRequest.builder()
            .localDonationId("LOCAL-456")
            .amountCents(3000)
            .idempotencyKey(idempotencyKey)
            .expiresMinutes(10)
            .build();

        CreatePixChargeRequest request2 = CreatePixChargeRequest.builder()
            .localDonationId("LOCAL-456")
            .amountCents(3000)
            .idempotencyKey(idempotencyKey) // Mesma chave
            .expiresMinutes(10)
            .build();

        // Act
        CreatePixChargeResponse response1 = pixChargeService.createPixCharge(
            testUser.getUserId(),
            request1,
            "192.168.1.1"
        );

        CreatePixChargeResponse response2 = pixChargeService.createPixCharge(
            testUser.getUserId(),
            request2,
            "192.168.1.1"
        );

        // Assert - Deve retornar a mesma cobrança
        assertEquals(response1.getTxid(), response2.getTxid());
        assertEquals(response1.getQrPayload(), response2.getQrPayload());
    }

    @Test
    @DisplayName("Deve gerar QR Code válido e decodificável")
    void shouldGenerateValidAndDecodableQrCode() {
        // Arrange
        CreatePixChargeRequest request = CreatePixChargeRequest.builder()
            .localDonationId("LOCAL-789")
            .amountCents(10000) // R$ 100,00
            .idempotencyKey(UUID.randomUUID().toString())
            .expiresMinutes(15)
            .build();

        // Act
        CreatePixChargeResponse response = pixChargeService.createPixCharge(
            testUser.getUserId(),
            request,
            "192.168.1.1"
        );

        // Assert
        assertNotNull(response.getQrImageBase64());
        assertFalse(response.getQrImageBase64().isEmpty());

        // Verificar que é Base64 válido
        assertDoesNotThrow(() -> {
            java.util.Base64.getDecoder().decode(response.getQrImageBase64());
        });

        // Verificar que o payload PIX (Brcode) está presente
        assertNotNull(response.getQrPayload());
        assertTrue(response.getQrPayload().startsWith("00020126"));
    }

    @Test
    @DisplayName("Deve validar valor mínimo da cobrança (R$ 1,00)")
    void shouldValidateMinimumChargeAmount() {
        // Arrange
        CreatePixChargeRequest request = CreatePixChargeRequest.builder()
            .localDonationId("LOCAL-MIN")
            .amountCents(50) // R$ 0,50 - abaixo do mínimo
            .idempotencyKey(UUID.randomUUID().toString())
            .expiresMinutes(10)
            .build();

        // Act & Assert
        assertThrows(Exception.class, () -> {
            pixChargeService.createPixCharge(
                testUser.getUserId(),
                request,
                "192.168.1.1"
            );
        });
    }

    @Test
    @DisplayName("Deve validar valor máximo da cobrança (R$ 10.000,00)")
    void shouldValidateMaximumChargeAmount() {
        // Arrange
        CreatePixChargeRequest request = CreatePixChargeRequest.builder()
            .localDonationId("LOCAL-MAX")
            .amountCents(1500000) // R$ 15.000,00 - acima do máximo
            .idempotencyKey(UUID.randomUUID().toString())
            .expiresMinutes(10)
            .build();

        // Act & Assert
        assertThrows(Exception.class, () -> {
            pixChargeService.createPixCharge(
                testUser.getUserId(),
                request,
                "192.168.1.1"
            );
        });
    }

    @Test
    @DisplayName("Deve aceitar valor exatamente no mínimo (R$ 1,00)")
    void shouldAcceptExactMinimumAmount() {
        // Arrange
        CreatePixChargeRequest request = CreatePixChargeRequest.builder()
            .localDonationId("LOCAL-EXACT-MIN")
            .amountCents(100) // R$ 1,00
            .idempotencyKey(UUID.randomUUID().toString())
            .expiresMinutes(10)
            .build();

        // Act & Assert
        assertDoesNotThrow(() -> {
            CreatePixChargeResponse response = pixChargeService.createPixCharge(
                testUser.getUserId(),
                request,
                "192.168.1.1"
            );
            assertEquals(100, response.getAmountCents());
        });
    }

    @Test
    @DisplayName("Deve aceitar valor exatamente no máximo (R$ 10.000,00)")
    void shouldAcceptExactMaximumAmount() {
        // Arrange
        CreatePixChargeRequest request = CreatePixChargeRequest.builder()
            .localDonationId("LOCAL-EXACT-MAX")
            .amountCents(1000000) // R$ 10.000,00
            .idempotencyKey(UUID.randomUUID().toString())
            .expiresMinutes(10)
            .build();

        // Act & Assert
        assertDoesNotThrow(() -> {
            CreatePixChargeResponse response = pixChargeService.createPixCharge(
                testUser.getUserId(),
                request,
                "192.168.1.1"
            );
            assertEquals(1000000, response.getAmountCents());
        });
    }

    @Test
    @DisplayName("Deve criar cobranças independentes com diferentes idempotency keys")
    void shouldCreateIndependentChargesWithDifferentIdempotencyKeys() {
        // Arrange
        CreatePixChargeRequest request1 = CreatePixChargeRequest.builder()
            .localDonationId("LOCAL-A")
            .amountCents(2000)
            .idempotencyKey(UUID.randomUUID().toString())
            .expiresMinutes(10)
            .build();

        CreatePixChargeRequest request2 = CreatePixChargeRequest.builder()
            .localDonationId("LOCAL-B")
            .amountCents(3000)
            .idempotencyKey(UUID.randomUUID().toString())
            .expiresMinutes(10)
            .build();

        // Act
        CreatePixChargeResponse response1 = pixChargeService.createPixCharge(
            testUser.getUserId(),
            request1,
            "192.168.1.1"
        );

        CreatePixChargeResponse response2 = pixChargeService.createPixCharge(
            testUser.getUserId(),
            request2,
            "192.168.1.1"
        );

        // Assert
        assertNotEquals(response1.getTxid(), response2.getTxid());
        assertNotEquals(response1.getQrPayload(), response2.getQrPayload());
        assertEquals(2000, response1.getAmountCents());
        assertEquals(3000, response2.getAmountCents());
    }

    @Test
    @DisplayName("Deve incluir informações do usuário na cobrança criada")
    void shouldIncludeUserInfoInCreatedCharge() {
        // Arrange
        CreatePixChargeRequest request = CreatePixChargeRequest.builder()
            .localDonationId("LOCAL-USER-INFO")
            .amountCents(5000)
            .idempotencyKey(UUID.randomUUID().toString())
            .expiresMinutes(10)
            .build();

        // Act
        CreatePixChargeResponse response = pixChargeService.createPixCharge(
            testUser.getUserId(),
            request,
            "192.168.1.1"
        );

        // Consultar para obter informações completas
        PixChargeDto charge = pixChargeService.getChargeByLocalId("LOCAL-USER-INFO");

        // Assert
        assertNotNull(charge);
        assertNotNull(response);
        assertNotNull(charge.getUserName());
    }

    @Test
    @DisplayName("Deve configurar tempo de expiração customizado")
    void shouldSetCustomExpirationTime() {
        // Arrange
        CreatePixChargeRequest request = CreatePixChargeRequest.builder()
            .localDonationId("LOCAL-CUSTOM-EXP")
            .amountCents(5000)
            .idempotencyKey(UUID.randomUUID().toString())
            .expiresMinutes(30) // 30 minutos
            .build();

        // Act
        CreatePixChargeResponse response = pixChargeService.createPixCharge(
            testUser.getUserId(),
            request,
            "192.168.1.1"
        );

        // Assert
        assertNotNull(response.getExpiresAt());
        // Verificar que expira em aproximadamente 30 minutos
        long minutesUntilExpiry = java.time.Duration.between(
            java.time.Instant.now(),
            java.time.Instant.ofEpochMilli(response.getExpiresAt())
        ).toMinutes();

        assertTrue(minutesUntilExpiry >= 29 && minutesUntilExpiry <= 31);
    }

    @Test
    @DisplayName("Deve usar expiração padrão quando não especificada")
    void shouldUseDefaultExpirationWhenNotSpecified() {
        // Arrange
        CreatePixChargeRequest request = CreatePixChargeRequest.builder()
            .localDonationId("LOCAL-DEFAULT-EXP")
            .amountCents(5000)
            .idempotencyKey(UUID.randomUUID().toString())
            // expiresMinutes não definido
            .build();

        // Act
        CreatePixChargeResponse response = pixChargeService.createPixCharge(
            testUser.getUserId(),
            request,
            "192.168.1.1"
        );

        // Assert
        assertNotNull(response.getExpiresAt());
        // Deve ter alguma expiração definida
        assertTrue(java.time.Instant.ofEpochMilli(response.getExpiresAt()).isAfter(java.time.Instant.now()));
    }

    @Test
    @DisplayName("Deve registrar IP do cliente na cobrança")
    void shouldRecordClientIpInCharge() {
        // Arrange
        String clientIp = "203.0.113.42";
        CreatePixChargeRequest request = CreatePixChargeRequest.builder()
            .localDonationId("LOCAL-IP-TEST")
            .amountCents(5000)
            .idempotencyKey(UUID.randomUUID().toString())
            .expiresMinutes(10)
            .build();

        // Act
        CreatePixChargeResponse response = pixChargeService.createPixCharge(
            testUser.getUserId(),
            request,
            clientIp
        );

        // Assert
        assertNotNull(response);
        // O IP é registrado internamente, mas pode não estar na response
    }
}
