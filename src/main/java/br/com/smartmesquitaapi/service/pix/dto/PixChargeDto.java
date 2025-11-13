package br.com.smartmesquitaapi.service.pix.dto;

import br.com.smartmesquitaapi.domain.pix.PixChargeStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para consulta de cobrança PIX
 */
@Data
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PixChargeDto {

    private UUID id;
    private String localDonationId;
    private String txid;
    private Integer amountCents;
    private PixChargeStatus status;
    private String qrPayload;
    private String qrImageBase64;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private String userName;
    private String receiptImageUrl;

    public double getAmountInReais(){
        return amountCents / 100.0;
    }

    public Boolean isValid(){
        return status == PixChargeStatus.PENDING && expiresAt.isAfter(LocalDateTime.now());
    }
}
