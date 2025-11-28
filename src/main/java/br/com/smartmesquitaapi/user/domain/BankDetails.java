package br.com.smartmesquitaapi.user.domain;

import br.com.smartmesquitaapi.config.crypto.CryptoConverter;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Value Object que representa os dados bancários de um usuário.
 * Embeddable - será parte da tabela users.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
@Builder
public class BankDetails implements Serializable {

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "agency_number", length = 20)
    private String agency;

    @Column(name = "account_number", length = 40)
    @Convert(converter = CryptoConverter.class)
    private String accountNumber;

    @Column(name = "account_holder", length = 200)
    private String accountHolder;

    /**
     * URL do comprovante de titularidade (documento enviado)
     */
    @Column(name = "ownership_proof_url", length = 500)
    private String ownershipProofUrl;

    /**
     * CNPJ da instituição (se aplicável)
     */
    @Column(name = "cnpj", length = 18)
    private String cnpj;

    @Enumerated(EnumType.STRING)
    @Column(name = "pix_key_type", length = 20)
    private PixKeyType pixKeyType;

    /**
     * Data/hora em que a chave foi verificada
     */
    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "pix_key", length = 255)
    @Convert(converter = CryptoConverter.class)
    private String pixKey;

    /**
     * Indica se a chave PIX foi verificada/validada
     * (comprovante de titularidade aprovado)
     */
    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Override
    public String toString() {
        var account = accountNumber == null ? "null" : mask(accountNumber);
        return "BankDetails{" +
                "bankName='" + bankName + '\'' +
                ", agency='" + agency + '\'' +
                ", accountNumber='" + account + '\'' +
                ", pixKey='" + pixKey +
                '}';
    }

    private String mask(String s) {
        if (s.length() <= 4) return "****";
        return "****" + s.substring(s.length() - 4);
    }

    // ========== MÉTODOS DE NEGÓCIO ==========

    /**
     * Verifica se os dados bancários estão completos
     */
    public boolean isComplete() {
        return pixKey != null && !pixKey.isBlank()
                && pixKeyType != null
                && accountHolder != null && !accountHolder.isBlank();
    }

    /**
     * Marca a chave como verificada
     */
    public void markAsVerified(String proofUrl) {
        this.isVerified = true;
        this.verifiedAt = LocalDateTime.now();
        this.ownershipProofUrl = proofUrl;
    }

    /**
     * Revoga a verificação
     */
    public void revokeVerification(String reason) {
        this.isVerified = false;
        // Nota: você pode adicionar um campo 'revocationReason' se necessário
    }

}
