package br.com.smartmesquitaapi.pix;

import br.com.smartmesquitaapi.organization.domain.Organization;
import br.com.smartmesquitaapi.api.exception.auth.UserInactiveException;
import br.com.smartmesquitaapi.pix.domain.PixCharge;
import br.com.smartmesquitaapi.pix.domain.PixChargeStatus;
import br.com.smartmesquitaapi.user.domain.BankDetails;
import br.com.smartmesquitaapi.user.domain.User;
import br.com.smartmesquitaapi.user.UserRepository;
import br.com.smartmesquitaapi.pix.infrastructure.EmvPayloadGenerator;
import br.com.smartmesquitaapi.pix.infrastructure.PixKeyValidator;
import br.com.smartmesquitaapi.pix.infrastructure.QrcodeImageGenerator;
import br.com.smartmesquitaapi.pix.dto.CreatePixChargeRequest;
import br.com.smartmesquitaapi.pix.dto.CreatePixChargeResponse;
import br.com.smartmesquitaapi.pix.dto.PixChargeDto;
import br.com.smartmesquitaapi.pix.exception.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class PixChargeService {

    private final PixChargeRepository pixChargeRepository;
    private final UserRepository userRepository;

    // Configurações de validação
    private static final int MIN_AMOUNT_CENTS = 100;
    private static final int MAX_AMOUNT_CENTS = 1000000;
    private static final int DEFAULT_EXPIRATION_MINUTES = 10;
    private static final int MAX_EXPIRATION_MINUTES = 60;
    private static final int MAX_CHARGES_PER_HOUR = 300;


    @Transactional
    public CreatePixChargeResponse createPixCharge(
            Organization organization,
            UUID userId,
            CreatePixChargeRequest request,
            String clientIp
    ) {

        validateOrganization(organization);

        log.info("Iniciando cobrança PIX - Org: {} | User: {} | Amount: {}",
                organization.getId(),
                userId != null ? userId : "TOTEM",
                request.getAmountCents());

        validateRequest(request);

        Optional<PixCharge> existingCharge = pixChargeRepository
                .findByOrganizationIdAndIdempotencyKey(organization.getId(), request.getIdempotencyKey());

        if (existingCharge.isPresent()) {
            return buildResponse(existingCharge.get());
        }

        validateRateLimit(organization.getId());

        String txid = generateTxid(request.getIdempotencyKey());

        if (pixChargeRepository.existsByTxid(txid)){
            txid = generateTxid(UUID.randomUUID().toString());
        }

        BankDetails bankDetails = organization.getBankDetails();
        String emvPayload = generateEmvPayload(
                bankDetails,
                organization.getOrgName(),
                txid,
                request.getAmountCents()
        );

        String qrImageBase64 = QrcodeImageGenerator.generateForMobile(emvPayload);

        LocalDateTime expiresAt = LocalDateTime.now()
                .plusMinutes(request.getExpiresMinutes() != null
                        ? request.getExpiresMinutes()
                        : DEFAULT_EXPIRATION_MINUTES);

        var chargeBuilder = PixCharge.builder()
                .organization(organization)
                .localDonationId(request.getLocalDonationId())
                .idempotencyKey(request.getIdempotencyKey())
                .txid(txid)
                .amountCents(request.getAmountCents())
                .qrPayload(emvPayload)
                .qrImageBase64(qrImageBase64)
                .status(PixChargeStatus.PENDING)
                .expiresAt(expiresAt)
                .pspName("static-key");

        if (userId != null) {
            chargeBuilder.user(userRepository.getReferenceById(userId));
        }

        PixCharge pixCharge = chargeBuilder.build();
        pixCharge = pixChargeRepository.save(pixCharge);

        log.info("Cobrança PIX criada - TxID: {} | Origem: {}", txid, userId != null ? "APP" : "TOTEM");

        return buildResponse(pixCharge);
    }

    public PixChargeDto getChargeByLocalId(String localDonationId) {
        PixCharge charge = pixChargeRepository.findByLocalDonationId(localDonationId)
                .orElseThrow(() -> new ChargeNotFoundException("Cobrança não encontrada: " + localDonationId));

        return mapToDto(charge);
    }

    public PixChargeDto getChargeByTxid(String txid) {
        PixCharge charge = pixChargeRepository.findByTxid(txid)
                .orElseThrow(() -> new ChargeNotFoundException("Cobrança não encontrada: " + txid));

        return mapToDto(charge);
    }

    /**
     * Confirma manualmente uma cobrança (via comprovante)
     */
    @Transactional
    public PixChargeDto confirmManually(
            String localDonationId,
            UUID confirmedByUserId,
            String receiptUrl,
            String notes
    ) {
        PixCharge charge = pixChargeRepository.findByLocalDonationId(localDonationId)
                .orElseThrow(() -> new ChargeNotFoundException("Cobrança não encontrada"));

        if (charge.getStatus().isFinal()) {
            throw new ChargeAlreadyProcessedException("Cobrança já foi processada");
        }

        charge.confirmManually(confirmedByUserId, receiptUrl, notes);
        charge = pixChargeRepository.save(charge);

        return mapToDto(charge);
    }


    @Transactional
    public int expireOldCharges() {
        var expiredCharges = pixChargeRepository.findExpiredPendingCharges(LocalDateTime.now());

        expiredCharges.forEach(charge -> {
            charge.markAsExpired();
            pixChargeRepository.save(charge);
        });

        return expiredCharges.size();
    }

    // ========== MÉTODOS PRIVADOS DE VALIDAÇÃO ==========

    private void validateRequest(CreatePixChargeRequest request) {
        if (request.getAmountCents() == null || request.getAmountCents() < MIN_AMOUNT_CENTS) {
            throw new InvalidAmountException(String.format("Valor mínimo: R$ %.2f", MIN_AMOUNT_CENTS / 100.0));
        }
        if (request.getAmountCents() > MAX_AMOUNT_CENTS) {
            throw new InvalidAmountException(String.format("Valor máximo: R$ %.2f", MAX_AMOUNT_CENTS / 100.0));
        }
        if (request.getIdempotencyKey() == null || request.getIdempotencyKey().isBlank()) {
            throw new InvalidRequestException("IdempotencyKey é obrigatória");
        }
    }

    private void validateOrganization(Organization org) {
        BankDetails bankDetails = org.getBankDetails();

        if (bankDetails == null || bankDetails.getPixKey() == null || bankDetails.getPixKey().isBlank()) {
            throw new PixKeyNotFoundException("Organização não possui chave PIX cadastrada");
        }

        if (!Boolean.TRUE.equals(bankDetails.getIsVerified())) {
             throw new PixKeyNotVerifiedException("Chave PIX da organização não verificada.");
        }

        if (!PixKeyValidator.isValid(bankDetails.getPixKey())) {
            throw new InvalidPixKeyException("Formato de chave PIX da organização inválido");
        }
    }

    private void validateRateLimit(UUID organizationId) {

        Long recentCharges = pixChargeRepository.countByOrganizationIdAndStatus(organizationId, PixChargeStatus.PENDING);

        if (recentCharges != null && recentCharges >= MAX_CHARGES_PER_HOUR) {
            throw new RateLimitExceededPixException("Limite de cobranças por hora excedido para esta organização");
        }
    }

    // ========== MÉTODOS PRIVADOS DE GERAÇÃO ==========

    private String generateTxid(String idempotencyKey) {

        String txid = idempotencyKey.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();

        if (txid.length() > 25) {
            txid = txid.substring(0, 25);
        } else if (txid.length() < 10) {
            txid = txid + System.currentTimeMillis();
            if (txid.length() > 25) {
                txid = txid.substring(0, 25);
            }
        }

        return txid;
    }

    private String generateEmvPayload(
            BankDetails bankDetails,
            String merchantName,
            String txid,
            Integer amountCents
    ) {
        try {
            // Cidade padrão (pode vir de configuração ou do cadastro do usuário)
            String merchantCity = "SAO PAULO"; // TODO: pegar do cadastro do usuário

            return EmvPayloadGenerator.generate(
                    bankDetails.getPixKey(),
                    merchantName,
                    merchantCity,
                    txid,
                    amountCents
            );
        } catch (Exception e) {
            throw new QrCodeGenerationException("Erro ao gerar QR Code: " + e.getMessage(), new Throwable());
        }
    }

    // ========== MÉTODOS DE MAPEAMENTO ==========

    private CreatePixChargeResponse buildResponse(PixCharge charge) {
        return CreatePixChargeResponse.builder()
                .txid(charge.getTxid())
                .qrPayload(charge.getQrPayload())
                .qrImageBase64(charge.getQrImageBase64())
                .expiresAt(charge.getExpiresAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli())
                .amountCents(charge.getAmountCents())
                .build();
    }

    private PixChargeDto mapToDto(PixCharge charge) {

        String userName = (charge.getUser() != null) ? charge.getUser().getName() : "Totem/Anonimo";

        return PixChargeDto.builder()
                .id(charge.getPixChargeId())
                .localDonationId(charge.getLocalDonationId())
                .txid(charge.getTxid())
                .amountCents(charge.getAmountCents())
                .status(charge.getStatus())
                .qrPayload(charge.getQrPayload())
                .qrImageBase64(charge.getQrImageBase64())
                .expiresAt(charge.getExpiresAt())
                .createdAt(charge.getCreatedAt())
                .userName(userName) // Usamos a string segura
                .receiptImageUrl(charge.getReceiptImageUrl())
                .build();
    }
}
