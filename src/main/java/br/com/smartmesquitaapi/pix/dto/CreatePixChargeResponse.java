package br.com.smartmesquitaapi.pix.dto;

import lombok.*;

/**
 * Response da criação de cobrança PIX
 * Contém o QR code e dados necessários para exibição no totem
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreatePixChargeResponse {

    private String txid;
    private String qrPayload;
    private String qrImageBase64;
    private Long expiresAt;
    private Integer amountCents;

}
