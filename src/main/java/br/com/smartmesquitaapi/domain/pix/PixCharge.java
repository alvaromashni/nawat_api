package br.com.smartmesquitaapi.domain.pix;

import br.com.smartmesquitaapi.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade que representa uma cobrança PIX gerada pelo sistema.
 * Cada cobrança está associada a um User (mesquita) e contém o QR Code gerado.
 */
@Entity
@Table(
        name = "pix_charges",
        indexes = {
                @Index(name = "idx_user_idempotency", columnList = "user_id,idempotency_key", unique = true),
                @Index(name = "idx_txid", columnList = "txid"),
                @Index(name = "idx_status_expires", columnList = "status,expires_at"),
                @Index(name = "idx_local_donation", columnList = "local_donation_id")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PixCharge {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID pixChargeId;

    /**
     * Referência ao User (mesquita/instituição) que receberá o pagamento
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * ID local gerado pelo totem Android (opcional, para tracking)
     */
    @Column(name = "local_donation_id", length = 100)
    private String localDonationId;

    /**
     * Chave de idempotência gerada pelo cliente (totem).
     * Garante que múltiplas requisições com a mesma chave não criem cobranças duplicadas.
     * UNIQUE constraint combinado com user_id.
     */
    @Column(name = "idempotency_key", nullable = false, length = 100)
    private String idempotencyKey;

    /**
     * Transaction ID do PIX (txid) - identificador único da transação.
     * Máximo 35 caracteres conforme especificação PIX.
     */
    @Column(name = "txid", nullable = false, unique = true, length = 35)
    private String txid;

    /**
     * Valor da cobrança em centavos (ex: 5000 = R$ 50,00)
     */
    @Column(name = "amount_cents", nullable = false)
    private Integer amountCents;

    /**
     * Payload EMV completo do QR Code PIX (formato texto)
     */
    @Column(name = "qr_payload", nullable = false, columnDefinition = "TEXT")
    private String qrPayload;

    /**
     * Imagem do QR Code em Base64 (PNG)
     */
    @Column(name = "qr_image_base64", columnDefinition = "TEXT")
    private String qrImageBase64;

    /**
     * Status atual da cobrança
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PixChargeStatus status;

    /**
     * Nome do PSP/Gateway usado (padrão: "static-key" para chave estática)
     */
    @Column(name = "psp_name", length = 50)
    @Builder.Default
    private String pspName = "static-key";

    /**
     * Data/hora de criação da cobrança
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Data/hora de expiração do QR Code
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Data/hora da última atualização
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * URL da imagem do comprovante (para confirmação manual)
     */
    @Column(name = "receipt_image_url", length = 500)
    private String receiptImageUrl;

    /**
     * ID do usuário (staff/admin) que confirmou manualmente
     */
    @Column(name = "confirmed_by_user_id")
    private UUID confirmedByUserId;

    /**
     * Data/hora da confirmação manual
     */
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    /**
     * Observações adicionais (reconciliação, motivo de cancelamento, etc)
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // ========== MÉTODOS DE NEGÓCIO ==========

    /**
     * Verifica se a cobrança está expirada
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Verifica se a cobrança está pendente e ainda válida
     */
    public boolean isPendingAndValid() {
        return status == PixChargeStatus.PENDING && !isExpired();
    }

    /**
     * Marca a cobrança como expirada
     */
    public void markAsExpired() {
        this.status = PixChargeStatus.EXPIRED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Confirma manualmente a cobrança com comprovante
     */
    public void confirmManually(UUID confirmedByUserId, String receiptUrl, String notes) {
        this.status = PixChargeStatus.CONFIRMED_MANUAL;
        this.confirmedByUserId = confirmedByUserId;
        this.confirmedAt = LocalDateTime.now();
        this.receiptImageUrl = receiptUrl;
        this.notes = notes;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Marca como pago (reconciliação automática)
     */
    public void markAsPaid(String notes) {
        this.status = PixChargeStatus.PAID;
        this.confirmedAt = LocalDateTime.now();
        this.notes = notes;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Cancela a cobrança
     */
    public void cancel(String reason) {
        this.status = PixChargeStatus.CANCELLED;
        this.notes = reason;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Retorna o valor em reais (formato decimal)
     */
    public Double getAmountInReais() {
        return amountCents / 100.0;
    }
}
