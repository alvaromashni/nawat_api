package br.com.smartmesquitaapi.pix;

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

/**
 * Service responsável por gerenciar cobranças PIX
 */
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
    private static final int MAX_CHARGES_PER_HOUR = 100;

    /**
     * Cria uma cobrança PIX com QR code
     * Implementa idempotência via userId + idempotencyKey
     *
     * @param userId ID do usuário (mesquita) que receberá o pagamento
     * @param request Dados da cobrança
     * @param clientIp IP do cliente (para auditoria)
     * @return Response com QR code e dados da cobrança
     */
    @Transactional
    public CreatePixChargeResponse createPixCharge(
            UUID userId,
            CreatePixChargeRequest request,
            String clientIp
    ) {
       log.info("Iniciando criação de cobrança PIX - User {} | Amount: {} | IdempotencyKey: {}",
               userId, request.getAmountCents(), request.getIdempotencyKey());

       // validar request
       validateRequest(request);

       // verificar idempotência
        Optional<PixCharge> existingCharge = pixChargeRepository
                .findByUserIdAndIdempotencyKey(userId, request.getIdempotencyKey());

        if (existingCharge.isPresent()) {
            return buildResponse(existingCharge.get());
        }

        // buscar e validar 'user'
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado:" + userId));

        validateUser(user);

        // validar rate limiting
        validateRateLimit(userId);

        // gerar TXID único
        String txid = generateTxid(request.getIdempotencyKey());

        // validar Txid único
        if (pixChargeRepository.existsByTxid(txid)){
            txid = generateTxid(UUID.randomUUID().toString()); // gerar novo se colidir
        }

        // gerar payload EMV
        BankDetails bankDetails = user.getBankDetails();
        String emvPayload = generateEmvPayload(
                bankDetails,
                user.getEmail(),
                txid,
                request.getAmountCents()
        );


        // 8. Gerar QR Code (Base64)
        String qrImageBase64 = QrcodeImageGenerator.generateForMobile(emvPayload);

        // 9. Calcular expiração
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusMinutes(request.getExpiresMinutes() != null
                        ? request.getExpiresMinutes()
                        : DEFAULT_EXPIRATION_MINUTES);

        // 10. Criar e persistir PixCharge
        PixCharge pixCharge = PixCharge.builder()
                .user(user)
                .localDonationId(request.getLocalDonationId())
                .idempotencyKey(request.getIdempotencyKey())
                .txid(txid)
                .amountCents(request.getAmountCents())
                .qrPayload(emvPayload)
                .qrImageBase64(qrImageBase64)
                .status(PixChargeStatus.PENDING)
                .expiresAt(expiresAt)
                .pspName("static-key")
                .build();

        pixCharge = pixChargeRepository.save(pixCharge);

        log.info("Cobrança PIX criada com sucesso - TxID: {} | User: {} | Amount: R$ {} | IP: {}",
                txid, userId, request.getAmountCents() / 100.0, clientIp);

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

    /**
     * Marca cobranças expiradas como EXPIRED
     * Deve ser chamado por um job scheduled
     */
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
        // Validar valor
        if (request.getAmountCents() == null || request.getAmountCents() < MIN_AMOUNT_CENTS) {
            throw new InvalidAmountException(
                    String.format("Valor mínimo: R$ %.2f", MIN_AMOUNT_CENTS / 100.0)
            );
        }

        if (request.getAmountCents() > MAX_AMOUNT_CENTS) {
            throw new InvalidAmountException(
                    String.format("Valor máximo: R$ %.2f", MAX_AMOUNT_CENTS / 100.0)
            );
        }

        // Validar idempotencyKey
        if (request.getIdempotencyKey() == null || request.getIdempotencyKey().isBlank()) {
            throw new InvalidRequestException("IdempotencyKey é obrigatória");
        }

        // Validar expiração
        if (request.getExpiresMinutes() != null) {
            if (request.getExpiresMinutes() < 1 || request.getExpiresMinutes() > MAX_EXPIRATION_MINUTES) {
                throw new InvalidRequestException(
                        String.format("Expiração deve estar entre 1 e %d minutos", MAX_EXPIRATION_MINUTES)
                );
            }
        }
    }

    private void validateUser(User user) {
        // Validar usuário ativo
        if (!user.getIsEnabled()) {
            throw new UserInactiveException("Usuário inativo");
        }

        // Validar dados bancários
        BankDetails bankDetails = user.getBankDetails();
        if (bankDetails == null || bankDetails.getPixKey() == null || bankDetails.getPixKey().isBlank()) {
            throw new PixKeyNotFoundException("Usuário não possui chave PIX cadastrada");
        }

        // Validar chave PIX verificada
        if (!Boolean.TRUE.equals(bankDetails.getIsVerified())) {
            throw new PixKeyNotVerifiedException(
                    "Chave PIX não verificada. Favor validar a titularidade da conta."
            );
        }

        // Validar formato da chave PIX
        if (!PixKeyValidator.isValid(bankDetails.getPixKey())) {
            throw new InvalidPixKeyException("Formato de chave PIX inválido");
        }
    }

    private void validateRateLimit(UUID userId) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);

        Long recentCharges = pixChargeRepository.countByUserAndStatus(userId, PixChargeStatus.PENDING);

        if (recentCharges != null && recentCharges >= MAX_CHARGES_PER_HOUR) {
            throw new RateLimitExceededPixException(
                    String.format("Limite de %d cobranças por hora excedido", MAX_CHARGES_PER_HOUR)
            );
        }
    }

    // ========== MÉTODOS PRIVADOS DE GERAÇÃO ==========

    private String generateTxid(String idempotencyKey) {
        // Gera TXID baseado na idempotencyKey (primeiros 25 chars alfanuméricos)
        String txid = idempotencyKey.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();

        if (txid.length() > 25) {
            txid = txid.substring(0, 25);
        } else if (txid.length() < 10) {
            // Se muito curto, adiciona timestamp
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
                .userName(charge.getUser().getName())
                .receiptImageUrl(charge.getReceiptImageUrl())
                .build();
    }
}
