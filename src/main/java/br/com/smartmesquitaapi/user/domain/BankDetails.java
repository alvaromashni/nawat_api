package br.com.smartmesquitaapi.user.domain;

import br.com.smartmesquitaapi.config.crypto.CryptoConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Embeddable
@Data
public class BankDetails {

    @Column(length = 100)
    private String bankName;

    @Column(length = 60)
    private String agency;

    @Column(length = 500)
    @Convert(converter = CryptoConverter.class)
    private String accountNumber;

    @Column(length = 200)
    private String accountHolder;

    @Column(length = 500)
    private String ownershipProofUrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PixKeyType pixKeyType;


    private LocalDateTime verifiedAt;

    @Column(length = 500)
    @Convert(converter = CryptoConverter.class)
    private String pixKey;

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

    public boolean isComplete() {
        return pixKey != null && !pixKey.isBlank()
                && pixKeyType != null
                && accountHolder != null && !accountHolder.isBlank();
    }

    public void markAsVerified(String proofUrl) {
        this.isVerified = true;
        this.verifiedAt = LocalDateTime.now();
        this.ownershipProofUrl = proofUrl;
    }

    public void revokeVerification(String reason) {
        this.isVerified = false;
    }

}
