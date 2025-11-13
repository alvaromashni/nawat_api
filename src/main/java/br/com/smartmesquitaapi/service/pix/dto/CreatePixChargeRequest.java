package br.com.smartmesquitaapi.service.pix.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request para criação de cobrança PIX
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePixChargeRequest {

    /**
     * "ID" local da doação (gerado pelo totem)
     */
    private String localDonationId;

    /**
     * Valor em centavos (ex: 5000 = R$50,00)
     */
    @NotNull(message = "Valor é obrigatório")
    @Min(value = 100, message = "Valor mínimo: R$ 1,00")
    @Max(value = 1000000, message = "Valor máximo: R$ 10.000,00")
    private Integer amountCents;

    /**
     * Chave de idempotência (UUID gerado pelo totem)
     * Garante que requisições duplicadas não criem múltiplas cobranças
     */
    @NotBlank(message = "IdempotencyKey é obrigatória")
    @Size(max = 100, message = "IdempotencyKey muito longa")
    private String idempotencyKey;

    /**
     * Tempo de expiração do QR code em minutos (padrão: 10 minutos)
     */
    @Min(value = 1, message = "Expiração mínima: 1 minuto")
    @Max(value = 60, message = "Expiração máxima: 60 minutos")
    private Integer expiresMinutes;
}

